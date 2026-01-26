package com.maechuri.mainserver.scenario.domain

data class SuspectTimeline(
    val timelineId: Long,
    val timeRange: String,
    val location: Location,
    val activity: String,
    val canProve: Boolean,
    val witness: String?
)
