package com.example.webdev_hardware_store

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class AuthIntegrationTest : AbstractIntegrationTest() {

    private fun uniqueEmail() = "user_${System.currentTimeMillis()}@test.com"

    private fun registerBody(email: String, password: String = "password123") =
        """{"username":"$email","password":"$password","firstName":"Иван","lastName":"Тестов"}"""

    @Test
    fun `POST auth register создаёт пользователя и возвращает токен`() {
        assertThat(
            mvc.post().uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(uniqueEmail()))
        )
            .hasStatusOk()
            .bodyJson()
            .hasPath("$.token")
    }

    @Test
    fun `POST auth register с дублирующим email возвращает 400`() {
        val email = uniqueEmail()
        mvc.post().uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(email))
            .exchange()

        assertThat(
            mvc.post().uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))
        ).hasStatus(400)
    }

    @Test
    fun `POST auth login с верными данными возвращает токен`() {
        val email = uniqueEmail()
        mvc.post().uri("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(email))
            .exchange()

        assertThat(
            mvc.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$email","password":"password123"}""")
        )
            .hasStatusOk()
            .bodyJson()
            .hasPath("$.token")
    }

    @Test
    fun `POST auth login с неверным паролем возвращает 401`() {
        assertThat(
            mvc.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"nobody@test.com","password":"wrong"}""")
        ).hasStatus(401)
    }
}
