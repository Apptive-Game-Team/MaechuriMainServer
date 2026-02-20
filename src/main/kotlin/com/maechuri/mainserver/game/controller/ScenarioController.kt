package com.maechuri.mainserver.game.controller

import com.maechuri.mainserver.game.dto.InteractRequest
import com.maechuri.mainserver.game.dto.InteractResponse
import com.maechuri.mainserver.game.dto.MapDataResponse
import com.maechuri.mainserver.game.dto.RecordResponse
import com.maechuri.mainserver.game.dto.RecordsListResponse
import com.maechuri.mainserver.game.dto.solve.ClientSolveRequest
import com.maechuri.mainserver.game.dto.solve.ClientSolveResponse
import com.maechuri.mainserver.game.service.InteractionService
import com.maechuri.mainserver.game.service.RecordService
import com.maechuri.mainserver.game.service.ScenarioService
import com.maechuri.mainserver.global.config.GameSessionFilter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/scenarios")
class ScenarioController(
    private val scenarioService: ScenarioService,
    private val interactionService: InteractionService,
    private val recordService: RecordService
) {

    @PostMapping("/{scenarioId}/solve")
    suspend fun solve(
        @PathVariable scenarioId: Long,
        @RequestBody request: ClientSolveRequest
    ): ClientSolveResponse {
        return interactionService.solve(scenarioId, request)
    }

    /**
     * Handles user interaction with a scenario object.
     *
     * Path variables are validated at the service layer to ensure positive values.
     * Game session ID is automatically extracted from cookies.
     *
     * @param scenarioId The scenario ID (validated: must be positive)
     * @param objectId The object ID (validated: must be positive)
     * @param request Optional interaction request with message and history
     * @param exchange ServerWebExchange to access game session ID from attributes
     * @return Interaction response with type, message, and optional history
     * @throws IllegalArgumentException if scenarioId or objectId are not positive
     */
    @PostMapping("/{scenarioId}/interact/{objectId}")
    suspend fun interact(
        @PathVariable scenarioId: Long,
        @PathVariable objectId: String,
        @RequestBody(required = false) request: InteractRequest?,
        exchange: ServerWebExchange
    ): InteractResponse {
        val actualRequest = request ?: InteractRequest()
        val gameSessionId = exchange.getAttribute<String>(GameSessionFilter.GAME_SESSION_ATTRIBUTE_NAME)
            ?: throw IllegalStateException("Game session ID not found in exchange attributes. Ensure GameSessionFilter is properly configured.")
        return interactionService.handleInteraction(scenarioId, objectId, actualRequest, gameSessionId)
    }

    /**
     * Retrieves all interacted records for a game session.
     * Game session ID is automatically extracted from cookies.
     *
     * @param scenarioId The scenario ID (validated: must be positive)
     * @param exchange ServerWebExchange to access game session ID from attributes
     * @return List of all interacted records with name and content
     * @throws IllegalArgumentException if scenarioId is not positive or gameSessionId is blank
     */
    @GetMapping("/{scenarioId}/records")
    suspend fun getAllRecords(
        @PathVariable scenarioId: Long,
        exchange: ServerWebExchange
    ): RecordsListResponse {
        val gameSessionId = exchange.getAttribute<String>(GameSessionFilter.GAME_SESSION_ATTRIBUTE_NAME)
            ?: throw IllegalStateException("Game session ID not found in exchange attributes. Ensure GameSessionFilter is properly configured.")
        return recordService.getAllInteractedRecords(scenarioId, gameSessionId)
    }

    /**
     * Retrieves a specific record (fact, suspect, or clue) by its composite ID.
     *
     * @param scenarioId The scenario ID (validated: must be positive)
     * @param recordId The record ID in format "tag:id" (e.g., "f:1" for fact, "s:101" for suspect, "c:1" for clue)
     * @return Record response with name and content
     * @throws IllegalArgumentException if recordId format is invalid or record not found
     */
    @GetMapping("/{scenarioId}/records/{recordId}")
    suspend fun getRecord(
        @PathVariable scenarioId: Long,
        @PathVariable recordId: String
    ): RecordResponse {
        return recordService.getRecord(scenarioId, recordId)
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