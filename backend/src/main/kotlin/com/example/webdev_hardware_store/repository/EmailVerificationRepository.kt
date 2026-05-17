package com.example.webdev_hardware_store.repository

import com.example.webdev_hardware_store.model.EmailVerification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface EmailVerificationRepository : JpaRepository<EmailVerification, Long> {
    fun findByEmailAndCode(email: String, code: String): EmailVerification?

    @Transactional
    fun deleteByEmail(email: String)
}
