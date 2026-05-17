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
const mobileView = ref<'single' | 'two-col'>('two-col')

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

            <div class="view-toggle">
              <button
                class="view-toggle__btn"
                :class="{ 'view-toggle__btn--active': mobileView === 'single' }"
                @click="mobileView = 'single'"
                title="Один товар"
              >
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <rect x="2" y="2" width="14" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                  <rect x="2" y="10" width="14" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
              </button>
              <button
                class="view-toggle__btn"
                :class="{ 'view-toggle__btn--active': mobileView === 'two-col' }"
                @click="mobileView = 'two-col'"
                title="Два товара"
              >
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <rect x="2" y="2" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                  <rect x="10" y="2" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                  <rect x="2" y="10" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                  <rect x="10" y="10" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="products-grid" :class="`products-grid--mobile-${mobileView}`">
            <ProductCard v-for="n in 8" :key="n" :product="undefined" :compact="mobileView === 'two-col'" />
          </div>

          <!-- Empty -->
          <div v-else-if="filteredProducts.length === 0" class="catalog-empty">
            <p>Товаров не найдено</p>
          </div>

          <!-- Products -->
          <div v-else class="products-grid" :class="`products-grid--mobile-${mobileView}`">
            <ProductCard
              v-for="product in filteredProducts"
              :key="product.id"
              :product="product"
              :show-add-to-cart="true"
              :show-quantity="true"
              :compact="mobileView === 'two-col'"
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
  @include page-layout;
  background: $color-bg-light;
}

.container { @include container; }

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: $gap-md;
  font-size: $font-base;

  &__link { display: flex; align-items: center; color: $color-text-muted; text-decoration: none; transition: color 0.2s; &:hover { color: $color-primary; } }
  &__separator { color: $color-border; font-size: $font-sm; }
  &__item { color: $color-text-secondary; &--active { color: $color-dark; font-weight: 600; } }
}

.catalog-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;

  @include below-md { flex-direction: column; }
}

.catalog-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: $radius-md;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;

  @include below-md { width: 100%; }
}

.filter-header {
  display: flex;
  align-items: center;
  gap: $gap-sm;
  padding: 14px $gap-md;
  font-size: $font-md;
  font-weight: 600;
  color: $color-dark;
  border-bottom: 1px solid $color-border-light;
}

.filter-section {
  padding: 14px $gap-md;
  border-bottom: 1px solid $color-border-light;
  &:last-child { border-bottom: none; }
  &__title { font-size: $font-base; font-weight: 600; color: $color-dark; margin: 0 0 10px; }
}

.filter-subcat {
  display: block;
  padding: $gap-xs 0;
  font-size: $font-base;
  color: $color-text-secondary;
  text-decoration: none;
  transition: color 0.15s;
  &:hover { color: $color-primary; }
  &--active { color: $color-primary; font-weight: 600; }
}

.price-range {
  display: flex;
  flex-direction: column;
  gap: $gap-sm;

  &__field {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-sm;
    color: $color-text-secondary;
    label { flex-shrink: 0; width: 14px; }
  }

  &__input {
    flex: 1;
    height: 30px;
    border: 1px solid $color-border;
    border-radius: $radius-sm;
    padding: 0 $gap-sm;
    font-size: $font-sm;
    outline: none;
    transition: border-color 0.2s;
    &:focus { border-color: $color-primary; }
  }

  &__currency { font-size: $font-sm; color: $color-text-secondary; flex-shrink: 0; }
}

.catalog-content { flex: 1; min-width: 0; }

.sort-bar {
  display: flex;
  gap: $gap-md;
  margin-bottom: $gap-md;
  padding: 10px $gap-md;
  background: #fff;
  border-radius: $radius-md;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.sort-btn {
  background: none;
  border: none;
  font-size: $font-base;
  color: $color-text-muted;
  cursor: pointer;
  padding: $gap-xs 0;
  display: flex;
  align-items: center;
  gap: $gap-xs;
  transition: color 0.2s;
  &:hover { color: $color-text-secondary; }
  &--active { color: $color-primary; font-weight: 600; }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $gap-md;

  @include below-lg { grid-template-columns: repeat(3, 1fr); }
  @include below-md { grid-template-columns: repeat(2, 1fr); }
  @include below-sm {
    &--mobile-single {
      grid-template-columns: 1fr;
      :deep(.product-card) {
        width: 100%;
        max-width: 360px;
        margin: 0 auto;
      }
    }
    &--mobile-two-col {
      grid-template-columns: repeat(2, 1fr);
      gap: $gap-sm;
      :deep(.product-card) {
        width: 100%;
        min-height: unset;
      }
      :deep(.product-info__title) { font-size: $font-base; }
      :deep(.price) { font-size: $font-md; }
      :deep(.btn-add-to-cart) { font-size: $font-sm; padding: 8px $gap-sm; }
    }
  }
}

.view-toggle {
  display: none;
  align-items: center;
  gap: 4px;
  margin-left: auto;

  @include below-sm { display: flex; }

  &__btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: 1px solid $color-border;
    border-radius: $radius-sm;
    background: none;
    color: $color-text-muted;
    cursor: pointer;
    transition: all 0.15s;

    &:hover { border-color: $color-primary; color: $color-primary; }
    &--active {
      background: $color-primary;
      border-color: $color-primary;
      color: $color-dark;
    }
  }
}

.catalog-empty {
  text-align: center;
  padding: 60px $container-pad;
  color: $color-text-faint;
  font-size: $small-size;
  background: #fff;
  border-radius: $radius-md;
}
</style>
