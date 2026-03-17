package com.maechuri.mainserver.scenario.scheduler

import com.maechuri.mainserver.admin.AdminService
import com.maechuri.mainserver.scenario.client.AiClient
import com.maechuri.mainserver.scenario.dto.ScenarioCreateResponse
import com.maechuri.mainserver.scenario.dto.ScenarioCreateStatus
import com.maechuri.mainserver.scenario.dto.ScenarioStatusResponse
import com.maechuri.mainserver.scenario.service.ScenarioGenerationService
import com.maechuri.mainserver.storage.service.ImageGenerationService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ScenarioDailySchedulerTest {

    private val scenarioGenerationService: ScenarioGenerationService = mock()
    private val aiClient: AiClient = mock()
    private val imageGenerationService: ImageGenerationService = mock()
    private val adminService: AdminService = mock()

    private val scheduler = ScenarioDailyScheduler(
        scenarioGenerationService, aiClient, imageGenerationService, adminService
    )

    private fun createResponse(key: String = "test-key") =
        ScenarioCreateResponse(key = key, message = "ok", status = ScenarioCreateStatus.PENDING, theme = "random")

    private fun statusResponse(status: ScenarioCreateStatus, scenarioId: Long? = null, error: String? = null) =
        ScenarioStatusResponse(key = "test-key", status = status, theme = "random", scenarioId = scenarioId, error = error)

    @Test
    fun `generateScenarioWithRetry succeeds on first attempt`() = runTest {
        whenever(scenarioGenerationService.startGeneration(any())).thenReturn(createResponse())
        whenever(aiClient.getScenarioCreateTask(any()))
            .thenReturn(statusResponse(ScenarioCreateStatus.COMPLETED, scenarioId = 1L))

        scheduler.generateScenarioWithRetry()

        verify(imageGenerationService, times(1)).generateImagesForScenario(1L)
        verify(adminService, times(1)).updateScenarioDate(any(), any())
    }

    @Test
    fun `generateScenarioWithRetry retries on FAILED and succeeds on second attempt`() = runTest {
        val firstKey = "key-1"
        val secondKey = "key-2"

        whenever(scenarioGenerationService.startGeneration(any()))
            .thenReturn(createResponse(firstKey))
            .thenReturn(createResponse(secondKey))
        whenever(aiClient.getScenarioCreateTask(firstKey))
            .thenReturn(statusResponse(ScenarioCreateStatus.FAILED))
        whenever(aiClient.getScenarioCreateTask(secondKey))
            .thenReturn(statusResponse(ScenarioCreateStatus.COMPLETED, scenarioId = 2L))

        scheduler.generateScenarioWithRetry()

        verify(scenarioGenerationService, times(2)).startGeneration(any())
        verify(imageGenerationService, times(1)).generateImagesForScenario(2L)
        verify(adminService, times(1)).updateScenarioDate(any(), any())
    }

    @Test
    fun `generateScenarioWithRetry stops after MAX_ATTEMPTS`() = runTest {
        whenever(scenarioGenerationService.startGeneration(any())).thenReturn(createResponse())
        whenever(aiClient.getScenarioCreateTask(any()))
            .thenReturn(statusResponse(ScenarioCreateStatus.FAILED))

        scheduler.generateScenarioWithRetry()

        verify(scenarioGenerationService, times(ScenarioDailyScheduler.MAX_ATTEMPTS)).startGeneration(any())
        verify(imageGenerationService, never()).generateImagesForScenario(any())
        verify(adminService, never()).updateScenarioDate(any(), any())
    }

    @Test
    fun `generateScenarioWithRetry returns false when startGeneration throws`() = runTest {
        whenever(scenarioGenerationService.startGeneration(any()))
            .thenThrow(RuntimeException("AI server unavailable"))

        scheduler.generateScenarioWithRetry()

        verify(imageGenerationService, never()).generateImagesForScenario(any())
        verify(adminService, never()).updateScenarioDate(any(), any())
        verify(scenarioGenerationService, times(ScenarioDailyScheduler.MAX_ATTEMPTS)).startGeneration(any())
    }

    @Test
    fun `generateScenarioWithRetry returns false when scenarioId is null on COMPLETED`() = runTest {
        whenever(scenarioGenerationService.startGeneration(any())).thenReturn(createResponse())
        whenever(aiClient.getScenarioCreateTask(any()))
            .thenReturn(statusResponse(ScenarioCreateStatus.COMPLETED, scenarioId = null))

        scheduler.generateScenarioWithRetry()

        verify(imageGenerationService, never()).generateImagesForScenario(any())
        verify(adminService, never()).updateScenarioDate(any(), any())
    }

    @Test
    fun `generateScenarioWithRetry stops polling after MAX_POLL_COUNT and retries`() = runTest {
        whenever(scenarioGenerationService.startGeneration(any())).thenReturn(createResponse())
        // Always return PROCESSING so the poll loop times out
        whenever(aiClient.getScenarioCreateTask(any()))
            .thenReturn(statusResponse(ScenarioCreateStatus.PROCESSING))

        scheduler.generateScenarioWithRetry()

        verify(scenarioGenerationService, times(ScenarioDailyScheduler.MAX_ATTEMPTS)).startGeneration(any())
        verify(imageGenerationService, never()).generateImagesForScenario(any())
        verify(adminService, never()).updateScenarioDate(any(), any())
    }

    @Test
    fun `generateScenarioWithRetry retries when polling throws exception`() = runTest {
        val firstKey = "key-1"
        val secondKey = "key-2"

        whenever(scenarioGenerationService.startGeneration(any()))
            .thenReturn(createResponse(firstKey))
            .thenReturn(createResponse(secondKey))
        whenever(aiClient.getScenarioCreateTask(firstKey))
            .thenThrow(RuntimeException("network error"))
        whenever(aiClient.getScenarioCreateTask(secondKey))
            .thenReturn(statusResponse(ScenarioCreateStatus.COMPLETED, scenarioId = 3L))

        scheduler.generateScenarioWithRetry()

        verify(scenarioGenerationService, times(2)).startGeneration(any())
        verify(imageGenerationService, times(1)).generateImagesForScenario(3L)
    }
}
