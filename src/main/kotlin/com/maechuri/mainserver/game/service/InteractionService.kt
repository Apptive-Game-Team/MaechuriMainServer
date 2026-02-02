package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.client.AiClient
import com.maechuri.mainserver.game.dto.ClueChatRequest
import com.maechuri.mainserver.game.dto.InteractRequest
import com.maechuri.mainserver.game.dto.InteractResponse
import com.maechuri.mainserver.game.dto.SuspectChatRequest
import com.maechuri.mainserver.scenario.repository.ClueRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service

@Service
class InteractionService(
    private val clueRepository: ClueRepository,
    private val aiClient: AiClient
) {

    suspend fun handleInteraction(scenarioId: Long, objectId: String, request: InteractRequest): InteractResponse {
        val objectRealId = objectId.split(":").get(1).toLong()
        return when (objectId.get(0)) {
            's' -> handleSuspectInteraction(scenarioId, objectRealId, request)
            'd' -> handleDetectiveInteraction(scenarioId, request)
            'c' -> handleClueInteraction(scenarioId, objectRealId)
            else -> throw IllegalArgumentException("Unknown object id type: $objectId")
        }
    }

    private suspend fun handleDetectiveInteraction(scenarioId: Long, request: InteractRequest): InteractResponse {
        val userMessage = request.message ?: "안녕하세요"

        val responseMessage = aiClient.generateDetectiveResponse(
            ClueChatRequest(
                "default",
                scenarioId,
                userMessage
            )
        )

        return InteractResponse(
            type = "two-way",
            message = responseMessage.answer,
        )
    }

    private suspend fun handleSuspectInteraction(scenarioId: Long, suspectId: Long, request: InteractRequest): InteractResponse {

        val userMessage = request.message ?: "안녕하세요"

        val responseMessage = aiClient.generateSuspectResponse(
            SuspectChatRequest(
                "default",
                scenarioId,
                suspectId,
                userMessage
            )
        )

        return InteractResponse(
            type = "two-way",
            message = responseMessage.answer,
        )
    }

    private suspend fun handleClueInteraction(scenarioId: Long, objectId: Long): InteractResponse {
        val clue = clueRepository.findByScenarioIdAndClueId(scenarioId, objectId)
            .awaitSingle()

        return InteractResponse(
            type = "simple",
            message = clue.description,
            name = clue.name,
        )
    }
}