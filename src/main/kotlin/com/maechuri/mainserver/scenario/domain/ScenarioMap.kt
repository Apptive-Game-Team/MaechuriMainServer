package com.maechuri.mainserver.scenario.domain

data class ScenarioMap(
    val mapId: Long,
    val type: String,
    val name: String,
    val x: Short,
    val y: Short,
    val width: Short,
    val height: Short,
    val extraData: Map<String, Any>,
)
