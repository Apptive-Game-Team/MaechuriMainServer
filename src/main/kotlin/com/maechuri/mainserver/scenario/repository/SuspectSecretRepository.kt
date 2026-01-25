package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.SuspectSecret
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface SuspectSecretRepository : R2dbcRepository<SuspectSecret, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<SuspectSecret>
}
