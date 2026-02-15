package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Suspect
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SuspectRepository : R2dbcRepository<Suspect, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<Suspect>
    fun findByScenarioIdAndSuspectId(scenarioId: Long, suspectId: Long): Mono<Suspect>
}
