package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.domain.*
import com.maechuri.mainserver.scenario.entity.ScenarioMap
import com.maechuri.mainserver.scenario.entity.Clue
import com.maechuri.mainserver.scenario.entity.Fact
import com.maechuri.mainserver.scenario.entity.Location
import com.maechuri.mainserver.scenario.entity.Scenario
import com.maechuri.mainserver.scenario.entity.ScenarioContext
import com.maechuri.mainserver.scenario.entity.Suspect
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.sql.Time
import java.sql.Timestamp

val objectMapper: ObjectMapper = jacksonObjectMapper()

// Entity to Domain Mappings
fun Scenario.toDomain(): com.maechuri.mainserver.scenario.domain.Scenario {
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
        incidentLocationId = this.incidentLocationId,
        primaryObject = this.primaryObject,
        crimeTimeStart = this.crimeTimeStart.toLocalTime(),
        crimeTimeEnd = this.crimeTimeEnd.toLocalTime(),
        crimeLocationId = this.crimeLocationId,
        crimeMethod = this.crimeMethod,
        noSupernatural = this.noSupernatural,
        noTimeTravel = this.noTimeTravel,
        createdAt = this.createdAt.toLocalDateTime()
    )
}

fun Location.toDomain(): com.maechuri.mainserver.scenario.domain.Location {
    val canSeeList: List<Long> = if (this.canSee.isNotBlank()) objectMapper.readValue(this.canSee) else emptyList()
    val cannotSeeList: List<Long> = if (this.cannotSee.isNotBlank()) objectMapper.readValue(this.cannotSee) else emptyList()

    return com.maechuri.mainserver.scenario.domain.Location(
        locationId = this.locationId,
        name = this.name,
        canSee = canSeeList,
        cannotSee = cannotSeeList,
        accessRequires = this.accessRequires
    )
}

fun Clue.toDomain(location: com.maechuri.mainserver.scenario.domain.Location): com.maechuri.mainserver.scenario.domain.Clue {
    val relatedFactIdsList: List<Long>? = this.relatedFactIds?.let { if (it.isNotBlank()) objectMapper.readValue(it) else emptyList() }

    return com.maechuri.mainserver.scenario.domain.Clue(
        clueId = this.clueId,
        name = this.name,
        location = location,
        description = this.description,
        logicExplanation = this.logicExplanation,
        decodedAnswer = this.decodedAnswer,
        isRedHerring = this.isRedHerring,
        relatedFactIds = relatedFactIdsList,
        x = this.x,
        y = this.y,
    )
}

fun Suspect.toDomain(): com.maechuri.mainserver.scenario.domain.Suspect {
    val criticalClueIdsList: List<Long> = if (this.criticalClueIds.isNotBlank()) objectMapper.readValue(this.criticalClueIds) else emptyList()

    return com.maechuri.mainserver.scenario.domain.Suspect(
        suspectId = this.suspectId,
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
        criticalClueIds = criticalClueIdsList,
        x = this.x,
        y = this.y,
    )
}

fun Fact.toDomain(): com.maechuri.mainserver.scenario.domain.Fact {
    val contentMap: Map<String, Any> = if (this.content.isNotBlank()) objectMapper.readValue(this.content) else emptyMap()

    return com.maechuri.mainserver.scenario.domain.Fact(
        scenarioId = this.scenarioId,
        suspectId = this.suspectId,
        factId = this.factId,
        threshold = this.threshold,
        type = this.type,
        content = contentMap,
    )
}

fun ScenarioMap.toDomain(): com.maechuri.mainserver.scenario.domain.ScenarioMap {
    val extraDataMap: Map<String, Any> = if (this.extraData.isNotBlank()) objectMapper.readValue(this.extraData) else emptyMap()
    return com.maechuri.mainserver.scenario.domain.ScenarioMap(
        mapId = this.mapId,
        type = this.type,
        name = this.name,
        x = this.x,
        y = this.y,
        width = this.width,
        height = this.height,
        extraData = extraDataMap,
    )
}

fun ScenarioContext.toDomain(): com.maechuri.mainserver.scenario.domain.ScenarioContext {
    val extraDataMap: Map<String, Any>? = this.extraData?.let { if (it.isNotBlank()) objectMapper.readValue(it) else emptyMap() }
    return com.maechuri.mainserver.scenario.domain.ScenarioContext(
        contextId = this.contextId,
        type = this.type,
        content = this.content,
        extraData = extraDataMap,
    )
}

// Domain to Entity Mappings
fun com.maechuri.mainserver.scenario.domain.Scenario.toEntity(): Scenario {
    return Scenario(
        scenarioId = this.scenarioId,
        difficulty = this.difficulty,
        theme = this.theme,
        tone = this.tone,
        language = this.language,
        incidentType = this.incidentType,
        incidentSummary = this.incidentSummary,
        incidentTimeStart = Time.valueOf(this.incidentTimeStart),
        incidentTimeEnd = Time.valueOf(this.incidentTimeEnd),
        incidentLocationId = this.incidentLocationId,
        primaryObject = this.primaryObject,
        crimeTimeStart = Time.valueOf(this.crimeTimeStart),
        crimeTimeEnd = Time.valueOf(this.crimeTimeEnd),
        crimeMethod = this.crimeMethod,
        noSupernatural = this.noSupernatural,
        noTimeTravel = this.noTimeTravel,
        createdAt = Timestamp.valueOf(this.createdAt),
        crimeLocationId = this.crimeLocationId,
    )
}

fun com.maechuri.mainserver.scenario.domain.Location.toEntity(scenarioId: Long): Location {
    return Location(
        scenarioId = scenarioId,
        locationId = this.locationId,
        name = this.name,
        canSee = objectMapper.writeValueAsString(this.canSee),
        cannotSee = objectMapper.writeValueAsString(this.cannotSee),
        accessRequires = this.accessRequires
    )
}

fun com.maechuri.mainserver.scenario.domain.Clue.toEntity(scenarioId: Long): Clue {
    return Clue(
        scenarioId = scenarioId,
        clueId = this.clueId,
        name = this.name,
        locationId = this.location.locationId,
        description = this.description,
        logicExplanation = this.logicExplanation,
        decodedAnswer = this.decodedAnswer,
        isRedHerring = this.isRedHerring,
        relatedFactIds = this.relatedFactIds?.let { objectMapper.writeValueAsString(it) },
        x = this.x,
        y = this.y,
    )
}

fun com.maechuri.mainserver.scenario.domain.Suspect.toEntity(scenarioId: Long): Suspect {
    return Suspect(
        scenarioId = scenarioId,
        suspectId = this.suspectId,
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
        criticalClueIds = objectMapper.writeValueAsString(this.criticalClueIds),
        x = this.x,
        y = this.y,
    )
}

fun com.maechuri.mainserver.scenario.domain.Fact.toEntity(): Fact {
    return Fact(
        scenarioId = this.scenarioId,
        suspectId = this.suspectId,
        factId = this.factId,
        threshold = this.threshold,
        type = this.type,
        content = objectMapper.writeValueAsString(this.content),
    )
}

fun com.maechuri.mainserver.scenario.domain.ScenarioMap.toEntity(scenarioId: Long): ScenarioMap {
    return ScenarioMap(
        scenarioId = scenarioId,
        mapId = this.mapId,
        type = this.type,
        name = this.name,
        x = this.x,
        y = this.y,
        width = this.width,
        height = this.height,
        extraData = objectMapper.writeValueAsString(this.extraData),
    )
}

fun com.maechuri.mainserver.scenario.domain.ScenarioContext.toEntity(scenarioId: Long): ScenarioContext {
    return ScenarioContext(
        scenarioId = scenarioId,
        contextId = this.contextId,
        type = this.type,
        content = this.content,
        extraData = this.extraData?.let { objectMapper.writeValueAsString(it) },
    )
}