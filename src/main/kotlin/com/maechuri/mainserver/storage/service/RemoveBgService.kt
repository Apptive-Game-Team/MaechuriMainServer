package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.storage.client.RemoveBgClient
import mu.KotlinLogging
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException

internal val logger = KotlinLogging.logger {}

@Service
class RemoveBgService(
    private val removeBgClient: RemoveBgClient

) : BackgroundRemovalService {
    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        return removeBgClient.removeBackground(imageBytes)
    }
}
