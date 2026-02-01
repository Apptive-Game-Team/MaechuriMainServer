package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.ScenarioContext
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface ScenarioContextRepository : R2dbcRepository<ScenarioContext, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<ScenarioContext>
}
