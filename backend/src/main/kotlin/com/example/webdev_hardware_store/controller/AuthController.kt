package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.config.JwtUtil
import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(
    val username: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)
data class AuthResponse(val token: String)
data class ErrorResponse(val message: String)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<*> {
        return try {
            authManager.authenticate(UsernamePasswordAuthenticationToken(req.username, req.password))
            val user = userRepository.findByUsername(req.username)!!
            val token = jwtUtil.generateToken(user.id, user.username, user.role, user.firstName, user.lastName, user.phone)
            ResponseEntity.ok(AuthResponse(token))
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse("Неверная почта или пароль"))
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<*> {
        if (userRepository.findByUsername(req.username) != null) {
            return ResponseEntity.badRequest().body(ErrorResponse("Пользователь с такой почтой уже зарегистрирован"))
        }
        val buyer = User(
            username = req.username,
            password = passwordEncoder.encode(req.password)!!,
            role = "BUYER",
            firstName = req.firstName,
            lastName = req.lastName,
            phone = req.phone
        )
        val saved = userRepository.save(buyer)
        val token = jwtUtil.generateToken(saved.id, saved.username, saved.role, saved.firstName, saved.lastName, saved.phone)
        return ResponseEntity.ok(AuthResponse(token))
    }
}
