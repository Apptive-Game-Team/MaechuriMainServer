package com.maechuri.mainserver.game.service

import com.maechuri.mainserver.game.domain.ConversationHistory
import com.maechuri.mainserver.global.service.JwtProvider
import org.springframework.stereotype.Service
import tools.jackson.module.kotlin.convertValue
import tools.jackson.module.kotlin.jacksonObjectMapper

@Service
class HistoryService(
    private val jwtProvider: JwtProvider
) {

    private val jsonMapper = jacksonObjectMapper()
    private val HISTORY_KEY = "history"
    private val HISTORY_SUBJECT = "history"

    fun decodeHistory(objectId: Long, historyJwt: String): ConversationHistory {
        val conversationJson = jwtProvider.verifyAndExtract(historyJwt)[HISTORY_KEY] ?: return ConversationHistory(objectId, listOf())
        return jsonMapper.convertValue<ConversationHistory>(conversationJson)
    }

    fun encodeHistory(history: ConversationHistory): String {
        return jwtProvider.createToken(HISTORY_SUBJECT, mapOf(HISTORY_KEY to history))
    }
}