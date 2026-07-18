package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.config.CustomUserDetails
import com.example.webdev_hardware_store.dto.UpdateBuyerDto
import com.example.webdev_hardware_store.dto.UserProfileDto
import com.example.webdev_hardware_store.model.Order
import com.example.webdev_hardware_store.model.OrderItem
import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.OrderRepository
import com.example.webdev_hardware_store.repository.ProductRepository
import com.example.webdev_hardware_store.repository.UserRepository
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

// ── Request / Response DTOs ────────────────────────────────────────────────

data class CreateOrderItemDto(
    val productId: Long,
    @field:Min(1) val quantity: Int,
)

data class CreateOrderDto(
    @field:NotEmpty @field:Valid val items: List<CreateOrderItemDto>,
    @field:PositiveOrZero val deliveryCost: Double,
    val deliveryMethod: String? = null,
    val deliveryAddress: String? = null,
)

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?
)

data class OrderResponse(
    val id: Long,
    val total: Double,
    val deliveryCost: Double,
    val deliveryMethod: String,
    val deliveryAddress: String?,
    val status: String,
    val createdAt: String,
    val items: List<OrderItemResponse>
)

// ── Controller ─────────────────────────────────────────────────────────────

@Tag(name = "Покупатель", description = "Профиль и заказы авторизованного покупателя")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/buyer")
class BuyerController(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal principal: CustomUserDetails): UserProfileDto =
        userRepository.findById(principal.id).orElseThrow { NoSuchElementException() }.toProfileDto()

    @PutMapping("/profile")
    fun updateProfile(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @RequestBody data: UpdateBuyerDto
    ): UserProfileDto {
        val user = userRepository.findById(principal.id).orElseThrow { NoSuchElementException() }
        val newPassword: String = if (!data.password.isNullOrBlank())
            passwordEncoder.encode(data.password!!) ?: ""
        else
            user.password ?: ""
        val newUsername: String = if (!data.email.isNullOrBlank()) data.email!! else user.username
        val updated = user.copy(
            firstName = if (data.firstName != null) data.firstName.takeIf { it.isNotBlank() } else user.firstName,
            lastName  = if (data.lastName  != null) data.lastName.takeIf  { it.isNotBlank() } else user.lastName,
            address   = if (data.address   != null) data.address.takeIf   { it.isNotBlank() } else user.address,
            phone     = if (data.phone     != null) data.phone.takeIf     { it.isNotBlank() } else user.phone,
            password  = newPassword,
            username  = newUsername
        )
        return userRepository.save(updated).toProfileDto()
    }

    @Transactional
    @CacheEvict(value = ["product"], allEntries = true)
    @PostMapping("/orders")
    fun createOrder(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid @RequestBody dto: CreateOrderDto
    ): OrderResponse {
        val user = userRepository.findById(principal.id).orElseThrow { NoSuchElementException() }

        // Блокируем строки товаров (SELECT FOR UPDATE) и проверяем остаток.
        // Сортировка по id исключает взаимную блокировку между параллельными транзакциями.
        val lockedProducts = dto.items
            .sortedBy { it.productId }
            .map { item ->
                val product = productRepository.findByIdForUpdate(item.productId)
                    .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Товар #${item.productId} не найден") }

                if (product.quantity < item.quantity) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Недостаточно товара «${product.name}»: в наличии ${product.quantity}, запрошено ${item.quantity}"
                    )
                }
                item to product
            }

        // Уменьшаем остатки и считаем сумму по ценам из БД (не доверяем клиенту).
        var itemsTotal = 0.0
        val orderItems = lockedProducts.map { (item, product) ->
            product.quantity -= item.quantity
            productRepository.save(product)
            itemsTotal += product.price * item.quantity

            OrderItem(
                productId   = product.id,
                productName = product.name,
                price       = product.price.toBigDecimal(),
                quantity    = item.quantity,
                imageUrl    = product.imageUrls.firstOrNull()
            )
        }

        val order = Order(
            user            = user,
            total           = (itemsTotal + dto.deliveryCost).toBigDecimal(),
            deliveryCost    = dto.deliveryCost.toBigDecimal(),
            deliveryMethod  = dto.deliveryMethod ?: "pickup",
            deliveryAddress = dto.deliveryAddress
        )
        orderItems.forEach { it.order = order }
        order.items.addAll(orderItems)

        return toResponse(orderRepository.save(order))
    }

    @GetMapping("/orders")
    fun getOrders(@AuthenticationPrincipal principal: CustomUserDetails): List<OrderResponse> =
        orderRepository.findByUserIdOrderByCreatedAtDesc(principal.id).map { toResponse(it) }

    @Transactional
    @GetMapping("/orders/{id}")
    fun getOrder(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable id: Long
    ): OrderResponse {
        val order = orderRepository.findByIdWithUser(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")
        }
        if (order.user.id != principal.id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к заказу")
        return toResponse(order)
    }

    @Transactional
    @PatchMapping("/orders/{id}/cancel")
    fun cancelOrder(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable id: Long
    ): OrderResponse {
        val order = orderRepository.findByIdWithUser(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")
        }
        if (order.user.id != principal.id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к заказу")
        if (order.status != "pending")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Отменить можно только заказ со статусом «Ожидает»")
        order.status = "cancelled"
        order.cancelledBy = "user"
        return toResponse(orderRepository.save(order))
    }

    private fun User.toProfileDto() = UserProfileDto(
        id        = id,
        email     = username,
        firstName = firstName,
        lastName  = lastName,
        phone     = phone,
        address   = address,
        role      = role,
    )

    private fun toResponse(order: Order) = OrderResponse(
        id              = order.id,
        total           = order.total.toDouble(),
        deliveryCost    = order.deliveryCost.toDouble(),
        deliveryMethod  = order.deliveryMethod,
        deliveryAddress = order.deliveryAddress,
        status          = order.status,
        createdAt       = order.createdAt.toString(),
        items           = order.items.map { item ->
            OrderItemResponse(
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
