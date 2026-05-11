package com.example.webdev_hardware_store.model

import jakarta.persistence.*

@Entity
@Table(name = "sub_catalogs")
data class SubCatalog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var slug: String,

    @Column(nullable = false)
    var catalogId: Long,

    @Column
    var imageUrl: String? = null
)
