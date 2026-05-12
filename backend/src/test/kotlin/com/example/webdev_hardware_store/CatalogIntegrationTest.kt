package com.example.webdev_hardware_store

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@Sql("/sql/catalog-data.sql")
@Sql("/sql/catalog-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CatalogIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun `GET categories tree возвращает 200 и непустой массив`() {
        assertThat(mvc.get().uri("/categories/tree"))
            .hasStatusOk()
            .bodyJson()
            .hasPath("$[0]")
    }

    @Test
    fun `GET categories tree содержит поле subcategories`() {
        assertThat(mvc.get().uri("/categories/tree"))
            .hasStatusOk()
            .bodyJson()
            .hasPath("$[0].subcategories")
    }

    @Test
    fun `GET categories popular возвращает 200`() {
        assertThat(mvc.get().uri("/categories/popular").param("limit", "10"))
            .hasStatusOk()
            .bodyJson()
            .hasPath("$[0]")
    }

    @Test
    fun `GET categories popular с limit=1 возвращает не более одной записи`() {
        val result = mvc.get().uri("/categories/popular").param("limit", "1").exchange()
        assertThat(result).hasStatusOk()

        val body = result.response.contentAsString
        val count = body.split("},{").size
        assertThat(count).isLessThanOrEqualTo(1)
    }
}
