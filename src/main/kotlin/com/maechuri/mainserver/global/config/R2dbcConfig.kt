package com.maechuri.mainserver.global.config

import io.r2dbc.postgresql.codec.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

@Configuration
class R2dbcConfig {

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val objectMapper = ObjectMapper()
        val converters = mutableListOf<Converter<*, *>>()
        converters.add(JsonToLongListConverter(objectMapper))
        converters.add(LongListToJsonConverter(objectMapper))
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters)
    }

    class JsonToLongListConverter(private val objectMapper: ObjectMapper) : Converter<Json, List<Long>> {
        override fun convert(source: Json): List<Long> {
            return objectMapper.readValue(source.asString(), object : TypeReference<List<Long>>() {}) ?: emptyList()
        }
    }

    class LongListToJsonConverter(private val objectMapper: ObjectMapper) : Converter<List<Long>, Json> {
        override fun convert(source: List<Long>): Json {
            return Json.of(objectMapper.writeValueAsString(source))
        }
    }
}
