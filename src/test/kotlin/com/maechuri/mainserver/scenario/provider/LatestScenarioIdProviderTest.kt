package com.maechuri.mainserver.scenario.provider

import com.maechuri.mainserver.scenario.entity.Difficulty
import com.maechuri.mainserver.scenario.entity.Scenario
import com.maechuri.mainserver.scenario.repository.ScenarioRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LatestScenarioIdProviderTest {

    private val scenarioRepository: ScenarioRepository = mock()
    private val provider = LatestScenarioIdProvider(scenarioRepository)

    @Test
    fun `getTodayScenarioId returns the id of the most recently created scenario`() = runBlocking {
        val latestScenario = Scenario(
            scenarioId = 42L,
            difficulty = Difficulty.easy,
            theme = "Latest Theme",
            tone = "Tone",
            language = "ko",
            incidentType = "Type",
            incidentSummary = "Summary",
            incidentTimeStart = Time.valueOf(LocalTime.NOON),
            incidentTimeEnd = Time.valueOf(LocalTime.MIDNIGHT),
            primaryObject = "Object",
            crimeTimeStart = Time.valueOf(LocalTime.NOON),
            crimeTimeEnd = Time.valueOf(LocalTime.MIDNIGHT),
            crimeMethod = "Method",
            noSupernatural = true,
            noTimeTravel = true,
            createdAt = Timestamp(System.currentTimeMillis()),
            incidentLocationId = null,
            crimeLocationId = null
        )

        whenever(scenarioRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Mono.just(latestScenario))

        val result = provider.getTodayScenarioId()

        assertEquals(42L, result)
    }

    @Test
    fun `getTodayScenarioId throws when no scenarios exist`() = runBlocking {
        whenever(scenarioRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Mono.empty())

        assertFailsWith<IllegalStateException> {
            provider.getTodayScenarioId()
        }
        Unit
    }
}
