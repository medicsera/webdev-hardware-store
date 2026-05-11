package com.example.webdev_hardware_store.service

import com.example.webdev_hardware_store.dto.ProductDto
import com.example.webdev_hardware_store.model.Product
import com.example.webdev_hardware_store.repository.ProductRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(private val productRepository: ProductRepository) {

    private fun toEntity(dto: ProductDto, existing: Product? = null): Product {
        val entity = existing ?: Product(
            name = dto.name,
            description = dto.description,
            price = dto.price,
            quantity = dto.quantity,
            catalogId = dto.catalogId,
            subCatalogId = dto.subCatalogId
        )
        entity.apply {
            name = dto.name
            description = dto.description
            price = dto.price
            quantity = dto.quantity
            catalogId = dto.catalogId
            subCatalogId = dto.subCatalogId
            characteristics = dto.characteristics.toMutableMap()
        }
        return entity
    }

    fun toDto(entity: Product) = ProductDto(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        price = entity.price,
        quantity = entity.quantity,
        catalogId = entity.catalogId,
        subCatalogId = entity.subCatalogId,
        imageUrls = entity.imageUrls.toList(),
        characteristics = entity.characteristics.toMap()
    )

    @Transactional
    @Caching(evict = [
        CacheEvict(value = ["product"], allEntries = true)
    ])
    fun save(dto: ProductDto): ProductDto {
        val entity = if (dto.id != null) {
            val existing = productRepository.findById(dto.id)
                .orElseThrow { IllegalArgumentException("Product ${dto.id} not found") }
            toEntity(dto, existing)
        } else {
            toEntity(dto)
        }
        return toDto(productRepository.save(entity))
    }

    @Transactional
    @CacheEvict(value = ["product"], key = "#id")
    fun addImages(id: Long, urls: List<String>): ProductDto {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product $id not found") }
        product.imageUrls.addAll(urls)
        return toDto(productRepository.save(product))
    }

    @Transactional
    @CacheEvict(value = ["product"], key = "#id")
    fun removeImage(id: Long, imageUrl: String): ProductDto {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product $id not found") }
        product.imageUrls.remove(imageUrl)
        return toDto(productRepository.save(product))
    }

    @Transactional
    @CacheEvict(value = ["product"], key = "#id")
    fun delete(id: Long) {
        if (!productRepository.existsById(id)) {
            throw IllegalArgumentException("Product $id does not exist")
        }
        productRepository.deleteById(id)
    }

    fun findAll(): List<ProductDto> = productRepository.findAll().map { toDto(it) }

    @Cacheable(value = ["product"], key = "#id")
    fun findById(id: Long): ProductDto =
        productRepository.findById(id)
            .map { toDto(it) }
            .orElseThrow { IllegalArgumentException("Product $id not found") }
}
