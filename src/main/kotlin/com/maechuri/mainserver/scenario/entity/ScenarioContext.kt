package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("scenario_context")
data class ScenarioContext(
    val scenarioId: Long,
    val contextId: Long,
    val type: String,
    val content: String,
    val extraData: String?, // jsonb
)
