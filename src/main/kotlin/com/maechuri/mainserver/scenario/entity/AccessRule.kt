package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class AccessRule(
    val scenarioId: Int,
    @Id
    val ruleId: Int,
    val location: String,
    val requires: String
)
