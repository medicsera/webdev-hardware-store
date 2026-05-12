<script setup lang="ts">
import { onMounted } from 'vue'
import { useCategories } from '@/composables/useCategories'
import { useRouter } from 'vue-router'

const router = useRouter()
const { popularCategories, loading, fetchPopularCategories } = useCategories()

onMounted(() => {
  fetchPopularCategories(6)
})

const goToCategory = (slug: string) => {
  router.push(`/catalog/${slug}`)
}

const handleImageError = (e: Event) => {
  console.warn('Не удалось загрузить изображение категории')
  const target = e.target as HTMLImageElement
  target.src = '/placeholder-product.jpg'
}
</script>

<template>
  <section class="popular-categories">
    <div class="popular-categories__header">
      <h2 class="popular-categories__title">Популярные каталоги</h2>
      <router-link to="/categories" class="popular-categories__all">
        Все категории →
      </router-link>
    </div>

    <div class="popular-categories__grid">
      <!-- Skeletons -->
      <template v-if="loading">
        <div
            v-for="n in 6"
            :key="`skeleton-${n}`"
            class="category-card category-card--skeleton"
        >
          <div class="skeleton skeleton--image"></div>
          <div class="skeleton-content">
            <div class="skeleton skeleton--title"></div>
            <div class="skeleton skeleton--count"></div>
          </div>
        </div>
      </template>

      <template v-else>
        <div
            v-for="category in popularCategories"
            :key="category.id"
            class="category-card"
            @click="goToCategory(category.slug)"
        >
          <div class="category-card__image-wrapper">
            <img
                :src="category.imageUrl || '/images/placeholder-product.jpg'"
                :alt="category.name"
                class="category-card__image"
                loading="lazy"
                @error="handleImageError"
            />
          </div>
          <div class="category-card__content">
            <h3 class="category-card__title">{{ category.name }}</h3>
            <span class="category-card__count">
              {{ category.productsCount }} товаров
            </span>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.popular-categories {
  margin: 0 8.5% 48px;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }

  &__title {
    font-size: 24px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0;
  }

  &__all {
    font-size: 16px;
    color: #42b983;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s;

    &:hover {
      color: #369970;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }
}

.category-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  }

  &--skeleton {
    pointer-events: none;
  }

  &__image-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;
    background: #f5f5f5;
    overflow: hidden;
  }

  &__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  &:hover &__image {
    transform: scale(1.05);
  }

  &__content {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
    line-height: 1.4;
  }

  &__count {
    font-size: 14px;
    color: #42b983;
    font-weight: 500;
  }
}

.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 6px;

  &--image {
    width: 100%;
    aspect-ratio: 16 / 9;
    border-radius: 0;
  }

  &--title {
    height: 18px;
    width: 75%;
  }

  &--count {
    height: 16px;
    width: 50%;
  }
}

.skeleton-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 768px) {
  .popular-categories {
    margin: 0 4% 32px;
    &__grid {
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 12px;
    }
  }
}
</style>