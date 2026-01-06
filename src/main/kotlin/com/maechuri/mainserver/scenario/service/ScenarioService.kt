package com.maechuri.mainserver.scenario.service

import com.maechuri.mainserver.game.domain.ConversationHistory
import com.maechuri.mainserver.game.domain.Message
import com.maechuri.mainserver.scenario.client.AiClient
import com.maechuri.mainserver.scenario.client.MapDataClient
import com.maechuri.mainserver.scenario.dto.InteractRequest
import com.maechuri.mainserver.scenario.dto.InteractResponse
import com.maechuri.mainserver.scenario.dto.MapDataResponse
import com.maechuri.mainserver.scenario.repository.ScenarioObjectRepository
import com.maechuri.mainserver.game.service.HistoryService
import org.springframework.stereotype.Service

@Service
class ScenarioService(
    private val historyService: HistoryService,
    private val objectRepository: ScenarioObjectRepository,
    private val mapDataClient: MapDataClient,
    private val aiClient: AiClient
) {

    fun handleInteraction(scenarioId: Long, objectId: Long, request: InteractRequest): InteractResponse {
        val interactionType = objectRepository.getObjectInteractionType(objectId) ?: "simple"

        return when (interactionType) {
            "two-way" -> handleTwoWayInteraction(objectId, request)
            "simple" -> handleSimpleInteraction(objectId)
            else -> throw IllegalArgumentException("Unknown interaction type: $interactionType")
        }
    }

    private fun handleTwoWayInteraction(objectId: Long, request: InteractRequest): InteractResponse {
        // Decode history if provided, or create new
        val history = if (!request.history.isNullOrEmpty()) {
            historyService.decodeHistory(objectId, request.history)
        } else {
            ConversationHistory(objectId, emptyList())
        }

        // If this is the initial request (no message from user), return greeting
        if (request.message.isNullOrEmpty()) {
            val responseMessage = objectRepository.getInitialGreeting(objectId) ?: "안녕하세요"
            val newHistory = ConversationHistory(
                objectId,
                history.conversation + Message("assistant", responseMessage)
            )
            return InteractResponse(
                type = "two-way",
                message = responseMessage,
                history = historyService.encodeHistory(newHistory)
            )
        }

        // Process user message through AI client
        val userMessage = Message("user", request.message)
        val responseMessage = aiClient.generateResponse(objectId, request.message, history.conversation)
        val assistantMessage = Message("assistant", responseMessage)

        val newHistory = ConversationHistory(
            objectId,
            history.conversation + listOf(userMessage, assistantMessage)
        )

        return InteractResponse(
            type = "two-way",
            message = responseMessage,
            history = historyService.encodeHistory(newHistory)
        )
    }

    private fun handleSimpleInteraction(objectId: Long): InteractResponse {
        val (message, name) = objectRepository.getSimpleInteractionMessage(objectId) 
            ?: Pair("안녕하세요", null)
        
        return InteractResponse(
            type = "simple",
            message = message,
            name = name
        )
    }

    fun getMapData(scenarioId: Long): MapDataResponse {
        return mapDataClient.getMapData(scenarioId)
    }

    fun getTodayMapData(): MapDataResponse {
        return mapDataClient.getTodayMapData()
    }
}
