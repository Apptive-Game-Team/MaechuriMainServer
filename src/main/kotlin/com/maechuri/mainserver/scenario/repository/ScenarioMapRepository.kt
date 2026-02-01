package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.ScenarioMap
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface ScenarioMapRepository : R2dbcRepository<ScenarioMap, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<ScenarioMap>
}
