package com.maechuri.mainserver.scenario.entity

import org.springframework.data.annotation.Id

data class SuspectTimeline(
    val scenarioId: Int,
    val suspectId: Int,
    @Id
    val timelineId: Int,
    val timeRange: String,
    val location: String,
    val activity: String,
    val canProve: Boolean,
    val witness: String?
)
