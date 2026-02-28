package com.maechuri.mainserver

import com.maechuri.mainserver.storage.config.RemoveBgProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(RemoveBgProperties::class)
class MainServerApplication

fun main(args: Array<String>) {
    runApplication<MainServerApplication>(*args)
}
