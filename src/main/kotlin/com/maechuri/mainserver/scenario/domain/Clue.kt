package com.maechuri.mainserver.scenario.domain

data class Clue(
    val clueId: Long,
    val name: String,
    val location: Location,
    val description: String,
    val logicExplanation: String,
    val decodedAnswer: String?,
    val isRedHerring: Boolean,
    val relatedFactIds: List<Long>?,
    val x: Short?,
    val y: Short?,
)