package com.example.webdev_hardware_store.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebConfig(
    @Value("\${app.upload-dir}") private val uploadDir: String
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val absolutePath = File(uploadDir).absolutePath
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$absolutePath/")
    }
}
