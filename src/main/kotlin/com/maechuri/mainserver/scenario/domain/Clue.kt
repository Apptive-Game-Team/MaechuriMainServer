package com.maechuri.mainserver.scenario.domain

data class Clue(
    val clueId: Long,
    val name: String,
    val location: Location,
    val description: String,
    val logicExplanation: String,
    val decodedAnswer: String?,
    val isRedHerring: Boolean,
    val relatedSuspectIds: List<Long>?,
    val x: Short?,
    val y: Short?,
    val visualDescription: String? = null,
    val assetId: Long? = null,
    val assetsUrl: String? = null,
    )