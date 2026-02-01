package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.MapDataResponse

interface MapDataClient {
    suspend fun getMapData(scenarioId: Long): MapDataResponse
    suspend fun getTodayMapData(): MapDataResponse
}
