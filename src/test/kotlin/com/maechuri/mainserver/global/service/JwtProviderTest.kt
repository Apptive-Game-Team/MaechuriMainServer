package com.maechuri.mainserver.global.service

import com.maechuri.mainserver.MainServerApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertTrue

@SpringBootTest(classes = [MainServerApplication::class])
class JwtProviderTest {

    @Autowired private lateinit var jwtProvider: JwtProvider

    @Test
    fun create_jwt_and_verify_success() {
        val history = listOf("user1", "user2", "user3")
        val jwtString = jwtProvider.createToken(history)
        val map = jwtProvider.verifyAndExtract(jwtString)
        assertTrue(history.equals(map.get("history")))
    }
}