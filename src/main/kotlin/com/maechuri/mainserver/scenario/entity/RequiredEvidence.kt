package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class RequiredEvidence(
    val scenarioId: Int,
    @Id
    val evidenceId: Int,
    val type: String,
    val minCount: Int
)
