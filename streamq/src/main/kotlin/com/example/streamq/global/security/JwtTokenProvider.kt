package com.example.streamq.global.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.JwtException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

private const val EXPIRATION_TIME = 1000 * 60 * 30L // 30분

@Component
class JwtTokenProvider (
    @Value("\${security.secretkey}") private val secretKey: String
){
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())
    private val log = LoggerFactory.getLogger(this::class.java)

    fun generateToken(id: Long, role: String): String {
        val now = Date()
        val expiration = Date(now.time + EXPIRATION_TIME)

        return Jwts.builder()
            .claim("role", role)
            .subject(id.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    fun getUserInfoIfValid(token: String): Pair<Long, String>? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)

            val id = claims.payload.subject.toLong()
            val role = claims.payload.get("role", String::class.java) ?: return null

            Pair(id, role)
        } catch (e: JwtException) {
            log.error("JWT 검증 실패: {}", e.message)
            null
        } catch (e: IllegalArgumentException) {
            log.error("JWT 파싱 에러: {}", e.message)
            null
        }
    }
}
