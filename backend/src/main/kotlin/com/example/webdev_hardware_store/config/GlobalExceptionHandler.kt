package com.example.webdev_hardware_store.config

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException

data class ApiError(val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDuplicate(e: DataIntegrityViolationException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError("Такой элемент уже существует"))

    @ExceptionHandler(IOException::class)
    fun handleIo(e: IOException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("Ошибка при работе с файлом: ${e.message}"))
}
