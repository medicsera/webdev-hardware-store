package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.Product
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCatalogId(catalogId: Long, pageable: Pageable): Page<Product>
    fun findBySubCatalogId(subCatalogId: Long, pageable: Pageable): Page<Product>
    fun countByCatalogId(catalogId: Long): Long
    fun findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(name: String, description: String, pageable: Pageable): Page<Product>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Optional<Product>
}
