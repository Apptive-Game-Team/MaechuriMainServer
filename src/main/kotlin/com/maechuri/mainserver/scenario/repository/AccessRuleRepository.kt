package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.AccessRule
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface AccessRuleRepository : R2dbcRepository<AccessRule, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<AccessRule>
}
