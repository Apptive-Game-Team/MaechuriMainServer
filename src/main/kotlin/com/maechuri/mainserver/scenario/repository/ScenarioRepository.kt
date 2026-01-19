package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Scenario
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface ScenarioRepository : R2dbcRepository<Scenario, Long> {
}
