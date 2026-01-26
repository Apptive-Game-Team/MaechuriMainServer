package com.maechuri.mainserver.scenario.dto

data class ScenarioCreateResponse(
    val key: String,
    val message: String,
    val status: ScenarioCreateStatus,
    val theme: String,
    val scenarioId: Long? = null,
)