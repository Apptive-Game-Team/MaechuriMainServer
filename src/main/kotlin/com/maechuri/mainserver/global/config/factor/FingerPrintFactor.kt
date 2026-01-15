package com.maechuri.mainserver.global.config.factor

import dev.yunseong.apilimitwebflux.domain.Factor
import org.springframework.web.server.ServerWebExchange

class FingerPrintFactor: Factor<String> {

    private val FINGERPRINT_ID_KEY = "fingerPrintId"

    override fun getKey(exchange: ServerWebExchange): String {
        return exchange.request.headers.get(FINGERPRINT_ID_KEY)?.firstOrNull() ?: "Unknown"
    }
}