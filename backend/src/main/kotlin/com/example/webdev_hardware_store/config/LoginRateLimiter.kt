package com.example.webdev_hardware_store.config

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LoginRateLimiter(private val redis: StringRedisTemplate) {

    companion object {
        private const val MAX_ATTEMPTS = 10
        private val WINDOW = Duration.ofMinutes(15)
    }

    fun isBlocked(ip: String): Boolean {
        val count = redis.opsForValue().get(key(ip))?.toLongOrNull() ?: 0L
        return count >= MAX_ATTEMPTS
    }

    fun recordFailure(ip: String) {
        val count = redis.opsForValue().increment(key(ip)) ?: 1L
        if (count == 1L) redis.expire(key(ip), WINDOW)
    }

    fun clear(ip: String) = redis.delete(key(ip))

    private fun key(ip: String) = "login:attempts:$ip"
}
