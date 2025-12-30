package com.maechuri.mainserver.storage.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
@EnableConfigurationProperties(MinioProperties::class)
class MinioConfig(private val minioProperties: MinioProperties) {

    @Bean
    fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(
            minioProperties.accessKey,
            minioProperties.secretKey
        )

        return S3Client.builder()
            .endpointOverride(URI.create(minioProperties.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(minioProperties.region))
            .forcePathStyle(true)
            .build()
    }

    @Bean
    fun s3Presigner(): S3Presigner {
        val credentials = AwsBasicCredentials.create(
            minioProperties.accessKey,
            minioProperties.secretKey
        )

        return S3Presigner.builder()
            .endpointOverride(URI.create(minioProperties.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(minioProperties.region))
            .build()
    }
}
