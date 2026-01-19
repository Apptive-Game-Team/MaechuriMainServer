package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class VisibilityRule(
    @Id
    val ruleId: Long,
    val scenarioId: Long,
    val fromLocation: String,
    val canSee: String,
    val cannotSee: String,
    val evidenceType: String,
)