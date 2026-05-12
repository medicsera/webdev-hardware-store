package com.example.webdev_hardware_store.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.IOException
import java.util.UUID

@Service
class ImageUploadService(
    @Value("\${app.upload-dir}") private val uploadDir: String
) {
    companion object {
        private const val MAX_BYTES = 5 * 1024 * 1024L // 5 MB

        private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")

        private val MIME_TO_EXT = mapOf(
            "image/jpeg" to "jpg",
            "image/png"  to "png",
            "image/webp" to "webp",
            "image/gif"  to "gif",
        )
    }

    fun save(file: MultipartFile): String {
        if (file.size > MAX_BYTES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл слишком большой. Максимальный размер — 5 МБ")
        }

        val contentType = file.contentType?.lowercase()?.trim() ?: ""
        if (contentType !in ALLOWED_MIME_TYPES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый тип файла. Разрешены: JPEG, PNG, WebP, GIF")
        }

        val bytes = file.bytes
        if (!matchesMagicBytes(bytes, contentType)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Содержимое файла не соответствует его типу")
        }

        val ext = MIME_TO_EXT[contentType] ?: "jpg"
        val dir = File(uploadDir, "products")
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Cannot create upload directory: ${dir.absolutePath}")
        }

        val filename = "${UUID.randomUUID()}.$ext"
        File(dir, filename).writeBytes(bytes)
        return "/uploads/products/$filename"
    }

    fun delete(imageUrl: String) {
        val filename = imageUrl.removePrefix("/uploads/products/")
        File(uploadDir, "products/$filename").delete()
    }

    private fun matchesMagicBytes(bytes: ByteArray, mimeType: String): Boolean {
        if (bytes.size < 12) return false
        return when (mimeType) {
            "image/jpeg" ->
                bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
            "image/png" ->
                bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
            "image/gif" ->
                bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() &&
                bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte()
            "image/webp" ->
                bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() &&
                bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
                bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() &&
                bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte()
            else -> false
        }
    }
}
