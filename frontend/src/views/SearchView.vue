<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { useCartStore } from '@/stores/cart'
import { productService } from '@/services/productApi'
import type { Product } from '@/types/product'

const route     = useRoute()
const cartStore = useCartStore()

const results  = ref<Product[]>([])
const total    = ref(0)
const loading  = ref(false)
const page     = ref(0)
const PAGE_SIZE = 20

const query = computed(() => (route.query.q as string) ?? '')

async function fetchResults(reset = false) {
  const q = query.value.trim()
  if (!q) { results.value = []; total.value = 0; return }
  if (reset) { page.value = 0; results.value = [] }
  loading.value = true
  try {
    const res = await productService.searchProducts(q, page.value, PAGE_SIZE)
    if (reset) results.value = res.content
    else results.value.push(...res.content)
    total.value = res.totalElements
  } catch {
    /* ignore */
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  fetchResults()
}

const hasMore = computed(() => results.value.length < total.value)

onMounted(() => fetchResults(true))
watch(query, () => fetchResults(true))

const handleAddToCart = (product: Product, qty: number) => cartStore.addToCart(product, qty)
</script>

<template>
  <main class="search-page">
    <div class="container">
      <div class="breadcrumbs">
        <router-link to="/" class="breadcrumbs__link">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M1 6L7 1L13 6V12C13 12.55 12.55 13 12 13H9V9H5V13H2C1.45 13 1 12.55 1 12V6Z" stroke="#999" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </router-link>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__item breadcrumbs__item--active">Поиск</span>
      </div>

      <h1 class="search-title">
        Результаты поиска:
        <span class="search-title__query">«{{ query }}»</span>
      </h1>

      <!-- Loading skeleton -->
      <div v-if="loading && results.length === 0" class="products-grid">
        <ProductCard v-for="n in 8" :key="n" :product="undefined" />
      </div>

      <!-- Empty -->
      <div v-else-if="!loading && results.length === 0" class="search-empty">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <circle cx="28" cy="28" r="20" stroke="#ddd" stroke-width="3"/>
          <path d="M44 44L56 56" stroke="#ddd" stroke-width="3" stroke-linecap="round"/>
          <path d="M20 28H36M28 20V36" stroke="#ddd" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        <p class="search-empty__text">По запросу <strong>«{{ query }}»</strong> ничего не найдено</p>
        <router-link to="/catalog" class="btn-catalog">Перейти в каталог</router-link>
      </div>

      <!-- Results -->
      <template v-else>
        <p class="search-count">Найдено: {{ total }} {{ total === 1 ? 'товар' : total < 5 ? 'товара' : 'товаров' }}</p>

        <div class="products-grid">
          <ProductCard
            v-for="product in results"
            :key="product.id"
            :product="product"
            :show-add-to-cart="true"
            :show-quantity="true"
            @add-to-cart="handleAddToCart"
          />
        </div>

        <div v-if="hasMore" class="load-more-wrap">
          <button class="btn-load-more" :disabled="loading" @click="loadMore">
            <span v-if="loading">Загрузка...</span>
            <span v-else>Показать ещё ({{ total - results.length }})</span>
          </button>
        </div>
      </template>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.search-page {
  background: #f0f0f0;
  min-height: calc(100vh - 110px);
  padding: 20px 0 40px;
}

.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;

  &__link { display: flex; align-items: center; color: #999; text-decoration: none; &:hover { color: #f4b942; } }
  &__separator { color: #ccc; font-size: 12px; }
  &__item { color: #666; &--active { color: #2c3e50; font-weight: 600; } }
}

.search-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px;

  &__query { color: #f4b942; }
}

.search-count {
  font-size: 13px;
  color: #888;
  margin: 0 0 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.search-empty {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  &__text { font-size: 15px; color: #666; margin: 0; }
}

.btn-catalog {
  display: inline-block;
  padding: 10px 28px;
  background: #f4b942;
  color: #2c3e50;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  &:hover { background: #e0a830; }
}

.load-more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.btn-load-more {
  padding: 10px 36px;
  background: white;
  border: 2px solid #f4b942;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #f4b942;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) { background: #f4b942; color: #2c3e50; }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
}

@media (max-width: 1024px) { .products-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .products-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
