package com.maechuri.mainserver.game.scenario.client

import com.maechuri.mainserver.game.scenario.dto.MapDataResponse

interface MapDataClient {
    fun getMapData(scenarioId: Long): MapDataResponse
    fun getTodayMapData(): MapDataResponse
}
