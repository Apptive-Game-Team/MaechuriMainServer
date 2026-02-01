package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.*
import com.maechuri.mainserver.scenario.provider.ScenarioProvider
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Primary
@Component
class ScenarioMapDataClient(
    private val scenarioProvider: ScenarioProvider
) : MapDataClient {

    override suspend fun getMapData(scenarioId: Long): MapDataResponse {
        val scenario = scenarioProvider.findScenario(scenarioId)

        val assets = listOf(
            AssetInfo(id = "wall", imageUrl = "https://s3.yunseong.dev/maechuri/objects/wood_floor.json"),
            AssetInfo(id = "floor", imageUrl = "https://s3.yunseong.dev/maechuri/objects/tile_floor.json"),
            AssetInfo(id = "clue", imageUrl = "https://s3.yunseong.dev/maechuri/objects/cook_1.json"),
            AssetInfo(id = "suspect", imageUrl = "https://s3.yunseong.dev/maechuri/objects/cook_2.json"),
            AssetInfo(id = "player", imageUrl = "https://s3.yunseong.dev/maechuri/objects/player.json")
        )

        // For simplicity, we'll assume a fixed-size map for now.
        // A more robust implementation would calculate the bounding box of all scenario maps.
        val mapWidth = 50
        val mapHeight = 50

        val floorLayer = Layer(
            orderInLayer = 1,
            name = "floor",
            type = listOf("Non-Interactable", "Passable"),
            tileMap = List(mapHeight) { MutableList(mapWidth) { 0 } }
        )

        val wallLayer = Layer(
            orderInLayer = 2,
            name = "wall",
            type = listOf("Non-Interactable", "Non-Passable", "Blocks-Vision"),
            tileMap = List(mapHeight) { MutableList(mapWidth) { 0 } }
        )

        // Populate layers based on ScenarioMap entities
        scenario.maps.forEach { scenarioMap ->
            val layerToModify = if (scenarioMap.type == "room") floorLayer.tileMap else wallLayer.tileMap
            val tileId = if (scenarioMap.type == "room") 2 else 1 // floor: 2, wall: 1 (as in mock)
            for (y in scenarioMap.y until (scenarioMap.y + scenarioMap.height)) {
                for (x in scenarioMap.x until (scenarioMap.x + scenarioMap.width)) {
                    if (y < mapHeight && x < mapWidth) {
                        (layerToModify[y] as MutableList<Int>)[x] = tileId
                    }
                }
            }
        }

        val objects = mutableListOf<MapObject>()
        scenario.suspects.forEach { suspect ->
            if (suspect.x != null && suspect.y != null) {
                objects.add(
                    MapObject(
                        id = "s:${suspect.suspectId}",
                        orderInLayer = 3,
                        name = suspect.name,
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = suspect.x.toInt(), y = suspect.y.toInt())
                    )
                )
            }
        }

        scenario.clues.forEach { clue ->
            if (clue.x != null && clue.y != null) {
                objects.add(
                    MapObject(
                        id = "c:${clue.clueId}",
                        orderInLayer = 3,
                        name = clue.name,
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = clue.x.toInt(), y = clue.y.toInt())
                    )
                )
            }
        }

        return MapDataResponse(
            createdDate = scenario.createdAt.format(DateTimeFormatter.ISO_DATE),
            scenarioId = scenario.scenarioId ?: -1,
            scenarioName = scenario.theme,
            map = MapData(
                layers = listOf(floorLayer, wallLayer),
                objects = objects,
                assets = assets
            )
        )
    }

    override suspend fun getTodayMapData(): MapDataResponse {
        // This should be implemented to fetch the "scenario of the day"
        // For now, we can delegate to getMapData with a fixed ID, e.g., 1L
        return getMapData(1L)
    }
}
