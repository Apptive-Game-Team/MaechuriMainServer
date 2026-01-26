package com.maechuri.mainserver.scenario.entity

import com.maechuri.mainserver.scenario.domain.Clue
import com.maechuri.mainserver.scenario.domain.Location
import com.maechuri.mainserver.scenario.domain.Scenario
import com.maechuri.mainserver.scenario.domain.Suspect
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
    val incidentLocation: String,
    val primaryObject: String,

    val crimeTimeStart: Time,
    val crimeTimeEnd: Time,
    val crimeLocation: String,
    val crimeMethod: String,

    val noSupernatural: Boolean,
    val noTimeTravel: Boolean,

    val createdAt: Timestamp
) {
    fun toDomain(clues: List<Clue>, suspects: List<Suspect>, locations: List<Location>): Scenario {
        return Scenario(
            scenarioId = scenarioId,
            difficulty = difficulty,
            theme = theme,
            tone = tone,
            language = language,
            incidentType = incidentType,
            incidentSummary = incidentSummary,
            incidentTimeEnd = incidentTimeEnd.toLocalTime(),
            incidentTimeStart = incidentTimeStart.toLocalTime(),
            incidentLocation = incidentLocation,
            primaryObject = primaryObject,
            crimeTimeStart = crimeTimeStart.toLocalTime(),
            crimeTimeEnd = crimeTimeEnd.toLocalTime(),
            crimeLocation = crimeLocation,
            crimeMethod = crimeMethod,
            noSupernatural = noSupernatural,
            noTimeTravel = noTimeTravel,
            clues = clues,
            suspects = suspects,
            locations = locations,
            createdAt = createdAt.toLocalDateTime()
        )
    }
}