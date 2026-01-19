package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.RequiredEvidence
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface RequiredEvidenceRepository : R2dbcRepository<RequiredEvidence, Int> {
}
