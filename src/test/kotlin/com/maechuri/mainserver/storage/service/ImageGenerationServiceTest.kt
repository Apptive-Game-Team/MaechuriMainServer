package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.scenario.entity.Clue
import com.maechuri.mainserver.scenario.entity.Suspect
import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import com.maechuri.mainserver.storage.client.LeonardoClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
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
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class ImageGenerationServiceTest {

    @Mock
    private lateinit var leonardoClient: LeonardoClient

    @Mock
    private lateinit var minioService: MinioService

    @Mock
    private lateinit var suspectRepository: SuspectRepository

    @Mock
    private lateinit var clueRepository: ClueRepository

    @Mock
    private lateinit var databaseClient: DatabaseClient

    @Mock
    private lateinit var backgroundRemovalService: BackgroundRemovalService

    @InjectMocks
    private lateinit var imageGenerationService: ImageGenerationService

    private val whitePixelPng = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x02, 0x00, 0x00, 0x00, 0x90.toByte(), 0x77, 0x53, 0xde.toByte(), 0x00, 0x00, 0x00, 0x0c, 0x49, 0x44, 0x41, 0x54, 0x78, 0x9c.toByte(), 0x63, 0x60, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01, 0x4e, 0x3d, 0xd8.toByte(), 0xea.toByte(), 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4e, 0x44, 0xae.toByte(), 0x42, 0x60, 0x82.toByte())
    private val transparentPixelPng = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1f.toByte(), 0x15, 0xc4.toByte(), 0x89.toByte(), 0x00, 0x00, 0x00, 0x0a, 0x49, 0x44, 0x41, 0x54, 0x78, 0x9c.toByte(), 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0d, 0x0a, 0x2d, 0xb4.toByte(), 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4e, 0x44, 0xae.toByte(), 0x42, 0x60, 0x82.toByte())

    @Test
    fun `generateImagesForScenario should use BackgroundRemovalService`(): Unit = runBlocking {
        // Arrange
        val scenarioId = 1L
        val suspect = Suspect(
            scenarioId = scenarioId,
            suspectId = 1L,
            name = "Test Suspect",
            visualDescription = "A test suspect",
            assetsUrl = null, // Needs image generation
            role = "", age = 0, gender = "", description = "", isCulprit = false, motive = "", alibiSummary = "", speechStyle = "", emotionalTendency = "", lyingPattern = "", x = null, y = null
        )

        whenever(suspectRepository.findAllByScenarioId(scenarioId)).thenReturn(Flux.just(suspect))
        whenever(clueRepository.findAllByScenarioId(scenarioId)).thenReturn(Flux.empty())

        val spec = mock<FetchSpec<Map<String, Any>>>()
        whenever(spec.rowsUpdated()).thenReturn(Mono.just(1L))
        val clientSpec = mock<DatabaseClient.GenericExecuteSpec>()
        whenever(clientSpec.bind(any<String>(), any())).thenReturn(clientSpec)
        whenever(clientSpec.fetch()).thenReturn(spec)

        whenever(databaseClient.sql(any<String>())).thenReturn(clientSpec)


        whenever(leonardoClient.createGeneration(any())).thenReturn("generation-id")
        whenever(leonardoClient.waitForGeneration(any())).thenReturn("http://image.url")
        whenever(leonardoClient.downloadImage(any())).thenReturn(whitePixelPng)

        whenever(backgroundRemovalService.removeBackground(whitePixelPng)).thenReturn(transparentPixelPng)

        whenever(minioService.getPermanentUrl(any())).thenReturn("http://permanent.url")

        // Act
        imageGenerationService.generateImagesForScenario(scenarioId)

        // Assert
        verify(backgroundRemovalService, times(1)).removeBackground(whitePixelPng)
        verify(minioService, times(1)).uploadObject(eq("suspect/1/1.png"), any(), eq("image/png"))
        verify(minioService, times(1)).uploadText(eq("suspect/1/1.json"), any())
    }
}
