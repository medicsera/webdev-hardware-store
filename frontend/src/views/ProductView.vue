<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import type { Product } from '@/types/product'

const route = useRoute()
const cartStore = useCartStore()

const loading = ref(true)
const quantity = ref(1)
const isAdding = ref(false)

const product = ref<Product | null>(null)

const mockProduct: Product = {
  id: 1,
  name: 'Товар',
  price: 100,
  inStock: true,
  image: '/placeholder-product.jpg',
  description: 'Текст описания',
  specifications: {
    'Размер': '2x2x2',
    'Вес': '2кг.'
  }
}

const formattedPrice = computed(() => {
  if (!product.value) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 2,
  }).format(product.value.price)
})

const imageUrl = computed(() => product.value?.image || '/placeholder-product.jpg')

const fetchProduct = async () => {
  loading.value = true
  try {
    // const data = await productService.getProductById(Number(route.params.id))
    // product.value = data
    await new Promise(r => setTimeout(r, 300))
    product.value = mockProduct
  } catch {
    product.value = mockProduct
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    fetchProduct()
  }
)

const increment = () => {
  quantity.value++
}

const decrement = () => {
  if (quantity.value <= 1) return
  quantity.value--
}

const handleAddToCart = () => {
  if (!product.value || isAdding.value) return
  isAdding.value = true
  cartStore.addToCart({ ...product.value, quantity: quantity.value })
  setTimeout(() => {
    isAdding.value = false
  }, 2000)
}

onMounted(() => {
  fetchProduct()
})
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
        <span class="breadcrumbs__item">Категория</span>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__item breadcrumbs__item--active">Товар</span>
      </div>

      <h1 class="product-title">Товар</h1>

      <!-- Product Detail -->
      <div v-if="loading" class="product-detail product-detail--loading">
        <div class="product-image product-image--skeleton"></div>
        <div class="product-specs product-specs--skeleton"></div>
        <div class="product-buy product-buy--skeleton"></div>
      </div>

      <div v-else class="product-detail">
        <!-- Image -->
        <div class="product-image">
          <img :src="imageUrl" :alt="product?.name" />
        </div>

        <!-- Specifications -->
        <div class="product-specs">
          <h3 class="product-specs__title">Характеристики</h3>
          <div
            v-for="(value, key) in product?.specifications"
            :key="key"
            class="spec-row"
          >
            <span class="spec-row__label">{{ key }}</span>
            <span class="spec-row__dots"></span>
            <span class="spec-row__value">{{ value }}</span>
          </div>
        </div>

        <!-- Buy Block -->
        <div class="product-buy">
          <div class="product-buy__price">{{ formattedPrice }}</div>
          <div class="product-buy__stock" :class="{ 'product-buy__stock--out': !product?.inStock }">
            {{ product?.inStock ? 'В наличии' : 'Нет в наличии' }}
          </div>
          <div class="product-buy__actions">
            <button
              v-if="product?.inStock"
              class="btn-add-to-cart"
              :class="{ 'btn-add-to-cart--adding': isAdding }"
              @click="handleAddToCart"
              :disabled="isAdding"
            >
              <span v-if="isAdding">✓ Добавлено</span>
              <span v-else>В корзину</span>
            </button>
            <div v-if="product?.inStock" class="quantity-selector">
              <button class="quantity-btn quantity-btn--minus" @click="decrement" :disabled="quantity <= 1">−</button>
              <span class="quantity-value">{{ quantity }} шт.</span>
              <button class="quantity-btn quantity-btn--plus" @click="increment">+</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div class="product-description">
        <div class="product-description__header">Описание</div>
        <div class="product-description__content">
          {{ product?.description || 'Текст описания' }}
        </div>
      </div>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.product-page {
  background: #f0f0f0;
  min-height: calc(100vh - 110px);
  padding: 20px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

// Breadcrumbs
.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;

  &__link {
    display: flex;
    align-items: center;
    color: #999;
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: #f4b942;
    }
  }

  &__separator {
    color: #ccc;
    font-size: 12px;
  }

  &__item {
    color: #666;

    &--active {
      color: #2c3e50;
      font-weight: 600;
    }
  }
}

.product-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 24px;
}

// Product Detail Layout
.product-detail {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  align-items: flex-start;

  &--loading {
    .product-image--skeleton,
    .product-specs--skeleton,
    .product-buy--skeleton {
      background: linear-gradient(90deg, #e0e0e0 25%, #d0d0d0 50%, #e0e0e0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s infinite;
      border-radius: 8px;
    }

    .product-image--skeleton {
      width: 520px;
      height: 380px;
      flex-shrink: 0;
    }

    .product-specs--skeleton {
      flex: 1;
      height: 120px;
    }

    .product-buy--skeleton {
      width: 260px;
      height: 140px;
    }
  }
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

// Image
.product-image {
  width: 520px;
  height: 380px;
  flex-shrink: 0;
  background: #ddd;
  border-radius: 4px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

// Specifications
.product-specs {
  flex: 1;
  min-width: 0;

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 10px;
  }
}

.spec-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 6px;
  font-size: 13px;

  &__label {
    color: #555;
    flex-shrink: 0;
  }

  &__dots {
    flex: 1;
    border-bottom: 1px dotted #aaa;
    min-width: 20px;
    margin: 0 4px;
    position: relative;
    top: -3px;
  }

  &__value {
    color: #2c3e50;
    font-weight: 500;
    flex-shrink: 0;
  }
}

// Buy Block
.product-buy {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  flex-shrink: 0;
  width: 260px;

  &__price {
    font-size: 18px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 4px;
  }

  &__stock {
    font-size: 13px;
    color: #27ae60;
    font-weight: 500;
    margin-bottom: 10px;

    &--out {
      color: #e74c3c;
    }
  }

  &__actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.btn-add-to-cart {
  padding: 8px 16px;
  background: #f4b942;
  color: #2c3e50;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #e0a830;
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &--adding {
    background: #42b983;
    color: white;
  }
}

.quantity-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 4px;
}

.quantity-btn {
  width: 24px;
  height: 26px;
  border: none;
  background: white;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #42b983;
    color: white;
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  &--minus {
    color: #e74c3c;
  }

  &--plus {
    color: #27ae60;
  }
}

.quantity-value {
  font-size: 12px;
  font-weight: 500;
  min-width: 50px;
  text-align: center;
  color: #555;
}

// Description
.product-description {
  background: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 6px;
  overflow: hidden;

  &__header {
    background: white;
    border: 1px solid #ccc;
    border-bottom: none;
    border-radius: 6px 6px 0 0;
    padding: 8px 14px;
    font-size: 13px;
    font-weight: 600;
    color: #2c3e50;
  }

  &__content {
    padding: 14px;
    font-size: 13px;
    color: #555;
    min-height: 200px;
    line-height: 1.6;
  }
}
</style>
