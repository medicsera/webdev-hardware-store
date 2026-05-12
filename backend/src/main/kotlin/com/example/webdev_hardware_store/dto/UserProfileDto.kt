package com.example.webdev_hardware_store.dto

data class UserProfileDto(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val address: String?,
    val role: String,
)
