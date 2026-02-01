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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ScenarioMapDataClientTest {

    private val scenarioProvider: ScenarioProvider = mock()
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

        whenever(scenarioProvider.findScenario(scenarioId)).thenReturn(mockScenario)

        // When
        val response = scenarioMapDataClient.getMapData(scenarioId)

        // Then
        assertNotNull(response)
        assertEquals(scenarioId, response.scenarioId)
        assertEquals("Test Theme", response.scenarioName)

        assertEquals(5, response.map.assets.size)
        assertEquals("wall", response.map.assets[0].id)
        assertEquals("floor", response.map.assets[1].id)

        assertEquals(2, response.map.layers.size)
        // Check if the room is drawn on the floor layer
        assertEquals(2, response.map.layers[0].tileMap[2][2])

        assertEquals(2, response.map.objects.size)
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
    }
}
