package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.MapDataResponse

interface MapDataClient {
    fun getMapData(scenarioId: Long): MapDataResponse
    fun getTodayMapData(): MapDataResponse
}
