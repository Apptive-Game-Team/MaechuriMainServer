package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Clue
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface ClueRepository : R2dbcRepository<Clue, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<Clue>
}
