package com.maechuri.mainserver.scenario.service

import com.maechuri.mainserver.game.repository.GameSessionRepository
import com.maechuri.mainserver.scenario.dto.ScenarioScheduleItemResponse
import com.maechuri.mainserver.scenario.dto.ScenarioScheduleResponse
import com.maechuri.mainserver.scenario.entity.Scenario
import com.maechuri.mainserver.scenario.entity.ScenarioState
import com.maechuri.mainserver.scenario.repository.ScenarioRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class ScenarioScheduleService(
    private val scenarioRepository: ScenarioRepository,
    private val gameSessionRepository: GameSessionRepository
) {

    suspend fun getSchedule(sessionId: String, year: Int, month: Int): ScenarioScheduleResponse {
        val yearMonth = YearMonth.of(year, month)
        val from = yearMonth.atDay(1)
        val to = yearMonth.atEndOfMonth()

        val scenarios = scenarioRepository.findByDateBetween(from, to)
            .collectList()
            .awaitSingle()

        val items = scenarios
            .filter { it.date != null && it.scenarioId != null }
            .map { scenario ->

                val state = getState(sessionId, scenario)

                ScenarioScheduleItemResponse(
                    date = scenario.date!!,
                    scenarioId = scenario.scenarioId!!,
                    state = state,
                )
            }
            .sortedBy { it.date }

        return ScenarioScheduleResponse(month = month, scenarios = items)
    }

    suspend fun getState(sessionId: String, scenario: Scenario): ScenarioState {
        val today = LocalDate.now()

        if (scenario.date!! > today) {
            return ScenarioState.Inactive
        }

        val gameSession = gameSessionRepository.findBySessionIdAndScenarioId(sessionId, scenario.scenarioId!!)
        return when {
            gameSession == null -> ScenarioState.Ready
            gameSession.completedAt != null -> ScenarioState.Finished
            else -> ScenarioState.Visited
        }
    }
}
