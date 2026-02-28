package com.maechuri.mainserver.storage.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "removebg")
data class RemoveBgProperties(
    val apiKey: String,
    val baseUrl: String = "https://api.remove.bg"
)
