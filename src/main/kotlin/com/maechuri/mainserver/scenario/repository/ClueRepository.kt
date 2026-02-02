package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Clue
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ClueRepository : R2dbcRepository<Clue, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<Clue>
    fun findByScenarioIdAndClueId(scenarioId: Long, clueId: Long): Mono<Clue>
}
