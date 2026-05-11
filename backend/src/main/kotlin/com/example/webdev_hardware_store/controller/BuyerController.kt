package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.dto.UpdateBuyerDto
import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.UserRepository
import com.example.webdev_hardware_store.config.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/buyer")
class BuyerController(
    private val userRepository: UserRepository,
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
        val updated = user.copy(
            firstName = data.firstName,
            lastName = data.lastName,
            address = data.address,
            phone = data.phone,
            password = if (!data.password.isNullOrBlank())
                passwordEncoder.encode(data.password)!!
            else
                user.password,
            username = if (!data.email.isNullOrBlank()) data.email else user.username
        )
        return userRepository.save(updated)
    }
}
