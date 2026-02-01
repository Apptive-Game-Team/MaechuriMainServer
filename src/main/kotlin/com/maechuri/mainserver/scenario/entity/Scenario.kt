package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.sql.Time
import java.sql.Timestamp

@Table(name = "scenario")
data class Scenario(
    @Id
    val scenarioId: Long? = null,
    val difficulty: Difficulty,
    val theme: String,
    val tone: String,
    val language: String,

    val incidentType: String,
    val incidentSummary: String,
    val incidentTimeStart: Time,
    val incidentTimeEnd: Time,
    val primaryObject: String,

    val crimeTimeStart: Time,
    val crimeTimeEnd: Time,
    val crimeMethod: String,

    val noSupernatural: Boolean,
    val noTimeTravel: Boolean,

    val createdAt: Timestamp,
    val incidentLocationId: Long?,
    val crimeLocationId: Long?,
)