package com.maechuri.mainserver.scenario.domain

import com.maechuri.mainserver.scenario.entity.Difficulty
import java.time.LocalDateTime
import java.time.LocalTime

data class Scenario(
    val scenarioId: Long? = null,
    val difficulty: Difficulty,
    val theme: String,
    val tone: String,
    val language: String,

    val incidentType: String,
    val incidentSummary: String,
    val incidentTimeStart: LocalTime,
    val incidentTimeEnd: LocalTime,
    val incidentLocationId: Long?,
    val primaryObject: String,

    val crimeTimeStart: LocalTime,
    val crimeTimeEnd: LocalTime,
    val crimeLocationId: Long?,
    val crimeMethod: String,

    val noSupernatural: Boolean,
    val noTimeTravel: Boolean,

    val createdAt: LocalDateTime,

    var locations: List<Location> = emptyList(),
    var clues: List<Clue> = emptyList(),
    var suspects: List<Suspect> = emptyList(),
    var facts: List<Fact> = emptyList(),
    var maps: List<ScenarioMap> = emptyList(),
    var contexts: List<ScenarioContext> = emptyList(),
)