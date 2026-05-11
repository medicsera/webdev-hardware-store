package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.model.Catalog
import com.example.webdev_hardware_store.model.SubCatalog
import com.example.webdev_hardware_store.repository.CatalogRepository
import com.example.webdev_hardware_store.repository.SubCatalogRepository
import com.example.webdev_hardware_store.service.ImageUploadService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

data class CatalogRequest(val name: String, val slug: String)
data class SubCatalogRequest(val name: String, val slug: String, val catalogId: Long)

@RestController
@RequestMapping("/admin")
class CatalogController(
    private val catalogRepo: CatalogRepository,
    private val subCatalogRepo: SubCatalogRepository,
    private val imageUploadService: ImageUploadService
) {

    // ---- Catalogs ----

    @GetMapping("/catalogs")
    fun listCatalogs(): List<Catalog> = catalogRepo.findAll()

    @PostMapping("/catalogs")
    @CacheEvict(value = ["categories"], allEntries = true)
    fun createCatalog(@RequestBody req: CatalogRequest): Catalog =
        catalogRepo.save(Catalog(name = req.name, slug = req.slug))

    @PutMapping("/catalogs/{id}")
    @CacheEvict(value = ["categories"], allEntries = true)
    fun updateCatalog(@PathVariable id: Long, @RequestBody req: CatalogRequest): ResponseEntity<Catalog> {
        val catalog = catalogRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        catalog.name = req.name
        catalog.slug = req.slug
        return ResponseEntity.ok(catalogRepo.save(catalog))
    }

    @PostMapping("/catalogs/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @CacheEvict(value = ["categories"], allEntries = true)
    fun uploadCatalogImage(@PathVariable id: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<Catalog> {
        val catalog = catalogRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        catalog.imageUrl = imageUploadService.save(file)
        return ResponseEntity.ok(catalogRepo.save(catalog))
    }

    @DeleteMapping("/catalogs/{id}")
    @CacheEvict(value = ["categories"], allEntries = true)
    fun deleteCatalog(@PathVariable id: Long): ResponseEntity<Void> {
        if (!catalogRepo.existsById(id)) return ResponseEntity.notFound().build()
        catalogRepo.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    // ---- SubCatalogs ----

    @GetMapping("/subcatalogs")
    fun listSubCatalogs(): List<SubCatalog> = subCatalogRepo.findAll()

    @PostMapping("/subcatalogs")
    @CacheEvict(value = ["categories"], allEntries = true)
    fun createSubCatalog(@RequestBody req: SubCatalogRequest): SubCatalog =
        subCatalogRepo.save(SubCatalog(name = req.name, slug = req.slug, catalogId = req.catalogId))

    @PutMapping("/subcatalogs/{id}")
    @CacheEvict(value = ["categories"], allEntries = true)
    fun updateSubCatalog(@PathVariable id: Long, @RequestBody req: SubCatalogRequest): ResponseEntity<SubCatalog> {
        val sc = subCatalogRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        sc.name = req.name
        sc.slug = req.slug
        sc.catalogId = req.catalogId
        return ResponseEntity.ok(subCatalogRepo.save(sc))
    }

    @PostMapping("/subcatalogs/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @CacheEvict(value = ["categories"], allEntries = true)
    fun uploadSubCatalogImage(@PathVariable id: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<SubCatalog> {
        val sc = subCatalogRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        sc.imageUrl = imageUploadService.save(file)
        return ResponseEntity.ok(subCatalogRepo.save(sc))
    }

    @DeleteMapping("/subcatalogs/{id}")
    @CacheEvict(value = ["categories"], allEntries = true)
    fun deleteSubCatalog(@PathVariable id: Long): ResponseEntity<Void> {
        if (!subCatalogRepo.existsById(id)) return ResponseEntity.notFound().build()
        subCatalogRepo.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
