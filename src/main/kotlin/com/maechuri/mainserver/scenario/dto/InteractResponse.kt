package com.maechuri.mainserver.scenario.dto

data class InteractResponse(
    val type: String,
    val message: String,
    val history: String? = null,
    val name: String? = null
)
