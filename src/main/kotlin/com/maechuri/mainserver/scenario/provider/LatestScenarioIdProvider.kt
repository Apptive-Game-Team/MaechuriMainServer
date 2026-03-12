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
        val scenario = scenarioRepository.findByDate(today).awaitSingleOrNull()
            ?: throw IllegalStateException("No scenario found for today ($today)")
        return scenario.scenarioId
            ?: throw IllegalStateException("Today's scenario has no ID")
    }
}
