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
  padding: $gap-lg;
  background: $color-bg-light;

  @include below-md { padding: $gap-md; }
  @include below-sm { padding: $gap-sm; }

  .banner {
    width: 100%;
    margin-bottom: $gap-lg;
    border-radius: $radius-md;
    display: block;
  }

  .error-message {
    text-align: center;
    padding: $gap-xl;
    background: #fff;
    border-radius: $radius-md;
    margin: 20px 0;

    button {
      margin-top: $gap-md;
      padding: 10px 20px;
      background: $color-success-light;
      color: #fff;
      border: none;
      border-radius: $radius-md;
      cursor: pointer;
      &:hover { filter: brightness(0.9); }
    }
  }
}
</style>