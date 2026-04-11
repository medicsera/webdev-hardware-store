<script setup lang="ts">
import { ref, computed, watch } from 'vue'
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
  addToCart: [product: Product]
  quantityChange: [productId: number, quantity: number]
}>()

const quantity = ref(1)

watch(() => props.product, (newProduct) => {
  if (newProduct) {
    quantity.value = newProduct.quantity || 1
  }
}, { immediate: true })

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
  return props.product?.image || '/placeholder-product.jpg'
})

const isInStock = computed(() => {
  return props.product?.inStock !== false
})

const productName = computed(() => {
  return props.product?.name || 'Загрузка...'
})

const handleAddToCart = () => {
  if (!props.product || isAdding.value) return

  isAdding.value = true
  emit('addToCart', { ...props.product, quantity: quantity.value })

  setTimeout(() => {
    isAdding.value = false
  }, 2000)
}

const increment = () => {
  if (!props.product) return
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

    <div class="product-info">
      <h3 class="product-info__title">
        {{ productName }}
      </h3>

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
          <span class="quantity-value">{{ quantity }} шт.</span>
          <button
              class="quantity-btn quantity-btn--plus"
              @click="increment"
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
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    transform: translateY(-4px);
  }

  &--loading {
    opacity: 1;
    min-height: 380px;
    pointer-events: none;
  }

  &--out-of-stock {
    opacity: 0.7;
  }
}

.product-image {
  position: relative;
  width: 100%;
  background: #f5f5f5;
  aspect-ratio: 1 / 1;
  overflow: hidden;

  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  &:hover &__img {
    transform: scale(1.05);
  }

  &__overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;

    .out-of-stock {
      color: white;
      font-weight: 600;
      font-size: 1.125rem;
      text-align: center;
      padding: 1rem;
    }
  }
}

.product-info {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 0.75rem;

  &__title {
    font-size: 1.125rem;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
    line-height: 1.3;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__price {
    margin-top: auto;
    padding-top: 0.5rem;
  }

  .price {
    font-size: 1.25rem;
    font-weight: 700;
    color: #42b983;
  }

  &__actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 0.75rem;
  }
}

.btn-add-to-cart {
  flex: 1;
  padding: 0.75rem 1rem;
  background: #f4b942;
  color: #2c3e50;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #e0a830;
    transform: translateY(-2px);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
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
  background: #f5f5f5;
  padding: 0.5rem;
  border-radius: 8px;
}

.quantity-btn {
  width: 20px;
  height: 32px;
  border: none;
  background: white;
  border-radius: 6px;
  font-size: 1.1rem;
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
  font-weight: 500;
  min-width: 50px;
  text-align: center;
}

.skeleton {
  background: linear-gradient(
          90deg,
          #f0f0f0 25%,
          #e0e0e0 50%,
          #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;

  &--image {
    width: 100%;
    height: 100%;
  }

  &--title {
    height: 24px;
    width: 80%;
  }

  &--price {
    height: 28px;
    width: 50%;
  }
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>