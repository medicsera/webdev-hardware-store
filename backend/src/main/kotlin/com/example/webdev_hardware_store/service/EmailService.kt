package com.example.webdev_hardware_store.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.from}") private val from: String,
) {
    fun sendVerificationCode(to: String, code: String) {
        val mime = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mime, false, "UTF-8")
        helper.setFrom(from)
        helper.setTo(to)
        helper.setSubject("Kod podtverzhdeniya - Factura")
        helper.setText(
            "Vash kod podtverzhdeniya: $code\n\nKod dejstvuet 15 minut."
        )
        mailSender.send(mime)
    }
}
