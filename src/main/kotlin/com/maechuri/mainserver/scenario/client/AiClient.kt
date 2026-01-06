package com.maechuri.mainserver.scenario.client

interface AiClient {
    fun generateResponse(objectId: Long, userMessage: String, conversationHistory: List<com.maechuri.mainserver.game.domain.Message>): String
}
