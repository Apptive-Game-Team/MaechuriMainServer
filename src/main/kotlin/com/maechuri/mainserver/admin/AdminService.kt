package com.maechuri.mainserver.admin

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service

@Service
class AdminService(private val databaseClient: DatabaseClient) {

    suspend fun updateSuspectPosition(scenarioId: Long, suspectId: Long, locationId: Long, x: Short, y: Short) {
        val updated = databaseClient.sql("UPDATE suspect SET location_id = :locationId, x = :x, y = :y WHERE scenario_id = :scenarioId AND suspect_id = :suspectId")
            .bind("locationId", locationId)
            .bind("x", x)
            .bind("y", y)
            .bind("scenarioId", scenarioId)
            .bind("suspectId", suspectId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        if (updated == 0L) throw NoSuchElementException("Suspect $suspectId not found in scenario $scenarioId")
    }

    suspend fun updateCluePosition(scenarioId: Long, clueId: Long, x: Short, y: Short) {
        val updated = databaseClient.sql("UPDATE clue SET x = :x, y = :y WHERE scenario_id = :scenarioId AND clue_id = :clueId")
            .bind("x", x)
            .bind("y", y)
            .bind("scenarioId", scenarioId)
            .bind("clueId", clueId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        if (updated == 0L) throw NoSuchElementException("Clue $clueId not found in scenario $scenarioId")
    }
}
