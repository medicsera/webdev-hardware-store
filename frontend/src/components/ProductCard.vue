<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Product } from '@/types/product'

interface Props {
  product?: Product
  showAddToCart?: boolean
  showQuantity?: boolean
  compact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  product: undefined,
  showAddToCart: true,
  showQuantity: false,
  compact: false,
})

const emit = defineEmits<{
  addToCart: [product: Product, quantity: number]
  quantityChange: [productId: number, quantity: number]
}>()

const quantity = ref(1)

const isAdding = ref(false)

const formattedPrice = computed(() => {
  if (!props.product) return '0 ₽'

  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 2,
  }).format(props.product.price)
})

const imageUrl = computed(() => {
  const urls = props.product?.imageUrls
  return (urls && urls.length > 0) ? urls[0] : '/placeholder-product.jpg'
})

const isInStock = computed(() => {
  return (props.product?.quantity ?? 1) > 0
})

const productName = computed(() => {
  return props.product?.name || 'Загрузка...'
})

const maxStock = computed(() => props.product?.quantity ?? Infinity)

const clampQuantity = () => {
  if (!Number.isInteger(quantity.value) || quantity.value < 1) quantity.value = 1
  else if (quantity.value > maxStock.value) quantity.value = maxStock.value as number
}

const handleAddToCart = () => {
  if (!props.product || isAdding.value) return
  clampQuantity()
  isAdding.value = true
  emit('addToCart', props.product, quantity.value)
  setTimeout(() => { isAdding.value = false }, 2000)
}

const increment = () => {
  if (!props.product || quantity.value >= maxStock.value) return
  quantity.value++
  emit('quantityChange', props.product.id, quantity.value)
}

const decrement = () => {
  if (!props.product || quantity.value <= 1) return
  quantity.value--
  emit('quantityChange', props.product.id, quantity.value)
}
</script>

<template>
  <div v-if="!product" class="product-card product-card--loading">
    <div class="product-image">
      <div class="skeleton skeleton--image"></div>
    </div>
    <div class="product-info">
      <div class="skeleton skeleton--title"></div>
      <div class="skeleton skeleton--price"></div>
    </div>
  </div>

  <article
      v-else
      class="product-card"
      :class="{
      'product-card--compact': compact,
      'product-card--out-of-stock': !isInStock
    }"
  >
    <router-link :to="`/product/${product.id}`" class="product-image-link">
      <div class="product-image">
        <img
            :src="imageUrl"
            :alt="productName"
            class="product-image__img"
            loading="lazy"
        />
        <div v-if="!isInStock" class="product-image__overlay">
          <span class="out-of-stock">Нет в наличии</span>
        </div>
      </div>
    </router-link>

    <div class="product-info">
      <router-link :to="`/product/${product.id}`" class="product-info__title-link">
        <h3 class="product-info__title">
          {{ productName }}
        </h3>
      </router-link>

      <div class="product-info__price">
        <span class="price">{{ formattedPrice }}</span>
      </div>

      <div class="product-info__actions">
        <button
            v-if="showAddToCart && isInStock"
            class="btn-add-to-cart"
            :class="{ 'btn-add-to-cart--adding': isAdding }"
            @click="handleAddToCart"
            :disabled="isAdding"
        >
          <span v-if="isAdding">✓ Добавлено</span>
          <span v-else>В корзину</span>
        </button>

        <div v-if="showQuantity && isInStock" class="quantity-selector">
          <button
              class="quantity-btn quantity-btn--minus"
              @click="decrement"
              :disabled="quantity <= 1"
          >
            −
          </button>
          <input
              type="number"
              class="quantity-input"
              v-model.number="quantity"
              min="1"
              @blur="clampQuantity"
              @keydown.enter.prevent="clampQuantity"
          />
          <button
              class="quantity-btn quantity-btn--plus"
              @click="increment"
              :disabled="quantity >= maxStock"
          >
            +
          </button>
        </div>
      </div>
    </div>
  </article>
</template>

<style lang="scss" scoped>
.product-card {
  width: 242px;
  min-height: 401px;
  background: #fff;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  @include below-sm {
    width: 148px;
    min-height: unset;
  }

  &:hover {
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    transform: translateY(-4px);
  }

  &--loading {
    min-height: 380px;
    pointer-events: none;
    @include below-sm { min-height: 220px; }
  }

  &--out-of-stock { opacity: 0.7; }
}

.product-image-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.product-image {
  position: relative;
  width: 100%;
  background: $color-bg-light;
  aspect-ratio: 1 / 1;
  overflow: hidden;

  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  &:hover &__img { transform: scale(1.05); }

  &__overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    @include flex-center;

    .out-of-stock {
      color: #fff;
      font-weight: 600;
      font-size: $font-lg;
      text-align: center;
      padding: $gap-md;
    }
  }
}

.product-info {
  padding: $gap-md;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 12px;

  &__title-link {
    text-decoration: none;
    color: inherit;
    display: block;

    &:hover .product-info__title { color: $color-primary; }
  }

  &__title {
    font-size: $font-lg;
    font-weight: 600;
    color: $color-dark;
    margin: 0;
    line-height: 1.3;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    transition: color 0.2s;

    @include below-sm { font-size: $font-base; }
  }

  &__price {
    margin-top: auto;
    padding-top: $gap-sm;
  }

  .price {
    font-size: $font-xl;
    font-weight: 700;
    color: $color-success-light;

    @include below-sm { font-size: $font-md; }
  }

  &__actions {
    display: flex;
    gap: 12px;
    margin-top: 12px;

    @include below-sm {
      flex-direction: column;
      gap: 10px;
    }
  }
}

.btn-add-to-cart {
  flex: 1;
  padding: 12px $gap-md;
  background: $color-primary;

  @include below-sm { padding: 12px; font-size: $font-md; }
  color: $color-dark;
  border: none;
  border-radius: $radius-md;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) { background: $color-primary-dark; transform: translateY(-2px); }
  &:active:not(:disabled) { transform: translateY(0); }
  &:disabled { opacity: 0.7; cursor: not-allowed; }
  &--adding { background: $color-success-light; color: #fff; }
}

.quantity-selector {
  display: flex;
  align-items: center;
  background: $color-bg-light;
  padding: $gap-sm;
  border-radius: $radius-md;

  @include below-sm { justify-content: center; }
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
  font-weight: 500;
  width: 44px;
  text-align: center;
  border: none;
  background: transparent;
  outline: none;
  -moz-appearance: textfield;
  font-size: $font-sm;
  color: $color-text;

  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
}

.skeleton {
  @include skeleton;
  border-radius: $radius-sm;

  &--image { width: 100%; height: 100%; border-radius: 0; }
  &--title { height: 24px; width: 80%; }
  &--price { height: 28px; width: 50%; }
}
</style>