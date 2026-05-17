<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCatalogMenu } from '@/composables/useCatalogMenu'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { productService } from '@/services/productApi'
import type { Product } from '@/types/product'

const router = useRouter()
const { categories, loading, fetchCategoriesTree } = useCatalogMenu()
const cartStore = useCartStore()
const authStore = useAuthStore()

const catalogOpen = ref(false)
const hoveredCategory = ref<number | null>(null)
const catalogDropdownRef = ref<HTMLElement | null>(null)

// ── search ──
const searchQuery        = ref('')
const searchResults      = ref<Product[]>([])
const searchTotal        = ref(0)
const searchLoading      = ref(false)
const showDropdown       = ref(false)
const searchContainerRef = ref<HTMLElement | null>(null)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

function formatPrice(price: number) {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(price)
}

function handleSearchInput() {
  if (debounceTimer) clearTimeout(debounceTimer)
  const q = searchQuery.value.trim()
  if (q.length < 2) {
    showDropdown.value = false
    searchResults.value = []
    return
  }
  debounceTimer = setTimeout(performSearch, 300)
}

async function performSearch() {
  const q = searchQuery.value.trim()
  if (q.length < 2) return
  searchLoading.value = true
  showDropdown.value = true
  try {
    const res = await productService.searchProducts(q, 0, 20)
    searchResults.value = res.content
    searchTotal.value = res.totalElements
  } catch {
    searchResults.value = []
    searchTotal.value = 0
  } finally {
    searchLoading.value = false
  }
}

function navigateToSearch() {
  const q = searchQuery.value.trim()
  if (!q) return
  showDropdown.value = false
  router.push({ path: '/search', query: { q } })
}

function selectProduct(id: number) {
  showDropdown.value = false
  searchQuery.value = ''
  router.push(`/product/${id}`)
}

function closeSearch() {
  showDropdown.value = false
}

function onSearchFocus() {
  if (searchResults.value.length > 0) showDropdown.value = true
}

onMounted(() => {
  fetchCategoriesTree()
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleEscape)
  if (debounceTimer) clearTimeout(debounceTimer)
})

const toggleCatalog = (e: Event) => {
  e.stopPropagation()
  catalogOpen.value = !catalogOpen.value
}

const handleClickOutside = (e: MouseEvent) => {
  if (catalogDropdownRef.value && !catalogDropdownRef.value.contains(e.target as Node)) {
    catalogOpen.value = false
  }
  if (searchContainerRef.value && !searchContainerRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

const handleEscape = (e: KeyboardEvent) => {
  if (e.key === 'Escape') {
    catalogOpen.value = false
    showDropdown.value = false
  }
}

const goToSubcategory = (categorySlug: string, subcategorySlug: string) => {
  router.push(`/catalog/${categorySlug}/${subcategorySlug}`)
  catalogOpen.value = false
}

const goToCategory = (slug: string) => {
  router.push(`/catalog/${slug}`)
  catalogOpen.value = false
}

const goToCart = () => {
  router.push('/cart')
}

const goToProfile = () => {
  if (authStore.isAuthenticated) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <header class="app-header">
    <nav class="container">
      <router-link to="/">
        <img src="/small-logo.jpg" alt="" class="logo">
      </router-link>
      <button class="catalog" :class="{ 'catalog--active': catalogOpen }" @click="toggleCatalog">
        <img src="@/assets/catalog-icon.svg" alt="" />
        <span class="catalog-label">Каталог</span>
      </button>
      <div class="search" ref="searchContainerRef">
        <input
          class="search-input"
          placeholder="Поиск товара"
          type="text"
          v-model="searchQuery"
          @input="handleSearchInput"
          @keydown.enter.prevent="navigateToSearch"
          @keydown.escape="closeSearch"
          @focus="onSearchFocus"
          autocomplete="off"
        />
        <img src="@/assets/search-icon.svg" alt="" class="search-icon" @click="navigateToSearch" />

        <!-- Search dropdown -->
        <transition name="search-fade">
          <div v-if="showDropdown" class="search-dropdown">
            <div v-if="searchLoading" class="search-dropdown__loading">
              <span class="search-spinner"></span> Поиск...
            </div>
            <template v-else-if="searchResults.length > 0">
              <div
                v-for="product in searchResults.slice(0, 6)"
                :key="product.id"
                class="search-result"
                @click="selectProduct(product.id)"
              >
                <img
                  :src="product.imageUrls?.[0] || '/placeholder-product.jpg'"
                  class="search-result__img"
                  alt=""
                />
                <div class="search-result__info">
                  <span class="search-result__name">{{ product.name }}</span>
                  <span class="search-result__price">{{ formatPrice(product.price) }}</span>
                </div>
              </div>
              <div v-if="searchTotal > 6" class="search-dropdown__footer" @click="navigateToSearch">
                Показать все результаты ({{ searchTotal }}) →
              </div>
            </template>
            <div v-else class="search-dropdown__empty">Ничего не найдено</div>
          </div>
        </transition>
      </div>
      <div class="contacts">
        <div class="contacts-div">
          <a href="tel:+79235997489" class="contacts-telephone">8-923-599-74-89</a>
        </div>
        <div class="contacts-icons">
          <a href="tel:+79235997489" title="Позвонить">
            <img src="@/assets/telephone-icon.svg" alt="Телефон"/>
          </a>
          <a href="mailto:info@factura.ru" title="Написать на почту">
            <img src="@/assets/mail-icon.svg" alt="Email"/>
          </a>
          <a href="https://vk.com/facturasayan" target="_blank" rel="noopener" title="ВКонтакте">
            <img src="@/assets/vk-icon.svg" alt="ВКонтакте"/>
          </a>
          <a href="https://t.me/sseraZzz" target="_blank" rel="noopener" title="Telegram">
            <img src="@/assets/telegram-icon.svg" alt="Telegram"/>
          </a>
        </div>
      </div>
      <div class="cart" @click="goToCart">
        <button class="cart-btn">
          <img src="@/assets/cart-icon.svg" alt="" />
          <span v-if="cartStore.totalItems > 0" class="cart-badge">{{ cartStore.totalItems > 99 ? '99+' : cartStore.totalItems }}</span>
        </button>
        <span class="cart-span">Корзина</span>
      </div>
      <div v-if="authStore.isAuthenticated" class="user-menu">
        <div class="user-info" @click="goToProfile">
          <button class="user-btn">
            <img src="@/assets/profile-icon.svg" alt="" />
          </button>
          <span class="user-name">{{ authStore.currentUser?.firstName }}</span>
        </div>
        <button class="logout-btn" @click="handleLogout" title="Выйти">
          <svg width="20" height="50" viewBox="0 0 16 16" fill="none">
            <path d="M6 14H2V2H6M10 10L14 8L10 6M14 8H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <div v-else class="profile" @click="goToProfile">
        <button class="profile-btn">
          <img src="@/assets/profile-icon.svg" alt="" />
        </button>
        <span class="profile-span">Профиль</span>
      </div>
    </nav>

    <!-- Catalog dropdown -->
    <transition name="slide-down">
      <div v-if="catalogOpen" class="catalog-dropdown" ref="catalogDropdownRef">
        <div class="catalog-categories">
          <div
            v-if="loading"
            class="catalog-skeleton"
          >
            <div v-for="n in 6" :key="n" class="skeleton-line"></div>
          </div>
          <template v-else>
            <div
              class="catalog-item catalog-item--all"
              @click="router.push('/catalog'); catalogOpen = false"
            >
              <span class="catalog-item__name">Все товары</span>
            </div>
            <div
              v-for="category in categories"
              :key="category.id"
              class="catalog-item"
              :class="{ 'catalog-item--active': hoveredCategory === category.id }"
              @mouseenter="hoveredCategory = category.id"
              @click="goToCategory(category.slug)"
            >
              <span class="catalog-item__name">{{ category.name }}</span>
              <svg class="catalog-item__arrow" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M6 3L11 8L6 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </template>
        </div>
        <div class="catalog-subcategories">
          <template v-if="hoveredCategory !== null">
            <div
              v-for="sub in categories.find(c => c.id === hoveredCategory)?.subcategories"
              :key="sub!.id"
              class="subcategory-item"
              @click="goToSubcategory(categories.find(c => c.id === hoveredCategory)!.slug, sub!.slug)"
            >
              {{ sub!.name }}
            </div>
          </template>
          <div v-else class="subcategory-placeholder">
            Выберите категорию
          </div>
          <router-link to="/categories" class="all-categories-link" @click="catalogOpen = false">
            Все категории →
          </router-link>
        </div>
      </div>
    </transition>
  </header>
</template>

<style lang="scss" scoped>
.app-header {
  display: block;
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.container {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: $gap-sm;
  margin: 30px 10%;
  height: 50px;
  position: relative;

  @include below-lg { margin: 24px 5%; }
  @include below-md { margin: 16px $container-pad; }
  @include below-sm { margin: 12px $gap-md; gap: $gap-xs; }
}

.logo {
  width: auto;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  flex-shrink: 0;

  @include below-sm { height: 38px; }
}

.catalog {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: $gap-xs;
  height: 100%;
  width: 150px;
  border: none;
  border-radius: 10px;
  font-size: $default-size;
  background-color: $color-primary;
  cursor: pointer;
  transition: background-color 0.2s;
  flex-shrink: 0;

  img { width: 20px; height: 20px; }

  &--active { background-color: $color-primary-dark; }

  @include below-md { width: 48px; }
}

.catalog-label {
  @include below-md { display: none; }
}

.catalog-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-top: 1px solid $color-border-light;
  z-index: 99;
  min-height: 320px;

  @include below-md {
    flex-direction: column;
    min-height: auto;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.catalog-categories {
  width: 280px;
  border-right: 1px solid $color-border-light;
  padding: $gap-sm 0;
  flex-shrink: 0;

  @include below-md {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid $color-border-light;
    padding: $gap-xs 0;
  }
}

.catalog-skeleton { padding: $gap-sm $gap-md; }

.skeleton-line {
  height: 40px;
  @include skeleton;
  border-radius: $radius-md;
  margin-bottom: $gap-sm;
}

.catalog-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px $gap-md;
  cursor: pointer;
  transition: background-color 0.15s;

  &:hover, &--active { background-color: $color-bg-light; }

  &__name { font-size: $font-md; font-weight: 500; color: $color-dark; }

  &__arrow {
    color: $color-border;
    transition: color 0.15s, transform 0.15s;
  }

  &--active &__arrow { color: $color-primary; transform: translateX(2px); }

  &--all {
    border-bottom: 1px solid $color-border-light;
    .catalog-item__name { color: $color-primary; font-weight: 600; }
  }
}

.catalog-subcategories {
  flex: 1;
  padding: $gap-md $gap-lg;
  display: flex;
  flex-direction: column;
  gap: $gap-xs;

  @include below-md { padding: $gap-sm $gap-md; }
}

.subcategory-item {
  padding: $gap-sm 12px;
  font-size: $font-md;
  color: $color-dark;
  cursor: pointer;
  border-radius: $radius-md;
  transition: background-color 0.15s;
  &:hover { background-color: $color-bg-light; color: $color-primary; }
}

.subcategory-placeholder {
  padding: 20px 12px;
  font-size: $font-md;
  color: $color-text-muted;
}

.all-categories-link {
  display: inline-block;
  margin-top: auto;
  padding: 10px 12px 0;
  font-size: $font-base;
  font-weight: 600;
  color: $color-primary;
  text-decoration: none;
  transition: color 0.15s;
  &:hover { color: $color-primary-dark; }
}

.slide-down-enter-active,
.slide-down-leave-active  { transition: all 0.2s ease; }
.slide-down-enter-from,
.slide-down-leave-to      { opacity: 0; transform: translateY(-8px); }

// ─── Search ──────────────────────────────────────────────────────
.search {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 700px;
  height: 90%;
  border-radius: 10px;
  background-color: $light-grey-color;
  position: relative;
  min-width: 0;

  @include below-lg { max-width: 480px; }
  @include below-md { max-width: none; }

  &-input {
    font-size: $default-size;
    flex: 1;
    width: 0;
    min-width: 0;
    border: 0;
    outline: 0;
    background: transparent;
    margin-left: 20px;

    @include below-md { font-size: $font-md; margin-left: 12px; }
    @include below-sm { font-size: $font-base; margin-left: 8px; }
  }

  &-icon {
    width: 18px;
    height: 18px;
    margin-right: 10px;
    cursor: pointer;
    opacity: 0.6;
    transition: opacity 0.15s;
    flex-shrink: 0;
    &:hover { opacity: 1; }
  }
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid $color-border-light;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 200;
  overflow: hidden;

  &__loading {
    display: flex;
    align-items: center;
    gap: $gap-sm;
    padding: 14px $gap-md;
    font-size: $font-base;
    color: $color-text-muted;
  }

  &__empty {
    padding: 14px $gap-md;
    font-size: $font-base;
    color: $color-text-faint;
    text-align: center;
  }

  &__footer {
    padding: 10px $gap-md;
    font-size: $font-base;
    font-weight: 600;
    color: $color-primary;
    border-top: 1px solid $color-bg;
    cursor: pointer;
    text-align: center;
    transition: background 0.15s;
    &:hover { background: $color-primary-light; }
  }
}

.search-result {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px $gap-md;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid $color-bg-light;
  &:last-of-type { border-bottom: none; }
  &:hover { background: #f9f9f9; }

  &__img {
    width: 44px;
    height: 44px;
    object-fit: cover;
    border-radius: $radius-md;
    border: 1px solid $color-border-light;
    flex-shrink: 0;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
    flex: 1;
  }

  &__name {
    font-size: $font-base;
    font-weight: 500;
    color: $color-dark;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__price { font-size: $font-base; font-weight: 700; color: $color-primary; }
}

.search-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid $color-border;
  border-top-color: $color-primary;
  border-radius: $radius-full;
  animation: spin 0.6s linear infinite;
}

.search-fade-enter-active,
.search-fade-leave-active { transition: opacity 0.15s, transform 0.15s; }
.search-fade-enter-from,
.search-fade-leave-to     { opacity: 0; transform: translateY(-4px); }

// ─── Contacts ────────────────────────────────────────────────────
.contacts {
  width: 180px;
  height: 100%;
  flex-shrink: 0;

  @include below-lg { display: none; }

  &-div { display: flex; justify-content: center; }

  &-telephone {
    font-size: $default-size;
    color: $color-dark;
    text-decoration: none;
    transition: color 0.2s;
    &:hover { color: $color-primary; }
  }

  &-icons {
    display: flex;
    justify-content: space-between;
    margin: 0 10%;

    a {
      display: flex;
      align-items: center;
      opacity: 0.7;
      transition: opacity 0.2s, transform 0.2s;

      img { width: 20px; height: 20px; }

      &:hover {
        opacity: 1;
        transform: translateY(-2px);
      }
    }
  }
}

// ─── Cart ────────────────────────────────────────────────────────
.cart {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;
  cursor: pointer;
  position: relative;
  transition: transform 0.2s ease;
  flex-shrink: 0;

  &:hover  { transform: translateY(-2px); }
  &:active { transform: translateY(0); }

  &-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    background: transparent;
    border: 0;
    cursor: pointer;
    img { width: 24px; height: 24px; transition: transform 0.2s ease, filter 0.2s ease; }
    &:hover img { transform: scale(1.1); filter: drop-shadow(0 2px 4px rgba($color-primary, 0.4)); }
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
    transition: color 0.2s ease;

    @include below-sm { display: none; }
  }

  &:hover &-span { color: $color-primary; }
}

.cart-badge {
  position: absolute;
  top: -6px;
  right: -10px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: $color-danger;
  color: #fff;
  font-size: $font-xs;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  pointer-events: none;
  animation: badge-pop 0.3s ease;
}

@keyframes badge-pop {
  0%   { transform: scale(0); }
  70%  { transform: scale(1.2); }
  100% { transform: scale(1); }
}

// ─── Profile / User ──────────────────────────────────────────────
.profile {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;
  cursor: pointer;
  transition: transform 0.2s ease;
  flex-shrink: 0;

  &:hover  { transform: translateY(-2px); }
  &:active { transform: translateY(0); }

  &-btn {
    display: flex;
    justify-content: center;
    background: transparent;
    border: 0;
    cursor: pointer;
    img { width: 24px; height: 24px; transition: transform 0.2s ease, filter 0.2s ease; }
    &:hover img { transform: scale(1.1) rotate(5deg); filter: drop-shadow(0 2px 4px rgba($color-primary, 0.4)); }
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
    transition: color 0.2s ease;

    @include below-sm { display: none; }
  }

  &:hover &-span { color: $color-primary; }
}

.user-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $gap-sm;
  height: 100%;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  transition: transform 0.2s ease;
  &:hover  { transform: translateY(-2px); }
  &:active { transform: translateY(0); }
}

.user-btn {
  display: flex;
  justify-content: center;
  background: transparent;
  border: 0;
  cursor: pointer;
  padding: 0;
  img { width: 24px; height: 24px; transition: transform 0.2s ease, filter 0.2s ease; }
  &:hover img { transform: scale(1.1) rotate(5deg); filter: drop-shadow(0 2px 4px rgba($color-primary, 0.4)); }
}

.user-name {
  font-size: $font-base;
  font-weight: 600;
  color: $color-dark;
  white-space: nowrap;

  @include below-sm { display: none; }
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  padding: $gap-xs;
  color: $color-text-muted;
  transition: color 0.2s;
  &:hover { color: $color-danger; }
}
</style>