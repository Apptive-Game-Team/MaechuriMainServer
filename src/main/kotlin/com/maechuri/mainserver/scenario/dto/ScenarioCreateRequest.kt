package com.maechuri.mainserver.scenario.dto

data class ScenarioCreateRequest(
    val key: String,
    val theme: String = "random"
) {
}