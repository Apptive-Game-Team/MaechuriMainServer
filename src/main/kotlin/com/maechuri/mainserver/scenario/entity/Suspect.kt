package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class Suspect(
    val scenarioId: Int,
    @Id
    val suspectId: Int,
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
    val criticalEvidenceIds: List<Int> // Maps from JSONB type
)
