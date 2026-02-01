package com.maechuri.mainserver.game.client

import com.maechuri.mainserver.scenario.domain.Clue
import com.maechuri.mainserver.scenario.domain.Location
import com.maechuri.mainserver.scenario.domain.Scenario
import com.maechuri.mainserver.scenario.domain.ScenarioMap
import com.maechuri.mainserver.scenario.domain.Suspect
import com.maechuri.mainserver.scenario.entity.Difficulty
import com.maechuri.mainserver.scenario.provider.ScenarioProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyLong
import org.mockito.kotlin.mock
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScenarioMapDataClientTest {

    private val scenarioProvider: ScenarioProvider = mock() // Using mockito-kotlin's mock for now
    private val scenarioMapDataClient = ScenarioMapDataClient(scenarioProvider)

    @Test
    fun `getMapData returns correctly mapped data`() = runBlocking {
        // Given
        val scenarioId = 1L
        val mockLocation = Location(locationId = 1L, name = "Test Room", canSee = emptyList(), cannotSee = emptyList(), accessRequires = null)
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
                Clue(clueId = 1L, name = "Test Clue", location = mockLocation, description = "A clue", logicExplanation = "Logic", decodedAnswer = null, isRedHerring = false, relatedFactIds = emptyList(), x = 5, y = 5)
            ),
            suspects = listOf(
                Suspect(suspectId = 101L, name = "Test Suspect", role = "Witness", age = 30, gender = "Male", description = "A suspect", isCulprit = false, motive = null, alibiSummary = "Alibi", speechStyle = "Polite", emotionalTendency = "Calm", lyingPattern = "None", criticalClueIds = emptyList(), x = 10, y = 10)
            ),
            maps = listOf(
                ScenarioMap(mapId = 1L, type = "room", name = "Test Room", x = 2, y = 2, width = 10, height = 10, extraData = emptyMap())
            )
        )

        Mockito.`when`(scenarioProvider.findScenario(anyLong())).thenAnswer {
            mockScenario
        }

        // When
        val response = scenarioMapDataClient.getMapData(scenarioId)

        // Then
        assertNotNull(response)
        assertEquals(scenarioId, response.scenarioId)
        assertEquals("Test Theme", response.scenarioName)

        assertEquals(5, response.map.assets.size)
        assertNotNull(response.map.assets.find { it.id == "s:101" })
        assertEquals("https://s3.yunseong.dev/maechuri/objects/cook_2.json", response.map.assets.find { it.id == "s:101" }?.imageUrl)
        assertNotNull(response.map.assets.find { it.id == "c:1" })
        assertEquals("https://s3.yunseong.dev/maechuri/objects/cook_1.json", response.map.assets.find { it.id == "c:1" }?.imageUrl)
        assertNotNull(response.map.assets.find { it.id == "p:1" })
        assertEquals("https://s3.yunseong.dev/maechuri/objects/player.json", response.map.assets.find { it.id == "p:1" }?.imageUrl)

        assertEquals(2, response.map.layers.size) // floor and wall layers

        val floorLayer = response.map.layers.find { it.name == "floor" }
        val wallLayer = response.map.layers.find { it.name == "wall" }
        assertNotNull(floorLayer)
        assertNotNull(wallLayer)

        // Check room area (x=2 to 11, y=2 to 11)
        for (y in 2 until 12) {
            for (x in 2 until 12) {
                assertEquals(2, floorLayer.tileMap[y][x], "Floor tile should be 2 inside room at ($x, $y)")
                assertEquals(0, wallLayer.tileMap[y][x], "Wall tile should be 0 inside room at ($x, $y)")
            }
        }

        // Check outside room area (e.g., (0,0) or (1,1))
        assertEquals(0, floorLayer.tileMap[0][0], "Floor tile should be 0 outside room at (0,0)")
        assertEquals(1, wallLayer.tileMap[0][0], "Wall tile should be 1 outside room at (0,0)")

        assertEquals(3, response.map.objects.size) // Suspect, Clue, Player
        val suspectObject = response.map.objects.find { it.id == "s:101" }
        assertNotNull(suspectObject)
        assertEquals("Test Suspect", suspectObject.name)
        assertEquals(10, suspectObject.position.x)
        assertEquals(10, suspectObject.position.y)

        val clueObject = response.map.objects.find { it.id == "c:1" }
        assertNotNull(clueObject)
        assertEquals("Test Clue", clueObject.name)
        assertEquals(5, clueObject.position.x)
        assertEquals(5, clueObject.position.y)

        val playerObject = response.map.objects.find { it.id == "p:1" }
        assertNotNull(playerObject)
        assertEquals("플레이어", playerObject.name)
        assertTrue(playerObject.position.x >= 0 && playerObject.position.x < 50, "Player X position out of bounds")
        assertTrue(playerObject.position.y >= 0 && playerObject.position.y < 50, "Player Y position out of bounds")
    }
}
