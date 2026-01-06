package com.maechuri.mainserver.api.scenario.dto

data class InteractResponse(
    val type: String,
    val message: String,
    val history: String? = null,
    val name: String? = null
)
