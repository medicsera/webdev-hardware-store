package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.dto.ProductDto
import com.example.webdev_hardware_store.repository.OrderRepository
import com.example.webdev_hardware_store.service.ImageUploadService
import com.example.webdev_hardware_store.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

data class UpdateOrderStatusDto(val status: String)

private val ALLOWED_ORDER_STATUSES = setOf(
    "pending", "processing", "shipped", "delivered",
    "ready_for_pickup", "picked_up", "cancelled"
)

data class AdminOrderItemDto(
    val id: Long,
    val productId: Long,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?
)

data class AdminOrderDto(
    val id: Long,
    val total: Double,
    val deliveryCost: Double,
    val deliveryMethod: String,
    val deliveryAddress: String?,
    val status: String,
    val createdAt: String,
    val userEmail: String,
    val userFirstName: String?,
    val userLastName: String?,
    val userPhone: String?,
    val items: List<AdminOrderItemDto>
)

@RestController
@RequestMapping("/admin")
class AdminController(
    private val productService: ProductService,
    private val imageUploadService: ImageUploadService,
    private val orderRepository: OrderRepository
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

    // ── Orders ──────────────────────────────────────────────────────────────

    @GetMapping("/orders")
    fun listOrders(): List<AdminOrderDto> =
        orderRepository.findAllWithUserOrderByCreatedAtDesc().map { toAdminDto(it) }

    @Transactional
    @PatchMapping("/orders/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @RequestBody body: UpdateOrderStatusDto
    ): AdminOrderDto {
        val newStatus = body.status
        if (newStatus !in ALLOWED_ORDER_STATUSES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый статус: $newStatus")
        }
        val order = orderRepository.findByIdWithUser(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")
        }
        order.status = newStatus
        return toAdminDto(orderRepository.save(order))
    }

    private fun toAdminDto(order: com.example.webdev_hardware_store.model.Order) = AdminOrderDto(
        id              = order.id,
        total           = order.total.toDouble(),
        deliveryCost    = order.deliveryCost.toDouble(),
        deliveryMethod  = order.deliveryMethod,
        deliveryAddress = order.deliveryAddress,
        status          = order.status,
        createdAt       = order.createdAt.toString(),
        userEmail    = order.user.username,
        userFirstName = order.user.firstName,
        userLastName  = order.user.lastName,
        userPhone     = order.user.phone,
        items        = order.items.map { item ->
            AdminOrderItemDto(
                id        = item.id,
                productId = item.productId,
                name      = item.productName,
                price     = item.price.toDouble(),
                quantity  = item.quantity,
                imageUrl  = item.imageUrl
            )
        }
    )
}
