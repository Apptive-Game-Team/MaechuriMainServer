package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.game.dto.*
import com.maechuri.mainserver.scenario.provider.ScenarioProvider
import com.maechuri.mainserver.scenario.provider.TodayScenarioIdProvider
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter


private const val TILE_EMPTY = 0
private const val TILE_WALL = 1
private const val TILE_FLOOR = 2
private const val TILE_UPPER_WALL = 3
private const val TILE_LEFT_WALL = 4
private const val TILE_RIGHT_WALL = 5

private const val OBJECT_ORDER = 3

@Primary
@Component
class ScenarioMapDataClient(
    private val scenarioProvider: ScenarioProvider,
    private val todayScenarioIdProvider: TodayScenarioIdProvider
) : MapDataClient {

    override suspend fun getMapData(scenarioId: Long): MapDataResponse {
        val scenario = scenarioProvider.findScenario(scenarioId)

        val mapWidth = (scenario.locations.maxOfOrNull { it.x + it.width } ?: 49) + 1
        val mapHeight = (scenario.locations.maxOfOrNull { it.y + it.height } ?: 49) + 1

        val assets = mutableListOf(
            AssetInfo(id = "1", imageUrl = "https://s3.yunseong.dev/maechuri/objects/ceil.json"),
            AssetInfo(id = "2", imageUrl = "https://s3.yunseong.dev/maechuri/objects/floor.json"),
            AssetInfo(id = "3", imageUrl = "https://s3.yunseong.dev/maechuri/objects/upper_wall.json"),
            AssetInfo(id = "4", imageUrl = "https://s3.yunseong.dev/maechuri/objects/left_wall.json"),
            AssetInfo(id = "5", imageUrl = "https://s3.yunseong.dev/maechuri/objects/right_wall.json")
        )

        val floorTiles = Array(mapHeight) { IntArray(mapWidth) { TILE_EMPTY } }
        val wallTiles = Array(mapHeight) { IntArray(mapWidth) { TILE_WALL } }

        // Populate layers based on Location entities
        scenario.locations.forEach { loc ->
            for (y in loc.y.toInt() until (loc.y + loc.height)) {
                for (x in loc.x.toInt() until (loc.x + loc.width)) {
                    if (y in 0 until mapHeight && x in 0 until mapWidth) {
                        floorTiles[y][x] = TILE_FLOOR
                        wallTiles[y][x] = TILE_EMPTY
                    }
                }
            }
        }

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {

                if (wallTiles[y][x] == TILE_WALL && y + 1 < mapHeight && floorTiles[y + 1][x] == TILE_FLOOR) {
                    wallTiles[y][x] = TILE_UPPER_WALL
                }

                if (wallTiles[y][x] == TILE_WALL && x + 1 < mapWidth && floorTiles[y][x + 1] == TILE_FLOOR) {
                    wallTiles[y][x] = TILE_RIGHT_WALL
                }
                if (wallTiles[y][x] == TILE_WALL && x - 1 >= 0 && floorTiles[y][x - 1] == TILE_FLOOR) {
                    wallTiles[y][x] = TILE_LEFT_WALL
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

        val locationMapById = scenario.locations.associateBy { it.locationId }

        scenario.suspects.forEach { suspect ->
            if (suspect.x != null && suspect.y != null) {
                val loc = suspect.locationId?.let { locationMapById[it] }
                val absX = if (loc != null) loc.x + suspect.x.toInt() else suspect.x.toInt()
                val absY = if (loc != null) loc.y + suspect.y.toInt() else suspect.y.toInt()
                placeObject(
                    id = "s:${suspect.suspectId}",
                    name = suspect.name,
                    type = listOf("Interactable", "Non-Passable"),
                    pos = Position(x = absX, y = absY),
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
