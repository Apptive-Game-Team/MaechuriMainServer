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

    /**
     * Handles a user interaction with a scenario object.
     *
     * The interaction type is resolved from the object metadata via
     * [ScenarioObjectRepository.getObjectInteractionType]. If no type is
     * configured, `"simple"` is used as the default.
     *
     * - `"two-way"`: used for conversational objects that maintain stateful
     *   dialogue with the user. The request may contain an encoded `history`
     *   string, which is decoded into [ConversationHistory] by
     *   [HistoryService.decodeHistory]. New messages (user + assistant) are
     *   appended and the updated history is re‑encoded with
     *   [HistoryService.encodeHistory] and returned in [InteractResponse.history]
     *   so the client can pass it back on subsequent calls.
     *
     * - `"simple"`: used for one‑off interactions that do not track
     *   conversation state. The `history` field in the request is ignored and
     *   no history is returned in the response.
     *
     * @param scenarioId identifier of the scenario containing the object.
     *                   Currently used for routing at a higher level; the
     *                   interaction logic itself is based on [objectId].
     * @param objectId identifier of the interacted object; determines the
     *                 interaction type and content source.
     * @param request interaction payload from the client, including the
     *                optional encoded history and user message.
     * @return [InteractResponse] containing the interaction type, the
     *         assistant's message, and (for `"two-way"`) the updated encoded
     *         conversation history.
     */
    /**
     * Handles a user interaction with a scenario object.
     *
     * The interaction type is resolved from the object metadata via [ScenarioObjectRepository.getObjectInteractionType].
     * If the object doesn't exist or has no configured type, defaults to "simple".
     *
     * **Interaction Types:**
     * - **"two-way"**: Used for conversational objects that maintain stateful dialogue with the user.
     *   The request may contain an encoded `history` string, which is decoded into [ConversationHistory] by
     *   [HistoryService.decodeHistory]. New messages (user + assistant) are appended and the updated history
     *   is re-encoded with [HistoryService.encodeHistory] and returned in [InteractResponse.history]
     *   so the client can pass it back on subsequent calls.
     *
     * - **"simple"**: Used for one-off interactions that do not track conversation state.
     *   The `history` field in the request is ignored and no history is returned in the response.
     *
     * @param scenarioId Identifier of the scenario containing the object.
     *                   Currently used for routing at a higher level; the interaction logic itself is based on [objectId].
     * @param objectId Identifier of the interacted object; determines the interaction type and content source.
     *                 Must be a positive value.
     * @param request Interaction payload from the client, including the optional encoded history and user message.
     * @return [InteractResponse] containing the interaction type, the assistant's message,
     *         and (for "two-way") the updated encoded conversation history.
     * @throws IllegalArgumentException if scenarioId or objectId are not positive values
     */
    fun handleInteraction(scenarioId: Long, objectId: Long, request: InteractRequest): InteractResponse {
        require(scenarioId > 0) { "scenarioId must be positive" }
        require(objectId > 0) { "objectId must be positive" }
        
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

    /**
     * Retrieves map data for a specific scenario.
     *
     * @param scenarioId Identifier of the scenario. Must be a positive value.
     * @return [MapDataResponse] containing the scenario map data including layers, objects, and assets.
     * @throws IllegalArgumentException if scenarioId is not positive
     */
    fun getMapData(scenarioId: Long): MapDataResponse {
        require(scenarioId > 0) { "scenarioId must be positive" }
        return mapDataClient.getMapData(scenarioId)
    }

    fun getTodayMapData(): MapDataResponse {
        return mapDataClient.getTodayMapData()
    }
}
