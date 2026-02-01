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

        val assets = mutableListOf(
            AssetInfo(id = "1", imageUrl = "https://s3.yunseong.dev/maechuri/objects/wood_floor.json"),
            AssetInfo(id = "2", imageUrl = "https://s3.yunseong.dev/maechuri/objects/tile_floor.json")
        )

        // For simplicity, we'll assume a fixed-size map for now.
        // A more robust implementation would calculate the bounding box of all scenario maps.
        val mapWidth = 50
        val mapHeight = 50

        val floorLayer = Layer(
            orderInLayer = 1,
            name = "floor",
            type = listOf("Non-Interactable", "Passable"),
            tileMap = List(mapHeight) { MutableList(mapWidth) { 0 } } // Initialize with empty tiles
        )

        val wallLayer = Layer(
            orderInLayer = 2,
            name = "wall",
            type = listOf("Non-Interactable", "Non-Passable", "Blocks-Vision"),
            tileMap = List(mapHeight) { MutableList(mapWidth) { 1 } } // Initialize with solid walls
        )

        // Populate layers based on ScenarioMap entities
        scenario.maps.forEach { scenarioMap ->
            for (y in scenarioMap.y until (scenarioMap.y + scenarioMap.height)) {
                for (x in scenarioMap.x until (scenarioMap.x + scenarioMap.width)) {
                    if (y >= 0 && y < mapHeight && x >= 0 && x < mapWidth) {
                        // Fill floor layer with floor tiles in room/corridor areas
                        (floorLayer.tileMap[y.toInt()] as MutableList<Int>)[x.toInt()] = 2 // floor tile

                        // Clear wall layer in room/corridor areas
                        (wallLayer.tileMap[y.toInt()] as MutableList<Int>)[x.toInt()] = 0 // empty space
                    }
                }
            }
        }

        val objects = mutableListOf<MapObject>()
        val occupiedCoordinates = mutableSetOf<Position>()

        scenario.suspects.forEach { suspect ->
            if (suspect.x != null && suspect.y != null) {
                val objectId = "s:${suspect.suspectId}"
                val pos = Position(x = suspect.x.toInt(), y = suspect.y.toInt())
                objects.add(
                    MapObject(
                        id = objectId,
                        orderInLayer = 3,
                        name = suspect.name,
                        type = listOf("Interactable", "Non-Passable"),
                        position = pos
                    )
                )
                assets.add(AssetInfo(id = objectId, imageUrl = "https://s3.yunseong.dev/maechuri/objects/cook_2.json"))
                occupiedCoordinates.add(pos)
            }
        }

        scenario.clues.forEach { clue ->
            if (clue.x != null && clue.y != null) {
                val objectId = "c:${clue.clueId}"
                val pos = Position(x = clue.x.toInt(), y = clue.y.toInt())
                objects.add(
                    MapObject(
                        id = objectId,
                        orderInLayer = 3,
                        name = clue.name,
                        type = listOf("Interactable", "Non-Passable"),
                        position = pos
                    )
                )
                assets.add(AssetInfo(id = objectId, imageUrl = "https://s3.yunseong.dev/maechuri/objects/cook_1.json"))
                occupiedCoordinates.add(pos)
            }
        }

        // Find available floor spots for the player
        val availableSpots = mutableListOf<Position>()
        floorLayer.tileMap.forEachIndexed { y, row ->
            row.forEachIndexed { x, tileId ->
                if (tileId == 2) { // '2' is the floor tile
                    val pos = Position(x, y)
                    if (!occupiedCoordinates.contains(pos)) {
                        availableSpots.add(pos)
                    }
                }
            }
        }

        // Add player at a random available spot
        if (availableSpots.isNotEmpty()) {
            val randomSpot = availableSpots.random()
            val playerId = "p:1"
            objects.add(
                MapObject(
                    id = playerId,
                    orderInLayer = 3,
                    name = "플레이어",
                    type = listOf("Interactable", "Passable"), // Player is usually passable
                    position = randomSpot
                )
            )
            assets.add(AssetInfo(id = playerId, imageUrl = "https://s3.yunseong.dev/maechuri/objects/player.json"))
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
