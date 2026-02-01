package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("fact")
data class Fact(
    val scenarioId: Long,
    val suspectId: Long,
    val factId: Long,
    val threshold: Int,
    val type: String,
    val content: String, // jsonb
)
