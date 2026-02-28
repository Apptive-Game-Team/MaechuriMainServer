package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.storage.client.RemoveBgClient
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class RemoveBgService(
    private val removeBgClient: RemoveBgClient
) : BackgroundRemovalService {
    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        return removeBgClient.removeBackground(imageBytes)
    }
}
