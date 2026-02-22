package com.maechuri.mainserver.admin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service

@Service
class AdminService(private val jdbcClient: JdbcClient) {

    suspend fun updateSuspectPosition(scenarioId: Long, suspectId: Long, x: Short, y: Short) {
        withContext(Dispatchers.IO) {
            val updated = jdbcClient.sql("UPDATE suspect SET x = :x, y = :y WHERE scenario_id = :scenarioId AND suspect_id = :suspectId")
                .param("x", x)
                .param("y", y)
                .param("scenarioId", scenarioId)
                .param("suspectId", suspectId)
                .update()
            if (updated == 0) throw NoSuchElementException("Suspect $suspectId not found in scenario $scenarioId")
        }
    }

    suspend fun updateCluePosition(scenarioId: Long, clueId: Long, x: Short, y: Short) {
        withContext(Dispatchers.IO) {
            val updated = jdbcClient.sql("UPDATE clue SET x = :x, y = :y WHERE scenario_id = :scenarioId AND clue_id = :clueId")
                .param("x", x)
                .param("y", y)
                .param("scenarioId", scenarioId)
                .param("clueId", clueId)
                .update()
            if (updated == 0) throw NoSuchElementException("Clue $clueId not found in scenario $scenarioId")
        }
    }
}
