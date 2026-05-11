package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.dto.ProductDto
import com.example.webdev_hardware_store.service.ImageUploadService
import com.example.webdev_hardware_store.service.ProductService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin")
class AdminController(
    private val productService: ProductService,
    private val imageUploadService: ImageUploadService
) {

    @GetMapping("/products")
    fun list(): List<ProductDto> = productService.findAll()

    @GetMapping("/products/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<ProductDto> =
        try { ResponseEntity.ok(productService.findById(id)) }
        catch (e: IllegalArgumentException) { ResponseEntity.notFound().build() }

    @PostMapping("/products")
    fun createOrUpdate(@RequestBody product: ProductDto): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.save(product))

    @PutMapping("/products/{id}")
    fun update(@PathVariable id: Long, @RequestBody product: ProductDto): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.save(product.copy(id = id)))

    @PostMapping("/products/{id}/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImages(
        @PathVariable id: Long,
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<ProductDto> {
        val urls = files.map { imageUploadService.save(it) }
        return ResponseEntity.ok(productService.addImages(id, urls))
    }

    @DeleteMapping("/products/{id}/images")
    fun deleteImage(
        @PathVariable id: Long,
        @RequestParam imageUrl: String
    ): ResponseEntity<ProductDto> {
        imageUploadService.delete(imageUrl)
        return ResponseEntity.ok(productService.removeImage(id, imageUrl))
    }

    @DeleteMapping("/products/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
