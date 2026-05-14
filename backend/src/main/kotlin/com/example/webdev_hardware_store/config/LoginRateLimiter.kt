package com.example.webdev_hardware_store.config

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LoginRateLimiter(private val redis: StringRedisTemplate) {

    companion object {
        private const val LOGIN_MAX    = 10
        private const val REGISTER_MAX = 5
        private val LOGIN_WINDOW    = Duration.ofMinutes(15)
        private val REGISTER_WINDOW = Duration.ofHours(1)
    }

    fun isBlocked(ip: String)     = count("login:attempts:$ip")    >= LOGIN_MAX
    fun recordFailure(ip: String) = increment("login:attempts:$ip", LOGIN_WINDOW)
    fun clear(ip: String)         { redis.delete("login:attempts:$ip") }

    fun isRegisterBlocked(ip: String) = count("register:attempts:$ip") >= REGISTER_MAX
    fun recordRegister(ip: String)    = increment("register:attempts:$ip", REGISTER_WINDOW)

    private fun count(key: String): Long =
        redis.opsForValue().get(key)?.toLongOrNull() ?: 0L

    private fun increment(key: String, window: Duration) {
        val n = redis.opsForValue().increment(key) ?: 1L
        if (n == 1L) redis.expire(key, window)
    }
}
