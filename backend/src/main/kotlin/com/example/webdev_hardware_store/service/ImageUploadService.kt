package com.example.webdev_hardware_store.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.util.UUID

@Service
class ImageUploadService(
    @Value("\${app.upload-dir}") private val uploadDir: String
) {
    fun save(file: MultipartFile): String {
        val dir = File(uploadDir, "products")
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Cannot create upload directory: ${dir.absolutePath}")
        }
        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val filename = "${UUID.randomUUID()}.$ext"
        File(dir, filename).outputStream().use { file.inputStream.copyTo(it) }
        return "/uploads/products/$filename"
    }

    fun delete(imageUrl: String) {
        val filename = imageUrl.removePrefix("/uploads/products/")
        File(uploadDir, "products/$filename").delete()
    }
}
