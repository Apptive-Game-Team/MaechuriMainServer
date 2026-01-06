package com.maechuri.mainserver.game.scenario.repository

import org.springframework.stereotype.Repository

@Repository
class MockScenarioObjectRepository : ScenarioObjectRepository {
    
    // Mock data for interaction types per object
    private val objectInteractionTypes = mapOf(
        100L to "two-way",
        101L to "simple",
        102L to "simple"
    )
    
    // Mock data for simple interactions
    private val simpleMessages = mapOf(
        101L to Pair("안녕 난 요리사 이선민이야", "이선민"),
        102L to Pair("안녕하세요", null)
    )
    
    override fun getObjectInteractionType(objectId: Long): String? {
        return objectInteractionTypes[objectId]
    }
    
    override fun getSimpleInteractionMessage(objectId: Long): Pair<String, String?>? {
        return simpleMessages[objectId]
    }
}
