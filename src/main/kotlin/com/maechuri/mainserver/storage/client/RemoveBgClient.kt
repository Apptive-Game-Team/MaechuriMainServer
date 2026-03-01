package com.maechuri.mainserver.storage.client

import com.maechuri.mainserver.storage.config.RemoveBgProperties
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient

@Component
class RemoveBgClient(private val properties: RemoveBgProperties) {

    private val webClient: WebClient by lazy {
        WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader("X-Api-Key", properties.apiKey)
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
            .build()
    }

    suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("image_file", object : ByteArrayResource(imageBytes) {
            override fun getFilename(): String {
                return "image.png"
            }
        })
        body.add("size", "auto")

        return webClient.post()
            .uri("/v1.0/removebg")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .awaitBody<ByteArray>()
    }
}
