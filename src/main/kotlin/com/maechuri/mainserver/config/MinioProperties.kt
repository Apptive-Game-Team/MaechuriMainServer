package com.maechuri.mainserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "minio")
data class MinioProperties @ConstructorBinding constructor(
    val accessKey: String,
    val secretKey: String,
    val endpoint: String,
    val bucketName: String,
    val region: String
)
