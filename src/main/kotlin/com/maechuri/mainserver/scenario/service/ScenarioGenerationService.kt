package com.maechuri.mainserver.scenario.service

import com.maechuri.mainserver.scenario.client.AiClient
import com.maechuri.mainserver.scenario.dto.ScenarioCreateRequest
import com.maechuri.mainserver.scenario.dto.ScenarioTaskListResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ScenarioGenerationService(
    @Qualifier("scenario_ai_client") private val aiClient: AiClient
) {

    suspend fun startGeneration(theme: String): com.maechuri.mainserver.scenario.dto.ScenarioCreateResponse {
        val key = UUID.randomUUID().toString()
        val request = ScenarioCreateRequest(key, theme)
        return aiClient.createScenario(request)
    }

    suspend fun getGenerationTasks(): ScenarioTaskListResponse {
        return aiClient.getScenarioCreateTasks()
    }
}
