package com.example.webdev_hardware_store.config

import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminSetup(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val admin = userRepository.findByUsername("admin@store.ru") ?: return
        if (!passwordEncoder.matches("admin123", admin.password)) {
            userRepository.save(
                User(
                    id = admin.id,
                    username = admin.username,
                    password = passwordEncoder.encode("admin123")!!,
                    role = admin.role,
                    firstName = admin.firstName,
                    lastName = admin.lastName,
                    address = admin.address,
                    phone = admin.phone
                )
            )
        }
    }
}
