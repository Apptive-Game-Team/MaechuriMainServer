package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("furniture")
data class Furniture(
    @Id
    val id: Long,
    val scenarioId: Long,
    val locationId: Long,
    val name: String,
    val description: String?,
    val originX: Int,
    val originY: Int,
    val width: Int,
    val height: Int,
    val assetsUrl: String?,
)
