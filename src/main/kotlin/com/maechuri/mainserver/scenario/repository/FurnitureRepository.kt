package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Furniture
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface FurnitureRepository : R2dbcRepository<Furniture, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<Furniture>
}
