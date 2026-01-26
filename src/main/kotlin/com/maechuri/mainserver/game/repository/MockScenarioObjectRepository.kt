package com.maechuri.mainserver.game.repository

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
    
    // Mock data for initial greetings in two-way interactions
    private val initialGreetings = mapOf(
        100L to "안녕 너가 말해봐"
    )
    
    override fun getObjectInteractionType(objectId: Long): String? {
        return objectInteractionTypes[objectId]
    }
    
    override fun getSimpleInteractionMessage(objectId: Long): Pair<String, String?>? {
        return simpleMessages[objectId]
    }
    
    override fun getInitialGreeting(objectId: Long): String? {
        return initialGreetings[objectId]
    }
}
