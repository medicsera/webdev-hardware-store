<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore, type CartItem } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const loading     = ref(true)
const isSubmitting = ref(false)

const deliveryCost          = 350
const freeDeliveryThreshold = 5000

const cartItems = computed(() => cartStore.items)

const subtotal = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.cartQuantity, 0)
)

const isFreeDelivery = computed(() => subtotal.value >= freeDeliveryThreshold)
const delivery = computed(() => cartItems.value.length > 0 ? (isFreeDelivery.value ? 0 : deliveryCost) : 0)
const total    = computed(() => subtotal.value + delivery.value)

const formattedSubtotal = computed(() => formatPrice(subtotal.value))
const formattedDelivery = computed(() => isFreeDelivery.value ? 'Бесплатно' : formatPrice(delivery.value))
const formattedTotal    = computed(() => formatPrice(total.value))

function formatPrice(price: number): string {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 2 }).format(price)
}

function incrementQuantity(item: CartItem) {
  cartStore.updateQuantity(item.id, item.cartQuantity + 1)
}

function decrementQuantity(item: CartItem) {
  if (item.cartQuantity <= 1) return
  cartStore.updateQuantity(item.id, item.cartQuantity - 1)
}

function removeItem(item: CartItem) {
  cartStore.removeFromCart(item.id)
}

const handleCheckout = async () => {
  if (isSubmitting.value || cartItems.value.length === 0) return
  isSubmitting.value = true
  try {
    await new Promise(r => setTimeout(r, 1000))
    cartStore.clearCart()
    router.push('/')
  } catch {
    isSubmitting.value = false
  }
}

onMounted(() => { loading.value = false })
</script>

<template>
  <main class="cart-page">
    <div class="container">
      <div class="breadcrumbs">
        <router-link to="/" class="breadcrumbs__link">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M1 6L7 1L13 6V12C13 12.55 12.55 13 12 13H9V9H5V13H2C1.45 13 1 12.55 1 12V6Z" stroke="#999" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </router-link>
        <span class="breadcrumbs__separator">/</span>
        <span class="breadcrumbs__item breadcrumbs__item--active">Корзина</span>
      </div>

      <h1 class="cart-title">Корзина</h1>

      <!-- Empty cart -->
      <div v-if="cartItems.length === 0" class="cart-empty">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="38" stroke="#ddd" stroke-width="2"/>
          <path d="M25 30H60L55 50H30L25 30Z" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M30 30L28 22H20" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="35" cy="56" r="3" fill="#ccc"/>
          <circle cx="50" cy="56" r="3" fill="#ccc"/>
        </svg>
        <h2 class="cart-empty__title">Корзина пуста</h2>
        <p class="cart-empty__text">Добавьте товары из каталога</p>
        <router-link to="/catalog" class="btn-go-to-catalog">Перейти в каталог</router-link>
      </div>

      <!-- Cart content -->
      <div v-else class="cart-layout">
        <div class="cart-items">
          <div class="cart-items__header">
            <span class="cart-items__count">{{ cartItems.length }} {{ cartItems.length === 1 ? 'товар' : 'товаров' }}</span>
            <button class="btn-clear-cart" @click="cartStore.clearCart()">Очистить корзину</button>
          </div>

          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <router-link :to="`/product/${item.id}`" class="cart-item__image-link">
              <img
                :src="item.imageUrls?.[0] || '/placeholder-product.jpg'"
                :alt="item.name"
                class="cart-item__image"
              />
            </router-link>

            <div class="cart-item__info">
              <router-link :to="`/product/${item.id}`" class="cart-item__name">{{ item.name }}</router-link>
              <span class="cart-item__article">Арт: {{ item.id.toString().padStart(6, '0') }}</span>
            </div>

            <div class="cart-item__quantity">
              <button class="quantity-btn quantity-btn--minus" @click="decrementQuantity(item)" :disabled="item.cartQuantity <= 1">−</button>
              <span class="quantity-value">{{ item.cartQuantity }}</span>
              <button class="quantity-btn quantity-btn--plus" @click="incrementQuantity(item)">+</button>
            </div>

            <div class="cart-item__price">
              <span class="cart-item__price-label">Цена:</span>
              {{ formatPrice(item.price) }}
            </div>

            <div class="cart-item__subtotal">
              <span class="cart-item__price-label">Итого:</span>
              {{ formatPrice(item.price * item.cartQuantity) }}
            </div>

            <button class="cart-item__remove" @click="removeItem(item)" title="Удалить">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M4 4L14 14M14 4L4 14" stroke="#999" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Summary -->
        <div class="cart-summary">
          <h3 class="cart-summary__title">Итого</h3>

          <div class="cart-summary__row">
            <span>Товары ({{ cartItems.length }})</span>
            <span>{{ formattedSubtotal }}</span>
          </div>

          <div class="cart-summary__row">
            <span>Доставка</span>
            <span :class="{ 'cart-summary__free': isFreeDelivery }">{{ formattedDelivery }}</span>
          </div>

          <div v-if="!isFreeDelivery && cartItems.length > 0" class="cart-summary__hint">
            До бесплатной доставки ещё {{ formatPrice(freeDeliveryThreshold - subtotal) }}
          </div>

          <div class="cart-summary__divider"></div>

          <div class="cart-summary__row cart-summary__row--total">
            <span>К оплате</span>
            <span>{{ formattedTotal }}</span>
          </div>

          <button
            class="btn-checkout"
            :class="{ 'btn-checkout--submitting': isSubmitting }"
            :disabled="isSubmitting || cartItems.length === 0"
            @click="handleCheckout"
          >
            <span v-if="isSubmitting">Оформление...</span>
            <span v-else>Оформить заказ</span>
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.cart-page {
  background: #f0f0f0;
  min-height: calc(100vh - 110px);
  padding: 20px 0 40px;
}

.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;

  &__link { display: flex; align-items: center; color: #999; text-decoration: none; &:hover { color: #f4b942; } }
  &__separator { color: #ccc; font-size: 12px; }
  &__item { color: #666; &--active { color: #2c3e50; font-weight: 600; } }
}

.cart-title { font-size: 18px; font-weight: 700; color: #2c3e50; margin: 0 0 24px; }

.cart-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.cart-empty {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  svg { margin-bottom: 20px; }
  &__title { font-size: 20px; font-weight: 700; color: #2c3e50; margin: 0 0 8px; }
  &__text { font-size: 14px; color: #999; margin: 0 0 24px; }
}

.btn-go-to-catalog {
  display: inline-block;
  padding: 10px 28px;
  background: #f4b942;
  color: #2c3e50;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  &:hover { background: #e0a830; }
}

.cart-items {
  flex: 1;
  min-width: 0;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    padding: 0 4px;
  }

  &__count { font-size: 13px; color: #666; }
}

.btn-clear-cart {
  background: none;
  border: none;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  padding: 4px 0;
  &:hover { color: #e74c3c; }
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;

  &__image-link { display: block; flex-shrink: 0; text-decoration: none; }
  &__image { width: 80px; height: 80px; object-fit: cover; border-radius: 4px; }

  &__info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__name {
    font-size: 14px;
    font-weight: 600;
    color: #2c3e50;
    text-decoration: none;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    &:hover { color: #f4b942; }
  }

  &__article { font-size: 12px; color: #999; }

  &__quantity {
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border: 1px solid #ddd;
    border-radius: 6px;
    padding: 4px;
    flex-shrink: 0;
  }

  &__price,
  &__subtotal {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 2px;
    flex-shrink: 0;
    min-width: 90px;
  }

  &__price-label { font-size: 11px; color: #999; }
  &__price   { font-size: 14px; font-weight: 600; color: #2c3e50; }
  &__subtotal { font-size: 15px; font-weight: 700; color: #2c3e50; }

  &__remove {
    background: none;
    border: none;
    cursor: pointer;
    padding: 8px;
    border-radius: 4px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    &:hover { background: #fee; svg path { stroke: #e74c3c; } }
  }
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

  &:hover:not(:disabled) { background: #42b983; color: white; }
  &:disabled { opacity: 0.3; cursor: not-allowed; }
  &--minus { color: #e74c3c; }
  &--plus  { color: #27ae60; }
}

.quantity-value { font-size: 12px; font-weight: 500; min-width: 36px; text-align: center; color: #555; }

.cart-summary {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;

  &__title { font-size: 16px; font-weight: 700; color: #2c3e50; margin: 0 0 16px; }

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;
    color: #555;
    margin-bottom: 8px;
    &--total { font-size: 16px; font-weight: 700; color: #2c3e50; margin-bottom: 0; }
  }

  &__free { color: #27ae60; font-weight: 600; }

  &__hint {
    font-size: 12px;
    color: #999;
    margin-bottom: 12px;
    padding: 8px;
    background: #f9f9f9;
    border-radius: 4px;
    text-align: center;
  }

  &__divider { height: 1px; background: #eee; margin: 12px 0; }
}

.btn-checkout {
  width: 100%;
  padding: 12px;
  background: #f4b942;
  color: #2c3e50;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 16px;

  &:hover:not(:disabled) { background: #e0a830; }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
  &--submitting { background: #42b983; color: white; }
}

@media (max-width: 768px) {
  .cart-layout { flex-direction: column; }
  .cart-summary { width: 100%; }
}
</style>
