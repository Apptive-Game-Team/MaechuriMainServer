package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("location")
data class Location(
    val scenarioId: Long,
    val locationId: Long,
    val name: String,
    val type: String,
    val x: Short,
    val y: Short,
    val width: Short,
    val height: Short,
    val canSee: String, // jsonb
    val cannotSee: String, // jsonb
    val accessRequires: String?,
    val floorUrl: String? = null,
    val wallUrl: String? = null,
)
