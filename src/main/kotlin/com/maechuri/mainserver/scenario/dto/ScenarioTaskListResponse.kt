package com.maechuri.mainserver.scenario.dto

data class ScenarioTaskListResponse(
    val total: Int,
    val tasks: List<ScenarioStatusResponse>
) {
}