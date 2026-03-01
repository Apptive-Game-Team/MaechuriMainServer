package com.maechuri.mainserver.scenario.domain

data class Suspect(
    val suspectId: Long,
    val name: String,
    val role: String,
    val age: Int,
    val gender: String,
    val description: String,
    val isCulprit: Boolean,
    val motive: String?,
    val alibiSummary: String,
    val speechStyle: String,
    val emotionalTendency: String,
    val lyingPattern: String,
    val x: Short?,
    val y: Short?,
    val visualDescription: String? = null,
    val assetsUrl: String? = null,
)