package com.maechuri.mainserver.scenario.entity

import org.springframework.data.relational.core.mapping.Table

@Table("location")
data class Location(
    val scenarioId: Long,
    val locationId: Long,
    val name: String,
    val canSee: String, // jsonb
    val cannotSee: String, // jsonb
    val accessRequires: String?,
)