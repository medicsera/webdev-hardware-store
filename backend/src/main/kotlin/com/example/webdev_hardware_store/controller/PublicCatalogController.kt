package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.repository.CatalogRepository
import com.example.webdev_hardware_store.repository.ProductRepository
import com.example.webdev_hardware_store.repository.SubCatalogRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.*

data class SubcategoryDto(val id: Long, val name: String, val slug: String)
data class CategoryTreeDto(
    val id: Long,
    val name: String,
    val slug: String,
    val imageUrl: String?,
    val productsCount: Long,
    val subcategories: List<SubcategoryDto>
)

@RestController
@RequestMapping("/categories")
class PublicCatalogController(
    private val catalogRepo: CatalogRepository,
    private val subCatalogRepo: SubCatalogRepository,
    private val productRepo: ProductRepository
) {
    @GetMapping
    @Cacheable(value = ["categories"], key = "'all'")
    fun getAll(): List<CategoryTreeDto> = buildTree()

    @GetMapping("/tree")
    @Cacheable(value = ["categories"], key = "'all'")
    fun getTree(): List<CategoryTreeDto> = buildTree()

    @GetMapping("/popular")
    @Cacheable(value = ["categories"], key = "'popular-' + #limit")
    fun getPopular(@RequestParam(defaultValue = "6") limit: Int): List<CategoryTreeDto> =
        buildTree().take(limit)

    private fun buildTree(): List<CategoryTreeDto> {
        val subcatalogsByParent = subCatalogRepo.findAll().groupBy { it.catalogId }
        return catalogRepo.findAll().map { cat ->
            CategoryTreeDto(
                id = cat.id,
                name = cat.name,
                slug = cat.slug,
                imageUrl = cat.imageUrl,
                productsCount = productRepo.countByCatalogId(cat.id),
                subcategories = (subcatalogsByParent[cat.id] ?: emptyList())
                    .map { SubcategoryDto(it.id, it.name, it.slug) }
            )
        }
    }
}
