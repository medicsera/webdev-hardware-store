package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.config.CustomUserDetails
import com.example.webdev_hardware_store.dto.UpdateBuyerDto
import com.example.webdev_hardware_store.model.Order
import com.example.webdev_hardware_store.model.OrderItem
import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.OrderRepository
import com.example.webdev_hardware_store.repository.UserRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

// ── Request / Response DTOs ────────────────────────────────────────────────

data class CreateOrderItemDto(
    val productId: Long,
    val name: String,
    val price: Double,
    val imageUrl: String?,
    val quantity: Int
)

data class CreateOrderDto(
    val items: List<CreateOrderItemDto>,
    val deliveryCost: Double
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
    private val passwordEncoder: PasswordEncoder
) {

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal principal: CustomUserDetails): User =
        userRepository.findById(principal.id).orElseThrow()

    @PutMapping("/profile")
    fun updateProfile(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @RequestBody data: UpdateBuyerDto
    ): User {
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
        return userRepository.save(updated)
    }

    @PostMapping("/orders")
    fun createOrder(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @RequestBody dto: CreateOrderDto
    ): OrderResponse {
        val user = userRepository.findById(principal.id).orElseThrow()
        val itemsTotal = dto.items.sumOf { it.price * it.quantity }
        val total = itemsTotal + dto.deliveryCost

        val order = Order(user = user, total = total.toBigDecimal(), deliveryCost = dto.deliveryCost.toBigDecimal())
        val orderItems = dto.items.map { item ->
            OrderItem(
                order       = order,
                productId   = item.productId,
                productName = item.name,
                price       = item.price.toBigDecimal(),
                quantity    = item.quantity,
                imageUrl    = item.imageUrl
            )
        }
        order.items.addAll(orderItems)

        val saved = orderRepository.save(order)
        return toResponse(saved)
    }

    @GetMapping("/orders")
    fun getOrders(@AuthenticationPrincipal principal: CustomUserDetails): List<OrderResponse> =
        orderRepository.findByUserIdOrderByCreatedAtDesc(principal.id).map { toResponse(it) }

    private fun toResponse(order: Order) = OrderResponse(
        id           = order.id,
        total        = order.total.toDouble(),
        deliveryCost = order.deliveryCost.toDouble(),
        status       = order.status,
        createdAt    = order.createdAt.toString(),
        items        = order.items.map { item ->
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
