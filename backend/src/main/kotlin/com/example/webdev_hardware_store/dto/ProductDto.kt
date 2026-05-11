package com.example.webdev_hardware_store.dto

data class ProductDto(
    val id: Long? = null,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val catalogId: Long? = null,
    val subCatalogId: Long? = null,
    val imageUrls: List<String> = emptyList(),
    val characteristics: Map<String, String> = emptyMap()
)
