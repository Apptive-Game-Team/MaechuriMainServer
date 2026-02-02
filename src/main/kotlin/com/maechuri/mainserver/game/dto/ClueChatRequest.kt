package com.maechuri.mainserver.game.dto

data class ClueChatRequest(
    val session_id: String,
    val scenario_id: Long,
    val user_message: String,
)
