package com.maechuri.mainserver.game.repository

interface ScenarioObjectRepository {
    fun getObjectInteractionType(objectId: Long): String?
    fun getSimpleInteractionMessage(objectId: Long): Pair<String, String?>?
    fun getInitialGreeting(objectId: Long): String?
}
