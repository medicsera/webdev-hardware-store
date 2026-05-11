package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.SubCatalog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubCatalogRepository : JpaRepository<SubCatalog, Long> {
    fun findByCatalogId(catalogId: Long): List<SubCatalog>
}
