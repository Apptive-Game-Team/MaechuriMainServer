package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class AccessRule(
    val scenarioId: Long,
    @Id
    val ruleId: Long,
    val locationId: Long,
    val requires: String
)
