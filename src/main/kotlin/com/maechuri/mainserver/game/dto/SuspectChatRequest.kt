package com.maechuri.mainserver.game.dto

data class SuspectChatRequest(
    val sessionId: String,
    val scenarioId: Long,
    val suspectId: Long,
    val userMessage: String
) {
}