package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.SuspectTimeline
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface SuspectTimelineRepository : R2dbcRepository<SuspectTimeline, Long> {
    fun findAllByScenarioId(scenarioId: Long): Flux<SuspectTimeline>
}
