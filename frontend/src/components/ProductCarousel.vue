<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ProductCard from './ProductCard.vue'
import { useCartStore } from '@/stores/cart'
import type { Product } from '@/types/product'

interface Props {
  title?: string
  products?: Product[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Рекомендации',
  products: () => [],
  loading: false,
})

const cartStore = useCartStore()
const carouselRef = ref<HTMLElement | null>(null)
const scrollPosition = ref(0) // 👈 Отслеживаем позицию скролла

const demoProducts: Product[] = Array.from({ length: 12 }, (_, i) => ({
  id: i + 1,
  name: `Товар ${i + 1}`,
  price: 100 * (i + 1),
  inStock: true,
  image: `/images/placeholder-product.jpg`
}))

const productsToShow = computed(() => {
  if (props.loading) return null
  if (props.products && props.products.length > 0) return props.products
  return demoProducts
})

// 👇 Используем scrollPosition для реактивности
const canScrollLeft = computed(() => {
  return scrollPosition.value > 0
})

const canScrollRight = computed(() => {
  const container = carouselRef.value
  if (!container) return false
  return scrollPosition.value < (container.scrollWidth - container.clientWidth - 10)
})

const scroll = (direction: 'left' | 'right') => {
  const container = carouselRef.value
  if (!container) return

  const scrollAmount = 256 + 16
  const targetScroll = direction === 'left'
      ? container.scrollLeft - scrollAmount
      : container.scrollLeft + scrollAmount

  container.scrollTo({
    left: targetScroll,
    behavior: 'smooth'
  })
}

const scrollLeft = () => {
  if (canScrollLeft.value) scroll('left')
}

const scrollRight = () => {
  if (canScrollRight.value) scroll('right')
}

const handleWheel = (e: WheelEvent) => {
  const container = carouselRef.value
  if (!container) return

  e.preventDefault()
  container.scrollLeft += e.deltaY
}

// 👈 Обновляем scrollPosition при скролле
const updateScrollPosition = () => {
  const container = carouselRef.value
  if (container) {
    scrollPosition.value = container.scrollLeft
  }
}

onMounted(() => {
  const container = carouselRef.value
  if (container) {
    // Начальная позиция
    updateScrollPosition()

    // Слушаем скролл
    container.addEventListener('scroll', updateScrollPosition)
  }
})

const handleAddToCart = (product: Product) => {
  cartStore.addToCart(product)
}
</script>

<template>
  <section class="product-carousel">
    <div class="product-carousel__header">
      <h2 class="product-carousel__title">{{ title }}</h2>
    </div>

    <div
        ref="carouselRef"
        class="product-carousel__container"
        @wheel.prevent="handleWheel"
    >
      <template v-if="productsToShow === null">
        <ProductCard
            v-for="n in 6"
            :key="`skeleton-${n}`"
            :product="undefined"
        />
      </template>

      <template v-else-if="productsToShow && productsToShow.length > 0">
        <ProductCard
            v-for="product in productsToShow"
            :key="product.id"
            :product="product"
            :show-add-to-cart="true"
            :show-quantity="false"
            @add-to-cart="handleAddToCart"
        />
      </template>

      <div v-else class="product-carousel__empty">
        <p>Товары не найдены</p>
      </div>
    </div>

    <div class="product-carousel__controls">
      <button
          class="carousel-btn carousel-btn--prev"
          :disabled="!canScrollLeft"
          @click="scrollLeft"
          aria-label="Предыдущие товары"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>

      <button
          class="carousel-btn carousel-btn--next"
          :disabled="!canScrollRight"
          @click="scrollRight"
          aria-label="Следующие товары"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.product-carousel {
  margin: 0 8.5% 24px;
  position: relative;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 4px;
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
  }

  &__controls {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 16px;
  }

  &__container {
    display: flex;
    gap: 16px;
    overflow-x: auto;
    overflow-y: hidden;
    scroll-snap-type: x mandatory;
    scroll-behavior: smooth;

    scrollbar-width: none;
    -ms-overflow-style: none;
    &::-webkit-scrollbar {
      display: none;
    }

    > * {
      flex-shrink: 0;
      scroll-snap-align: start;
    }
  }

  &__empty {
    width: 100%;
    text-align: center;
    padding: 40px 20px;
    color: #999;
    font-size: 16px;
  }
}

.carousel-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e0e0e0;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #2c3e50;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover:not(:disabled) {
    background: #f4b942;
    border-color: #f4b942;
    color: white;
    transform: scale(1.05);
  }

  &:active:not(:disabled) {
    transform: scale(0.95);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
    border-color: #f0f0f0;

    &:hover {
      background: white;
      transform: none;
    }
  }

  svg {
    width: 20px;
    height: 20px;
  }
}
</style>