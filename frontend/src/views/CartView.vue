<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore, type CartItem } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/auth'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const loading      = ref(true)
const isSubmitting = ref(false)

const deliveryCost          = 350
const freeDeliveryThreshold = 5000

type DeliveryMethod = 'pickup' | 'delivery'
const deliveryMethod  = ref<DeliveryMethod>('pickup')
const deliveryAddress = ref('')
const addressError    = ref(false)

const cartItems = computed(() => cartStore.items)

const subtotal = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.cartQuantity, 0)
)

const isDelivery     = computed(() => deliveryMethod.value === 'delivery')
const isFreeDelivery = computed(() => isDelivery.value && subtotal.value >= freeDeliveryThreshold)
const delivery = computed(() => {
  if (!isDelivery.value || cartItems.value.length === 0) return 0
  return isFreeDelivery.value ? 0 : deliveryCost
})
const total = computed(() => subtotal.value + delivery.value)

const formattedSubtotal = computed(() => formatPrice(subtotal.value))
const formattedDelivery = computed(() => {
  if (!isDelivery.value) return 'Самовывоз'
  return isFreeDelivery.value ? 'Бесплатно' : formatPrice(delivery.value)
})
const formattedTotal = computed(() => formatPrice(total.value))

function formatPrice(price: number): string {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 2 }).format(price)
}

function incrementQuantity(item: CartItem) {
  if (item.cartQuantity >= item.stock) return
  cartStore.updateQuantity(item.id, item.cartQuantity + 1)
}

function decrementQuantity(item: CartItem) {
  if (item.cartQuantity <= 1) return
  cartStore.updateQuantity(item.id, item.cartQuantity - 1)
}

function updateQuantityFromInput(item: CartItem, event: Event) {
  const val = parseInt((event.target as HTMLInputElement).value)
  const clamped = isNaN(val) || val < 1 ? 1 : Math.min(val, item.stock)
  cartStore.updateQuantity(item.id, clamped)
  ;(event.target as HTMLInputElement).value = String(clamped)
}

function removeItem(item: CartItem) {
  cartStore.removeFromCart(item.id)
}

const handleCheckout = async () => {
  if (isSubmitting.value || cartItems.value.length === 0) return

  if (isDelivery.value && !deliveryAddress.value.trim()) {
    addressError.value = true
    return
  }

  isSubmitting.value = true
  try {
    await api.post('/buyer/orders', {
      items: cartItems.value.map(item => ({
        productId: item.id,
        quantity: item.cartQuantity
      })),
      deliveryMethod: deliveryMethod.value,
      deliveryAddress: isDelivery.value ? `г. Саяногорск, ${deliveryAddress.value.trim()}` : null,
      deliveryCost: delivery.value
    })
    cartStore.clearCart()
    router.push('/profile')
  } catch (err) {
    console.error('Checkout failed', err)
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
              <input
                type="number"
                class="quantity-input"
                :value="item.cartQuantity"
                min="1"
                :max="item.stock"
                @change="updateQuantityFromInput(item, $event)"
                @blur="updateQuantityFromInput(item, $event)"
              />
              <button class="quantity-btn quantity-btn--plus" @click="incrementQuantity(item)" :disabled="item.cartQuantity >= item.stock">+</button>
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

          <!-- Способ получения -->
          <div class="delivery-method">
            <p class="delivery-method__label">Способ получения</p>
            <label class="delivery-method__option" :class="{ 'delivery-method__option--active': deliveryMethod === 'pickup' }">
              <input type="radio" v-model="deliveryMethod" value="pickup" />
              <span class="delivery-method__icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M8 1.5C5.5 1.5 3.5 3.5 3.5 6C3.5 9.5 8 14.5 8 14.5C8 14.5 12.5 9.5 12.5 6C12.5 3.5 10.5 1.5 8 1.5ZM8 7.5C7.17 7.5 6.5 6.83 6.5 6C6.5 5.17 7.17 4.5 8 4.5C8.83 4.5 9.5 5.17 9.5 6C9.5 6.83 8.83 7.5 8 7.5Z" fill="currentColor"/>
                </svg>
              </span>
              <span class="delivery-method__text">
                <strong>Самовывоз</strong>
                <small>г. Саяногорск, Ленинградский микрорайон, 26/1 н</small>
              </span>
            </label>
            <label class="delivery-method__option" :class="{ 'delivery-method__option--active': deliveryMethod === 'delivery' }">
              <input type="radio" v-model="deliveryMethod" value="delivery" />
              <span class="delivery-method__icon">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M1 4H10V11H1V4Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
                  <path d="M10 6L13 6L15 8.5V11H10V6Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
                  <circle cx="3.5" cy="12.5" r="1.5" stroke="currentColor" stroke-width="1.2"/>
                  <circle cx="12.5" cy="12.5" r="1.5" stroke="currentColor" stroke-width="1.2"/>
                </svg>
              </span>
              <span class="delivery-method__text">
                <strong>Доставка по городу</strong>
                <small>{{ subtotal >= freeDeliveryThreshold ? 'Бесплатно' : `${deliveryCost} ₽` }} · г. Саяногорск</small>
              </span>
            </label>

            <div v-if="deliveryMethod === 'delivery'" class="delivery-address">
              <label class="delivery-address__label" for="address">Адрес доставки</label>
              <div class="delivery-address__prefix">г. Саяногорск,</div>
              <input
                id="address"
                type="text"
                class="delivery-address__input"
                :class="{ 'delivery-address__input--error': addressError }"
                v-model="deliveryAddress"
                placeholder="ул. Примерная, д. 1, кв. 10"
                @input="addressError = false"
              />
              <p v-if="addressError" class="delivery-address__error">Укажите адрес доставки</p>
            </div>
          </div>

          <div class="cart-summary__row">
            <span>Товары ({{ cartItems.length }})</span>
            <span>{{ formattedSubtotal }}</span>
          </div>

          <div class="cart-summary__row">
            <span>Доставка</span>
            <span :class="{ 'cart-summary__free': isFreeDelivery || !isDelivery }">{{ formattedDelivery }}</span>
          </div>

          <div v-if="isDelivery && !isFreeDelivery && cartItems.length > 0" class="cart-summary__hint">
            До бесплатной доставки ещё {{ formatPrice(freeDeliveryThreshold - subtotal) }}
          </div>

          <div class="cart-summary__divider"></div>

          <div class="cart-summary__row cart-summary__row--total">
            <span>К оплате</span>
            <span>{{ formattedTotal }}</span>
          </div>

          <div v-if="!authStore.isAuthenticated" class="cart-auth-prompt">
            <p class="cart-auth-prompt__text">Чтобы оформить заказ:</p>
            <div class="cart-auth-prompt__links">
              <router-link to="/login" class="cart-auth-prompt__btn cart-auth-prompt__btn--login">Войти</router-link>
              <span class="cart-auth-prompt__or">или</span>
              <router-link to="/register" class="cart-auth-prompt__btn cart-auth-prompt__btn--register">Зарегистрироваться</router-link>
            </div>
          </div>
          <button
            v-else
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
  @include page-layout;
}

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

.cart-title {
  font-size: $font-xl;
  font-weight: 700;
  color: $color-dark;
  margin: 0 0 $gap-lg;

  @include below-sm { font-size: $font-lg; margin-bottom: $gap-md; }
}

.cart-layout {
  display: flex;
  gap: $gap-lg;
  align-items: flex-start;

  @include below-md { flex-direction: column; }
}

.cart-empty {
  text-align: center;
  padding: 60px $container-pad;
  background: #fff;
  border-radius: $radius-md;
  svg { margin-bottom: $gap-md; }
  &__title { font-size: $font-2xl; font-weight: 700; color: $color-dark; margin: 0 0 $gap-sm; }
  &__text { font-size: $font-md; color: $color-text-muted; margin: 0 0 $gap-lg; }
}

.btn-go-to-catalog {
  display: inline-block;
  padding: 10px 28px;
  background: $color-primary;
  color: $color-dark;
  border-radius: $radius-md;
  font-size: $font-md;
  font-weight: 600;
  text-decoration: none;
  &:hover { background: $color-primary-dark; }
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

  &__count { font-size: $font-base; color: $color-text-secondary; }
}

.btn-clear-cart {
  background: none;
  border: none;
  font-size: $font-base;
  color: $color-text-muted;
  cursor: pointer;
  padding: 4px 0;
  &:hover { color: $color-danger; }
}

.cart-item {
  display: flex;
  align-items: center;
  gap: $gap-md;
  background: #fff;
  border-radius: $radius-md;
  padding: $gap-md;
  margin-bottom: 12px;

  @include below-sm {
    flex-wrap: wrap;
    gap: $gap-sm;
  }

  &__image-link { display: block; flex-shrink: 0; text-decoration: none; }
  &__image { width: 80px; height: 80px; object-fit: cover; border-radius: $radius-sm; }

  &__info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__name {
    font-size: $font-md;
    font-weight: 600;
    color: $color-dark;
    text-decoration: none;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    &:hover { color: $color-primary; }
  }

  &__article { font-size: $font-sm; color: $color-text-muted; }

  &__quantity {
    display: flex;
    align-items: center;
    background: $color-bg-light;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    padding: $gap-xs;
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

    @include below-sm { min-width: 70px; }
  }

  &__price-label { font-size: $font-xs; color: $color-text-muted; }
  &__price   { font-size: $font-md; font-weight: 600; color: $color-dark; }
  &__subtotal { font-size: $font-lg; font-weight: 700; color: $color-dark; }

  &__remove {
    background: none;
    border: none;
    cursor: pointer;
    padding: $gap-sm;
    border-radius: $radius-sm;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    &:hover { background: $color-danger-bg; svg path { stroke: $color-danger; } }
  }
}

.quantity-btn {
  width: 24px;
  height: 26px;
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
  width: 40px;
  text-align: center;
  color: $color-text;
  border: none;
  background: transparent;
  outline: none;
  -moz-appearance: textfield;

  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
}

.cart-summary {
  width: 280px;
  flex-shrink: 0;
  @include card;
  padding: $gap-md;

  @include below-md { width: 100%; }

  &__title { font-size: $font-lg; font-weight: 700; color: $color-dark; margin: 0 0 $gap-md; }

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: $font-base;
    color: $color-text;
    margin-bottom: $gap-sm;
    &--total { font-size: $font-lg; font-weight: 700; color: $color-dark; margin-bottom: 0; }
  }

  &__free { color: $color-success; font-weight: 600; }

  &__hint {
    font-size: $font-sm;
    color: $color-text-muted;
    margin-bottom: 12px;
    padding: $gap-sm;
    background: $color-bg-light;
    border-radius: $radius-sm;
    text-align: center;
  }

  &__divider { height: 1px; background: $color-border-light; margin: 12px 0; }
}

.cart-auth-prompt {
  margin-top: $gap-md;
  padding: 14px;
  background: $color-primary-light;
  border: 1px solid $color-primary;
  border-radius: $radius-md;
  text-align: center;

  &__text {
    font-size: $font-base;
    color: $color-text;
    margin: 0 0 10px;
  }

  &__links {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $gap-sm;
    flex-wrap: wrap;
  }

  &__or {
    font-size: $font-base;
    color: $color-text-faint;
  }

  &__btn {
    font-size: $font-base;
    font-weight: 600;
    text-decoration: none;
    padding: 6px 14px;
    border-radius: $radius-sm;
    transition: all 0.2s;

    &--login {
      background: $color-primary;
      color: $color-dark;
      &:hover { background: $color-primary-dark; }
    }

    &--register {
      background: #fff;
      color: $color-dark;
      border: 1px solid $color-border;
      &:hover { border-color: $color-primary; }
    }
  }
}

.btn-checkout {
  width: 100%;
  padding: 12px;
  background: $color-primary;
  color: $color-dark;
  border: none;
  border-radius: $radius-md;
  font-size: $font-md;
  font-weight: 600;
  cursor: pointer;
  margin-top: $gap-md;
  transition: all 0.2s;

  &:hover:not(:disabled) { background: $color-primary-dark; }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
  &--submitting { background: $color-success-light; color: #fff; }
}

.delivery-method {
  margin-bottom: $gap-md;

  &__label {
    font-size: $font-sm;
    font-weight: 600;
    color: $color-text-muted;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin: 0 0 $gap-sm;
  }

  &__option {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 12px;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    cursor: pointer;
    margin-bottom: $gap-sm;
    transition: border-color 0.2s, background 0.2s;

    input[type='radio'] { display: none; }

    &--active {
      border-color: $color-primary;
      background: $color-primary-light;
    }

    &:hover:not(&--active) { border-color: $color-border; }
  }

  &__icon {
    color: $color-text-muted;
    flex-shrink: 0;
    margin-top: 2px;

    .delivery-method__option--active & { color: $color-primary-dark; }
  }

  &__text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    strong { font-size: $font-base; color: $color-dark; font-weight: 600; }
    small  { font-size: $font-xs; color: $color-text-muted; }
  }
}

.delivery-address {
  margin-top: 4px;
  padding: 12px;
  background: $color-bg-light;
  border-radius: $radius-md;
  border: 1px solid $color-border-light;

  &__label {
    display: block;
    font-size: $font-sm;
    font-weight: 600;
    color: $color-text;
    margin-bottom: 6px;
  }

  &__prefix {
    font-size: $font-sm;
    color: $color-text-muted;
    margin-bottom: 4px;
  }

  &__input {
    width: 100%;
    padding: 8px 10px;
    border: 1px solid $color-border;
    border-radius: $radius-sm;
    font-size: $font-base;
    color: $color-dark;
    background: #fff;
    box-sizing: border-box;
    outline: none;
    transition: border-color 0.2s;

    &:focus { border-color: $color-primary; }
    &--error { border-color: $color-danger; }
    &::placeholder { color: $color-text-faint; }
  }

  &__error {
    font-size: $font-xs;
    color: $color-danger;
    margin: 4px 0 0;
  }
}
</style>
