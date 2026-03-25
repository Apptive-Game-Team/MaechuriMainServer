package com.maechuri.mainserver

import com.maechuri.mainserver.storage.config.U2NetProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(U2NetProperties::class)
@EnableScheduling
class MainServerApplication

fun main(args: Array<String>) {
    runApplication<MainServerApplication>(*args)
}
