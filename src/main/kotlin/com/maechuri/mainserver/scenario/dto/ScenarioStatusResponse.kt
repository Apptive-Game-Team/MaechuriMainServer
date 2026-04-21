package com.maechuri.mainserver.scenario.dto

data class ScenarioStatusResponse(
    val key: String,
    val status: ScenarioCreateStatus,
    val theme: String,
    val scenario_id: Long?,
    val error: String?
) {

}

