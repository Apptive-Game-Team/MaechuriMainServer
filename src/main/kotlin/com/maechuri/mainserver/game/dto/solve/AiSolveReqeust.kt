package com.maechuri.mainserver.game.dto.solve

data class AiSolveReqeust(
    val scenario_id: Long,
    val culprit_id: List<Long>,
    val user_solution: String
) {
    companion object {
        fun from(scenarioId: Long, clientSolveRequest: ClientSolveRequest): AiSolveReqeust {
            return AiSolveReqeust(
                scenario_id = scenarioId,
                culprit_id = clientSolveRequest.suspectIds.map { it.split(":")[1].toLong() }.toList(),
                user_solution = clientSolveRequest.message
            )
        }
    }
}