package com.maechuri.mainserver

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
    ]
)
class MainServerApplicationTests {

    @Test
    fun contextLoads() {
    }

}
