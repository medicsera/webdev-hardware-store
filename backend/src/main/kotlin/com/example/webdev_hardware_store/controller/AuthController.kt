package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.config.JwtUtil
import com.example.webdev_hardware_store.config.LoginRateLimiter
import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

data class LoginRequest(
    @field:NotBlank @field:Email val username: String,
    @field:NotBlank val password: String,
)

data class RegisterRequest(
    @field:NotBlank @field:Email val username: String,
    @field:NotBlank @field:Size(min = 6, max = 100) val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
)
data class AuthResponse(val token: String)
data class ErrorResponse(val message: String)

@Tag(name = "Аутентификация", description = "Регистрация и вход")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val rateLimiter: LoginRateLimiter,
) {

    @Operation(summary = "Войти в аккаунт", description = "Возвращает JWT-токен")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody req: LoginRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<*> {
        val ip = resolveIp(servletRequest)

        if (rateLimiter.isBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse("Слишком много попыток входа. Попробуйте через 15 минут"))
        }

        return try {
            authManager.authenticate(UsernamePasswordAuthenticationToken(req.username, req.password))
            rateLimiter.clear(ip)
            val user = userRepository.findByUsername(req.username)!!
            val token = jwtUtil.generateToken(user.id, user.username, user.role)
            ResponseEntity.ok(AuthResponse(token))
        } catch (e: AuthenticationException) {
            rateLimiter.recordFailure(ip)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse("Неверная почта или пароль"))
        }
    }

    // nginx добавляет реальный IP последним в цепочке X-Forwarded-For,
    // поэтому берём последний элемент — он не может быть подделан клиентом.
    private fun resolveIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")?.split(",")?.lastOrNull()?.trim()
            ?: request.remoteAddr

    @Operation(summary = "Зарегистрировать нового пользователя")
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody req: RegisterRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<*> {
        val ip = resolveIp(servletRequest)
        if (rateLimiter.isRegisterBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse("Слишком много регистраций. Попробуйте через час"))
        }
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
        rateLimiter.recordRegister(ip)
        val token = jwtUtil.generateToken(saved.id, saved.username, saved.role)
        return ResponseEntity.ok(AuthResponse(token))
    }
}
