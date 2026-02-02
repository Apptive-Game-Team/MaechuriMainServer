package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.ClueChatRequest
import com.maechuri.mainserver.game.dto.ClueChatResponse
import com.maechuri.mainserver.game.dto.SuspectChatRequest
import com.maechuri.mainserver.game.dto.SuspectChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AiClient(
    val webClient: WebClient
) {

    suspend fun generateSuspectResponse(request: SuspectChatRequest): SuspectChatResponse {
        return webClient.post()
            .uri("/api/chats/suspect")
            .bodyValue(request)
            .retrieve()
            .awaitBody<SuspectChatResponse>()
    }
    suspend fun generateDetectiveResponse(request: ClueChatRequest): ClueChatResponse {
        return webClient.post()
            .uri("/api/chats/general")
            .bodyValue(request)
            .retrieve()
            .awaitBody<ClueChatResponse>()
    }
}
