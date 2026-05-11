package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCatalogId(catalogId: Long, pageable: Pageable): Page<Product>
    fun findBySubCatalogId(subCatalogId: Long, pageable: Pageable): Page<Product>
    fun countByCatalogId(catalogId: Long): Long
}
