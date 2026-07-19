<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { productService } from '@/services/productApi'
import type { Product } from '@/types/product'
import AdminProductModal from '@/components/AdminProductModal.vue'

const route     = useRoute()
const cartStore = useCartStore()
const authStore = useAuthStore()

const isAdmin        = computed(() => authStore.currentUser?.role === 'ADMIN')
const showEditModal  = ref(false)

const loading  = ref(true)
const quantity = ref(1)
const isAdding = ref(false)
const product  = ref<Product | null>(null)
const activeImage = ref(0)

const formattedPrice = computed(() => {
  if (!product.value) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 2 })
    .format(product.value.price)
})

const imageUrl = computed(() => {
  const urls = product.value?.imageUrls
  if (!urls?.length) return '/placeholder-product.jpg'
  return urls[activeImage.value] ?? urls[0]
})

const isInStock = computed(() => (product.value?.quantity ?? 0) > 0)

const fetchProduct = async () => {
  loading.value = true
  activeImage.value = 0
  try {
    product.value = await productService.getProductById(Number(route.params.id))
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, fetchProduct)
onMounted(fetchProduct)

const imageCount = computed(() => product.value?.imageUrls.length ?? 0)

function prevImage() {
  activeImage.value = (activeImage.value - 1 + imageCount.value) % imageCount.value
}
function nextImage() {
  activeImage.value = (activeImage.value + 1) % imageCount.value
}

const maxStock = computed(() => product.value?.quantity ?? Infinity)

const clampQuantity = () => {
  if (!Number.isInteger(quantity.value) || quantity.value < 1) quantity.value = 1
  else if (quantity.value > maxStock.value) quantity.value = maxStock.value as number
}

const handleAddToCart = () => {
  if (!product.value || isAdding.value) return
  clampQuantity()
  isAdding.value = true
  cartStore.addToCart(product.value, quantity.value)
  setTimeout(() => { isAdding.value = false }, 2000)
}
</script>

<template>
  <main class="product-page">
    <div class="container">
      <!-- Breadcrumbs -->
      <div class="breadcrumbs">
        <router-link to="/" class="breadcrumbs__link">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M1 6L7 1L13 6V12C13 12.55 12.55 13 12 13H9V9H5V13H2C1.45 13 1 12.55 1 12V6Z" stroke="#999" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </router-link>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__item breadcrumbs__item--active">{{ product?.name ?? 'Товар' }}</span>
      </div>

      <h1 class="product-title">{{ product?.name ?? '...' }}</h1>

      <!-- Loading skeleton -->
      <div v-if="loading" class="product-detail product-detail--loading">
        <div class="product-image product-image--skeleton"></div>
        <div class="product-specs product-specs--skeleton"></div>
        <div class="product-buy product-buy--skeleton"></div>
      </div>

      <!-- Not found -->
      <div v-else-if="!product" class="product-notfound">
        Товар не найден
      </div>

      <div v-else class="product-detail">
        <!-- Image gallery -->
        <div class="product-gallery">
          <div class="product-image">
            <img :src="imageUrl" :alt="product.name" />
            <template v-if="product.imageUrls.length > 1">
              <button class="gallery-arrow gallery-arrow--left" @click.stop="prevImage" aria-label="Предыдущее фото">&#8249;</button>
              <button class="gallery-arrow gallery-arrow--right" @click.stop="nextImage" aria-label="Следующее фото">&#8250;</button>
              <div class="gallery-dots">
                <span
                  v-for="(_, i) in product.imageUrls"
                  :key="i"
                  class="gallery-dot"
                  :class="{ 'gallery-dot--active': activeImage === i }"
                  @click.stop="activeImage = i"
                ></span>
              </div>
            </template>
          </div>
          <div v-if="product.imageUrls.length > 1" class="product-thumbnails">
            <button
              v-for="(url, i) in product.imageUrls"
              :key="i"
              class="product-thumbnail"
              :class="{ 'product-thumbnail--active': activeImage === i }"
              @click="activeImage = i"
            >
              <img :src="url" :alt="`Фото ${i + 1}`" />
            </button>
          </div>
        </div>

        <!-- Specifications -->
        <div class="product-specs">
          <h3 class="product-specs__title">Характеристики</h3>
          <template v-if="Object.keys(product.characteristics).length">
            <div v-for="(value, key) in product.characteristics" :key="key" class="spec-row">
              <span class="spec-row__label">{{ key }}</span>
              <span class="spec-row__dots"></span>
              <span class="spec-row__value">{{ value }}</span>
            </div>
          </template>
          <p v-else class="spec-empty">Характеристики не указаны</p>
        </div>

        <!-- Buy Block -->
        <div class="product-buy">
          <div class="product-buy__price">{{ formattedPrice }}</div>
          <div class="product-buy__stock" :class="{ 'product-buy__stock--out': !isInStock }">
            {{ isInStock ? 'В наличии' : 'Нет в наличии' }}
          </div>
          <div class="product-buy__actions">
            <button
              v-if="isAdmin"
              class="btn-edit-product"
              @click="showEditModal = true"
            >Изменить товар</button>
            <button
              v-if="isInStock"
              class="btn-add-to-cart"
              :class="{ 'btn-add-to-cart--adding': isAdding }"
              @click="handleAddToCart"
              :disabled="isAdding"
            >
              <span v-if="isAdding">✓ Добавлено</span>
              <span v-else>В корзину</span>
            </button>
            <div v-if="isInStock" class="quantity-selector">
              <button class="quantity-btn quantity-btn--minus" @click="quantity > 1 && quantity--" :disabled="quantity <= 1">−</button>
              <input
                type="number"
                class="quantity-input"
                v-model.number="quantity"
                min="1"
                :max="maxStock"
                @blur="clampQuantity"
                @keydown.enter.prevent="clampQuantity"
              />
              <button class="quantity-btn quantity-btn--plus" @click="quantity < maxStock && quantity++" :disabled="quantity >= maxStock">+</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div v-if="product" class="product-description">
        <div class="product-description__header">Описание</div>
        <div class="product-description__content">
          {{ product.description || '—' }}
        </div>
      </div>
    </div>

    <AdminProductModal
      v-if="showEditModal && product"
      :product="product"
      @close="showEditModal = false"
      @saved="showEditModal = false; fetchProduct()"
      @deleted="showEditModal = false; $router.push('/')"
    />
  </main>
</template>

<style lang="scss" scoped>
.product-page { @include page-layout; }

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

.product-title {
  font-size: $font-xl;
  font-weight: 700;
  color: $color-dark;
  margin: 0 0 $gap-lg;

  @include below-sm { font-size: $font-lg; margin-bottom: $gap-md; }
}

.product-detail {
  display: flex;
  gap: $gap-lg;
  margin-bottom: $gap-lg;
  align-items: flex-start;

  @include below-lg { flex-direction: column; }

  &--loading {
    .product-image--skeleton,
    .product-specs--skeleton,
    .product-buy--skeleton {
      @include skeleton;
      border-radius: $radius-md;
    }
    .product-image--skeleton { width: 520px; max-width: 100%; height: 380px; flex-shrink: 0; }
    .product-specs--skeleton { flex: 1; height: 120px; }
    .product-buy--skeleton   { width: 260px; height: 140px; }

    @include below-lg {
      .product-image--skeleton { width: 100%; height: 280px; }
      .product-buy--skeleton   { width: 100%; }
    }
  }
}

.product-notfound {
  text-align: center;
  padding: 60px;
  color: $color-text-faint;
  font-size: $font-lg;
  background: #fff;
  border-radius: $radius-md;
}

// Gallery
.product-gallery {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;

  @include below-lg { width: 100%; }
}

.product-image {
  position: relative;
  width: 520px;
  height: 380px;
  background: $color-border;
  border-radius: $radius-sm;
  overflow: hidden;

  img { width: 100%; height: 100%; object-fit: cover; display: block; }
  &:hover .gallery-arrow { opacity: 1; }

  @include below-lg  { width: 100%; height: 300px; }
  @include below-md  { height: 260px; }
  @include below-sm  { height: 220px; }
}

.gallery-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  border: none;
  border-radius: $radius-full;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  @include flex-center;
  opacity: 0;
  transition: opacity 0.2s, background 0.2s;
  z-index: 2;

  &--left  { left: 10px; }
  &--right { right: 10px; }
  &:hover  { background: rgba(0, 0, 0, 0.7); }

  @include below-md {
    opacity: 0.8;
    width: 40px;
    height: 40px;
    font-size: 24px;
  }
}

.gallery-dots {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 2;
}

.gallery-dot {
  width: 7px;
  height: 7px;
  border-radius: $radius-full;
  background: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
  &--active { background: #fff; transform: scale(1.25); }
  &:hover   { background: rgba(255, 255, 255, 0.85); }

  @include below-md {
    width: 10px;
    height: 10px;
    padding: 8px;
    background-clip: content-box;
  }
}

.product-thumbnails {
  display: flex;
  gap: $gap-sm;
  flex-wrap: wrap;
}

.product-thumbnail {
  width: 64px;
  height: 64px;
  border: 2px solid $color-border;
  border-radius: $radius-sm;
  overflow: hidden;
  cursor: pointer;
  padding: 0;
  background: none;
  transition: border-color 0.15s;
  img { width: 100%; height: 100%; object-fit: cover; display: block; }
  &--active, &:hover { border-color: $color-primary; }
}

// Specs
.product-specs {
  flex: 1;
  min-width: 0;
  &__title { font-size: $font-md; font-weight: 600; color: $color-dark; margin: 0 0 10px; }
}

.spec-row {
  display: flex;
  align-items: baseline;
  gap: $gap-xs;
  margin-bottom: 6px;
  font-size: $font-base;

  &__label { color: $color-text; flex-shrink: 0; }
  &__dots {
    flex: 1;
    border-bottom: 1px dotted $color-text-faint;
    min-width: 20px;
    margin: 0 $gap-xs;
    position: relative;
    top: -3px;
  }
  &__value { color: $color-dark; font-weight: 500; flex-shrink: 0; }
}

.spec-empty { font-size: $font-base; color: $color-text-faint; margin: 0; }

// Buy Block
.product-buy {
  @include card;
  padding: $gap-md;
  flex-shrink: 0;
  width: 260px;

  @include below-lg { width: 100%; }

  &__price { font-size: $font-xl; font-weight: 700; color: $color-dark; margin-bottom: $gap-xs; }

  &__stock {
    font-size: $font-base;
    color: $color-success;
    font-weight: 500;
    margin-bottom: 10px;
    &--out { color: $color-danger; }
  }

  &__actions { display: flex; flex-direction: column; gap: $gap-sm; }
}

.btn-edit-product {
  padding: $gap-sm $gap-md;
  background: #fff;
  color: $color-primary;
  border: 1px solid $color-primary;
  border-radius: $radius-md;
  font-size: $font-base;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
  &:hover { background: $color-primary-light; }
}

.btn-add-to-cart {
  padding: $gap-sm $gap-md;
  background: $color-primary;
  color: $color-dark;
  border: none;
  border-radius: $radius-md;
  font-size: $font-base;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  &:hover:not(:disabled) { background: $color-primary-dark; }
  &:disabled { opacity: 0.7; cursor: not-allowed; }
  &--adding { background: $color-success-light; color: #fff; }
}

.quantity-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg-light;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $gap-xs;
}

.quantity-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #fff;
  border-radius: $radius-sm;
  font-size: $font-md;
  cursor: pointer;
  @include flex-center;
  transition: all 0.2s;
  &:hover:not(:disabled) { background: $color-success-light; color: #fff; }
  &:disabled { opacity: 0.3; cursor: not-allowed; }
  &--minus { color: $color-danger; }
  &--plus  { color: $color-success; }
}

.quantity-input {
  font-size: $font-sm;
  font-weight: 500;
  width: 52px;
  text-align: center;
  color: $color-text;
  border: none;
  background: transparent;
  outline: none;
  -moz-appearance: textfield;
  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
}

// Description
.product-description {
  background: $color-bg;
  border: 1px solid #ccc;
  border-radius: $radius-md;
  overflow: hidden;

  &__header {
    background: #fff;
    border: 1px solid #ccc;
    border-bottom: none;
    border-radius: $radius-md $radius-md 0 0;
    padding: $gap-sm 14px;
    font-size: $font-base;
    font-weight: 600;
    color: $color-dark;
  }

  &__content {
    padding: 14px;
    font-size: $font-base;
    color: $color-text;
    min-height: 200px;
    line-height: 1.6;
    white-space: pre-wrap;
    @include below-sm { min-height: 100px; }
  }
}
</style>
