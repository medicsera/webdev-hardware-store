<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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

const forceUpdate = ref(0)

const demoProducts: Product[] = Array.from({ length: 12 }, (_, i) => ({
  id: i + 1,
  name: `Товар ${i + 1}`,
  price: 100 * (i + 1),
  quantity: 1,
  imageUrls: [],
  characteristics: {}
}))

const productsToShow = computed(() => {
  if (props.loading) return null
  if (props.products && props.products.length > 0) return props.products
  return demoProducts
})

const canScrollLeft = computed(() => {
  forceUpdate.value
  const container = carouselRef.value
  return container ? container.scrollLeft > 0 : false
})

const canScrollRight = computed(() => {
  forceUpdate.value
  const container = carouselRef.value
  if (!container) return false
  return container.scrollLeft < (container.scrollWidth - container.clientWidth - 10)
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

  setTimeout(() => {
    forceUpdate.value++
  }, 300)
}

const scrollLeft = () => { if (canScrollLeft.value) scroll('left') }
const scrollRight = () => { if (canScrollRight.value) scroll('right') }

const handleWheel = (e: WheelEvent) => {
  const container = carouselRef.value
  if (!container) return

  container.scrollLeft += e.deltaY
  forceUpdate.value++
}

const handleScroll = () => {
  forceUpdate.value++
}

onMounted(() => {
  const container = carouselRef.value
  if (container) {
    container.addEventListener('scroll', handleScroll)
    forceUpdate.value++
  }
})

onUnmounted(() => {
  const container = carouselRef.value
  if (container) {
    container.removeEventListener('scroll', handleScroll)
  }
})

const handleAddToCart = (product: Product, qty: number) => {
  cartStore.addToCart(product, qty)
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
        @wheel="handleWheel"
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
            :show-quantity="true"
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
  margin: 0 8.5% $gap-lg;
  position: relative;

  @include below-md { margin: 0 $container-pad $gap-lg; }
  @include below-sm { margin: 0 $gap-sm $gap-md; }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $gap-md;
    padding: 0 4px;
  }

  &__title {
    font-size: $font-3xl;
    font-weight: 700;
    color: $color-dark;
    margin: 0;

    @include below-sm { font-size: $font-xl; }
  }

  &__controls {
    display: flex;
    justify-content: center;
    gap: $gap-sm;
    margin-top: $gap-md;
  }

  &__container {
    display: flex;
    gap: $gap-md;
    overflow-x: auto;
    overflow-y: hidden;
    scroll-snap-type: x mandatory;
    scroll-behavior: smooth;
    padding-bottom: $gap-sm;

    scrollbar-width: none;
    -ms-overflow-style: none;
    &::-webkit-scrollbar { display: none; }

    > * {
      flex-shrink: 0;
      scroll-snap-align: start;
    }
  }

  &__empty {
    width: 100%;
    text-align: center;
    padding: $gap-xl $container-pad;
    color: $color-text-muted;
    font-size: $font-lg;
  }
}

.carousel-btn {
  width: 40px;
  height: 40px;
  border-radius: $radius-full;
  border: 2px solid $color-border;
  background: #fff;
  cursor: pointer;
  @include flex-center;
  transition: all 0.2s;
  color: $color-dark;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover:not(:disabled) {
    background: $color-primary;
    border-color: $color-primary;
    color: #fff;
    transform: scale(1.05);
  }

  &:active:not(:disabled) { transform: scale(0.95); }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
    border-color: $color-bg;
    &:hover { background: #fff; transform: none; }
  }

  svg { width: 20px; height: 20px; }
}
</style>