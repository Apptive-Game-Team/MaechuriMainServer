package com.maechuri.mainserver.scenario.domain

data class Fact(
    val scenarioId: Long,
    val suspectId: Long,
    val factId: Long,
    val threshold: Int,
    val type: String,
    val content: Map<String, Any>, // This should align with the JSONB content in the DB
)
