package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.repository.CatalogRepository
import com.example.webdev_hardware_store.repository.SubCatalogRepository
import org.springframework.web.bind.annotation.*

data class SubcategoryDto(val id: Long, val name: String, val slug: String)
data class CategoryTreeDto(
    val id: Long,
    val name: String,
    val slug: String,
    val subcategories: List<SubcategoryDto>
)

@RestController
@RequestMapping("/categories")
class PublicCatalogController(
    private val catalogRepo: CatalogRepository,
    private val subCatalogRepo: SubCatalogRepository
) {
    @GetMapping
    fun getAll(): List<CategoryTreeDto> = buildTree()

    @GetMapping("/tree")
    fun getTree(): List<CategoryTreeDto> = buildTree()

    @GetMapping("/popular")
    fun getPopular(@RequestParam(defaultValue = "6") limit: Int): List<CategoryTreeDto> =
        buildTree().take(limit)

    private fun buildTree(): List<CategoryTreeDto> {
        val subcatalogsByParent = subCatalogRepo.findAll().groupBy { it.catalogId }
        return catalogRepo.findAll().map { cat ->
            CategoryTreeDto(
                id = cat.id,
                name = cat.name,
                slug = cat.slug,
                subcategories = (subcatalogsByParent[cat.id] ?: emptyList())
                    .map { SubcategoryDto(it.id, it.name, it.slug) }
            )
        }
    }
}
