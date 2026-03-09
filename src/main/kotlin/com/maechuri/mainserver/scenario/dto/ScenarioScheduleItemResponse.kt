package com.maechuri.mainserver.scenario.dto

import com.maechuri.mainserver.scenario.entity.ScenarioState
import java.time.LocalDate

data class ScenarioScheduleItemResponse(
    val date: LocalDate,
    val scenarioId: Long,
    val state: ScenarioState,
)
