package com.maechuri.mainserver.game.scenario.repository

interface ScenarioObjectRepository {
    fun getObjectInteractionType(objectId: Long): String?
    fun getSimpleInteractionMessage(objectId: Long): Pair<String, String?>?
}
