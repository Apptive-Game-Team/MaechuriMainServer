package com.maechuri.mainserver.scenario.domain

data class SuspectSecret(
    val secretId: Long,
    val threshold: Int,
    val content: String,

    val triggerClues: List<Clue>
)
