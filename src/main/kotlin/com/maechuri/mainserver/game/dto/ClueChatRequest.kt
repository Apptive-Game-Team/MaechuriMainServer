package com.maechuri.mainserver.game.dto

data class ClueChatRequest(
    val sessionId: String,
    val scenarioId: Long,
    val clueId: Long,
    val userMessage: String,
)
