package com.maechuri.mainserver.scenario.provider

interface TodayScenarioIdProvider {
    suspend fun getTodayScenarioId(): Long
}
