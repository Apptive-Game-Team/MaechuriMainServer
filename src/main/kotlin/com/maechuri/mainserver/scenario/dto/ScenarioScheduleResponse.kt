package com.maechuri.mainserver.scenario.dto

data class ScenarioScheduleResponse(
    val month: Int,
    val scenarios: List<ScenarioScheduleItemResponse>,
)
