package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.AccessRule
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface AccessRuleRepository : R2dbcRepository<AccessRule, Int> {
}
