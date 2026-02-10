package com.maechuri.mainserver.game.dto.solve

data class ClientSolveRequest(
    val message: String,
    val suspectIds: List<String>,
)