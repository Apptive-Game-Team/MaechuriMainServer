package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.domain.Scenario
import com.maechuri.mainserver.scenario.repository.AccessRuleRepository
import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.LocationRepository
import com.maechuri.mainserver.scenario.repository.ScenarioRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import com.maechuri.mainserver.scenario.repository.SuspectSecretRepository
import com.maechuri.mainserver.scenario.repository.SuspectTimelineRepository
import com.maechuri.mainserver.scenario.repository.VisibilityRuleRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component


@Component
class ScenarioProvider(
    val accessRuleRepository: AccessRuleRepository,
    val clueRepository: ClueRepository,
    val locationRepository: LocationRepository,
    val scenarioRepository: ScenarioRepository,
    val suspectRepository: SuspectRepository,
    val suspectSecretRepository: SuspectSecretRepository,
    val suspectTimelineRepository: SuspectTimelineRepository,
    val visibilityRuleRepository: VisibilityRuleRepository
) {

    suspend fun findScenario(scenarioId: Long): Scenario {
        val scenarioEntity = scenarioRepository.findById(scenarioId).awaitSingle()

        // 1. Fetch all entities
        val locationEntities = locationRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val clueEntities = clueRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val suspectEntities = suspectRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val accessRuleEntities = accessRuleRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val suspectSecretEntities = suspectSecretRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()
        val suspectTimelineEntities = suspectTimelineRepository.findAllByScenarioId(scenarioId).collectList().awaitSingle()

        // 2. Create domain locations
        val domainLocations = locationEntities.map { loc ->
            val rules = accessRuleEntities.filter { it.locationId == loc.locationId }.map { it.toDomain() }
            loc.toDomain(rules)
        }
        val locationMap = domainLocations.associateBy { it.locationId }

        // 3. Create placeholder domain suspects
        val suspectPlaceholders = suspectEntities.map { it.toDomain(emptyList(), emptyList(), emptyList()) }
        val suspectPlaceholderMap = suspectPlaceholders.associateBy { it.suspectId.toLong() }

        // 4. Create placeholder domain clues
        val cluePlaceholders = clueEntities.map {
            val location = locationMap[it.locationId]!!
            val relatedSuspects = it.relatedSuspectIds.mapNotNull { suspectId -> suspectPlaceholderMap[suspectId] }
            it.toDomain(location, relatedSuspects)
        }
        val cluePlaceholderMap = cluePlaceholders.associateBy { it.clueId.toLong() }

        // 5. Create final domain suspects
        val finalDomainSuspects = suspectEntities.map { suspectEntity ->
            val criticalClues = suspectEntity.criticalClueIds.mapNotNull { clueId -> cluePlaceholderMap[clueId] }
            val secrets = suspectSecretEntities
                .filter { it.suspectId == suspectEntity.suspectId }
                .map { secret ->
                    val triggerClues = secret.triggerClueIds.mapNotNull { clueId -> cluePlaceholderMap[clueId] }
                    secret.toDomain(triggerClues)
                }
            val timelines = suspectTimelineEntities
                .filter { it.suspectId == suspectEntity.suspectId }
                .map { timeline ->
                    val location = locationMap[timeline.locationId]!!
                    timeline.toDomain(location)
                }
            suspectEntity.toDomain(criticalClues, secrets, timelines)
        }
        val finalSuspectMap = finalDomainSuspects.associateBy { it.suspectId.toLong() }

        // 6. Create final domain clues by re-linking suspects
        val finalDomainClues = cluePlaceholders.map { cluePlaceholder ->
            val finalRelatedSuspects = cluePlaceholder.relatedSuspects.mapNotNull { suspectPlaceholder ->
                finalSuspectMap[suspectPlaceholder.suspectId.toLong()]
            }
            cluePlaceholder.copy(relatedSuspects = finalRelatedSuspects)
        }

        return scenarioEntity.toDomain(domainLocations, finalDomainClues, finalDomainSuspects)
    }
}