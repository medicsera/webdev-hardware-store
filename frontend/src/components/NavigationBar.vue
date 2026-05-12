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
          <span class="contacts-telephone">8-923-599-74-89</span>
        </div>
        <div class="contacts-icons">
          <img src="@/assets/telephone-icon.svg" alt=""/>
          <img src="@/assets/mail-icon.svg" alt=""/>
          <img src="@/assets/vk-icon.svg" alt=""/>
          <img src="@/assets/telegram-icon.svg" alt=""/>
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
.app-header{
  display: inline;
  position: sticky;
  top: 0;
  z-index: 100;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.container {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 30px 10%;
  height: 50px;
  position: relative;
}

.logo {
  width: auto;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
}

.catalog{
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 100%;
  width: 150px;
  border: 2px;
  border-radius: 10px;
  font-size: $default-size;
  background-color: $light-orange-color;
  cursor: pointer;
  transition: background-color 0.2s;

  &--active {
    background-color: #d4a035;
  }
}

.catalog-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  display: flex;
  background: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-top: 1px solid #eee;
  z-index: 99;
  min-height: 320px;
}

.catalog-categories {
  width: 280px;
  border-right: 1px solid #eee;
  padding: 8px 0;
  flex-shrink: 0;
}

.catalog-skeleton {
  padding: 8px 16px;
}

.skeleton-line {
  height: 40px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 6px;
  margin-bottom: 8px;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.catalog-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.15s;

  &:hover,
  &--active {
    background-color: #f5f5f5;
  }

  &__name {
    font-size: 14px;
    font-weight: 500;
    color: #2c3e50;
  }

  &__arrow {
    color: #ccc;
    transition: color 0.15s, transform 0.15s;
  }

  &--active &__arrow {
    color: #f4b942;
    transform: translateX(2px);
  }

  &--all {
    border-bottom: 1px solid #eee;
    .catalog-item__name { color: #f4b942; font-weight: 600; }
  }
}

.catalog-subcategories {
  flex: 1;
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.subcategory-item {
  padding: 8px 12px;
  font-size: 14px;
  color: #2c3e50;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.15s;

  &:hover {
    background-color: #f5f5f5;
    color: #f4b942;
  }
}

.subcategory-placeholder {
  padding: 20px 12px;
  font-size: 14px;
  color: #999;
}

.all-categories-link {
  display: inline-block;
  margin-top: auto;
  padding: 10px 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: #f4b942;
  text-decoration: none;
  transition: color 0.15s;

  &:hover {
    color: #d4a035;
  }
}

// Slide-down transition
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
.search {
  display: flex;
  align-items: center;
  height: 90%;
  border-radius: 10px;
  background-color: $light-grey-color;
  position: relative;

  &-input {
    font-size: $default-size;
    width: 700px;
    border: 0;
    outline: 0;
    background: 0;
    margin-left: 20px;
  }

  &-icon {
    margin-right: 10px;
    cursor: pointer;
    opacity: 0.6;
    transition: opacity 0.15s;
    &:hover { opacity: 1; }
  }
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 200;
  overflow: hidden;

  &__loading {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 14px 16px;
    font-size: 13px;
    color: #888;
  }

  &__empty {
    padding: 14px 16px;
    font-size: 13px;
    color: #aaa;
    text-align: center;
  }

  &__footer {
    padding: 10px 16px;
    font-size: 13px;
    font-weight: 600;
    color: #f4b942;
    border-top: 1px solid #f0f0f0;
    cursor: pointer;
    text-align: center;
    transition: background 0.15s;

    &:hover { background: #fffbf0; }
  }
}

.search-result {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f5f5f5;
  &:last-of-type { border-bottom: none; }

  &:hover { background: #f9f9f9; }

  &__img {
    width: 44px;
    height: 44px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #eee;
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
    font-size: 13px;
    font-weight: 500;
    color: #2c3e50;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__price {
    font-size: 13px;
    font-weight: 700;
    color: #f4b942;
  }
}

.search-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid #ddd;
  border-top-color: #f4b942;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.search-fade-enter-active,
.search-fade-leave-active { transition: opacity 0.15s, transform 0.15s; }
.search-fade-enter-from,
.search-fade-leave-to    { opacity: 0; transform: translateY(-4px); }
.contacts {
  width: 180px;
  height: 100%;

  &-div {
    display: flex;
    justify-content: center;
  }

  &-telephone {
    font-size: $default-size;
  }

  &-icons {
    display: flex;
    justify-content: space-between;
    margin: 0 10%;
    cursor: pointer;
  }
}

.cart {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;
  cursor: pointer;
  position: relative;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }

  &-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    background: 0;
    border: 0;
    cursor: pointer;
    transition: transform 0.2s ease;

    img {
      transition: transform 0.2s ease, filter 0.2s ease;
    }

    &:hover img {
      transform: scale(1.1);
      filter: drop-shadow(0 2px 4px rgba(244, 185, 66, 0.4));
    }
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
    transition: color 0.2s ease;
  }

  &:hover &-span {
    color: #f4b942;
  }
}

.cart-badge {
  position: absolute;
  top: -6px;
  right: -10px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #e74c3c;
  color: white;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  pointer-events: none;
  animation: badge-pop 0.3s ease;
}

@keyframes badge-pop {
  0% { transform: scale(0); }
  70% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.profile {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }

  &-btn {
    display: flex;
    justify-content: center;
    background: 0;
    border: 0;
    cursor: pointer;
    transition: transform 0.2s ease;

    img {
      transition: transform 0.2s ease, filter 0.2s ease;
    }

    &:hover img {
      transform: scale(1.1) rotate(5deg);
      filter: drop-shadow(0 2px 4px rgba(244, 185, 66, 0.4));
    }
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
    transition: color 0.2s ease;
  }

  &:hover &-span {
    color: #f4b942;
  }
}

.user-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 100%;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}

.user-btn {
  display: flex;
  justify-content: center;
  background: 0;
  border: 0;
  cursor: pointer;
  padding: 0;

  img {
    transition: transform 0.2s ease, filter 0.2s ease;
  }

  &:hover img {
    transform: scale(1.1) rotate(5deg);
    filter: drop-shadow(0 2px 4px rgba(244, 185, 66, 0.4));
  }
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #999;
  transition: color 0.2s;

  &:hover {
    color: #e74c3c;
  }
}


</style>