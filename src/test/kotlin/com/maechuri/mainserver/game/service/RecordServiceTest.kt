package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.scenario.entity.Clue
import com.maechuri.mainserver.scenario.entity.Fact
import com.maechuri.mainserver.scenario.entity.Suspect
import com.maechuri.mainserver.scenario.repository.ClueRepository
import com.maechuri.mainserver.scenario.repository.FactRepository
import com.maechuri.mainserver.scenario.repository.SuspectRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono

class RecordServiceTest {

    private lateinit var factRepository: FactRepository
    private lateinit var suspectRepository: SuspectRepository
    private lateinit var clueRepository: ClueRepository
    private lateinit var recordService: RecordService

    @BeforeEach
    fun setUp() {
        factRepository = mock()
        suspectRepository = mock()
        clueRepository = mock()
        recordService = RecordService(factRepository, suspectRepository, clueRepository)
    }

    @Test
    fun `getRecord returns fact when tag is f`() = runBlocking {
        val scenarioId = 1L
        val factId = 102L
        val fact = Fact(
            scenarioId = scenarioId,
            suspectId = 102,
            factId = factId,
            threshold = 5,
            type = "secret",
            content = """{"secret": "빅토리아 부인은 최근 거액의 도박 빚을 졌으며, 남편에게 유산을 빨리 상속받고 싶어 했습니다."}"""
        )

        whenever(factRepository.findByScenarioIdAndFactId(scenarioId, factId))
            .thenReturn(Mono.just(fact))

        val result = recordService.getRecord(scenarioId, "f:102")

        assertEquals("Fact #102", result.name)
        assertEquals(fact.content, result.content)
    }

    @Test
    fun `getRecord returns suspect when tag is s`() = runBlocking {
        val scenarioId = 1L
        val suspectId = 101L
        val suspect = Suspect(
            scenarioId = scenarioId,
            suspectId = suspectId,
            name = "집사 지브스",
            role = "집사",
            age = 58,
            gender = "남성",
            description = "수십 년간 이 저택에서 일해 온 충직한 집사. 항상 침착함을 유지합니다.",
            isCulprit = false,
            motive = null,
            alibiSummary = "사건 추정 시각에 자신의 방에서 휴식을 취하고 있었다고 주장합니다.",
            speechStyle = "정중하고 격식 있는",
            emotionalTendency = "차분함",
            lyingPattern = "눈을 마주치지 못함",
            criticalClueIds = "[]",
            x = 5,
            y = 15
        )

        whenever(suspectRepository.findByScenarioIdAndSuspectId(scenarioId, suspectId))
            .thenReturn(Mono.just(suspect))

        val result = recordService.getRecord(scenarioId, "s:101")

        assertEquals("집사 지브스", result.name)
        assertEquals(suspect.description, result.content)
    }

    @Test
    fun `getRecord returns clue when tag is c`() = runBlocking {
        val scenarioId = 1L
        val clueId = 1L
        val clue = Clue(
            scenarioId = scenarioId,
            clueId = clueId,
            name = "피 묻은 칼",
            locationId = 3,
            description = "주방 싱크대에서 발견된 피 묻은 식칼. 혈액형은 피해자의 것과 일치합니다.",
            logicExplanation = "범행에 사용된 흉기일 가능성이 높습니다.",
            decodedAnswer = null,
            isRedHerring = false,
            relatedFactIds = "[]",
            x = 26,
            y = 6
        )

        whenever(clueRepository.findByScenarioIdAndClueId(scenarioId, clueId))
            .thenReturn(Mono.just(clue))

        val result = recordService.getRecord(scenarioId, "c:1")

        assertEquals("피 묻은 칼", result.name)
        assertEquals(clue.description, result.content)
    }

    @Test
    fun `getRecord throws exception for invalid tag`() = runBlocking {
        val exception = assertThrows<IllegalArgumentException> {
            recordService.getRecord(1L, "x:1")
        }
        assertTrue(exception.message!!.contains("Invalid record tag"))
    }

    @Test
    fun `getRecord throws exception for invalid format`() = runBlocking {
        val exception = assertThrows<IllegalArgumentException> {
            recordService.getRecord(1L, "invalid")
        }
        assertTrue(exception.message!!.contains("must be in format 'tag:id'"))
    }

    @Test
    fun `getRecord throws exception for non-numeric id`() = runBlocking {
        val exception = assertThrows<IllegalArgumentException> {
            recordService.getRecord(1L, "f:abc")
        }
        assertTrue(exception.message!!.contains("must be a valid number"))
    }

    @Test
    fun `getRecord throws exception for negative scenarioId`() = runBlocking {
        val exception = assertThrows<IllegalArgumentException> {
            recordService.getRecord(-1L, "f:1")
        }
        assertTrue(exception.message!!.contains("scenarioId must be positive"))
    }
}
