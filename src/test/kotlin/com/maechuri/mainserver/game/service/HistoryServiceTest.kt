package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.MainServerApplication
import com.maechuri.mainserver.game.domain.ConversationHistory
import com.maechuri.mainserver.game.domain.Message
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest(classes = [MainServerApplication::class])
class HistoryServiceTest {

    @Autowired lateinit var historyService: HistoryService

    @Test
    fun 히스토리Encoding하고Decoding_succcess() {
        val objectId = 100L
        val conversationHistory = ConversationHistory(
            objectId,
            listOf(Message("user", "안녕"), Message("suspect1", "안녕하세요"))
        )
        val jwt : String = historyService.encodeHistory(conversationHistory)

        val decodedHistory = historyService.decodeHistory(objectId, jwt)

        println(decodedHistory)
        println(objectId)
        assertEquals(objectId, decodedHistory?.objectId)
    }
}