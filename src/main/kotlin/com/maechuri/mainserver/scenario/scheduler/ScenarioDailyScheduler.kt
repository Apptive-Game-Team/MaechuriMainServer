package com.maechuri.mainserver.scenario.scheduler

import com.maechuri.mainserver.admin.AdminService
import com.maechuri.mainserver.scenario.client.AiClient
import com.maechuri.mainserver.scenario.dto.ScenarioCreateStatus
import com.maechuri.mainserver.scenario.service.ScenarioGenerationService
import com.maechuri.mainserver.storage.service.ImageGenerationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Component
class ScenarioDailyScheduler(
    private val scenarioGenerationService: ScenarioGenerationService,
    @Qualifier("scenario_ai_client") private val aiClient: AiClient,
    private val imageGenerationService: ImageGenerationService,
    private val adminService: AdminService,
) {

    companion object {
        const val MAX_ATTEMPTS = 5
        const val POLL_INTERVAL_MS = 30_000L
        const val MAX_POLL_COUNT = 240 // 240 × 30 s = 2 hours
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    fun scheduleScenarioGeneration() {
        scope.launch {
            generateScenarioWithRetry()
        }
    }

    internal suspend fun generateScenarioWithRetry() {
        val targetDate = LocalDate.now().plusDays(1)
        for (attempt in 1..MAX_ATTEMPTS) {
            log.info { "Scenario generation attempt $attempt/$MAX_ATTEMPTS (target date: $targetDate)" }
            val success = tryGenerateScenario(targetDate)
            if (success) {
                log.info { "Scenario generation succeeded on attempt $attempt" }
                return
            }
            log.warn { "Scenario generation attempt $attempt failed" }
        }
        log.error { "Scenario generation failed after $MAX_ATTEMPTS attempts" }
    }

    private suspend fun tryGenerateScenario(targetDate: LocalDate): Boolean {
        return try {
            val response = scenarioGenerationService.startGeneration("random")
            log.info { "Scenario generation started, key=${response.key}" }
            pollUntilComplete(response.key, targetDate)
        } catch (e: Exception) {
            log.error(e) { "Scenario generation threw an exception" }
            false
        }
    }

    private suspend fun pollUntilComplete(key: String, targetDate: LocalDate): Boolean {
        repeat(MAX_POLL_COUNT) {
            delay(POLL_INTERVAL_MS)
            val status = try {
                aiClient.getScenarioCreateTask(key)
            } catch (e: Exception) {
                log.error(e) { "Failed to poll status for key=$key" }
                return false
            }
            log.info { "Poll result for key=$key: status=${status.status}" }
            when (status.status) {
                ScenarioCreateStatus.COMPLETED -> {
                    val scenarioId = status.scenarioId ?: run {
                        log.error { "Scenario completed but scenarioId is null for key=$key" }
                        return false
                    }
                    return onScenarioCompleted(scenarioId, targetDate)
                }
                ScenarioCreateStatus.FAILED -> {
                    log.warn { "AI server reported FAILED for key=$key: ${status.error}" }
                    return false
                }
                else -> { /* PENDING or PROCESSING – keep polling */ }
            }
        }
        log.error { "Polling timed out after $MAX_POLL_COUNT attempts for key=$key" }
        return false
    }

    private suspend fun onScenarioCompleted(scenarioId: Long, targetDate: LocalDate): Boolean {
        return try {
            log.info { "Generating images for scenario $scenarioId" }
            imageGenerationService.generateImagesForScenario(scenarioId)
            adminService.updateScenarioDate(scenarioId, targetDate)
            log.info { "Scenario $scenarioId scheduled for $targetDate" }
            true
        } catch (e: Exception) {
            log.error(e) { "Failed to finalize scenario $scenarioId" }
            false
        }
    }
}
