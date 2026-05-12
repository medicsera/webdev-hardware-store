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
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

// ── Request / Response DTOs ────────────────────────────────────────────────

data class CreateOrderItemDto(
    val productId: Long,
    val quantity: Int
)

data class CreateOrderDto(
    val items: List<CreateOrderItemDto>,
    val deliveryCost: Double,
    val deliveryMethod: String? = null,
    val deliveryAddress: String? = null
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
        userRepository.findById(principal.id).orElseThrow().toProfileDto()

    @PutMapping("/profile")
    fun updateProfile(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @RequestBody data: UpdateBuyerDto
    ): UserProfileDto {
        val user = userRepository.findById(principal.id).orElseThrow()
        val newPassword: String = if (!data.password.isNullOrBlank())
            passwordEncoder.encode(data.password!!) ?: ""
        else
            user.password ?: ""
        val newUsername: String = if (!data.email.isNullOrBlank()) data.email!! else user.username
        val updated = user.copy(
            firstName = data.firstName,
            lastName  = data.lastName,
            address   = data.address,
            phone     = data.phone,
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
        @RequestBody dto: CreateOrderDto
    ): OrderResponse {
        val user = userRepository.findById(principal.id).orElseThrow()

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
