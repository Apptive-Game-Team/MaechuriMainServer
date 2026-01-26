package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class VisibilityRule(
    @Id
    val ruleId: Long,
    val scenarioId: Long,
    val fromLocationId: Long,
    val canSee: List<Long>,
    val cannotSee: List<Long>,
    val clueType: String,
)