package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.VisibilityRule
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface VisibilityRuleRepository : R2dbcRepository<VisibilityRule, Long> {
}
