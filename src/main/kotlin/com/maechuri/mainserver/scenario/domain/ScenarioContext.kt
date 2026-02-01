package com.maechuri.mainserver.scenario.domain

data class ScenarioContext(
    val contextId: Long,
    val type: String,
    val content: String,
    val extraData: Map<String, Any>?, // jsonb
)