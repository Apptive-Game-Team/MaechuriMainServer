package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.SuspectSecret
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface SuspectSecretRepository : R2dbcRepository<SuspectSecret, Int> {
}
