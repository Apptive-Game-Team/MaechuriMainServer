package com.maechuri.mainserver.game.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "game_session_record")
data class GameSessionRecord(
    @Id
    val id: Long? = null,
    val gameSessionId: String,
    val recordTag: String,
    val recordId: Long,
    val interactedAt: LocalDateTime = LocalDateTime.now()
)
