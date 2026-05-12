package com.example.webdev_hardware_store.config

import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.LoggingCacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig : CachingConfigurer {

    private val serializer: GenericJacksonJsonRedisSerializer =
        GenericJacksonJsonRedisSerializer.builder()
            .enableUnsafeDefaultTyping()
            .customize { b -> b.findAndAddModules() }
            .build()

    private fun cacheConfig(ttl: Duration): RedisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager =
        RedisCacheManager.builder(factory)
            .cacheDefaults(cacheConfig(Duration.ofMinutes(10)))
            .withInitialCacheConfigurations(
                mapOf(
                    "categories" to cacheConfig(Duration.ofHours(1)),
                    "product"    to cacheConfig(Duration.ofMinutes(30))
                )
            )
            .transactionAware()
            .build()

    override fun errorHandler(): CacheErrorHandler = LoggingCacheErrorHandler()
}
