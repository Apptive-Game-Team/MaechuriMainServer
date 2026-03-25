package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.scenario.repository.LocationRepository
import com.maechuri.mainserver.storage.client.LeonardoClient
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val log = KotlinLogging.logger {}

private const val FINAL_TILE_SIZE = 128
private const val WALL_COMPRESS_RATIO = 0.7
private const val WALL_TOP_COLOR_R = 45
private const val WALL_TOP_COLOR_G = 45
private const val WALL_TOP_COLOR_B = 45

@Service
class BackgroundImageService(
    private val leonardoClient: LeonardoClient,
    private val minioService: MinioService,
    private val locationRepository: LocationRepository,
    private val databaseClient: DatabaseClient,
) {

    /**
     * Generates floor and wall background tile images for all locations in the given scenario
     * that do not yet have [floorUrl] or [wallUrl] set.
     *
     * For each location:
     * - **Floor**: raw tile generated via Leonardo.ai → saved to MinIO → resized to 128×128 → saved.
     * - **Wall**: raw tile generated via Leonardo.ai → saved to MinIO → compressed to 70% height,
     *   gradient darkening applied, top 30% filled with rgb(45,45,45) → saved → resized to 128×128 → saved.
     */
    suspend fun generateBackgroundImagesForScenario(scenarioId: Long) {
        val locations = locationRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        for (location in locations) {
            if (!location.floorUrl.isNullOrBlank() && !location.wallUrl.isNullOrBlank()) continue
            try {
                val floorUrl = generateFloorImage(scenarioId, location.locationId, location.name, location.type)
                val wallUrl = generateWallImage(scenarioId, location.locationId, location.name, location.type)
                databaseClient.sql(
                    "UPDATE location SET floor_url = :floorUrl, wall_url = :wallUrl " +
                        "WHERE scenario_id = :scenarioId AND location_id = :locationId"
                )
                    .bind("floorUrl", floorUrl)
                    .bind("wallUrl", wallUrl)
                    .bind("scenarioId", scenarioId)
                    .bind("locationId", location.locationId)
                    .fetch().rowsUpdated().awaitSingle()
                log.info {
                    "Generated background images for location ${location.locationId} in scenario $scenarioId: " +
                        "floor=$floorUrl wall=$wallUrl"
                }
            } catch (e: Exception) {
                log.error(e) {
                    "Failed to generate background images for location ${location.locationId} in scenario $scenarioId"
                }
            }
        }
    }

    private suspend fun generateFloorImage(
        scenarioId: Long,
        locationId: Long,
        locationName: String,
        locationType: String,
    ): String {
        val prompt = buildFloorPrompt(locationName, locationType)
        val generationId = leonardoClient.createGeneration(prompt)
        val imageUrl = leonardoClient.waitForGeneration(generationId)
        val rawBytes = leonardoClient.downloadImage(imageUrl)

        val rawKey = "background/$scenarioId/floor_raw_$locationId.png"
        minioService.uploadObject(rawKey, rawBytes, "image/png")

        val resizedBytes = resizeTo128x128(rawBytes)
        val finalKey = "background/$scenarioId/floor_$locationId.png"
        minioService.uploadObject(finalKey, resizedBytes, "image/png")

        return minioService.getPermanentUrl(finalKey)
    }

    private suspend fun generateWallImage(
        scenarioId: Long,
        locationId: Long,
        locationName: String,
        locationType: String,
    ): String {
        val prompt = buildWallPrompt(locationName, locationType)
        val generationId = leonardoClient.createGeneration(prompt)
        val imageUrl = leonardoClient.waitForGeneration(generationId)
        val rawBytes = leonardoClient.downloadImage(imageUrl)

        val rawKey = "background/$scenarioId/wall_raw_$locationId.png"
        minioService.uploadObject(rawKey, rawBytes, "image/png")

        val processedBytes = processWallImage(rawBytes)
        val processedKey = "background/$scenarioId/wall_processed_$locationId.png"
        minioService.uploadObject(processedKey, processedBytes, "image/png")

        val resizedBytes = resizeTo128x128(processedBytes)
        val finalKey = "background/$scenarioId/wall_$locationId.png"
        minioService.uploadObject(finalKey, resizedBytes, "image/png")

        return minioService.getPermanentUrl(finalKey)
    }

    /**
     * Applies the wall post-processing pipeline to the raw image bytes:
     * 1. Compresses the image to 70% of its original height.
     * 2. Applies a top-to-bottom darkening gradient.
     * 3. Creates a new canvas of the original dimensions: the top 30% is filled with
     *    RGB(45, 45, 45) and the compressed image is placed flush against the bottom.
     *
     * Returns the result as a PNG byte array.
     */
    internal fun processWallImage(rawBytes: ByteArray): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(rawBytes))
            ?: error("Could not decode wall image bytes")
        val width = image.width
        val height = image.height

        // Step 1: Compress to 70% vertically
        val compressedHeight = (height * WALL_COMPRESS_RATIO).toInt()
        val compressed = BufferedImage(width, compressedHeight, BufferedImage.TYPE_INT_ARGB)
        val g2dCompress = compressed.createGraphics()
        g2dCompress.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2dCompress.drawImage(image, 0, 0, width, compressedHeight, null)
        g2dCompress.dispose()

        // Step 2: Apply gradient — darker towards the bottom
        for (y in 0 until compressedHeight) {
            // factor goes from 1.0 (top) down to 0.3 (bottom)
            val factor = 1.0 - (y.toDouble() / compressedHeight) * 0.7
            for (x in 0 until width) {
                val argb = compressed.getRGB(x, y)
                val a = (argb ushr 24) and 0xFF
                val r = (((argb ushr 16) and 0xFF) * factor).toInt().coerceIn(0, 255)
                val g = (((argb ushr 8) and 0xFF) * factor).toInt().coerceIn(0, 255)
                val b = ((argb and 0xFF) * factor).toInt().coerceIn(0, 255)
                compressed.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }

        // Step 3: Place compressed image at bottom; fill top 30% with RGB(45, 45, 45)
        val topHeight = height - compressedHeight
        val canvas = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2dCanvas = canvas.createGraphics()
        g2dCanvas.color = Color(WALL_TOP_COLOR_R, WALL_TOP_COLOR_G, WALL_TOP_COLOR_B)
        g2dCanvas.fillRect(0, 0, width, topHeight)
        g2dCanvas.drawImage(compressed, 0, topHeight, null)
        g2dCanvas.dispose()

        return ByteArrayOutputStream().use { out ->
            ImageIO.write(canvas, "png", out)
            out.toByteArray()
        }
    }

    internal fun resizeTo128x128(imageBytes: ByteArray): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: error("Could not decode image bytes for resize")
        val result = BufferedImage(FINAL_TILE_SIZE, FINAL_TILE_SIZE, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(image, 0, 0, FINAL_TILE_SIZE, FINAL_TILE_SIZE, null)
        g2d.dispose()
        return ByteArrayOutputStream().use { out ->
            ImageIO.write(result, "png", out)
            out.toByteArray()
        }
    }

    private fun buildFloorPrompt(locationName: String, locationType: String): String =
        "Single seamless floor tile texture, edges tileable, overhead top-down view, flat 2D game tile, square tile, " +
                "$locationName $locationType interior floor material, centered single tile, " +
                "no repetition, no grid, no multiple tiles, isolated texture, " +
                "no shadows, no gradients, uniform material, game asset style"

    private fun buildWallPrompt(locationName: String, locationType: String): String =
        "Seamless tileable wall texture, front-facing flat view, flat 2D game tile, square tile, " +
            "$locationName $locationType interior wall, consistent repeating pattern, no shadows, " +
            "architectural wall surface, game asset style"
}
