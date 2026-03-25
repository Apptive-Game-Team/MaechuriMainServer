package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.repository.ScenarioRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class LatestScenarioIdProvider(
    private val scenarioRepository: ScenarioRepository
) : TodayScenarioIdProvider {

    override suspend fun getTodayScenarioId(): Long {
        val today = LocalDate.now()
        val scenario = scenarioRepository.findTopByDateLessThanEqualOrderByDateDesc(today).awaitSingleOrNull()
            ?: throw IllegalStateException("No scenario found for today or any previous date ($today)")
        return scenario.scenarioId
            ?: throw IllegalStateException("Scenario for date ${scenario.date} has no ID")
    }
}
