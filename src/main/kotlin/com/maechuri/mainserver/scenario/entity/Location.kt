package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class Location(
    @Id
    val locationId: Long,
    val scenarioId: Long,
    val name: String,
)