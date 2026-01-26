package com.maechuri.mainserver.scenario.domain

data class Location(
    val locationId: Long,
    val name: String,

    val accessRules: List<AccessRule>
)