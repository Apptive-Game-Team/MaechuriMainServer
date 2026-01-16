package com.maechuri.mainserver.global.config

import com.maechuri.mainserver.global.config.factor.CookieFactor
import com.maechuri.mainserver.global.config.factor.FingerPrintFactor
import dev.yunseong.apilimitwebflux.domain.LimitRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class ApiLimitConfig {

    private val PATH: String = "/api/scenarios/**"
    private val ONE_DAY: Duration = Duration.ofDays(1)
    private val LIMIT: Int = 100

    @Bean
    fun cookieRule(): LimitRule<String> {
        return LimitRule(
            PATH,
            LIMIT,
            ONE_DAY,
            CookieFactor()
        )
    }

    @Bean
    fun fingerPrintFactor(): LimitRule<String> {
        return LimitRule(
            PATH,
            LIMIT,
            ONE_DAY,
            FingerPrintFactor()
        )
    }
}

