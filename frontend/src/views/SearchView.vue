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
  @include page-layout;
}

.container { @include container; }

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: $font-base;

  &__link { display: flex; align-items: center; color: $color-text-muted; text-decoration: none; &:hover { color: $color-primary; } }
  &__separator { color: $color-border; font-size: $font-sm; }
  &__item { color: $color-text-secondary; &--active { color: $color-dark; font-weight: 600; } }
}

.search-title {
  font-size: $font-xl;
  font-weight: 700;
  color: $color-dark;
  margin: 0 0 $gap-sm;
  &__query { color: $color-primary; }
}

.search-count { font-size: $font-base; color: $color-text-muted; margin: 0 0 20px; }

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $gap-md;

  @include below-lg { grid-template-columns: repeat(3, 1fr); }
  @include below-md { grid-template-columns: repeat(2, 1fr); }
  @include below-xs { grid-template-columns: 1fr; }
}

.search-empty {
  text-align: center;
  padding: 60px $container-pad;
  background: #fff;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $gap-md;

  &__text { font-size: $small-size; color: $color-text-secondary; margin: 0; }
}

.btn-catalog {
  display: inline-block;
  padding: 10px 28px;
  background: $color-primary;
  color: $color-dark;
  border-radius: $radius-md;
  font-size: $font-md;
  font-weight: 600;
  text-decoration: none;
  &:hover { background: $color-primary-dark; }
}

.load-more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.btn-load-more {
  padding: 10px 36px;
  background: #fff;
  border: 2px solid $color-primary;
  border-radius: $radius-md;
  font-size: $font-md;
  font-weight: 600;
  color: $color-primary;
  cursor: pointer;
  transition: all 0.2s;
  &:hover:not(:disabled) { background: $color-primary; color: $color-dark; }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
}
</style>
