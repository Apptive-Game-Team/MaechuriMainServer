package com.maechuri.mainserver.storage.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "leonardo")
data class LeonardoProperties @ConstructorBinding constructor(
    val apiKey: String,
    val baseUrl: String = "https://cloud.leonardo.ai/api/rest/v1",
    val modelId: String = "b24e16ff-06e3-43eb-8d33-4416c2d75876",
)
