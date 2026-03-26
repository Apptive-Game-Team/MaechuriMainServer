package com.maechuri.mainserver.scenario.entity

import io.r2dbc.postgresql.codec.Json
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
    val relatedSuspectIds: Json?, // jsonb
    val x: Short?,
    val y: Short?,
    val visualDescription: String? = null,
    val assetId: Long? = null,
)
