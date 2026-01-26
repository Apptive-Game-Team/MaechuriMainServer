package com.maechuri.mainserver.scenario.client

import com.maechuri.mainserver.scenario.dto.ScenarioCreateRequest
import com.maechuri.mainserver.scenario.dto.ScenarioCreateResponse
import com.maechuri.mainserver.scenario.dto.ScenarioStatusResponse
import com.maechuri.mainserver.scenario.dto.ScenarioTaskListResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component("scenario_ai_client")
class AiClient(
    val webClient: WebClient
) {

    suspend fun createScenario(request: ScenarioCreateRequest): ScenarioCreateResponse {
        return webClient.post()
            .uri("/api/scenarios/daily")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .awaitBody<ScenarioCreateResponse>()
    }

    suspend fun getScenarioCreateTasks(): ScenarioTaskListResponse {
        return webClient.get()
            .uri("/api/scenarios/tasks")
            .retrieve()
            .awaitBody<ScenarioTaskListResponse>()
    }

    suspend fun getScenarioCreateTask(key: String): ScenarioStatusResponse {
        return webClient.get()
            .uri("/api/scenarios/${key}")
            .retrieve()
            .awaitBody()
    }
}