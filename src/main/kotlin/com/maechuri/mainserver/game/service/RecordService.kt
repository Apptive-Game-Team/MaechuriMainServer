package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.dto.RecordResponse
import com.maechuri.mainserver.game.dto.RecordsListResponse
import com.maechuri.mainserver.game.repository.GameSessionRecordRepository
import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.FactRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

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
     * @throws IllegalArgumentException if recordId format is invalid or record not found
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
        
        val records = sessionRecords.mapNotNull { sessionRecord ->
            try {
                when (sessionRecord.recordTag) {
                    "f" -> fetchFact(scenarioId, sessionRecord.recordId)
                    "s" -> fetchSuspect(scenarioId, sessionRecord.recordId)
                    "c" -> fetchClue(scenarioId, sessionRecord.recordId)
                    else -> null
                }
            } catch (e: Exception) {
                // Skip records that can't be fetched (e.g., deleted or invalid)
                null
            }
        }
        
        return RecordsListResponse(records)
    }
    
    private suspend fun fetchFact(scenarioId: Long, factId: Long): RecordResponse {
        val fact = factRepository.findByScenarioIdAndFactId(scenarioId, factId)
            .awaitSingle()
        return RecordResponse(
            name = "Fact #$factId",
            content = fact.content
        )
    }
    
    private suspend fun fetchSuspect(scenarioId: Long, suspectId: Long): RecordResponse {
        val suspect = suspectRepository.findByScenarioIdAndSuspectId(scenarioId, suspectId)
            .awaitSingle()
        return RecordResponse(
            name = suspect.name,
            content = suspect.description
        )
    }
    
    private suspend fun fetchClue(scenarioId: Long, clueId: Long): RecordResponse {
        val clue = clueRepository.findByScenarioIdAndClueId(scenarioId, clueId)
            .awaitSingle()
        return RecordResponse(
            name = clue.name,
            content = clue.description
        )
    }
}
