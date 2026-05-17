<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategories } from '@/composables/useCategories'

const router = useRouter()
const { allCategories, loading, fetchAllCategories } = useCategories()

onMounted(fetchAllCategories)

const goToCategory = (slug: string) => router.push(`/catalog/${slug}`)
const goToSubcategory = (catSlug: string, subSlug: string) => router.push(`/catalog/${catSlug}/${subSlug}`)
</script>

<template>
  <main class="categories-page">
    <div class="container">
      <h1 class="page-title">Категории</h1>

      <!-- Skeleton -->
      <div v-if="loading" class="categories-grid">
        <div v-for="n in 6" :key="n" class="category-card category-card--skeleton">
          <div class="skeleton skeleton--title"></div>
          <div class="skeleton skeleton--image"></div>
          <div class="skeleton-subs">
            <div v-for="m in 8" :key="m" class="skeleton skeleton--sub"></div>
          </div>
        </div>
      </div>

      <!-- Grid -->
      <div v-else class="categories-grid">
        <div
          v-for="cat in allCategories"
          :key="cat.id"
          class="category-card"
        >
          <!-- Name -->
          <h2 class="category-card__name" @click="goToCategory(cat.slug)">
            {{ cat.name }}
          </h2>

          <!-- Image -->
          <div class="category-card__image" @click="goToCategory(cat.slug)">
            <img
              v-if="cat.imageUrl"
              :src="cat.imageUrl"
              :alt="cat.name"
            />
            <div v-else class="category-card__image-placeholder"></div>
          </div>

          <!-- Subcategories -->
          <div class="category-card__subs" v-if="cat.subcategories?.length">
            <a
              v-for="sub in cat.subcategories"
              :key="sub.id"
              class="sub-link"
              @click.prevent="goToSubcategory(cat.slug, sub.slug)"
            >{{ sub.name }}</a>
            <a class="sub-link sub-link--all" @click.prevent="goToCategory(cat.slug)">Все товары →</a>
          </div>
          <div v-else class="category-card__subs category-card__subs--empty">
            <a class="sub-link sub-link--all" @click.prevent="goToCategory(cat.slug)">Перейти в раздел →</a>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.categories-page {
  @include page-layout;
  padding-top: 28px;
}

.container { @include container; }

.page-title {
  font-size: $font-2xl;
  font-weight: 700;
  color: $color-dark;
  margin: 0 0 $gap-lg;

  @include below-sm { font-size: $font-xl; margin-bottom: $gap-md; }
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $gap-md;

  @include below-lg { grid-template-columns: repeat(2, 1fr); }
  @include below-xs { grid-template-columns: 1fr; }
}

.category-card {
  background: #fff;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }
  &--skeleton { pointer-events: none; min-height: 320px; }

  &__name {
    font-size: $small-size;
    font-weight: 700;
    color: $color-dark;
    text-align: center;
    margin: 0;
    padding: 14px $gap-md 12px;
    border-bottom: 1px solid $color-border-light;
    cursor: pointer;
    transition: color 0.15s;
    &:hover { color: $color-primary; }
  }

  &__image {
    width: 100%;
    aspect-ratio: 16 / 9;
    background: #e8e8e8;
    overflow: hidden;
    cursor: pointer;
    flex-shrink: 0;

    img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.3s ease; }
    &:hover img { transform: scale(1.04); }
  }

  &__image-placeholder { width: 100%; height: 100%; background: #d8d8d8; }

  &__subs {
    padding: 12px $gap-md 14px;
    columns: 2;
    column-gap: 12px;
    flex: 1;
    &--empty { columns: 1; }
  }
}

.sub-link {
  display: block;
  break-inside: avoid;
  font-size: $small-size;
  color: #444;
  text-decoration: none;
  cursor: pointer;
  padding: 3px 0;
  line-height: 1.45;
  transition: color 0.15s;

  &:hover { color: $color-primary; }
  &--all { color: $color-primary; font-weight: 600; margin-top: 6px; }
}

.skeleton {
  @include skeleton;
  border-radius: $radius-sm;

  &--title { height: 20px; margin: 14px $gap-md 12px; }
  &--image { width: 100%; aspect-ratio: 16 / 9; border-radius: 0; }
  &--sub { height: 14px; margin-bottom: 6px; width: 80%; }
}

.skeleton-subs {
  padding: 12px $gap-md;
  columns: 2;
  gap: $gap-sm;
}
</style>
