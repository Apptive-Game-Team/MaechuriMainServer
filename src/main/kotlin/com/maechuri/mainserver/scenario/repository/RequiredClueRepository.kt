package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.RequiredClue
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface RequiredClueRepository : R2dbcRepository<RequiredClue, Long> {
}