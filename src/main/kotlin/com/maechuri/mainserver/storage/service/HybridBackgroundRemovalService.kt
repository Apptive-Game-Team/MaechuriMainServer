package com.maechuri.mainserver.storage.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

private val hybridLogger = KotlinLogging.logger {}

@Service
@Primary
class HybridBackgroundRemovalService(
    @Qualifier("u2NetBackgroundRemovalService") private val u2NetService: BackgroundRemovalService,
    @Qualifier("whiteToTransparentService") private val whiteToTransparentService: BackgroundRemovalService
) : BackgroundRemovalService {

    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        return try {
            hybridLogger.info { "Attempting background removal with U2NetBackgroundRemovalService." }
            u2NetService.removeBackground(imageBytes)
        } catch (e: Exception) {
            hybridLogger.warn(e) { "U2NetBackgroundRemovalService failed. Falling back to WhiteToTransparentService." }
            whiteToTransparentService.removeBackground(imageBytes)
        }
    }
}
