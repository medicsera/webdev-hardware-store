package com.example.webdev_hardware_store.config

import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminSetup(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.username}") private val adminUsername: String,
    @Value("\${admin.password}") private val adminPassword: String,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val existing = userRepository.findByUsername(adminUsername)
        if (existing == null) {
            userRepository.save(
                User(
                    username = adminUsername,
                    password = passwordEncoder.encode(adminPassword)!!,
                    role = "ADMIN",
                )
            )
        } else if (!passwordEncoder.matches(adminPassword, existing.password)) {
            userRepository.save(existing.copy(password = passwordEncoder.encode(adminPassword)!!))
        }
    }
}
