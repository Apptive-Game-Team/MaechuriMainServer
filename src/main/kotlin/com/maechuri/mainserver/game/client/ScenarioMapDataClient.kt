package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.*
import com.maechuri.mainserver.scenario.provider.ScenarioProvider
import com.maechuri.mainserver.scenario.provider.TodayScenarioIdProvider
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

private const val MAP_WIDTH = 50
private const val MAP_HEIGHT = 50

private const val TILE_EMPTY = 0
private const val TILE_WALL = 1
private const val TILE_FLOOR = 2

private const val OBJECT_ORDER = 3

@Primary
@Component
class ScenarioMapDataClient(
    private val scenarioProvider: ScenarioProvider,
    private val todayScenarioIdProvider: TodayScenarioIdProvider
) : MapDataClient {

    override suspend fun getMapData(scenarioId: Long): MapDataResponse {
        val scenario = scenarioProvider.findScenario(scenarioId)

        val assets = mutableListOf(
            AssetInfo(id = "1", imageUrl = "https://s3.yunseong.dev/maechuri/objects/wood_floor.json"),
            AssetInfo(id = "2", imageUrl = "https://s3.yunseong.dev/maechuri/objects/tile_floor.json")
        )

        val floorTiles = Array(MAP_HEIGHT) { IntArray(MAP_WIDTH) { TILE_EMPTY } }
        val wallTiles = Array(MAP_HEIGHT) { IntArray(MAP_WIDTH) { TILE_WALL } }

        // Populate layers based on Location entities
        scenario.locations.forEach { loc ->
            for (y in loc.y.toInt() until (loc.y + loc.height)) {
                for (x in loc.x.toInt() until (loc.x + loc.width)) {
                    if (y in 0 until MAP_HEIGHT && x in 0 until MAP_WIDTH) {
                        floorTiles[y][x] = TILE_FLOOR
                        wallTiles[y][x] = TILE_EMPTY
                    }
                }
            }
        }

        val floorLayer = Layer(
            orderInLayer = 1,
            name = "floor",
            type = listOf("Non-Interactable", "Passable"),
            tileMap = floorTiles.map { it.toList() }
        )

        val wallLayer = Layer(
            orderInLayer = 2,
            name = "wall",
            type = listOf("Non-Interactable", "Non-Passable", "Blocks-Vision"),
            tileMap = wallTiles.map { it.toList() }
        )

        val objects = mutableListOf<MapObject>()
        val occupiedCoordinates = mutableSetOf<Position>()

        fun placeObject(id: String, name: String, type: List<String>, pos: Position, assetUrl: String) {
            objects.add(MapObject(id = id, orderInLayer = OBJECT_ORDER, name = name, type = type, position = pos))
            assets.add(AssetInfo(id = id, imageUrl = assetUrl))
            occupiedCoordinates.add(pos)
        }

        scenario.suspects.forEach { suspect ->
            if (suspect.x != null && suspect.y != null) {
                placeObject(
                    id = "s:${suspect.suspectId}",
                    name = suspect.name,
                    type = listOf("Interactable", "Non-Passable"),
                    pos = Position(x = suspect.x.toInt(), y = suspect.y.toInt()),
                    assetUrl = suspect.assetsUrl ?: "https://s3.yunseong.dev/maechuri/objects/suspect.json"
                )
            }
        }

        scenario.clues.forEach { clue ->
            if (clue.x != null && clue.y != null) {
                placeObject(
                    id = "c:${clue.clueId}",
                    name = clue.name,
                    type = listOf("Interactable", "Non-Passable"),
                    pos = Position(x = clue.location.x + clue.x.toInt(), y = clue.location.y + clue.y.toInt()),
                    assetUrl = clue.assetsUrl ?: "https://s3.yunseong.dev/maechuri/objects/memo.json"
                )
            }
        }

        // Find available floor spots for the player
        val availableSpots = mutableListOf<Position>()
        floorTiles.forEachIndexed { y, row ->
            row.forEachIndexed { x, tileId ->
                if (tileId == TILE_FLOOR) {
                    val pos = Position(x, y)
                    if (pos !in occupiedCoordinates) {
                        availableSpots.add(pos)
                    }
                }
            }
        }

        // Place player, detective, investigator at random available spots
        val npcPlacements = listOf(
            Triple("p:1", "플레이어", "https://s3.yunseong.dev/maechuri/objects/player.json") to listOf("Interactable", "Passable"),
            Triple("d:1", "형사", "https://s3.yunseong.dev/maechuri/objects/detective.json") to listOf("Interactable", "Non-Passable"),
            Triple("i:1", "조사원", "https://s3.yunseong.dev/maechuri/objects/investigator.json") to listOf("Interactable", "Non-Passable"),
        )

        for ((info, type) in npcPlacements) {
            if (availableSpots.isEmpty()) break
            val spot = availableSpots.random()
            placeObject(id = info.first, name = info.second, type = type, pos = spot, assetUrl = info.third)
            availableSpots.remove(spot)
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
        return getMapData(todayScenarioIdProvider.getTodayScenarioId())
    }
}
