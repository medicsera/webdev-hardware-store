<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { useProducts } from '@/composables/useProducts'
import { useCategories } from '@/composables/useCategories'
import { useCartStore } from '@/stores/cart'
import type { Category } from '@/types/category'
import type { Product } from '@/types/product'

const route = useRoute()
const { products, loading, fetchProducts } = useProducts()
const { allCategories, fetchAllCategories } = useCategories()
const cartStore = useCartStore()

const sortBy = ref<'default' | 'price-asc' | 'price-desc'>('default')
const priceFrom = ref('')
const priceTo = ref('')

const categorySlug    = computed(() => route.params.categorySlug as string | undefined)
const subcategorySlug = computed(() => route.params.subcategorySlug as string | undefined)

const currentCatalog = computed<Category | undefined>(() =>
  allCategories.value.find(c => c.slug === categorySlug.value)
)

const currentSubcatalog = computed(() =>
  currentCatalog.value?.subcategories?.find(s => s.slug === subcategorySlug.value)
)

const breadcrumbCategory    = computed(() => currentCatalog.value?.name    ?? categorySlug.value    ?? 'Все товары')
const breadcrumbSubcategory = computed(() => currentSubcatalog.value?.name ?? subcategorySlug.value ?? '')

async function loadProducts() {
  const catId = currentCatalog.value?.id
  const subId = currentSubcatalog.value?.id
  await fetchProducts(0, 40, catId, subId)
}

const filteredProducts = computed<Product[]>(() => {
  let list = [...products.value]
  const from = parseFloat(priceFrom.value)
  const to   = parseFloat(priceTo.value)
  if (!isNaN(from)) list = list.filter(p => p.price >= from)
  if (!isNaN(to))   list = list.filter(p => p.price <= to)
  if (sortBy.value === 'price-asc')  list.sort((a, b) => a.price - b.price)
  if (sortBy.value === 'price-desc') list.sort((a, b) => b.price - a.price)
  return list
})

onMounted(async () => {
  await fetchAllCategories()
  await loadProducts()
})

watch([categorySlug, subcategorySlug], async () => {
  await loadProducts()
})

watch(allCategories, async (cats) => {
  if (cats.length > 0) await loadProducts()
}, { once: true })

const handleAddToCart = (product: Product, qty: number) => {
  cartStore.addToCart(product, qty)
}
const handleQuantityChange = (_id: number, _qty: number) => {}
</script>

<template>
  <main class="catalog-page">
    <div class="container">
      <!-- Breadcrumbs -->
      <div class="breadcrumbs">
        <router-link to="/" class="breadcrumbs__link">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M1 6L7 1L13 6V12C13 12.55 12.55 13 12 13H9V9H5V13H2C1.45 13 1 12.55 1 12V6Z" stroke="#999" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </router-link>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__item">{{ breadcrumbCategory }}</span>
        <template v-if="breadcrumbSubcategory">
          <span class="breadcrumbs__separator">/</span>
          <span class="breadcrumbs__item breadcrumbs__item--active">{{ breadcrumbSubcategory }}</span>
        </template>
      </div>

      <div class="catalog-layout">
        <!-- Sidebar Filter -->
        <aside class="catalog-sidebar">
          <div class="filter-header">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 3H14M4 7H12M6 11H10" stroke="#666" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>Фильтр</span>
          </div>

          <!-- Price Filter -->
          <div class="filter-section">
            <h4 class="filter-section__title">Цена</h4>
            <div class="price-range">
              <div class="price-range__field">
                <label>от</label>
                <input type="number" v-model="priceFrom" class="price-range__input" placeholder="0" min="0" />
              </div>
              <div class="price-range__field">
                <label>до</label>
                <input type="number" v-model="priceTo" class="price-range__input" placeholder="∞" min="0" />
                <span class="price-range__currency">₽</span>
              </div>
            </div>
          </div>

          <!-- Subcategories -->
          <div v-if="currentCatalog?.subcategories?.length" class="filter-section">
            <h4 class="filter-section__title">Подразделы</h4>
            <router-link
              :to="`/catalog/${categorySlug}`"
              class="filter-subcat"
              :class="{ 'filter-subcat--active': !subcategorySlug }"
            >Все</router-link>
            <router-link
              v-for="sub in currentCatalog.subcategories"
              :key="sub.id"
              :to="`/catalog/${categorySlug}/${sub.slug}`"
              class="filter-subcat"
              :class="{ 'filter-subcat--active': subcategorySlug === sub.slug }"
            >{{ sub.name }}</router-link>
          </div>
        </aside>

        <!-- Product Grid -->
        <div class="catalog-content">
          <!-- Sort Bar -->
          <div class="sort-bar">
            <button class="sort-btn" :class="{ 'sort-btn--active': sortBy === 'default' }" @click="sortBy = 'default'">
              По умолчанию
            </button>
            <button
              class="sort-btn"
              :class="{ 'sort-btn--active': sortBy === 'price-asc' || sortBy === 'price-desc' }"
              @click="sortBy = sortBy === 'price-asc' ? 'price-desc' : 'price-asc'"
            >
              По цене
              <svg v-if="sortBy === 'price-asc'" width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M6 2L10 8H2L6 2Z" fill="#f4b942"/></svg>
              <svg v-else-if="sortBy === 'price-desc'" width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M6 10L2 4H10L6 10Z" fill="#f4b942"/></svg>
            </button>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="products-grid">
            <ProductCard v-for="n in 8" :key="n" :product="undefined" />
          </div>

          <!-- Empty -->
          <div v-else-if="filteredProducts.length === 0" class="catalog-empty">
            <p>Товаров не найдено</p>
          </div>

          <!-- Products -->
          <div v-else class="products-grid">
            <ProductCard
              v-for="product in filteredProducts"
              :key="product.id"
              :product="product"
              :show-add-to-cart="true"
              :show-quantity="true"
              @add-to-cart="handleAddToCart"
              @quantity-change="handleQuantityChange"
            />
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.catalog-page {
  background: #f5f5f5;
  min-height: calc(100vh - 110px);
  padding: 20px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  font-size: 13px;

  &__link {
    display: flex;
    align-items: center;
    color: #999;
    text-decoration: none;
    transition: color 0.2s;
    &:hover { color: #f4b942; }
  }

  &__separator { color: #ccc; font-size: 12px; }

  &__item {
    color: #666;
    &--active { color: #2c3e50; font-weight: 600; }
  }
}

.catalog-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.catalog-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  overflow: hidden;
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 1px solid #eee;
}

.filter-section {
  padding: 14px 16px;
  border-bottom: 1px solid #eee;
  &:last-child { border-bottom: none; }
  &__title { font-size: 13px; font-weight: 600; color: #2c3e50; margin: 0 0 10px; }
}

.filter-subcat {
  display: block;
  padding: 4px 0;
  font-size: 13px;
  color: #666;
  text-decoration: none;
  transition: color 0.15s;
  &:hover { color: #f4b942; }
  &--active { color: #f4b942; font-weight: 600; }
}

.price-range {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__field {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #666;
    label { flex-shrink: 0; width: 14px; }
  }

  &__input {
    flex: 1;
    height: 30px;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 0 8px;
    font-size: 12px;
    outline: none;
    transition: border-color 0.2s;
    &:focus { border-color: #f4b942; }
  }

  &__currency { font-size: 12px; color: #666; flex-shrink: 0; }
}

.catalog-content { flex: 1; min-width: 0; }

.sort-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.sort-btn {
  background: none;
  border: none;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  padding: 4px 0;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
  &:hover { color: #666; }
  &--active { color: #f4b942; font-weight: 600; }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.catalog-empty {
  text-align: center;
  padding: 60px 20px;
  color: #aaa;
  font-size: 15px;
  background: white;
  border-radius: 8px;
}

@media (max-width: 1024px) {
  .products-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 768px) {
  .catalog-layout { flex-direction: column; }
  .catalog-sidebar { width: 100%; }
  .products-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
