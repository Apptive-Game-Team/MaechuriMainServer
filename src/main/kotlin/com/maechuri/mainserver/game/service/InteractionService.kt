package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.client.AiClient
import com.maechuri.mainserver.game.dto.ClueChatRequest
import com.maechuri.mainserver.game.dto.InteractRequest
import com.maechuri.mainserver.game.dto.InteractResponse
import com.maechuri.mainserver.game.dto.SuspectChatRequest
import com.maechuri.mainserver.game.dto.solve.AiSolveReqeust
import com.maechuri.mainserver.game.dto.solve.AiSolveResponse
import com.maechuri.mainserver.game.dto.solve.ClientSolveRequest
import com.maechuri.mainserver.game.dto.solve.ClientSolveResponse
import com.maechuri.mainserver.game.entity.GameSessionRecord
import com.maechuri.mainserver.game.repository.GameSessionRecordRepository
import com.maechuri.mainserver.scenario.repository.ClueRepository
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class InteractionService(
    private val clueRepository: ClueRepository,
    private val aiClient: AiClient,
    private val gameSessionRecordRepository: GameSessionRecordRepository
) {

    suspend fun solve(scenarioId: Long, clientSolveRequest: ClientSolveRequest): ClientSolveResponse {
        return aiClient.checkSolution(
            AiSolveReqeust.from(scenarioId, clientSolveRequest)
        ).toNormalize()
    }

    suspend fun handleInteraction(scenarioId: Long, objectId: String, request: InteractRequest, gameSessionId: String): InteractResponse {
        require(scenarioId > 0) { "scenarioId must be positive" }
        require(objectId.isNotEmpty()) { "Object ID cannot be empty" }
        
        val parts = objectId.split(":")
        require(parts.size == 2) { "Object ID must be in format 'tag:id' (e.g., 's:101', 'c:1')" }
        
        val tag = parts[0]
        val objectRealId = parts[1].toLongOrNull() 
            ?: throw IllegalArgumentException("Object ID must contain a valid number after the colon")
        
        return when (tag) {
            "s" -> handleSuspectInteraction(scenarioId, objectRealId, request, gameSessionId)
            "i" -> handleDetectiveInteraction(scenarioId, request, gameSessionId)
            "c" -> handleClueInteraction(scenarioId, objectRealId, gameSessionId)
            else -> throw IllegalArgumentException("Unknown object id type: $tag. Must be 's', 'i', or 'c'")
        }
    }

    private suspend fun handleDetectiveInteraction(scenarioId: Long, request: InteractRequest, gameSessionId: String): InteractResponse {
        val userMessage = request.message ?: "안녕하세요"

        val responseMessage = aiClient.generateDetectiveResponse(
            ClueChatRequest(
                gameSessionId,
                scenarioId,
                userMessage
            )
        )

        return InteractResponse(
            type = "two-way",
            message = responseMessage.answer,
        )
    }

    private suspend fun handleSuspectInteraction(scenarioId: Long, suspectId: Long, request: InteractRequest, gameSessionId: String): InteractResponse {

        val userMessage = request.message ?: "안녕하세요"

        val responseMessage = aiClient.generateSuspectResponse(
            SuspectChatRequest(
                gameSessionId,
                scenarioId,
                suspectId,
                userMessage
            )
        )

        // Save suspect interaction to records
        saveRecord(gameSessionId, scenarioId, "s", suspectId)
        
        // Save revealed facts if any
        responseMessage.revealed_fact_ids?.forEach { factId ->
            saveRecord(gameSessionId, scenarioId, "f", factId)
        }

        return InteractResponse(
            type = "two-way",
            message = responseMessage.answer,
            pressure = responseMessage.pressure,
            pressureDelta = responseMessage.pressure_delta,
            revealedFactIds = responseMessage.revealed_fact_ids
        )
    }

    private suspend fun handleClueInteraction(scenarioId: Long, objectId: Long, gameSessionId: String): InteractResponse {
        val clue = clueRepository.findByScenarioIdAndClueId(scenarioId, objectId)
            .awaitSingle()

        // Save clue interaction to records
        saveRecord(gameSessionId, scenarioId, "c", objectId)

        return InteractResponse(
            type = "simple",
            message = clue.description,
            name = clue.name,
        )
    }

    private suspend fun saveRecord(gameSessionId: String, scenarioId: Long, recordTag: String, recordId: Long) {
        try {
            val record = GameSessionRecord(
                gameSessionId = gameSessionId,
                scenarioId = scenarioId,
                recordTag = recordTag,
                recordId = recordId,
                interactedAt = LocalDateTime.now()
            )
            gameSessionRecordRepository.save(record).awaitSingle()
            logger.debug { "Saved record: sessionId=$gameSessionId, scenarioId=$scenarioId, tag=$recordTag, id=$recordId" }
        } catch (e: org.springframework.dao.DataIntegrityViolationException) {
            // Record already exists (duplicate key), which is expected behavior
            logger.debug { "Record already exists: sessionId=$gameSessionId, scenarioId=$scenarioId, tag=$recordTag, id=$recordId" }
        } catch (e: Exception) {
            // Unexpected error, log as warning
            logger.warn(e) { "Failed to save record: sessionId=$gameSessionId, scenarioId=$scenarioId, tag=$recordTag, id=$recordId" }
        }
    }
}