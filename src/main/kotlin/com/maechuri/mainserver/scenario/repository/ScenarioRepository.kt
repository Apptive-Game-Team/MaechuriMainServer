package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Scenario
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface ScenarioRepository : R2dbcRepository<Scenario, Long> {
    fun findTopByOrderByCreatedAtDesc(): Mono<Scenario>
    fun findByDate(date: LocalDate): Mono<Scenario>
    fun findByDateBetween(from: LocalDate, to: LocalDate): Flux<Scenario>
    fun findTopByDateLessThanEqualOrderByDateDesc(date: LocalDate): Mono<Scenario>
}
