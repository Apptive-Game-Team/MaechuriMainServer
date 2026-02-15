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

    suspend fun handleInteraction(scenarioId: Long, objectId: String, request: InteractRequest): InteractResponse {
        val objectRealId = objectId.split(":").get(1).toLong()
        return when (objectId.get(0)) {
            's' -> handleSuspectInteraction(scenarioId, objectRealId, request)
            'i' -> handleDetectiveInteraction(scenarioId, request)
            'c' -> handleClueInteraction(scenarioId, objectRealId, request.gameSessionId)
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

        // Save suspect interaction to records if gameSessionId is provided
        request.gameSessionId?.let { sessionId ->
            saveRecord(sessionId, "s", suspectId)
            
            // Save revealed facts if any
            responseMessage.revealed_fact_ids?.forEach { factId ->
                saveRecord(sessionId, "f", factId)
            }
        }

        return InteractResponse(
            type = "two-way",
            message = responseMessage.answer,
            pressure = responseMessage.pressure,
            pressureDelta = responseMessage.pressure_delta,
            revealedFactIds = responseMessage.revealed_fact_ids
        )
    }

    private suspend fun handleClueInteraction(scenarioId: Long, objectId: Long, gameSessionId: String?): InteractResponse {
        val clue = clueRepository.findByScenarioIdAndClueId(scenarioId, objectId)
            .awaitSingle()

        // Save clue interaction to records if gameSessionId is provided
        gameSessionId?.let { sessionId ->
            saveRecord(sessionId, "c", objectId)
        }

        return InteractResponse(
            type = "simple",
            message = clue.description,
            name = clue.name,
        )
    }

    private suspend fun saveRecord(gameSessionId: String, recordTag: String, recordId: Long) {
        try {
            val record = GameSessionRecord(
                gameSessionId = gameSessionId,
                recordTag = recordTag,
                recordId = recordId,
                interactedAt = LocalDateTime.now()
            )
            gameSessionRecordRepository.save(record).awaitSingle()
            logger.debug { "Saved record: sessionId=$gameSessionId, tag=$recordTag, id=$recordId" }
        } catch (e: Exception) {
            // Ignore duplicate key errors (record already exists)
            logger.debug { "Record already exists or error saving: sessionId=$gameSessionId, tag=$recordTag, id=$recordId" }
        }
    }
}