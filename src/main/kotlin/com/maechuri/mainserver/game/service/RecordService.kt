package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.dto.RecordResponse
import com.maechuri.mainserver.game.dto.RecordsListResponse
import com.maechuri.mainserver.game.repository.GameSessionRecordRepository
import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.FactRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

@Service
class RecordService(
    private val factRepository: FactRepository,
    private val suspectRepository: SuspectRepository,
    private val clueRepository: ClueRepository,
    private val gameSessionRecordRepository: GameSessionRecordRepository
) {

    /**
     * Retrieves a record (fact, suspect, or clue) by its composite ID.
     * 
     * @param scenarioId The scenario identifier
     * @param recordId The record identifier in format "tag:id" (e.g., "f:1", "s:101", "c:1")
     * @return RecordResponse containing name and content
     * @throws ResponseStatusException with 404 status if record not found
     * @throws IllegalArgumentException if recordId format is invalid
     */
    suspend fun getRecord(scenarioId: Long, recordId: String): RecordResponse {
        require(scenarioId > 0) { "scenarioId must be positive" }
        
        val parts = recordId.split(":")
        require(parts.size == 2) { "recordId must be in format 'tag:id'" }
        
        val tag = parts[0]
        val id = parts[1].toLongOrNull() 
            ?: throw IllegalArgumentException("Record ID must be a valid number")
        
        return when (tag) {
            "f" -> fetchFact(scenarioId, id)
            "s" -> fetchSuspect(scenarioId, id)
            "c" -> fetchClue(scenarioId, id)
            else -> throw IllegalArgumentException("Invalid record tag: $tag. Must be 'f', 's', or 'c'")
        }
    }

    /**
     * Retrieves all interacted records for a game session.
     * 
     * @param scenarioId The scenario identifier
     * @param gameSessionId The game session identifier
     * @return RecordsListResponse containing list of all interacted records
     */
    suspend fun getAllInteractedRecords(scenarioId: Long, gameSessionId: String): RecordsListResponse {
        require(scenarioId > 0) { "scenarioId must be positive" }
        require(gameSessionId.isNotBlank()) { "gameSessionId must not be blank" }
        
        val sessionRecords = gameSessionRecordRepository.findAllByGameSessionId(gameSessionId)
            .asFlow()
            .toList()
        
        // Group session records by tag to batch fetch
        val factIds = sessionRecords.filter { it.recordTag == "f" }.map { it.recordId }.toSet()
        val suspectIds = sessionRecords.filter { it.recordTag == "s" }.map { it.recordId }.toSet()
        val clueIds = sessionRecords.filter { it.recordTag == "c" }.map { it.recordId }.toSet()
        
        // Batch fetch all records by type
        val facts = if (factIds.isNotEmpty()) {
            factRepository.findAllByScenarioId(scenarioId)
                .asFlow()
                .toList()
                .filter { it.factId in factIds }
                .associateBy { it.factId }
        } else {
            emptyMap()
        }
        
        val suspects = if (suspectIds.isNotEmpty()) {
            suspectRepository.findAllByScenarioId(scenarioId)
                .asFlow()
                .toList()
                .filter { it.suspectId in suspectIds }
                .associateBy { it.suspectId }
        } else {
            emptyMap()
        }
        
        val clues = if (clueIds.isNotEmpty()) {
            clueRepository.findAllByScenarioId(scenarioId)
                .asFlow()
                .toList()
                .filter { it.clueId in clueIds }
                .associateBy { it.clueId }
        } else {
            emptyMap()
        }
        
        // Map session records to responses in order
        val records = sessionRecords.mapNotNull { sessionRecord ->
            try {
                when (sessionRecord.recordTag) {
                    "f" -> {
                        val fact = facts[sessionRecord.recordId]
                        if (fact != null) {
                            RecordResponse(
                                id = "f:${fact.factId}",
                                name = "Fact #${fact.factId}",
                                content = fact.content
                            )
                        } else {
                            logger.warn { "Fact not found: id=${sessionRecord.recordId}, scenarioId=$scenarioId" }
                            null
                        }
                    }
                    "s" -> {
                        val suspect = suspects[sessionRecord.recordId]
                        if (suspect != null) {
                            RecordResponse(
                                id = "s:${suspect.suspectId}",
                                name = suspect.name,
                                content = suspect.description
                            )
                        } else {
                            logger.warn { "Suspect not found: id=${sessionRecord.recordId}, scenarioId=$scenarioId" }
                            null
                        }
                    }
                    "c" -> {
                        val clue = clues[sessionRecord.recordId]
                        if (clue != null) {
                            RecordResponse(
                                id = "c:${clue.clueId}",
                                name = clue.name,
                                content = clue.description
                            )
                        } else {
                            logger.warn { "Clue not found: id=${sessionRecord.recordId}, scenarioId=$scenarioId" }
                            null
                        }
                    }
                    else -> {
                        logger.warn { "Unknown record tag: ${sessionRecord.recordTag}" }
                        null
                    }
                }
            } catch (e: Exception) {
                // Unexpected error, log for debugging
                logger.error(e) { "Error fetching record: tag=${sessionRecord.recordTag}, id=${sessionRecord.recordId}, scenarioId=$scenarioId" }
                null
            }
        }
        
        return RecordsListResponse(records)
    }
    
    private suspend fun fetchFact(scenarioId: Long, factId: Long): RecordResponse {
        val fact = factRepository.findByScenarioIdAndFactId(scenarioId, factId)
            .awaitSingleOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fact not found: scenarioId=$scenarioId, factId=$factId")
        return RecordResponse(
            id = "f:${factId}",
            name = "Fact #$factId",
            content = fact.content
        )
    }
    
    private suspend fun fetchSuspect(scenarioId: Long, suspectId: Long): RecordResponse {
        val suspect = suspectRepository.findByScenarioIdAndSuspectId(scenarioId, suspectId)
            .awaitSingleOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Suspect not found: scenarioId=$scenarioId, suspectId=$suspectId")
        return RecordResponse(
            id = "s:${suspectId}",
            name = suspect.name,
            content = suspect.description
        )
    }
    
    private suspend fun fetchClue(scenarioId: Long, clueId: Long): RecordResponse {
        val clue = clueRepository.findByScenarioIdAndClueId(scenarioId, clueId)
            .awaitSingleOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Clue not found: scenarioId=$scenarioId, clueId=$clueId")
        return RecordResponse(
            id = "c:${clueId}",
            name = clue.name,
            content = clue.description
        )
    }
}
