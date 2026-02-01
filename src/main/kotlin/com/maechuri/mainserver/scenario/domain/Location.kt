package com.maechuri.mainserver.scenario.domain

data class Location(
    val locationId: Long,
    val name: String,
    val canSee: List<Long>,
    val cannotSee: List<Long>,
    val accessRequires: String?,
)