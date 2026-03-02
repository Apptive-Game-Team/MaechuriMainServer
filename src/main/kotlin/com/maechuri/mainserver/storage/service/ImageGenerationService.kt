package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import com.maechuri.mainserver.storage.client.LeonardoClient
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val log = KotlinLogging.logger {}

@Service
class ImageGenerationService(
    private val leonardoClient: LeonardoClient,
    private val minioService: MinioService,
    private val suspectRepository: SuspectRepository,
    private val clueRepository: ClueRepository,
    private val databaseClient: DatabaseClient,
    private val backgroundRemovalService: BackgroundRemovalService,
) {

    /**
     * Generates images for all suspects and clues in the given scenario that have a
     * [visualDescription] but no [assetsUrl] yet.
     */
    suspend fun generateImagesForScenario(scenarioId: Long) {
        val suspects = suspectRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        for (suspect in suspects) {
            if (suspect.visualDescription.isNullOrBlank() || !suspect.assetsUrl.isNullOrBlank()) continue
            try {
                val assetsUrl = generateAndUpload("suspect", scenarioId, suspect.suspectId, suspect.visualDescription)
                databaseClient.sql("UPDATE suspect SET assets_url = :assetsUrl WHERE scenario_id = :scenarioId AND suspect_id = :suspectId")
                    .bind("assetsUrl", assetsUrl)
                    .bind("scenarioId", scenarioId)
                    .bind("suspectId", suspect.suspectId)
                    .fetch().rowsUpdated().awaitSingle()
                log.info { "Generated image for suspect ${suspect.suspectId} in scenario $scenarioId: $assetsUrl" }
            } catch (e: Exception) {
                log.error(e) { "Failed to generate image for suspect ${suspect.suspectId} in scenario $scenarioId" }
            }
        }

        val clues = clueRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        for (clue in clues) {
            if (clue.visualDescription.isNullOrBlank() || !clue.assetsUrl.isNullOrBlank()) continue
            try {
                val assetsUrl = generateAndUpload("clue", scenarioId, clue.clueId, clue.visualDescription)
                databaseClient.sql("UPDATE clue SET assets_url = :assetsUrl WHERE scenario_id = :scenarioId AND clue_id = :clueId")
                    .bind("assetsUrl", assetsUrl)
                    .bind("scenarioId", scenarioId)
                    .bind("clueId", clue.clueId)
                    .fetch().rowsUpdated().awaitSingle()
                log.info { "Generated image for clue ${clue.clueId} in scenario $scenarioId: $assetsUrl" }
            } catch (e: Exception) {
                log.error(e) { "Failed to generate image for clue ${clue.clueId} in scenario $scenarioId" }
            }
        }
    }

    /**
     * Full pipeline: generate image via Leonardo.ai → process (trim + resize 64x64) →
     * upload PNG and JSON metadata to MinIO → return the permanent URL of the JSON metadata.
     */
    private suspend fun generateAndUpload(
        type: String,
        scenarioId: Long,
        objectId: Long,
        visualDescription: String,
    ): String {
        val style = "single 2D game asset, isolated on a pure solid white background, flat vector style, bold outlines, minimalist, no background elements, no scenery, isolated on a pure solid white background, clean minimalist background, no background elements, blank background"

        val subject = when (type) {
            "suspect" -> """
                A single full-body SD (chibi-style) character sprite of $visualDescription,
                super-deformed proportions with a large head and small body,
                cute and simplified design,
                entire body fully visible from head to toe,
                from the tips of the feet to the top of the head clearly shown,
                feet fully visible and not cut off,
                standing alone in the exact center of the frame,
                front-facing view,
                no zoom, no close-up,
                no cropping, no cut-off limbs,
                no extra props
            """.trimIndent()
            "clue" -> "One single item sprite of $visualDescription, centered, isolated, no other objects in frame"
            else -> "A single $visualDescription centered on white background"
        }
        val prompt = "$style, $subject"

        var generationId: String?
        try {
            generationId = leonardoClient.createGeneration(prompt)
        } catch (e: WebClientResponseException) {
            val errorBody = e.responseBodyAsString
            logger.error("Background removal failed. statusCode=${e.statusCode}, responseBody=$errorBody")
            throw e
        }

        val imageUrl = leonardoClient.waitForGeneration(generationId)
        val rawBytes = leonardoClient.downloadImage(imageUrl)

        val processedBytes = processImage(rawBytes)

        val rawKey = "$type/$scenarioId/raw_$objectId.png"
        val pngKey = "$type/$scenarioId/$objectId.png"
        val jsonKey = "$type/$scenarioId/$objectId.json"

        minioService.uploadObject(rawKey, rawBytes, "image/png")
        minioService.uploadObject(pngKey, processedBytes, "image/png")

        val permanentPngUrl = minioService.getPermanentUrl(pngKey)
        val metaJson = """{"front":"$permanentPngUrl"}"""
        minioService.uploadText(jsonKey, metaJson)

        return minioService.getPermanentUrl(jsonKey)
    }

    /**
     * Removes background, trims transparent pixels, and resizes the image to 64x64.
     * Returns the result as a PNG byte array.
     */
    private suspend fun processImage(rawBytes: ByteArray): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(rawBytes))
            ?: error("Could not decode image bytes from Leonardo.ai")

        // Resize first to reduce payload size for the background removal API
        val resizedImage = resizeTo64x64(image)
        val resizedBytes = ByteArrayOutputStream().use {
            ImageIO.write(resizedImage, "png", it)
            it.toByteArray()
        }

        // Remove background from the resized image
        val noBgBytes = backgroundRemovalService.removeBackground(resizedBytes)

        // Trim the result
        val finalImage = ImageIO.read(ByteArrayInputStream(noBgBytes))
            ?: error("Could not decode image after background removal")
        val trimmedImage = trimTransparentPixels(finalImage)

        // Encode final result
        return ByteArrayOutputStream().use {
            ImageIO.write(trimmedImage, "png", it)
            it.toByteArray()
        }
    }

    private fun trimTransparentPixels(image: BufferedImage): BufferedImage {
        var minX = image.width
        var minY = image.height
        var maxX = 0
        var maxY = 0

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val alpha = (image.getRGB(x, y) ushr 24) and 0xFF
                if (alpha > 0) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }

        if (minX > maxX || minY > maxY) return image
        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1)
    }

    private fun resizeTo64x64(image: BufferedImage): BufferedImage {
        val result = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(image, 0, 0, 64, 64, null)
        g2d.dispose()
        return result
    }
}
