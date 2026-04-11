<script setup lang="ts">
import { onMounted } from 'vue'
import ProductCarousel from '@/components/ProductCarousel.vue'
import PopularCategories from '@/components/PopularCategories.vue'
import { useProducts } from '@/composables/useProducts'

const { products, loading, error, fetchProducts } = useProducts()

onMounted(() => {
  fetchProducts(0, 10)
})
</script>

<template>
  <div class="home-view">
    <img src="/banner.jpg" alt="Баннер" class="banner">

    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="fetchProducts()">Попробовать снова</button>
    </div>

    <ProductCarousel
        v-else
        title="Рекомендации"
        :products="products"
        :loading="loading"
    />
    <PopularCategories />
  </div>
</template>

<style lang="scss" scoped>
.home-view {
  padding: 24px;
  background: #f5f5f5;

  .banner {
    width: 100%;
    margin-bottom: 24px;
    border-radius: 8px;
    display: block;
  }

  .error-message {
    text-align: center;
    padding: 40px;
    background: white;
    border-radius: 8px;
    margin: 20px 0;

    button {
      margin-top: 16px;
      padding: 10px 20px;
      background: #42b983;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;

      &:hover {
        background: #369970;
      }
    }
  }
}
</style>