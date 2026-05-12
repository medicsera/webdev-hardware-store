package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface OrderRepository : JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    fun findByIdWithUser(@org.springframework.data.repository.query.Param("id") id: Long): java.util.Optional<Order>

    @Query(value = """
        SELECT p.catalog_id
        FROM order_items oi
        JOIN products p ON oi.product_id = p.id
        WHERE p.catalog_id IS NOT NULL
        GROUP BY p.catalog_id
        ORDER BY SUM(oi.quantity) DESC
    """, nativeQuery = true)
    fun findPopularCatalogIds(): List<Long>

    // Загружает user через EntityGraph вместо JOIN FETCH, чтобы корректно работала SQL-пагинация
    @EntityGraph(attributePaths = ["user"])
    override fun findAll(spec: Specification<Order>?, pageable: Pageable): Page<Order>
}
