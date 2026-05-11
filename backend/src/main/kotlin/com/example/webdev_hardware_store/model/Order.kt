package com.example.webdev_hardware_store.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val total: BigDecimal,

    @Column(name = "delivery_cost", nullable = false)
    val deliveryCost: BigDecimal,

    @Column(nullable = false)
    var status: String = "pending",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf()
)
