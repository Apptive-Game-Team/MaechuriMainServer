package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.game.entity.Asset
import com.maechuri.mainserver.scenario.entity.AssetStatus
import com.maechuri.mainserver.game.repository.AssetRepository
import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import com.maechuri.mainserver.storage.client.LeonardoClient
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import javax.imageio.ImageIO

private val log = KotlinLogging.logger {}

@Service
class ImageGenerationService(
    private val leonardoClient: LeonardoClient,
    private val minioService: MinioService,
    private val suspectRepository: SuspectRepository,
    private val clueRepository: ClueRepository,
    private val backgroundRemovalService: BackgroundRemovalService,
    private val backgroundImageService: BackgroundImageService,
    private val assetRepository: AssetRepository,
    private val databaseClient: DatabaseClient,
) {

    /**
     * Generates images for all suspects and clues in the given scenario that have a
     * [visualDescription], using the [Asset] table to track and resume progress.
     */
    suspend fun generateImagesForScenario(scenarioId: Long) {
        val suspects = suspectRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        for (suspect in suspects) {
            if (suspect.visualDescription.isNullOrBlank()) continue
            try {
                val asset = getOrCreateAssetForSuspect(scenarioId, suspect.suspectId, suspect.visualDescription, suspect.assetId)
                if (suspect.assetId == null) {
                    databaseClient.sql { "UPDATE suspect SET asset_id = :assetId WHERE scenario_id = :scenarioId AND suspect_id = :suspectId;" }
                        .bind("scenarioId", scenarioId)
                        .bind("assetId", asset.id!!)
                        .bind("suspectId", suspect.suspectId)
                        .fetch().rowsUpdated().awaitSingle()
                }
                
                if (asset.status != AssetStatus.COMPLETED) {
                    generateAndUpload(asset, "suspect", scenarioId, suspect.suspectId)
                }
                log.info { "Generated image for suspect ${suspect.suspectId} in scenario $scenarioId" }
            } catch (e: Exception) {
                log.error(e) { "Failed to generate image for suspect ${suspect.suspectId} in scenario $scenarioId" }
            }
        }

        val clues = clueRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        for (clue in clues) {
            if (clue.visualDescription.isNullOrBlank()) continue
            try {
                val asset = getOrCreateAssetForClue(scenarioId, clue.clueId, clue.visualDescription, clue.assetId)
                if (clue.assetId == null) {
                    databaseClient.sql { "UPDATE clue SET asset_id = :assetId WHERE scenario_id = :scenarioId AND clue_id = :clueId;" }
                        .bind("scenarioId", scenarioId)
                        .bind("assetId", asset.id!!)
                        .bind("clueId", clue.clueId)
                        .fetch().rowsUpdated().awaitSingle()
                }

                if (asset.status != AssetStatus.COMPLETED) {
                    generateAndUpload(asset, "clue", scenarioId, clue.clueId)
                }
                log.info { "Generated image for clue ${clue.clueId} in scenario $scenarioId" }
            } catch (e: Exception) {
                log.error(e) { "Failed to generate image for clue ${clue.clueId} in scenario $scenarioId" }
            }
        }

        backgroundImageService.generateBackgroundImagesForScenario(scenarioId)
    }

    private suspend fun getOrCreateAssetForSuspect(
        scenarioId: Long,
        suspectId: Long,
        visualDescription: String,
        existingAssetId: Long?
    ): Asset {
        val name = "suspect-$scenarioId-$suspectId"
        if (existingAssetId != null) {
            return assetRepository.findById(existingAssetId).awaitSingleOrNull() ?: 
                assetRepository.findByName(name).awaitSingleOrNull() ?:
                createAsset(name, "suspect", visualDescription)
        }
        return assetRepository.findByName(name).awaitSingleOrNull() ?:
            createAsset(name, "suspect", visualDescription)
    }

    private suspend fun getOrCreateAssetForClue(
        scenarioId: Long,
        clueId: Long,
        visualDescription: String,
        existingAssetId: Long?
    ): Asset {
        val name = "clue-$scenarioId-$clueId"
        if (existingAssetId != null) {
            return assetRepository.findById(existingAssetId).awaitSingleOrNull() ?: 
                assetRepository.findByName(name).awaitSingleOrNull() ?:
                createAsset(name, "clue", visualDescription)
        }
        return assetRepository.findByName(name).awaitSingleOrNull() ?:
            createAsset(name, "clue", visualDescription)
    }

    private suspend fun createAsset(name: String, type: String, visualDescription: String): Asset {
        val prompt = buildPrompt(type, visualDescription)
        return assetRepository.save(
            Asset(
                name = name,
                prompt = prompt,
                status = AssetStatus.PENDING
            )
        ).awaitSingle()
    }

    private fun buildPrompt(type: String, visualDescription: String): String {
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
        return "$style, $subject"
    }

    /**
     * Resume-able pipeline: Leonardo.ai → Resize (256x256) → Background Removal & Trim → PNG/JSON metadata.
     */
    private suspend fun generateAndUpload(asset: Asset, type: String, scenarioId: Long, targetId: Long): String {
        var current = asset

        // Step 1: Generate via Leonardo.ai
        if (current.rawUrl.isNullOrBlank()) {
            val generationId = try {
                leonardoClient.createGeneration(current.prompt!!)
            } catch (e: WebClientResponseException) {
                log.error("Leonardo generation failed: ${e.responseBodyAsString}")
                throw e
            }
            val imageUrl = leonardoClient.waitForGeneration(generationId)
            val rawBytes = leonardoClient.downloadImage(imageUrl)

            val rawKey = "$type/$scenarioId/raw_$targetId.png"
            minioService.uploadObject(rawKey, rawBytes, "image/png")
            val rawUrl = minioService.getPermanentUrl(rawKey)

            current.rawUrl = rawUrl
            current.status = AssetStatus.GENERATED
            current.updatedAt = LocalDateTime.now()
            current = assetRepository.save(current).awaitSingle()
        }

        // Step 2: Resize (version with changed ratio/size)
        if (current.resizedUrl.isNullOrBlank()) {
            val rawBytes = minioService.downloadObject(extractKey(current.rawUrl!!))
            val resizedBytes = resizeImage(rawBytes, 256, 256)

            val resizedKey = "$type/$scenarioId/resized_$targetId.png"
            minioService.uploadObject(resizedKey, resizedBytes, "image/png")
            val resizedUrl = minioService.getPermanentUrl(resizedKey)

            current.resizedUrl = resizedUrl
            current.status = AssetStatus.PROCESSED
            current.updatedAt = LocalDateTime.now()
            current = assetRepository.save(current).awaitSingle()
        }

        // Step 3: Remove Background, Trim, and Final Metadata
        if (current.finalUrl.isNullOrBlank()) {
            val resizedBytes = minioService.downloadObject(extractKey(current.resizedUrl!!))
            
            // Remove background
            val noBgBytes = backgroundRemovalService.removeBackground(resizedBytes)

            // Trim
            val noBgImage = ImageIO.read(ByteArrayInputStream(noBgBytes))
                ?: error("Could not decode image after background removal")
            val trimmedImage = trimTransparentPixels(noBgImage)
            val finalPngBytes = ByteArrayOutputStream().use {
                ImageIO.write(trimmedImage, "png", it)
                it.toByteArray()
            }

            val pngKey = "$type/$scenarioId/$targetId.png"
            minioService.uploadObject(pngKey, finalPngBytes, "image/png")
            val permanentPngUrl = minioService.getPermanentUrl(pngKey)

            // Upload JSON metadata as the final URL
            val jsonKey = "$type/$scenarioId/$targetId.json"
            val metaJson = """{"front":"$permanentPngUrl"}"""
            minioService.uploadText(jsonKey, metaJson)
            val finalUrl = minioService.getPermanentUrl(jsonKey)

            current.finalUrl = finalUrl
            current.status = AssetStatus.COMPLETED
            current.updatedAt = LocalDateTime.now()
            current = assetRepository.save(current).awaitSingle()
        }

        return current.finalUrl!!
    }

    private fun extractKey(url: String): String {
        val parts = url.split("/")
        val bucketName = "maechuri" 
        val index = parts.indexOf(bucketName)
        if (index != -1 && index < parts.size - 1) {
            return parts.subList(index + 1, parts.size).joinToString("/")
        }
        return url.substringAfterLast("/")
    }

    private fun resizeImage(bytes: ByteArray, width: Int, height: Int): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(bytes))
            ?: error("Could not decode image bytes for resize")
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(image, 0, 0, width, height, null)
        g2d.dispose()
        return ByteArrayOutputStream().use {
            ImageIO.write(result, "png", it)
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
}
