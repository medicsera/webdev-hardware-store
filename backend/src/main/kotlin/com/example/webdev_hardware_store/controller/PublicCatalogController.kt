package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.service.PopularCategoryService
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
    private val popularCategoryService: PopularCategoryService
) {
    @GetMapping
    @Cacheable(value = ["categories"], key = "'all'")
    fun getAll(): List<CategoryTreeDto> = popularCategoryService.buildTree()

    @GetMapping("/tree")
    @Cacheable(value = ["categories"], key = "'all'")
    fun getTree(): List<CategoryTreeDto> = popularCategoryService.buildTree()

    @GetMapping("/popular")
    fun getPopular(@RequestParam(defaultValue = "6") limit: Int): List<CategoryTreeDto> =
        popularCategoryService.getPopular(limit)
}
