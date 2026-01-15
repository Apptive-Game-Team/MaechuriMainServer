package com.maechuri.mainserver.global.config

import com.maechuri.mainserver.global.config.factor.CookieFactor
import com.maechuri.mainserver.global.config.factor.FingerPrintFactor
import dev.yunseong.apilimitwebflux.domain.LimitRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class ApiLimitConfig {

    @Bean
    fun cookieRule(): LimitRule<String> {
        return LimitRule(
            "/api/scenarios/**",
            100,
            Duration.ofDays(1),
            CookieFactor()
        )
    }

    @Bean
    fun fingerPrintFactor(): LimitRule<String> {
        return LimitRule(
            "/api/scenarios/**",
            100,
            Duration.ofDays(1),
            FingerPrintFactor()
        )
    }
}

