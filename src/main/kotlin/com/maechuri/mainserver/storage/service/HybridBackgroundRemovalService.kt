package com.maechuri.mainserver.storage.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException

private val hybridLogger = KotlinLogging.logger {}

@Service
@Primary
class HybridBackgroundRemovalService(
    @Qualifier("removeBgService") private val removeBgService: BackgroundRemovalService,
    @Qualifier("whiteToTransparentService") private val whiteToTransparentService: BackgroundRemovalService
) : BackgroundRemovalService {

    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        return try {
            hybridLogger.info { "Attempting background removal with RemoveBgService." }
            removeBgService.removeBackground(imageBytes)
        } catch (e: WebClientResponseException) {
            hybridLogger.warn(e) { "RemoveBgService failed. Falling back to WhiteToTransparentService." }
            whiteToTransparentService.removeBackground(imageBytes)
        } catch (e: Exception) {
            hybridLogger.error(e) { "An unexpected error occurred during background removal. Falling back to WhiteToTransparentService." }
            whiteToTransparentService.removeBackground(imageBytes)
        }
    }
}
