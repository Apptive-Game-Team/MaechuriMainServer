package com.maechuri.mainserver.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class AiWebClientConfig {

    @Bean
    fun webClient(
        @Value("\${maechuri.ai-server.url}") aiServerUrl: String): WebClient {
        return WebClient.builder().baseUrl(aiServerUrl).build()
    }
}