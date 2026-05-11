package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.model.Product
import com.example.webdev_hardware_store.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class PublicProductController(private val productRepository: ProductRepository) {

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) catalogId: Long?,
        @RequestParam(required = false) subCatalogId: Long?
    ): Page<Product> {
        val pageable = PageRequest.of(page, size)
        return when {
            subCatalogId != null -> productRepository.findBySubCatalogId(subCatalogId, pageable)
            catalogId != null    -> productRepository.findByCatalogId(catalogId, pageable)
            else                 -> productRepository.findAll(pageable)
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Product> =
        productRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<Product> {
        val pageable = PageRequest.of(page, size)
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, pageable)
    }
}
