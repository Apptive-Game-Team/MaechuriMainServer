package com.maechuri.mainserver.game.dto

data class InteractResponse(
    val type: String,
    val message: String,
    val name: String? = null
)
