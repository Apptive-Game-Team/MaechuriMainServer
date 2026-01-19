package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class Clue(
    val scenarioId: Int,
    @Id
    val clueId: Int,
    val name: String,
    val foundAt: String,
    val description: String,
    val relatedSuspectIds: List<Int>, // Maps from JSONB type
    val logicExplanation: String,
    val isRedHerring: Boolean
)
