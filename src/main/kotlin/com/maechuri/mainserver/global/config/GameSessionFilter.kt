package com.maechuri.mainserver.global.config

import mu.KotlinLogging
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class GameSessionFilter : WebFilter {
    
    companion object {
        const val GAME_SESSION_COOKIE_NAME = "game_session_id"
        const val GAME_SESSION_ATTRIBUTE_NAME = "gameSessionId"
    }
    
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val cookies = exchange.request.cookies
        val existingSessionId = cookies.getFirst(GAME_SESSION_COOKIE_NAME)?.value
        
        val sessionId = if (existingSessionId.isNullOrBlank()) {
            // Generate new session ID
            val newSessionId = UUID.randomUUID().toString()
            
            // Add cookie to response
            val cookie = ResponseCookie.from(GAME_SESSION_COOKIE_NAME, newSessionId)
                .path("/")
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .sameSite("Lax")
                .maxAge(Duration.ofDays(30))
                .build()
            exchange.response.addCookie(cookie)
            
            logger.debug { "Created new game session: $newSessionId" }
            newSessionId
        } else {
            existingSessionId
        }
        
        // Store session ID in exchange attributes for easy access
        exchange.attributes[GAME_SESSION_ATTRIBUTE_NAME] = sessionId
        
        return chain.filter(exchange)
    }
}
