package com.maechuri.mainserver.scenario.client

import com.maechuri.mainserver.scenario.dto.MapDataResponse

interface MapDataClient {
    fun getMapData(scenarioId: Long): MapDataResponse
    fun getTodayMapData(): MapDataResponse
}
