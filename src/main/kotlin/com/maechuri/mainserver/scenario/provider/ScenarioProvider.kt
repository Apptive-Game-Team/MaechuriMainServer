package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.domain.Scenario
import com.maechuri.mainserver.scenario.repository.*
import com.maechuri.mainserver.game.repository.AssetRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component


@Component
class ScenarioProvider(
    private val clueRepository: ClueRepository,
    private val locationRepository: LocationRepository,
    private val scenarioRepository: ScenarioRepository,
    private val suspectRepository: SuspectRepository,
    private val factRepository: FactRepository,
    private val assetRepository: AssetRepository,
    private val furnitureRepository: FurnitureRepository,
) {

    suspend fun findScenario(scenarioId: Long): Scenario {
        val scenarioEntity = scenarioRepository.findById(scenarioId).awaitSingle()
        val domainScenario = scenarioEntity.toDomain()

        // 1. Fetch all entities
        val locationEntities = locationRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val clueEntities = clueRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val suspectEntities = suspectRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val factEntities = factRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val furnitureEntities = furnitureRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()

        val assetIds = (clueEntities.mapNotNull { it.assetId } + suspectEntities.mapNotNull { it.assetId }).distinct()
        val assetsMap = if (assetIds.isNotEmpty()) {
            assetRepository.findAllById(assetIds).collectList().awaitSingle().associateBy { it.id }
        } else {
            emptyMap()
        }

        // 2. Convert entities to domain objects
        val domainLocations = locationEntities.map { it.toDomain() }
        val locationMap = domainLocations.associateBy { it.locationId }

        val domainClues = clueEntities.map {
            val location = locationMap[it.locationId]
                ?: throw IllegalStateException("Location not found for clue ${it.clueId}")
            val asset = assetsMap[it.assetId]
            it.toDomain(location).copy(assetsUrl = asset?.finalUrl)
        }

        val domainSuspects = suspectEntities.map {
            val asset = assetsMap[it.assetId]
            it.toDomain().copy(assetsUrl = asset?.finalUrl)
        }
        val domainFacts = factEntities.map { it.toDomain() }
        val domainFurnitures = furnitureEntities.map { it.toDomain() }

        // 3. Assemble the full domain object
        domainScenario.locations = domainLocations
        domainScenario.clues = domainClues
        domainScenario.suspects = domainSuspects
        domainScenario.facts = domainFacts
        domainScenario.furnitures = domainFurnitures

        return domainScenario
    }
}
