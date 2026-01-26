package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class SuspectSecret(
    val scenarioId: Long,
    val suspectId: Long,
    @Id
    val secretId: Long,
    val threshold: Int,
    val content: String,
    val triggerClueIds: List<Long> // Maps from JSONB type
)
