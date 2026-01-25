package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Location
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface LocationRepository : R2dbcRepository<Location, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<Location>
}
