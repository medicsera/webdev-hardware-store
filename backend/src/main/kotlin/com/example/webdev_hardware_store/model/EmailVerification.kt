package com.example.webdev_hardware_store.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "email_verifications")
data class EmailVerification(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val email: String,

    @Column(nullable = false)
    val code: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime
)
