package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.domain.Message
import org.springframework.stereotype.Component

@Component
class MockAiClient : AiClient {
    
    override fun generateResponse(objectId: Long, userMessage: String, conversationHistory: List<Message>): String {
        // Mock AI response - simply echoes back the user's message
        return "네, 제가 들었습니다: $userMessage"
    }
}
