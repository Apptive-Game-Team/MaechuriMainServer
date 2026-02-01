package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Fact
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface FactRepository : R2dbcRepository<Fact, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<Fact>
}
