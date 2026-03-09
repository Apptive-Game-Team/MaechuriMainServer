package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.GameSession
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface GameSessionRepository : R2dbcRepository<GameSession, Long> {

    suspend fun findBySessionIdAndScenarioId(sessionId: String, scenarioId: Long): GameSession?
}