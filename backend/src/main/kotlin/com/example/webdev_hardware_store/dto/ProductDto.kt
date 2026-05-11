package com.example.webdev_hardware_store.dto

data class ProductDto(
    val id: Long? = null,
    val name: String,
    val description: String,
    val priceCents: Long,
    val imageUrl: String? = null
)
