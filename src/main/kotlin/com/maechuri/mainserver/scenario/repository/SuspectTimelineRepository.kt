package com.maechuri.mainserver.scenario.repository

import com.maechuri.mainserver.scenario.entity.SuspectTimeline
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface SuspectTimelineRepository : R2dbcRepository<SuspectTimeline, Int> {
}
