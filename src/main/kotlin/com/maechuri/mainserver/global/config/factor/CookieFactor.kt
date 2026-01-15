package com.maechuri.mainserver.global.config.factor

import dev.yunseong.apilimitwebflux.domain.Factor
import org.springframework.http.ResponseCookie
import org.springframework.web.server.ServerWebExchange
import java.time.Duration
import java.util.UUID

class CookieFactor : Factor<String> {

    private val API_LIMIT_KEY: String = "api_limit"

    override fun getKey(exchange: ServerWebExchange): String {

        var value: String? = exchange.request.cookies[API_LIMIT_KEY] as String?

        if (value.isNullOrEmpty()) {
            val rawUuid = UUID.randomUUID().toString()
            val cookie = ResponseCookie.from(API_LIMIT_KEY, rawUuid)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(1))
                .build()
            exchange.response.addCookie(cookie)

            value = rawUuid
        }

        return value
    }
}