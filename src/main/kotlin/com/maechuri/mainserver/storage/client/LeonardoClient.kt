package com.maechuri.mainserver.storage.client

import com.maechuri.mainserver.storage.config.LeonardoProperties
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import kotlin.lazy

private val log = KotlinLogging.logger {}

private const val POLL_DELAY_MS = 2_000L

@Component
class LeonardoClient(private val leonardoProperties: LeonardoProperties) {

    private val webClient: WebClient by lazy {
        WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { config ->
                        config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                    }
                    .build()
            )
            .baseUrl(leonardoProperties.baseUrl)
            .defaultHeader("Authorization", "Bearer ${leonardoProperties.apiKey}")
            .defaultHeader("Content-Type", "application/json")
            .build()
    }

    private val downloadClient: WebClient by lazy {
        WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { config ->
                        config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                    }
                    .build()
            )
            .build()
    }

    /**
     * Initiates an image generation job on Leonardo.ai.
     * @param prompt The visual description to generate an image from.
     * @return The generation job ID.
     */
    suspend fun createGeneration(prompt: String): String {
        val sanitizedPrompt = prompt.take(950).replace(Regex("[\"\\\\]"), "")
        val body = mapOf(
            "contrast" to 3.5,
            "prompt" to sanitizedPrompt,
            "modelId" to leonardoProperties.modelId,
            "width" to 1024,
            "height" to 1024,
            "num_images" to 1
        )

        try {
            val response = webClient.post()
                .uri("/generations")
                .bodyValue(body)
                .retrieve()
                .awaitBody<Map<String, Any>>()

            @Suppress("UNCHECKED_CAST")
            val job = response["sdGenerationJob"] as? Map<String, Any>
                ?: error("Unexpected Leonardo.ai response: missing sdGenerationJob")
            return job["generationId"] as? String
                ?: error("Unexpected Leonardo.ai response: missing generationId")

        } catch (e: WebClientResponseException) {
            val errorBody = e.responseBodyAsString
            val statusCode = e.statusCode

            log.error {
                "LeonardoClient failed. StatusCode=$statusCode, ErrorBody=$errorBody, Message=${e.message}"
            }
            throw e
        } catch (e: Exception) {
            log.error(e) { "An unexpected error occurred during generate image." }
            throw e
        }
    }

    /**
     * Polls Leonardo.ai until the generation is complete and returns the URL of the first generated image.
     * @param generationId The generation job ID returned by [createGeneration].
     * @param maxAttempts Maximum number of polling attempts (default 30, ~1 minute).
     * @return URL of the generated image.
     */
    suspend fun waitForGeneration(generationId: String, maxAttempts: Int = 30): String {
        repeat(maxAttempts) { attempt ->
            delay(POLL_DELAY_MS)
            val response = webClient.get()
                .uri("/generations/$generationId")
                .retrieve()
                .awaitBodyOrNull<Map<String, Any>>()

            @Suppress("UNCHECKED_CAST")
            val generation = response?.get("generations_by_pk") as? Map<String, Any>
            val status = generation?.get("status") as? String

            if (status == "COMPLETE") {
                val images = generation["generated_images"] as? List<Map<String, Any>>
                val url = images?.firstOrNull()?.get("url") as? String
                    ?: error("No generated images in completed generation $generationId")
                return url
            }
            if (status == "FAILED") {
                error("Leonardo.ai generation $generationId failed")
            }
            log.debug { "Generation $generationId attempt ${attempt + 1}: status=$status" }
        }
        error("Leonardo.ai generation $generationId timed out after $maxAttempts attempts")
    }

    /**
     * Downloads raw image bytes from a URL.
     * @param url The URL to download the image from.
     * @return Raw image bytes.
     */
    suspend fun downloadImage(url: String): ByteArray {
        return downloadClient.get()
            .uri(url)
            .retrieve()
            .awaitBody<ByteArray>()
    }
}
