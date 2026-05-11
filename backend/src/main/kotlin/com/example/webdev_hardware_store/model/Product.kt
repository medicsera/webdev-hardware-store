package com.example.webdev_hardware_store.model

import jakarta.persistence.*

@Entity
@Table(name = "products")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false)
    var price: Double,

    @Column(nullable = false)
    var quantity: Int,

    var catalogId: Long? = null,
    var subCatalogId: Long? = null,

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = [JoinColumn(name = "product_id")])
    @Column(name = "image_url")
    var imageUrls: MutableList<String> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_characteristics", joinColumns = [JoinColumn(name = "product_id")])
    @MapKeyColumn(name = "char_name")
    @Column(name = "char_value")
    var characteristics: MutableMap<String, String> = mutableMapOf()
)
