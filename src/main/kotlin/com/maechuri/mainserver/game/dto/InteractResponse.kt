package com.maechuri.mainserver.game.dto

data class InteractResponse(
    val type: String,
    val message: String,
    val name: String? = null,
    val pressure: Int? = null,
    val pressureDelta: Int? = null,
    val revealedRecordIds: List<String>? = null
)
