package com.maechuri.mainserver.game.repository

import com.maechuri.mainserver.game.entity.GameSessionRecord
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface GameSessionRecordRepository : R2dbcRepository<GameSessionRecord, Long> {
    fun findAllByGameSessionId(gameSessionId: String): Flux<GameSessionRecord>
}
