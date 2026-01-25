package com.maechuri.mainserver.scenario.domain

data class Clue(
    val clueId: Int,
    val name: String,
    val location: Location,
    val description: String,

    val relatedSuspects: List<Suspect>,

    val logicExplanation: String,
    val isRedHerring: Boolean
)