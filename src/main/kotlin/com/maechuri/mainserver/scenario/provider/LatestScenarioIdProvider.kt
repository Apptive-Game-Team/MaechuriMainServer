package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.repository.ScenarioRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class LatestScenarioIdProvider(
    private val scenarioRepository: ScenarioRepository
) : TodayScenarioIdProvider {

    override suspend fun getTodayScenarioId(): Long {
        val scenario = scenarioRepository.findTopByOrderByCreatedAtDesc().awaitSingleOrNull()
            ?: throw IllegalStateException("No scenarios found in the database")
        return scenario.scenarioId
            ?: throw IllegalStateException("Latest scenario has no ID")
    }
}
