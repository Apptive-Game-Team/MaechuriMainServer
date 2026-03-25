package com.maechuri.mainserver.storage.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "u2net")
data class U2NetProperties(
    val modelPath: String = "u2net.onnx"
)
