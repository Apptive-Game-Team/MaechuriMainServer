package com.maechuri.mainserver.game.dto

data class SuspectChatResponse(
    val userMessage: String,
    val answer: String,
    val pressure: Int,
    val pressureDelta: Int
)