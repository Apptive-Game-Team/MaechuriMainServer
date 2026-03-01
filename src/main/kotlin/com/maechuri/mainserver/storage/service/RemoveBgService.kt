package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.storage.client.RemoveBgClient
import mu.KotlinLogging
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException

private val logger = KotlinLogging.logger {}

@Service
@Primary
class RemoveBgService(
    private val removeBgClient: RemoveBgClient

) : BackgroundRemovalService {
    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        try {
            return removeBgClient.removeBackground(imageBytes)
        } catch (e: WebClientResponseException) {
            val errorBody = e.responseBodyAsString
            logger.error("에러 발생! 상태코드: ${e.statusCode}, 메시지: $errorBody")
            return imageBytes
        } catch (e: Exception) {
            throw e
        }
    }
}
