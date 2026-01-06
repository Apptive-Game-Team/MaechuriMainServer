package com.maechuri.mainserver.api.scenario.controller

import com.maechuri.mainserver.api.scenario.dto.InteractRequest
import com.maechuri.mainserver.api.scenario.dto.InteractResponse
import com.maechuri.mainserver.api.scenario.dto.MapDataResponse
import com.maechuri.mainserver.api.scenario.service.ScenarioService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/scenarios")
class ScenarioController(
    private val scenarioService: ScenarioService
) {

    @PostMapping("/{scenarioId}/interact/{objectId}")
    suspend fun interact(
        @PathVariable scenarioId: Long,
        @PathVariable objectId: Long,
        @RequestBody(required = false) request: InteractRequest?
    ): InteractResponse {
        val actualRequest = request ?: InteractRequest()
        return scenarioService.handleInteraction(scenarioId, objectId, actualRequest)
    }

    @GetMapping("/{scenarioId}/data/map")
    suspend fun getMapData(@PathVariable scenarioId: Long): MapDataResponse {
        return scenarioService.getMapData(scenarioId)
    }

    @GetMapping("/today/data/map")
    suspend fun getTodayMapData(): MapDataResponse {
        return scenarioService.getTodayMapData()
    }
}
