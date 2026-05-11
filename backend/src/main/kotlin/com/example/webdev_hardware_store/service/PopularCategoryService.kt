package com.example.webdev_hardware_store.service

import com.example.webdev_hardware_store.controller.CategoryTreeDto
import com.example.webdev_hardware_store.controller.SubcategoryDto
import com.example.webdev_hardware_store.repository.CatalogRepository
import com.example.webdev_hardware_store.repository.OrderRepository
import com.example.webdev_hardware_store.repository.ProductRepository
import com.example.webdev_hardware_store.repository.SubCatalogRepository
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PopularCategoryService(
    private val catalogRepo: CatalogRepository,
    private val subCatalogRepo: SubCatalogRepository,
    private val productRepo: ProductRepository,
    private val orderRepo: OrderRepository
) {
    fun buildTree(): List<CategoryTreeDto> {
        val subcsByParent = subCatalogRepo.findAll().groupBy { it.catalogId }
        return catalogRepo.findAll().map { cat ->
            CategoryTreeDto(
                id            = cat.id,
                name          = cat.name,
                slug          = cat.slug,
                imageUrl      = cat.imageUrl,
                productsCount = productRepo.countByCatalogId(cat.id),
                subcategories = (subcsByParent[cat.id] ?: emptyList())
                    .map { SubcategoryDto(it.id, it.name, it.slug) }
            )
        }
    }

    @Cacheable(value = ["categories"], key = "'popular-' + #limit")
    fun getPopular(limit: Int): List<CategoryTreeDto> = compute(limit)

    // Каждый день в 3:00 пересчитывает популярные категории и обновляет кэш
    @Scheduled(cron = "0 0 3 * * *")
    @CachePut(value = ["categories"], key = "'popular-6'")
    fun refreshPopularDaily(): List<CategoryTreeDto> = compute(6)

    private fun compute(limit: Int): List<CategoryTreeDto> {
        val popularIds = orderRepo.findPopularCatalogIds()
        val tree = buildTree()
        if (popularIds.isEmpty()) return tree.take(limit)
        val rankMap = popularIds.withIndex().associate { (rank, id) -> id to rank }
        return tree
            .sortedBy { rankMap[it.id] ?: Int.MAX_VALUE }
            .take(limit)
    }
}
