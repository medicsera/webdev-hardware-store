package com.example.webdev_hardware_store.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(@Value("\${jwt.secret}") secret: String) {

    private val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(id: Long, username: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + 1000 * 60 * 60 * 24)

        return Jwts.builder()
            .setSubject(username)
            .claim("id", id)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): Boolean =
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }

    fun getClaims(token: String) = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .body
}
