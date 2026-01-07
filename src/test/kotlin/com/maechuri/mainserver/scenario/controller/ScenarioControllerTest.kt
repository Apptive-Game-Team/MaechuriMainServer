package com.maechuri.mainserver.scenario.controller

import com.maechuri.mainserver.MainServerApplication
import com.maechuri.mainserver.scenario.dto.InteractRequest
import com.maechuri.mainserver.scenario.service.ScenarioService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(classes = [MainServerApplication::class])
class ScenarioServiceTest {

    @Autowired
    private lateinit var scenarioService: ScenarioService

    @Test
    fun `interact with simple type returns simple response`() = runBlocking {
        val response = scenarioService.handleInteraction(1, 101, InteractRequest())

        assertNotNull(response)
        assertEquals("simple", response.type)
        assertEquals("안녕 난 요리사 이선민이야", response.message)
        assertEquals("이선민", response.name)
    }

    @Test
    fun `interact with two-way type without message returns greeting`() = runBlocking {
        val response = scenarioService.handleInteraction(1, 100, InteractRequest())

        assertNotNull(response)
        assertEquals("two-way", response.type)
        assertNotNull(response.message)
        assertNotNull(response.history)
    }

    @Test
    fun `interact with two-way type with message returns response and history`() = runBlocking {
        val request = InteractRequest(message = "안녕하세요")
        val response = scenarioService.handleInteraction(1, 100, request)

        assertNotNull(response)
        assertEquals("two-way", response.type)
        assertNotNull(response.message)
        assertNotNull(response.history)
    }

    @Test
    fun `getMapData returns map data for scenario`() = runBlocking {
        val response = scenarioService.getMapData(1)

        assertNotNull(response)
        assertEquals(1L, response.scenarioId)
        assertEquals("요리사 3인방의 사건 현장", response.scenarioName)
        assertNotNull(response.map)
        assertEquals(2, response.map.layers.size)
        assertEquals(3, response.map.objects.size)
        assertEquals(4, response.map.assets.size)
    }

    @Test
    fun `getTodayMapData returns today's map data`() = runBlocking {
        val response = scenarioService.getTodayMapData()

        assertNotNull(response)
        assertEquals("요리사 3인방의 사건 현장", response.scenarioName)
        assertNotNull(response.map)
    }
}
