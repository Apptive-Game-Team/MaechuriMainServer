package com.maechuri.mainserver.scenario.domain

import io.micrometer.core.instrument.config.validate.Validated

class Suspect(
    val suspectId: Int,
    val name: String,
    val role: String,
    val age: Int,
    val gender: String,
    val description: String,
    val isCulprit: Boolean,
    val motive: String?,
    val alibiSummary: String,
    val speechStyle: String,
    val emotionalTendency: String,
    val lyingPattern: String,

    val criticalClues: List<Clue>,
    val secrets: List<SuspectSecret>,
    val timeLines: List<SuspectTimeline>
) {
}