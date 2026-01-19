package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class SuspectSecret(
    val scenarioId: Int,
    val suspectId: Int,
    @Id
    val secretId: Int,
    val threshold: Int,
    val content: String,
    val triggerEvidenceIds: List<Int> // Maps from JSONB type
)
