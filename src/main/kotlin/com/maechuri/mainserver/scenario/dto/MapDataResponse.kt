package com.maechuri.mainserver.scenario.dto

data class MapDataResponse(
    val createdDate: String,
    val scenarioId: Long,
    val scenarioName: String,
    val map: MapData
)

data class MapData(
    val layers: List<Layer>,
    val objects: List<MapObject>,
    val assets: List<AssetInfo>
)

data class Layer(
    val orderInLayer: Int,
    val name: String,
    val type: List<String>,
    val tileMap: List<List<Int>>
)

data class MapObject(
    val id: Long,
    val orderInLayer: Int,
    val name: String,
    val type: List<String>,
    val position: Position
)

data class Position(
    val x: Int,
    val y: Int
)

data class AssetInfo(
    val id: Long,
    val imageUrl: String
)
