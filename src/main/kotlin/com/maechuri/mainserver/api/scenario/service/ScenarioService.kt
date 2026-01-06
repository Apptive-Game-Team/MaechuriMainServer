package com.maechuri.mainserver.api.scenario.service

import com.maechuri.mainserver.api.scenario.dto.*
import com.maechuri.mainserver.game.domain.ConversationHistory
import com.maechuri.mainserver.game.domain.Message
import com.maechuri.mainserver.game.service.HistoryService
import org.springframework.stereotype.Service

@Service
class ScenarioService(
    private val historyService: HistoryService
) {

    // Mock data for interaction types per object
    private val objectInteractionTypes = mapOf(
        100L to "two-way",
        101L to "simple",
        102L to "simple"
    )

    // Mock data for simple interactions
    private val simpleMessages = mapOf(
        101L to Pair("안녕 난 요리사 이선민이야", "이선민"),
        102L to Pair("안녕하세요", null)
    )

    fun handleInteraction(scenarioId: Long, objectId: Long, request: InteractRequest): InteractResponse {
        val interactionType = objectInteractionTypes[objectId] ?: "simple"

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
            val responseMessage = "안녕 너가 말해봐"
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

        // Process user message and create response (mock AI response)
        val userMessage = Message("user", request.message)
        val responseMessage = "네, 제가 들었습니다: ${request.message}"
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
        val (message, name) = simpleMessages[objectId] ?: Pair("안녕하세요", null)
        
        return InteractResponse(
            type = "simple",
            message = message,
            name = name
        )
    }

    fun getMapData(scenarioId: Long): MapDataResponse {
        // Return mock data - can be extended later with real implementation
        return createMockMapData(scenarioId)
    }

    fun getTodayMapData(): MapDataResponse {
        // Return today's scenario map (mock data)
        // In real implementation, this would fetch today's scenario from database
        return createMockMapData(1L)
    }

    private fun createMockMapData(scenarioId: Long): MapDataResponse {
        return MapDataResponse(
            createdDate = "2025-12-22",
            scenarioId = scenarioId,
            scenarioName = "요리사 3인방의 사건 현장",
            map = MapData(
                layers = listOf(
                    Layer(
                        orderInLayer = 1,
                        name = "floor",
                        type = listOf("Non-Interactable", "Passable"),
                        tileMap = listOf(
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
                        )
                    ),
                    Layer(
                        orderInLayer = 2,
                        name = "wall",
                        type = listOf("Non-Interactable", "Non-Passable", "Blocks-Vision"),
                        tileMap = listOf(
                            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                        )
                    )
                ),
                objects = listOf(
                    MapObject(
                        id = 100,
                        orderInLayer = 3,
                        name = "요리사 1",
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = 2, y = 3)
                    ),
                    MapObject(
                        id = 101,
                        orderInLayer = 3,
                        name = "요리사 2",
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = 11, y = 3)
                    ),
                    MapObject(
                        id = 102,
                        orderInLayer = 3,
                        name = "요리사 3",
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = 15, y = 8)
                    )
                ),
                assets = listOf(
                    AssetInfo(
                        id = 1,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/wood_floor.json"
                    ),
                    AssetInfo(
                        id = 2,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/tile_floor.json"
                    ),
                    AssetInfo(
                        id = 100,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/cook_1.json"
                    ),
                    AssetInfo(
                        id = 999,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/player.json"
                    )
                )
            )
        )
    }
}
