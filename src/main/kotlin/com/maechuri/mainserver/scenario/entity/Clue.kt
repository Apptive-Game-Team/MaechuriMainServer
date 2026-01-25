package com.maechuri.mainserver.scenario.entity

import com.maechuri.mainserver.scenario.domain.Clue
import com.maechuri.mainserver.scenario.domain.Location
import com.maechuri.mainserver.scenario.domain.Suspect
import org.springframework.data.annotation.Id

data class Clue(
    val scenarioId: Long,
    @Id
    val clueId: Long,
    val name: String,
    val locationId: Long,
    val description: String,
    val relatedSuspectIds: List<Long>, // Maps from JSONB type
    val logicExplanation: String,
    val isRedHerring: Boolean
) {
    fun toDomain(suspects: List<Suspect>, location: Location): Clue {
        return Clue(
            clueId = clueId.toInt(),
            name = name,
            location = location,
            description = description,
            relatedSuspects = suspects,
            logicExplanation = logicExplanation,
            isRedHerring = isRedHerring
        )
    }
}
