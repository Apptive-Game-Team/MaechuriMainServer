package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.ClueChatRequest
import com.maechuri.mainserver.game.dto.ClueChatResponse
import com.maechuri.mainserver.game.dto.SuspectChatRequest
import com.maechuri.mainserver.game.dto.SuspectChatResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class AiClient(
    webClientBuilder: WebClient.Builder,
    @Value("\${maechuri.ai-server.url}") aiServerUrl: String
) {

    val webClient: WebClient = webClientBuilder.baseUrl(aiServerUrl).build()

    suspend fun generateSuspectResponse(request: SuspectChatRequest): SuspectChatResponse {
        return webClient.post()
            .uri("/api/chat/suspect")
            .bodyValue(request)
            .retrieve()
            .awaitBody<SuspectChatResponse>()
    }
    suspend fun generateClueResponse(request: ClueChatRequest): ClueChatResponse {
        return webClient.post()
            .uri("/api/chat/clue")
            .bodyValue(request)
            .retrieve()
            .awaitBody<ClueChatResponse>()
    }
}
