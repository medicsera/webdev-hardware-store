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
    var password: String,

    @Column(nullable = false)
    val role: String,

    var firstName: String? = null,
    var lastName: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var verified: Boolean = false
)
