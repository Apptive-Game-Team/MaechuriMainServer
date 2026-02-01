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

    val locations: List<Location> = emptyList(),
    val clues: List<Clue> = emptyList(),
    val suspects: List<Suspect> = emptyList(),
    val facts: List<Fact> = emptyList(),
    val maps: List<ScenarioMap> = emptyList(),
    val contexts: List<ScenarioContext> = emptyList(),
)