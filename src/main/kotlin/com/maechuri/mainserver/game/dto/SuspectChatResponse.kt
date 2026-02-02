package com.maechuri.mainserver.game.dto

data class SuspectChatResponse(
    val user_message: String,
    val answer: String,
    val pressure: Int,
    val pressure_delta: Int
)