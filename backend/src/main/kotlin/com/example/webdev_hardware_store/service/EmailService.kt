package com.example.webdev_hardware_store.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder

@Service
class EmailService(
    @Value("\${unisender.api-key}") private val apiKey: String,
    @Value("\${unisender.sender-name:Factura}") private val senderName: String,
    @Value("\${unisender.sender-email}") private val senderEmail: String,
    @Value("\${unisender.list-id:1}") private val listId: String,
) {
    private val restTemplate = RestTemplate()
    private val apiUrl = "https://api.unisender.com/ru/api/sendEmail"

    fun sendVerificationCode(to: String, code: String) {
        val htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #4CAF50;">Подтверждение email</h2>
                <p>Ваш код подтверждения:</p>
                <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px; color: #333;">$code</p>
                <p style="color: #666;">Код действует 15 минут.</p>
            </body>
            </html>
        """.trimIndent()

        val params = mapOf(
            "format" to "json",
            "api_key" to apiKey,
            "email" to to,
            "sender_name" to senderName,
            "sender_email" to senderEmail,
            "subject" to "Код подтверждения — Factura",
            "body" to htmlBody,
            "list_id" to listId,
            "lang" to "ru",
            "error_checking" to "1",
        )
        val query = params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        val url = "$apiUrl?$query"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        restTemplate.postForEntity(url, HttpEntity(null, headers), Map::class.java)
    }
}
