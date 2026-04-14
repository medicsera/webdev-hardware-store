<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { Order } from '@/types/order'

const orders = ref<Order[]>([])
const loading = ref(true)
const carouselRef = ref<HTMLElement | null>(null)
const forceUpdate = ref(0)

// Mock data for demonstration
const mockOrders: Order[] = [
  {
    id: 1,
    items: [
      { id: 101, name: 'Дрель Bosch GSB 13 RE', price: 5490, image: '/placeholder-product.jpg', quantity: 1 },
      { id: 102, name: 'Набор бит 32 шт', price: 890, image: '/placeholder-product.jpg', quantity: 2 }
    ],
    total: 7270,
    date: '20.08.2025',
    time: '17:15',
    status: 'delivered'
  },
  {
    id: 2,
    items: [
      { id: 201, name: 'Шуруповёрт Makita DF333D', price: 8990, image: '/placeholder-product.jpg', quantity: 1 }
    ],
    total: 8990,
    date: '15.08.2025',
    time: '10:30',
    status: 'shipped'
  },
  {
    id: 3,
    items: [
      { id: 301, name: 'Перфоратор DeWalt D25133K', price: 12500, image: '/placeholder-product.jpg', quantity: 1 },
      { id: 302, name: 'Буры SDS-plus набор', price: 1200, image: '/placeholder-product.jpg', quantity: 1 },
      { id: 303, name: 'Пылесборник', price: 350, image: '/placeholder-product.jpg', quantity: 3 }
    ],
    total: 14750,
    date: '10.08.2025',
    time: '14:45',
    status: 'processing'
  }
]

const formattedPrice = (price: number): string => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 2,
  }).format(price)
}

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

  const scrollAmount = 212
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

const handleScroll = () => {
  forceUpdate.value++
}

onMounted(async () => {
  loading.value = true
  await new Promise(r => setTimeout(r, 300))
  orders.value = mockOrders
  loading.value = false
  forceUpdate.value++

  const container = carouselRef.value
  if (container) {
    container.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  const container = carouselRef.value
  if (container) {
    container.removeEventListener('scroll', handleScroll)
  }
})
</script>

<template>
  <div class="order-history">
    <div v-if="loading" class="order-history__loading">
      <div v-for="n in 3" :key="n" class="order-card order-card--skeleton">
        <div class="skeleton skeleton--image"></div>
        <div class="skeleton skeleton--title"></div>
        <div class="skeleton skeleton--price"></div>
      </div>
    </div>

    <div v-else-if="orders.length === 0" class="order-history__empty">
      <p>У вас пока нет заказов</p>
      <router-link to="/catalog" class="order-history__link">Перейти в каталог</router-link>
    </div>

    <template v-else>
      <div
        ref="carouselRef"
        class="order-history__carousel"
      >
        <div
          v-for="order in orders"
          :key="order.id"
          class="order-card"
        >
          <div class="order-card__image-placeholder"></div>

          <div class="order-card__info">
            <h4 class="order-card__name">{{ order.items[0]?.name }}</h4>
            <p class="order-card__price">{{ formattedPrice(order.total) }}</p>
            <p class="order-card__date">Дата: {{ order.date }}</p>
            <p class="order-card__time">Время: {{ order.time }}</p>
          </div>
        </div>
      </div>

      <div class="order-history__controls">
        <button
          class="carousel-btn carousel-btn--prev"
          :disabled="!canScrollLeft"
          @click="scrollLeft"
          aria-label="Предыдущие заказы"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>

        <button
          class="carousel-btn carousel-btn--next"
          :disabled="!canScrollRight"
          @click="scrollRight"
          aria-label="Следующие заказы"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.order-history {
  &__loading {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  &__empty {
    text-align: center;
    padding: 40px;
    color: #999;

    p {
      margin: 0 0 16px;
      font-size: 15px;
    }
  }

  &__link {
    display: inline-block;
    padding: 10px 28px;
    background: #f4b942;
    color: #2c3e50;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    transition: background 0.2s;

    &:hover {
      background: #e0a830;
    }
  }

  &__carousel {
    display: flex;
    gap: 16px;
    overflow-x: auto;
    overflow-y: hidden;
    scroll-snap-type: x mandatory;
    scroll-behavior: smooth;
    padding-bottom: 8px;
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

  &__controls {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 16px;
  }
}

.order-card {
  flex-shrink: 0;
  width: 180px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &--skeleton {
    pointer-events: none;

    .skeleton {
      background: linear-gradient(90deg, #e0e0e0 25%, #d0d0d0 50%, #e0e0e0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s infinite;
      border-radius: 4px;

      &--image {
        width: 100%;
        height: 120px;
      }

      &--title {
        height: 14px;
        width: 80%;
        margin: 12px;
      }

      &--price {
        height: 16px;
        width: 50%;
        margin: 0 12px 12px;
      }
    }
  }

  &__image-placeholder {
    width: 100%;
    height: 120px;
    background: #d5d5d5;
  }

  &__info {
    padding: 10px 12px;
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  &__name {
    font-size: 12px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__price {
    font-size: 13px;
    font-weight: 700;
    color: #333;
    margin: 2px 0 0;
  }

  &__date,
  &__time {
    font-size: 11px;
    color: #333;
    margin: 0;
  }
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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
