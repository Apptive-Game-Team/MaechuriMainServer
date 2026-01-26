package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class RequiredClue(
    val scenarioId: Long,
    @Id
    val clueId: Long,
    val type: String,
    val minCount: Int
)
