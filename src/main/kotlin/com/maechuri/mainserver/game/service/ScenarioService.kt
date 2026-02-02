package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.client.MapDataClient
import com.maechuri.mainserver.game.dto.MapDataResponse
import org.springframework.stereotype.Service

@Service
class ScenarioService(
    private val mapDataClient: MapDataClient
) {

    /**
     * Retrieves map data for a specific scenario.
     *
     * @param scenarioId Identifier of the scenario. Must be a positive value.
     * @return [com.maechuri.mainserver.game.dto.MapDataResponse] containing the scenario map data including layers, objects, and assets.
     * @throws IllegalArgumentException if scenarioId is not positive
     */
    suspend fun getMapData(scenarioId: Long): MapDataResponse {
        require(scenarioId > 0) { "scenarioId must be positive" }
        return mapDataClient.getMapData(scenarioId)
    }

    suspend fun getTodayMapData(): MapDataResponse {
        return mapDataClient.getTodayMapData()
    }
}