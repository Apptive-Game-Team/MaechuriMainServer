package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.domain.*
import com.maechuri.mainserver.scenario.entity.AccessRule
import com.maechuri.mainserver.scenario.entity.Clue
import com.maechuri.mainserver.scenario.entity.Location
import com.maechuri.mainserver.scenario.entity.Scenario
import com.maechuri.mainserver.scenario.entity.Suspect
import com.maechuri.mainserver.scenario.entity.SuspectSecret
import com.maechuri.mainserver.scenario.entity.SuspectTimeline

fun Scenario.toDomain(
    locations: List<com.maechuri.mainserver.scenario.domain.Location>,
    clues: List<com.maechuri.mainserver.scenario.domain.Clue>,
    suspects: List<com.maechuri.mainserver.scenario.domain.Suspect>
): com.maechuri.mainserver.scenario.domain.Scenario {
    return com.maechuri.mainserver.scenario.domain.Scenario(
        scenarioId = this.scenarioId,
        difficulty = this.difficulty,
        theme = this.theme,
        tone = this.tone,
        language = this.language,
        incidentType = this.incidentType,
        incidentSummary = this.incidentSummary,
        incidentTimeStart = this.incidentTimeStart.toLocalTime(),
        incidentTimeEnd = this.incidentTimeEnd.toLocalTime(),
        incidentLocation = this.incidentLocation,
        primaryObject = this.primaryObject,
        crimeTimeStart = this.crimeTimeStart.toLocalTime(),
        crimeTimeEnd = this.crimeTimeEnd.toLocalTime(),
        crimeLocation = this.crimeLocation,
        crimeMethod = this.crimeMethod,
        noSupernatural = this.noSupernatural,
        noTimeTravel = this.noTimeTravel,
        locations = locations,
        clues = clues,
        suspects = suspects,
        createdAt = this.createdAt.toLocalDateTime()
    )
}

fun Location.toDomain(accessRules: List<com.maechuri.mainserver.scenario.domain.AccessRule>): com.maechuri.mainserver.scenario.domain.Location {
    return com.maechuri.mainserver.scenario.domain.Location(
        locationId = this.locationId,
        name = this.name,
        accessRules = accessRules
    )
}

fun AccessRule.toDomain(): com.maechuri.mainserver.scenario.domain.AccessRule {
    return com.maechuri.mainserver.scenario.domain.AccessRule(
        ruleId = this.ruleId,
        requires = this.requires
    )
}

fun Clue.toDomain(
    location: com.maechuri.mainserver.scenario.domain.Location,
    relatedSuspects: List<com.maechuri.mainserver.scenario.domain.Suspect>
): com.maechuri.mainserver.scenario.domain.Clue {
    return com.maechuri.mainserver.scenario.domain.Clue(
        clueId = this.clueId.toInt(),
        name = this.name,
        location = location,
        description = this.description,
        relatedSuspects = relatedSuspects,
        logicExplanation = this.logicExplanation,
        isRedHerring = this.isRedHerring
    )
}

fun Suspect.toDomain(
    criticalClues: List<com.maechuri.mainserver.scenario.domain.Clue>,
    secrets: List<com.maechuri.mainserver.scenario.domain.SuspectSecret>,
    timelines: List<com.maechuri.mainserver.scenario.domain.SuspectTimeline>
): com.maechuri.mainserver.scenario.domain.Suspect {
    return com.maechuri.mainserver.scenario.domain.Suspect(
        suspectId = this.suspectId.toInt(),
        name = this.name,
        role = this.role,
        age = this.age,
        gender = this.gender,
        description = this.description,
        isCulprit = this.isCulprit,
        motive = this.motive,
        alibiSummary = this.alibiSummary,
        speechStyle = this.speechStyle,
        emotionalTendency = this.emotionalTendency,
        lyingPattern = this.lyingPattern,
        criticalClues = criticalClues,
        secrets = secrets,
        timeLines = timelines
    )
}

fun SuspectSecret.toDomain(triggerClues: List<com.maechuri.mainserver.scenario.domain.Clue>): com.maechuri.mainserver.scenario.domain.SuspectSecret {
    return com.maechuri.mainserver.scenario.domain.SuspectSecret(
        secretId = this.secretId,
        threshold = this.threshold,
        content = this.content,
        triggerClues = triggerClues
    )
}

fun SuspectTimeline.toDomain(location: com.maechuri.mainserver.scenario.domain.Location): com.maechuri.mainserver.scenario.domain.SuspectTimeline {
    return com.maechuri.mainserver.scenario.domain.SuspectTimeline(
        timelineId = this.timelineId,
        timeRange = this.timeRange,
        location = location,
        activity = this.activity,
        canProve = this.canProve,
        witness = this.witness
    )
}