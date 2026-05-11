package com.example.webdev_hardware_store.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,          // BCrypt‑encoded

    @Column(nullable = false)
    val role: String,              // "ADMIN" or "BUYER"

    // buyer‑only fields
    var address: String? = null,
    var phone: String? = null
)