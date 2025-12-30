package com.maechuri.mainserver.global.service

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Date
import kotlin.io.encoding.Base64

@Component
class JwtProvider(
    @Value("\${maechuri.jwt.public-key-path}") private val publicKeyResource: Resource,
    @Value("\${maechuri.jwt.private-key-path}") private val privateKeyResource: Resource
) {

    private val publicKey: PublicKey
    private val privateKey: PrivateKey

    init {
        publicKey = decodePublicKey(publicKeyResource.inputStream.readBytes())
        privateKey = decodePrivateKey(privateKeyResource.inputStream.readBytes())
    }

    fun createToken(history: List<Any>): String {
        return Jwts.builder()
            .subject("chat-history")
            .claim("history", history)
            .issuedAt(Date())
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact()
    }

    fun verifyAndExtract(token: String): Map<String, Any> {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun decodePublicKey(keyBytes: ByteArray): PublicKey {
        val spec = X509EncodedKeySpec(parsePem(keyBytes))
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    // PEM 문자열(ByteArray)을 PrivateKey 객체로 변환
    private fun decodePrivateKey(keyBytes: ByteArray): PrivateKey {
        val spec = PKCS8EncodedKeySpec(parsePem(keyBytes))
        return KeyFactory.getInstance("RSA").generatePrivate(spec)
    }

    private fun parsePem(pemBytes: ByteArray): ByteArray {
        val pem = String(pemBytes)
            .replace("-----BEGIN (.*)-----".toRegex(), "")
            .replace("-----END (.*)-----".toRegex(), "")
            .replace("\\s".toRegex(), "")
        return Base64.decode(pem)
    }
}