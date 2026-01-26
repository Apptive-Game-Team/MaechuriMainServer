package com.maechuri.mainserver.scenario.entity

import com.maechuri.mainserver.scenario.domain.Clue
import com.maechuri.mainserver.scenario.domain.Suspect
import com.maechuri.mainserver.scenario.domain.SuspectSecret
import com.maechuri.mainserver.scenario.domain.SuspectTimeline
import org.springframework.data.annotation.Id

data class Suspect(
    val scenarioId: Long,
    @Id
    val suspectId: Long,
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
    val criticalClueIds: List<Long> // Maps from JSONB type
) {
    fun toDomain(
        criticalClues: List<Clue>,
        secrets: List<SuspectSecret>,
        timelines: List<SuspectTimeline>
    ): Suspect {
        return Suspect(
            suspectId = this.suspectId.toInt(),
            name = this.name,
            role = this.role,
            age = this.age,
            gender = this.gender,
            description = this.description,
            isCulprit = this.isCulprit,
            motive = this.motive,
            alibiSummary = this.alibiSummary,
            speechStyle = this.speechStyle,
            emotionalTendency = this.emotionalTendency,
            lyingPattern = this.lyingPattern,
            criticalClues = criticalClues,
            secrets = secrets,
            timeLines = timelines
        )
    }
}
