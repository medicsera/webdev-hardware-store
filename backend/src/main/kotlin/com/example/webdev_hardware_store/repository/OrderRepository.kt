package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>

    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC")
    fun findAllWithUserOrderByCreatedAtDesc(): List<Order>

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    fun findByIdWithUser(@org.springframework.data.repository.query.Param("id") id: Long): java.util.Optional<Order>

    // Возвращает catalog_id отсортированные по суммарному количеству заказанных позиций
    @Query(value = """
        SELECT p.catalog_id
        FROM order_items oi
        JOIN products p ON oi.product_id = p.id
        WHERE p.catalog_id IS NOT NULL
        GROUP BY p.catalog_id
        ORDER BY SUM(oi.quantity) DESC
    """, nativeQuery = true)
    fun findPopularCatalogIds(): List<Long>
}
