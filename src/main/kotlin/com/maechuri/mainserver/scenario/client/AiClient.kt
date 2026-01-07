package com.maechuri.mainserver.scenario.client

import com.maechuri.mainserver.game.domain.Message

interface AiClient {
    fun generateResponse(objectId: Long, userMessage: String, conversationHistory: List<Message>): String
}
