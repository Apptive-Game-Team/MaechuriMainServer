package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.scenario.domain.Clue
import com.maechuri.mainserver.scenario.domain.Location
import com.maechuri.mainserver.scenario.domain.Scenario
import com.maechuri.mainserver.scenario.domain.Suspect
import com.maechuri.mainserver.scenario.entity.Difficulty
import com.maechuri.mainserver.scenario.provider.ScenarioProvider
import com.maechuri.mainserver.scenario.provider.TodayScenarioIdProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyLong
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScenarioMapDataClientTest {

    private val scenarioProvider: ScenarioProvider = mock()
    private val todayScenarioIdProvider: TodayScenarioIdProvider = mock()
    private val scenarioMapDataClient = ScenarioMapDataClient(scenarioProvider, todayScenarioIdProvider)

    // Per-location tile IDs for the first (index=0) location in these tests
    // (LOCATION_TILE_ID_BASE=10, index*2=0 → floor=10, wall=11)
    private val expectedFloorTileId = 10
    private val expectedWallTileId = 11

    @Test
    fun `getMapData returns correctly mapped data`() = runBlocking {
        // Given
        val scenarioId = 1L
        val mockLocation = Location(
            locationId = 1L, name = "Test Room", type = "room",
            x = 2, y = 2, width = 10, height = 10,
            canSee = emptyList(), cannotSee = emptyList(), accessRequires = null
        )
        val mockScenario = Scenario(
            scenarioId = scenarioId,
            difficulty = Difficulty.easy,
            theme = "Test Theme",
            tone = "Test Tone",
            language = "ko",
            incidentType = "Test Incident",
            incidentSummary = "Summary",
            incidentTimeStart = LocalTime.NOON,
            incidentTimeEnd = LocalTime.MIDNIGHT,
            incidentLocationId = 1L,
            primaryObject = "Test Object",
            crimeTimeStart = LocalTime.NOON,
            crimeTimeEnd = LocalTime.MIDNIGHT,
            crimeLocationId = 1L,
            crimeMethod = "Test Method",
            noSupernatural = true,
            noTimeTravel = true,
            createdAt = LocalDateTime.now(),
            locations = listOf(mockLocation),
            clues = listOf(
                Clue(
                    clueId = 1L, name = "Test Clue", location = mockLocation,
                    description = "A clue", logicExplanation = "Logic",
                    decodedAnswer = null, isRedHerring = false,
                    relatedSuspectIds = emptyList(), x = 5, y = 5
                )
            ),
            suspects = listOf(
                Suspect(
                    suspectId = 101L, name = "Test Suspect", role = "Witness", age = 30,
                    gender = "Male", description = "A suspect", isCulprit = false, motive = null,
                    alibiSummary = "Alibi", speechStyle = "Polite", emotionalTendency = "Calm",
                    lyingPattern = "None", locationId = null, x = 10, y = 10
                )
            ),
        )

        Mockito.`when`(scenarioProvider.findScenario(anyLong())).thenAnswer { mockScenario }

        // When
        val response = scenarioMapDataClient.getMapData(scenarioId)

        // Then
        assertNotNull(response)
        assertEquals(scenarioId, response.scenarioId)
        assertEquals("Test Theme", response.scenarioName)

        // Assets: 1 default wall + 2 per-location (floor+wall) + 1 suspect + 1 clue + up to 3 NPCs
        assertTrue(response.map.assets.size >= 5, "Expected at least 5 assets")
        assertNotNull(response.map.assets.find { it.id == "s:101" })
        assertNotNull(response.map.assets.find { it.id == "c:1" })
        assertNotNull(response.map.assets.find { it.id == "p:1" })

        // Per-location floor and wall assets must be present
        assertNotNull(
            response.map.assets.find { it.id == expectedFloorTileId.toString() },
            "Expected a floor tile asset for the location"
        )
        assertNotNull(
            response.map.assets.find { it.id == expectedWallTileId.toString() },
            "Expected a wall tile asset for the location"
        )

        assertEquals(2, response.map.layers.size) // floor and wall layers

        val floorLayer = response.map.layers.find { it.name == "floor" }
        val wallLayer = response.map.layers.find { it.name == "wall" }
        assertNotNull(floorLayer)
        assertNotNull(wallLayer)

        // Check room area (x=2 to 11, y=2 to 11) — must use location-specific floor tile ID
        for (y in 2 until 12) {
            for (x in 2 until 12) {
                assertEquals(
                    expectedFloorTileId, floorLayer.tileMap[y][x],
                    "Floor tile should be $expectedFloorTileId inside room at ($x, $y)"
                )
                assertEquals(
                    0, wallLayer.tileMap[y][x],
                    "Wall tile should be 0 (empty) inside room at ($x, $y)"
                )
            }
        }

        // Far-outside tiles (e.g., (0,0)) not adjacent to any location → default wall
        assertEquals(0, floorLayer.tileMap[0][0], "Floor tile should be 0 outside room at (0,0)")
        assertEquals(1, wallLayer.tileMap[0][0], "Wall tile should be 1 (default) far outside room at (0,0)")

        // Wall tile just above the room (y=1, x inside room) should use location wall image
        assertEquals(
            expectedWallTileId, wallLayer.tileMap[1][5],
            "Wall tile just above the room should use the location's wall tile ID"
        )

        // Objects: suspect + clue + at least 1 NPC (player)
        assertTrue(response.map.objects.size >= 3, "Expected at least 3 objects")
        val suspectObject = response.map.objects.find { it.id == "s:101" }
        assertNotNull(suspectObject)
        assertEquals("Test Suspect", suspectObject.name)
        assertEquals(10, suspectObject.position.x)
        assertEquals(10, suspectObject.position.y)

        val clueObject = response.map.objects.find { it.id == "c:1" }
        assertNotNull(clueObject)
        assertEquals("Test Clue", clueObject.name)
        // Clue position is location-relative: location(2,2) + clue(5,5) = (7,7)
        assertEquals(7, clueObject.position.x)
        assertEquals(7, clueObject.position.y)

        val playerObject = response.map.objects.find { it.id == "p:1" }
        assertNotNull(playerObject)
        assertEquals("플레이어", playerObject.name)
        assertTrue(playerObject.position.x >= 0 && playerObject.position.x < 50, "Player X position out of bounds")
        assertTrue(playerObject.position.y >= 0 && playerObject.position.y < 50, "Player Y position out of bounds")
    }

    @Test
    fun `getTodayMapData delegates to getMapData with id from todayScenarioIdProvider`() = runBlocking {
        // Given
        val todayScenarioId = 7L
        val mockLocation = Location(
            locationId = 1L, name = "Today Room", type = "room",
            x = 0, y = 0, width = 5, height = 5,
            canSee = emptyList(), cannotSee = emptyList(), accessRequires = null
        )
        val mockScenario = Scenario(
            scenarioId = todayScenarioId,
            difficulty = Difficulty.easy,
            theme = "Today Theme",
            tone = "Tone",
            language = "ko",
            incidentType = "Incident",
            incidentSummary = "Summary",
            incidentTimeStart = LocalTime.NOON,
            incidentTimeEnd = LocalTime.MIDNIGHT,
            incidentLocationId = 1L,
            primaryObject = "Object",
            crimeTimeStart = LocalTime.NOON,
            crimeTimeEnd = LocalTime.MIDNIGHT,
            crimeLocationId = 1L,
            crimeMethod = "Method",
            noSupernatural = true,
            noTimeTravel = true,
            createdAt = LocalDateTime.now(),
            locations = listOf(mockLocation),
            clues = emptyList(),
            suspects = emptyList(),
        )

        whenever(todayScenarioIdProvider.getTodayScenarioId()).thenReturn(todayScenarioId)
        whenever(scenarioProvider.findScenario(anyLong())).thenAnswer { mockScenario }

        // When
        val response = scenarioMapDataClient.getTodayMapData()

        // Then
        assertNotNull(response)
        assertEquals(todayScenarioId, response.scenarioId)
        assertEquals("Today Theme", response.scenarioName)
    }
}
