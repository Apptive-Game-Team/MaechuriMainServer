package com.maechuri.mainserver.scenario.domain

data class Location(
    val locationId: Long,
    val name: String,
    val type: String,
    val x: Short,
    val y: Short,
    val width: Short,
    val height: Short,
    val canSee: List<Long>,
    val cannotSee: List<Long>,
    val accessRequires: String?,
    val floorUrl: String? = null,
    val wallUrl: String? = null,
)
