package com.maechuri.mainserver.scenario.domain

import com.maechuri.mainserver.scenario.domain.Clue
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
    val incidentLocation: String,
    val primaryObject: String,

    val crimeTimeStart: LocalTime,
    val crimeTimeEnd: LocalTime,
    val crimeLocation: String,
    val crimeMethod: String,

    val noSupernatural: Boolean,
    val noTimeTravel: Boolean,

    val clues: List<Clue>,
    val suspects: List<Suspect>,
    val locations: List<Location>,

    val createdAt: LocalDateTime
)