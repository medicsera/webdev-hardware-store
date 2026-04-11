<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCatalogMenu } from '@/composables/useCatalogMenu'

const router = useRouter()
const { categories, loading, fetchCategoriesTree } = useCatalogMenu()

const catalogOpen = ref(false)
const hoveredCategory = ref<number | null>(null)
const catalogRef = ref<HTMLElement | null>(null)

onMounted(() => {
  fetchCategoriesTree()
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleEscape)
})

const toggleCatalog = (e: Event) => {
  e.stopPropagation()
  catalogOpen.value = !catalogOpen.value
}

const handleClickOutside = (e: MouseEvent) => {
  if (catalogRef.value && !catalogRef.value.contains(e.target as Node)) {
    catalogOpen.value = false
  }
}

const handleEscape = (e: KeyboardEvent) => {
  if (e.key === 'Escape') {
    catalogOpen.value = false
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
</script>

<template>
  <header class="app-header" ref="catalogRef">
    <nav class="container">
      <router-link to="/">
        <img src="/small-logo.jpg" alt="" class="logo">
      </router-link>
      <button class="catalog" :class="{ 'catalog--active': catalogOpen }" @click="toggleCatalog">
        <img src="@/assets/catalog-icon.svg" alt="" />
        <span class="catalog-label">Каталог</span>
      </button>
      <div class="search">
        <input id="search" class="search-input" placeholder="Поиск товара" type="text"/>
        <img src="@/assets/search-icon.svg" alt="" class="search-icon"/>
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
      <div class="cart">
        <button class="cart-btn">
          <img src="@/assets/cart-icon.svg" alt="" />
        </button>
        <span class="cart-span">Корзина</span>
      </div>
      <div class="profile">
        <button class="profile-btn">
          <img src="@/assets/profile-icon.svg" alt="" />
        </button>
        <span class="profile-span">Профиль</span>
      </div>
    </nav>

    <!-- Catalog dropdown -->
    <transition name="slide-down">
      <div v-if="catalogOpen" class="catalog-dropdown">
        <div class="catalog-categories">
          <div
            v-if="loading"
            class="catalog-skeleton"
          >
            <div v-for="n in 6" :key="n" class="skeleton-line"></div>
          </div>
          <template v-else>
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
  font-style: oblique;
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
  }

}
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

  &-btn {
    display: flex;
    justify-content: center;
    background: 0;
    border: 0;
    cursor: pointer;
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
  }
}

.profile {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;

  &-btn {
    display: flex;
    justify-content: center;
    background: 0;
    border: 0;
    cursor: pointer;
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
  }
}


</style>