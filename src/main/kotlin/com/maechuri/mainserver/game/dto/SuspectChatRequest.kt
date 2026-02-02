package com.maechuri.mainserver.game.dto

data class SuspectChatRequest(
    val session_id: String,
    val scenario_id: Long,
    val suspect_id: Long,
    val user_message: String
)