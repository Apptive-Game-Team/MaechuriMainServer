package com.maechuri.mainserver.game.entity

import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table(name = "game_session")
class GameSession(
    val sessionId: String,
    val scenarioId: Long,
    val currentPressure: Int,
    val createdAt: Timestamp,
    val lastActivityAt: Timestamp,
    val completedAt: Timestamp?,
) {
}