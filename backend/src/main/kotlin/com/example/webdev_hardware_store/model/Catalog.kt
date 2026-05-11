package com.example.webdev_hardware_store.model

import jakarta.persistence.*

@Entity
@Table(name = "catalogs")
data class Catalog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column
    var imageUrl: String? = null
)
