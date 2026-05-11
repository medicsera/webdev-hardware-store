package com.example.webdev_hardware_store.dto

data class UpdateBuyerDto(
    val firstName: String?,
    val lastName: String?,
    val address: String?,
    val phone: String?,
    val password: String?,
    val email: String?
)
