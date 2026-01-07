package com.maechuri.mainserver.scenario.controller

import com.maechuri.mainserver.scenario.dto.InteractRequest
import com.maechuri.mainserver.scenario.dto.InteractResponse
import com.maechuri.mainserver.scenario.dto.MapDataResponse
import com.maechuri.mainserver.scenario.service.ScenarioService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/scenarios")
class ScenarioController(
    private val scenarioService: ScenarioService
) {

    /**
     * Handles user interaction with a scenario object.
     * 
     * Path variables are validated at the service layer to ensure positive values.
     * 
     * @param scenarioId The scenario ID (validated: must be positive)
     * @param objectId The object ID (validated: must be positive)
     * @param request Optional interaction request with message and history
     * @return Interaction response with type, message, and optional history
     * @throws IllegalArgumentException if scenarioId or objectId are not positive
     */
    @PostMapping("/{scenarioId}/interact/{objectId}")
    suspend fun interact(
        @PathVariable scenarioId: Long,
        @PathVariable objectId: Long,
        @RequestBody(required = false) request: InteractRequest?
    ): InteractResponse {
        val actualRequest = request ?: InteractRequest()
        return scenarioService.handleInteraction(scenarioId, objectId, actualRequest)
    }

    /**
     * Retrieves map data for a specific scenario.
     * 
     * Path variable is validated at the service layer to ensure positive value.
     * 
     * @param scenarioId The scenario ID (validated: must be positive)
     * @return Map data response with layers, objects, and assets
     * @throws IllegalArgumentException if scenarioId is not positive
     */
    @GetMapping("/{scenarioId}/data/map")
    suspend fun getMapData(@PathVariable scenarioId: Long): MapDataResponse {
        return scenarioService.getMapData(scenarioId)
    }

    /**
     * Retrieves today's scenario map data.
     * 
     * @return Map data response for today's scenario
     */
    @GetMapping("/today/data/map")
    suspend fun getTodayMapData(): MapDataResponse {
        return scenarioService.getTodayMapData()
    }
}
