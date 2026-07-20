package com.example.webdev_hardware_store.controller

import com.example.webdev_hardware_store.config.JwtUtil
import com.example.webdev_hardware_store.config.LoginRateLimiter
import com.example.webdev_hardware_store.model.EmailVerification
import com.example.webdev_hardware_store.model.User
import com.example.webdev_hardware_store.repository.EmailVerificationRepository
import com.example.webdev_hardware_store.repository.UserRepository
import com.example.webdev_hardware_store.service.EmailService
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
import java.time.LocalDateTime

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

data class VerifyRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val code: String,
)

data class ResendRequest(
    @field:NotBlank @field:Email val email: String,
)

data class ResetPasswordRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val code: String,
    @field:NotBlank @field:Size(min = 6, max = 100) val newPassword: String,
)

data class AuthResponse(val token: String)
data class MessageResponse(val message: String)
data class ErrorResponse(val message: String)

@Tag(name = "Аутентификация", description = "Регистрация и вход")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val rateLimiter: LoginRateLimiter,
    private val emailService: EmailService,
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
            if (!user.verified) {
                sendCode(user.username)
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse("Email не подтверждён. Код отправлен повторно."))
            }
            val token = jwtUtil.generateToken(user.id, user.username, user.role)
            ResponseEntity.ok(AuthResponse(token))
        } catch (e: AuthenticationException) {
            rateLimiter.recordFailure(ip)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse("Неверная почта или пароль"))
        }
    }

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
            phone = req.phone,
            verified = false,
        )
        val saved = userRepository.save(buyer)
        rateLimiter.recordRegister(ip)
        sendCode(saved.username)
        return ResponseEntity.ok(MessageResponse("Код подтверждения отправлен на ${saved.username}"))
    }

    @Operation(summary = "Подтвердить email кодом из письма")
    @PostMapping("/verify")
    fun verify(@Valid @RequestBody req: VerifyRequest): ResponseEntity<*> {
        val verification = emailVerificationRepository.findByEmailAndCode(req.email, req.code)
            ?: return ResponseEntity.badRequest().body(ErrorResponse("Неверный код"))

        if (verification.expiresAt.isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification)
            return ResponseEntity.badRequest().body(ErrorResponse("Код истёк. Запросите новый"))
        }

        val user = userRepository.findByUsername(req.email)
            ?: return ResponseEntity.badRequest().body(ErrorResponse("Пользователь не найден"))

        user.verified = true
        userRepository.save(user)
        emailVerificationRepository.deleteByEmail(req.email)

        val token = jwtUtil.generateToken(user.id, user.username, user.role)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @Operation(summary = "Повторно отправить код подтверждения")
    @PostMapping("/resend")
    fun resend(@Valid @RequestBody req: ResendRequest): ResponseEntity<*> {
        val user = userRepository.findByUsername(req.email)
            ?: return ResponseEntity.badRequest().body(ErrorResponse("Пользователь не найден"))

        if (user.verified) {
            return ResponseEntity.badRequest().body(ErrorResponse("Email уже подтверждён"))
        }

        sendCode(req.email)
        return ResponseEntity.ok(MessageResponse("Код отправлен на ${req.email}"))
    }

    @Operation(summary = "Забыли пароль — отправить код на почту")
    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody req: ResendRequest): ResponseEntity<*> {
        val user = userRepository.findByUsername(req.email)
            ?: return ResponseEntity.badRequest().body(ErrorResponse("Пользователь с такой почтой не найден"))

        sendCode(req.email)
        return ResponseEntity.ok(MessageResponse("Код для сброса пароля отправлен на ${req.email}"))
    }

    @Operation(summary = "Сбросить пароль по коду")
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody req: ResetPasswordRequest): ResponseEntity<*> {
        val verification = emailVerificationRepository.findByEmailAndCode(req.email, req.code)
            ?: return ResponseEntity.badRequest().body(ErrorResponse("Неверный код"))

        if (verification.expiresAt.isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification)
            return ResponseEntity.badRequest().body(ErrorResponse("Код истёк. Запросите новый"))
        }

        val user = userRepository.findByUsername(req.email)
            ?: return ResponseEntity.badRequest().body(ErrorResponse("Пользователь не найден"))

        user.password = passwordEncoder.encode(req.newPassword)!!
        userRepository.save(user)
        emailVerificationRepository.deleteByEmail(req.email)

        return ResponseEntity.ok(MessageResponse("Пароль успешно изменён"))
    }

    private fun sendCode(email: String) {
        emailVerificationRepository.deleteByEmail(email)
        val code = (100000..999999).random().toString()
        emailVerificationRepository.save(
            EmailVerification(email = email, code = code, expiresAt = LocalDateTime.now().plusMinutes(15))
        )
        try {
            emailService.sendVerificationCode(email, code)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // nginx добавляет реальный IP последним в цепочке X-Forwarded-For,
    // поэтому берём последний элемент — он не может быть подделан клиентом.
    private fun resolveIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")?.split(",")?.lastOrNull()?.trim()
            ?: request.remoteAddr
}
