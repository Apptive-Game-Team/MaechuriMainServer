package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.Suspect
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface SuspectRepository : R2dbcRepository<Suspect, Int> {
}
