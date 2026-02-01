package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("map")
data class ScenarioMap(
    val scenarioId: Long,
    val mapId: Long,
    val type: String,
    val name: String,
    val x: Short,
    val y: Short,
    val width: Short,
    val height: Short,
    val extraData: String, // jsonb
)
