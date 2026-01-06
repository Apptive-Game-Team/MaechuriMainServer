package com.maechuri.mainserver.game.scenario.client

import com.maechuri.mainserver.game.scenario.dto.*
import org.springframework.stereotype.Component

@Component
class MockMapDataClient : MapDataClient {
    
    override fun getMapData(scenarioId: Long): MapDataResponse {
        return createMockMapData(scenarioId)
    }
    
    override fun getTodayMapData(): MapDataResponse {
        // In real implementation, this would fetch today's scenario from database
        return createMockMapData(1L)
    }
    
    private fun createMockMapData(scenarioId: Long): MapDataResponse {
        return MapDataResponse(
            createdDate = "2025-12-22",
            scenarioId = scenarioId,
            scenarioName = "요리사 3인방의 사건 현장",
            map = MapData(
                layers = listOf(
                    Layer(
                        orderInLayer = 1,
                        name = "floor",
                        type = listOf("Non-Interactable", "Passable"),
                        tileMap = listOf(
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2),
                            listOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
                        )
                    ),
                    Layer(
                        orderInLayer = 2,
                        name = "wall",
                        type = listOf("Non-Interactable", "Non-Passable", "Blocks-Vision"),
                        tileMap = listOf(
                            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
                            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
                        )
                    )
                ),
                objects = listOf(
                    MapObject(
                        id = 100,
                        orderInLayer = 3,
                        name = "요리사 1",
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = 2, y = 3)
                    ),
                    MapObject(
                        id = 101,
                        orderInLayer = 3,
                        name = "요리사 2",
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = 11, y = 3)
                    ),
                    MapObject(
                        id = 102,
                        orderInLayer = 3,
                        name = "요리사 3",
                        type = listOf("Interactable", "Non-Passable"),
                        position = Position(x = 15, y = 8)
                    )
                ),
                assets = listOf(
                    AssetInfo(
                        id = 1,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/wood_floor.json"
                    ),
                    AssetInfo(
                        id = 2,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/tile_floor.json"
                    ),
                    AssetInfo(
                        id = 100,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/cook_1.json"
                    ),
                    AssetInfo(
                        id = 999,
                        imageUrl = "https://s3.yunseong.dev/maechuri/objects/player.json"
                    )
                )
            )
        )
    }
}
