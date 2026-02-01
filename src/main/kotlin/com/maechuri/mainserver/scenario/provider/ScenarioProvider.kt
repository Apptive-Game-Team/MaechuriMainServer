package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.domain.Scenario
import com.maechuri.mainserver.scenario.repository.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component


@Component
class ScenarioProvider(
    private val clueRepository: ClueRepository,
    private val locationRepository: LocationRepository,
    private val scenarioRepository: ScenarioRepository,
    private val suspectRepository: SuspectRepository,
    private val factRepository: FactRepository,
    private val scenarioMapRepository: ScenarioMapRepository,
    private val scenarioContextRepository: ScenarioContextRepository,
) {

    suspend fun findScenario(scenarioId: Long): Scenario {
        val scenarioEntity = scenarioRepository.findById(scenarioId).awaitSingle()
        val domainScenario = scenarioEntity.toDomain()

        // 1. Fetch all entities
        val locationEntities = locationRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val clueEntities = clueRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val suspectEntities = suspectRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val factEntities = factRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val mapEntities = scenarioMapRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val contextEntities = scenarioContextRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()

        // 2. Convert entities to domain objects
        val domainLocations = locationEntities.map { it.toDomain() }
        val locationMap = domainLocations.associateBy { it.locationId }

        val domainClues = clueEntities.map {
            val location = locationMap[it.locationId]
                ?: throw IllegalStateException("Location not found for clue ${it.clueId}")
            it.toDomain(location)
        }

        val domainSuspects = suspectEntities.map { it.toDomain() }
        val domainFacts = factEntities.map { it.toDomain() }
        val domainMaps = mapEntities.map { it.toDomain() }
        val domainContexts = contextEntities.map { it.toDomain() }

        // 3. Assemble the full domain object
        domainScenario.locations = domainLocations
        domainScenario.clues = domainClues
        domainScenario.suspects = domainSuspects
        domainScenario.facts = domainFacts
        domainScenario.maps = domainMaps
        domainScenario.contexts = domainContexts

        return domainScenario
    }
}