package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class SuspectTimeline(
    val scenarioId: Long,
    val suspectId: Long,
    @Id
    val timelineId: Long,
    val timeRange: String,
    val locationId: Long,
    val activity: String,
    val canProve: Boolean,
    val witness: String?
)
