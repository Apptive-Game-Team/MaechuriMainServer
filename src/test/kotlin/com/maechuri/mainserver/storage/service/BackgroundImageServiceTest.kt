package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.scenario.entity.Location
import com.maechuri.mainserver.scenario.repository.LocationRepository
import com.maechuri.mainserver.storage.client.LeonardoClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@ExtendWith(MockitoExtension::class)
class BackgroundImageServiceTest {

    @Mock
    private lateinit var leonardoClient: LeonardoClient

    @Mock
    private lateinit var minioService: MinioService

    @Mock
    private lateinit var locationRepository: LocationRepository

    @Mock
    private lateinit var databaseClient: DatabaseClient

    @InjectMocks
    private lateinit var backgroundImageService: BackgroundImageService

    /** Produces a minimal 4×4 solid-color PNG byte array for testing. */
    private fun solidColorPng(width: Int = 4, height: Int = 4): ByteArray {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.color = java.awt.Color(200, 150, 100)
        g.fillRect(0, 0, width, height)
        g.dispose()
        return ByteArrayOutputStream().also { ImageIO.write(img, "png", it) }.toByteArray()
    }

    @Test
    fun `generateBackgroundImagesForScenario generates floor and wall images for locations without URLs`() =
        runBlocking {
            val scenarioId = 1L
            val location = Location(
                scenarioId = scenarioId,
                locationId = 10L,
                name = "거실",
                type = "indoor",
                x = 0, y = 0, width = 5, height = 5,
                canSee = "[]", cannotSee = "[]",
                accessRequires = null,
                floorUrl = null,
                wallUrl = null,
            )

            whenever(locationRepository.findAllByScenarioId(scenarioId)).thenReturn(Flux.just(location))

            val spec = mock<FetchSpec<Map<String, Any>>>()
            whenever(spec.rowsUpdated()).thenReturn(Mono.just(1L))
            val clientSpec = mock<DatabaseClient.GenericExecuteSpec>()
            whenever(clientSpec.bind(any<String>(), any())).thenReturn(clientSpec)
            whenever(clientSpec.fetch()).thenReturn(spec)
            whenever(databaseClient.sql(any<String>())).thenReturn(clientSpec)

            val rawPng = solidColorPng()
            whenever(leonardoClient.createGeneration(any())).thenReturn("gen-id")
            whenever(leonardoClient.waitForGeneration(any())).thenReturn("http://img.url")
            whenever(leonardoClient.downloadImage(any())).thenReturn(rawPng)
            whenever(minioService.getPermanentUrl(any())).thenReturn("http://permanent.url")

            backgroundImageService.generateBackgroundImagesForScenario(scenarioId)

            // Leonardo called twice: once for floor, once for wall
            verify(leonardoClient, times(2)).createGeneration(any())
            verify(leonardoClient, times(2)).downloadImage(any())

            // Raw floor + final floor + raw wall + processed wall + final wall = 5 uploads
            verify(minioService, times(5)).uploadObject(any(), any(), eq("image/png"))

            // Raw floor key
            verify(minioService).uploadObject(
                eq("background/$scenarioId/floor_raw_${location.locationId}.png"), any(), eq("image/png")
            )
            // Final floor key (128×128)
            verify(minioService).uploadObject(
                eq("background/$scenarioId/floor_${location.locationId}.png"), any(), eq("image/png")
            )
            // Raw wall key
            verify(minioService).uploadObject(
                eq("background/$scenarioId/wall_raw_${location.locationId}.png"), any(), eq("image/png")
            )
            // Processed wall key
            verify(minioService).uploadObject(
                eq("background/$scenarioId/wall_processed_${location.locationId}.png"), any(), eq("image/png")
            )
            // Final wall key (128×128)
            verify(minioService).uploadObject(
                eq("background/$scenarioId/wall_${location.locationId}.png"), any(), eq("image/png")
            )

            // DB update called once
            verify(databaseClient).sql(
                argThat<String> { contains("UPDATE location SET floor_url") }
            )
        }

    @Test
    fun `generateBackgroundImagesForScenario skips locations that already have both URLs`() = runBlocking {
        val scenarioId = 2L
        val location = Location(
            scenarioId = scenarioId,
            locationId = 20L,
            name = "침실",
            type = "indoor",
            x = 0, y = 0, width = 3, height = 3,
            canSee = "[]", cannotSee = "[]",
            accessRequires = null,
            floorUrl = "http://existing-floor.png",
            wallUrl = "http://existing-wall.png",
        )

        whenever(locationRepository.findAllByScenarioId(scenarioId)).thenReturn(Flux.just(location))

        backgroundImageService.generateBackgroundImagesForScenario(scenarioId)

        verifyNoInteractions(leonardoClient)
        verifyNoInteractions(minioService)
    }

    @Test
    fun `processWallImage compresses to 70 percent height and fills top 30 percent with dark color`() {
        val size = 100
        val rawPng = solidColorPng(size, size)

        val result = backgroundImageService.processWallImage(rawPng)

        val resultImg = ImageIO.read(result.inputStream())
        // Output dimensions must equal original
        assertEquals(size, resultImg.width)
        assertEquals(size, resultImg.height)

        // Top 30 pixels (30%) should be rgb(45,45,45) — opaque
        val topPixel = resultImg.getRGB(size / 2, 0)
        val topR = (topPixel ushr 16) and 0xFF
        val topG = (topPixel ushr 8) and 0xFF
        val topB = topPixel and 0xFF
        assertEquals(45, topR, "Top fill R should be 45")
        assertEquals(45, topG, "Top fill G should be 45")
        assertEquals(45, topB, "Top fill B should be 45")
    }

    @Test
    fun `resizeTo128x128 produces a 128x128 PNG`() {
        val rawPng = solidColorPng(64, 64)

        val result = backgroundImageService.resizeTo128x128(rawPng)

        val resultImg = ImageIO.read(result.inputStream())
        assertEquals(128, resultImg.width)
        assertEquals(128, resultImg.height)
    }
}
