package com.maechuri.mainserver.global.config

import lombok.extern.slf4j.Slf4j
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import java.util.*
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

@Component
class RequestLoggingFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        val request = exchange.getRequest()

        var clientIp = request.getHeaders().getFirst("X-Forwarded-For")
        if (clientIp == null) {
            clientIp =
                if (request.getRemoteAddress() != null) request.getRemoteAddress()!!.getAddress()
                    .getHostAddress() else "Unknown"
        }

        logger.info(
            "[Request Start] method={}, uri={}, ip={}, userAgent={}",
            request.getMethod(),
            request.getURI(),
            clientIp,
            Objects.requireNonNullElse<String?>(
                request.getHeaders().getFirst(HttpHeaders.USER_AGENT),
                "Unknown"
            )
        )

        return chain.filter(exchange)
            .doFinally(Consumer { signalType: SignalType? ->
                val duration = System.currentTimeMillis() - startTime
                logger.info(
                    "[Request End] method={}, uri={}, duration={}ms",
                    request.getMethod(),
                    request.getURI(),
                    duration
                )
            })
    }
}
