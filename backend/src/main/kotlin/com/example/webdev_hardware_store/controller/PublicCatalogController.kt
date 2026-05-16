package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.service.PopularCategoryService
import org.springframework.cache.annotation.Cacheable
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Категории", description = "Публичное дерево категорий и популярные категории")
@RestController
@RequestMapping("/categories")
class PublicCatalogController(
    private val popularCategoryService: PopularCategoryService
) {
    @GetMapping
    @Cacheable(value = ["categories"], key = "'all'")
    fun getAll(): List<CategoryTreeDto> = popularCategoryService.buildTree()

    @GetMapping("/popular")
    fun getPopular(@RequestParam(defaultValue = "6") limit: Int): List<CategoryTreeDto> =
        popularCategoryService.getPopular(limit)
}
