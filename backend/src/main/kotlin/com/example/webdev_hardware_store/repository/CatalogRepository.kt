package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.Catalog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CatalogRepository : JpaRepository<Catalog, Long>
