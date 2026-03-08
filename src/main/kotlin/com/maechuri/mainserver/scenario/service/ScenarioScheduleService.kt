package com.maechuri.mainserver.scenario.service

import com.maechuri.mainserver.scenario.dto.ScenarioScheduleItemResponse
import com.maechuri.mainserver.scenario.dto.ScenarioScheduleResponse
import com.maechuri.mainserver.scenario.entity.ScenarioState
import com.maechuri.mainserver.scenario.repository.ScenarioRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class ScenarioScheduleService(
    private val scenarioRepository: ScenarioRepository,
) {

    suspend fun getSchedule(year: Int, month: Int): ScenarioScheduleResponse {
        val yearMonth = YearMonth.of(year, month)
        val from = yearMonth.atDay(1)
        val to = yearMonth.atEndOfMonth()

        val scenarios = scenarioRepository.findByDateBetween(from, to)
            .collectList()
            .awaitSingle()

        val today = LocalDate.now()

        val items = scenarios
            .filter { it.date != null }
            .map { scenario ->
                val state = when {
                    scenario.date!! < today -> ScenarioState.Finished
                    scenario.date > today -> ScenarioState.Inactive
                    else -> ScenarioState.Active
                }
                ScenarioScheduleItemResponse(
                    date = scenario.date,
                    scenarioId = scenario.scenarioId!!,
                    state = state,
                )
            }
            .sortedBy { it.date }

        return ScenarioScheduleResponse(month = month, scenarios = items)
    }
}
