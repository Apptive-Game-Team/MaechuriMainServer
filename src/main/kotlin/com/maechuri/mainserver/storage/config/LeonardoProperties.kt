package com.maechuri.mainserver.storage.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "leonardo")
data class LeonardoProperties @ConstructorBinding constructor(
    val apiKey: String,
    val baseUrl: String = "https://cloud.leonardo.ai/api/rest/v1",
    val modelId: String = "6bef9f1b-29cb-40c7-b9df-32b51c1f67d3",
)
