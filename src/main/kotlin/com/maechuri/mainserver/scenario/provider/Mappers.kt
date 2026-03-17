package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.domain.Clue as DomainClue
import com.maechuri.mainserver.scenario.domain.Fact as DomainFact
import com.maechuri.mainserver.scenario.domain.Location as DomainLocation
import com.maechuri.mainserver.scenario.domain.Scenario as DomainScenario
import com.maechuri.mainserver.scenario.domain.Suspect as DomainSuspect
import com.maechuri.mainserver.scenario.entity.Clue
import com.maechuri.mainserver.scenario.entity.Fact
import com.maechuri.mainserver.scenario.entity.Location
import com.maechuri.mainserver.scenario.entity.Scenario
import com.maechuri.mainserver.scenario.entity.Suspect
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.sql.Time
import java.sql.Timestamp

val objectMapper: ObjectMapper = jacksonObjectMapper()

// Entity to Domain Mappings
fun Scenario.toDomain(): DomainScenario {
    return DomainScenario(
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

fun Location.toDomain(): DomainLocation {
    val canSeeList: List<Long> = if (this.canSee.isNotBlank()) objectMapper.readValue(this.canSee) else emptyList()
    val cannotSeeList: List<Long> = if (this.cannotSee.isNotBlank()) objectMapper.readValue(this.cannotSee) else emptyList()

    return DomainLocation(
        locationId = this.locationId,
        name = this.name,
        type = this.type,
        x = this.x,
        y = this.y,
        width = this.width,
        height = this.height,
        canSee = canSeeList,
        cannotSee = cannotSeeList,
        accessRequires = this.accessRequires,
        floorUrl = this.floorUrl,
        wallUrl = this.wallUrl,
    )
}

fun Clue.toDomain(location: DomainLocation): DomainClue {
    val relatedSuspectIdsList: List<Long>? = this.relatedSuspectIds?.let { if (it.isNotBlank()) objectMapper.readValue(it) else emptyList() }

    return DomainClue(
        clueId = this.clueId,
        name = this.name,
        location = location,
        description = this.description,
        logicExplanation = this.logicExplanation,
        decodedAnswer = this.decodedAnswer,
        isRedHerring = this.isRedHerring,
        relatedSuspectIds = relatedSuspectIdsList,
        x = this.x,
        y = this.y,
        visualDescription = this.visualDescription,
        assetsUrl = this.assetsUrl,
    )
}

fun Suspect.toDomain(): DomainSuspect {
    return DomainSuspect(
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
        locationId = this.locationId,
        x = this.x,
        y = this.y,
        visualDescription = this.visualDescription,
        assetsUrl = this.assetsUrl,
    )
}

fun Fact.toDomain(): DomainFact {
    val contentMap: Map<String, Any> = if (this.content.isNotBlank()) objectMapper.readValue(this.content) else emptyMap()

    return DomainFact(
        scenarioId = this.scenarioId,
        suspectId = this.suspectId,
        factId = this.factId,
        threshold = this.threshold,
        type = this.type,
        content = contentMap,
    )
}

// Domain to Entity Mappings
fun DomainScenario.toEntity(): Scenario {
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

fun DomainLocation.toEntity(scenarioId: Long): Location {
    return Location(
        scenarioId = scenarioId,
        locationId = this.locationId,
        name = this.name,
        type = this.type,
        x = this.x,
        y = this.y,
        width = this.width,
        height = this.height,
        canSee = objectMapper.writeValueAsString(this.canSee),
        cannotSee = objectMapper.writeValueAsString(this.cannotSee),
        accessRequires = this.accessRequires,
        floorUrl = this.floorUrl,
        wallUrl = this.wallUrl,
    )
}

fun DomainClue.toEntity(scenarioId: Long): Clue {
    return Clue(
        scenarioId = scenarioId,
        clueId = this.clueId,
        name = this.name,
        locationId = this.location.locationId,
        description = this.description,
        logicExplanation = this.logicExplanation,
        decodedAnswer = this.decodedAnswer,
        isRedHerring = this.isRedHerring,
        relatedSuspectIds = this.relatedSuspectIds?.let { objectMapper.writeValueAsString(it) },
        x = this.x,
        y = this.y,
        visualDescription = this.visualDescription,
        assetsUrl = this.assetsUrl,
    )
}

fun DomainSuspect.toEntity(scenarioId: Long): Suspect {
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
        locationId = this.locationId,
        x = this.x,
        y = this.y,
        visualDescription = this.visualDescription,
        assetsUrl = this.assetsUrl,
    )
}

fun DomainFact.toEntity(): Fact {
    return Fact(
        scenarioId = this.scenarioId,
        suspectId = this.suspectId,
        factId = this.factId,
        threshold = this.threshold,
        type = this.type,
        content = objectMapper.writeValueAsString(this.content),
    )
}
