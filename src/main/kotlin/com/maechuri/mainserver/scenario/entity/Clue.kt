package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("clue")
data class Clue(
    val scenarioId: Long,
    val clueId: Long,
    val name: String,
    val locationId: Long,
    val description: String,
    val logicExplanation: String,
    val decodedAnswer: String?,
    val isRedHerring: Boolean,
    val relatedFactIds: String?, // jsonb
    val x: Short?,
    val y: Short?,
)
