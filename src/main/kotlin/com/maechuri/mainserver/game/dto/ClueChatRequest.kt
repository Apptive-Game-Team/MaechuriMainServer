package com.maechuri.mainserver.game.dto

data class ClueChatRequest(
    val sessionId: String,
    val scenarioId: Long,
    val userMessage: String,
)
