package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("suspect")
data class Suspect(
    val scenarioId: Long,
    val suspectId: Long,
    val name: String,
    val role: String,
    val age: Int,
    val gender: String,
    val description: String,
    val isCulprit: Boolean,
    val motive: String?,
    val alibiSummary: String,
    val speechStyle: String,
    val emotionalTendency: String,
    val lyingPattern: String,
    val criticalClueIds: String, // jsonb
    val x: Short?,
    val y: Short?,
)