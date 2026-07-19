<script setup lang="ts">
</script>

<template>
  <div class="info-page">
    <div class="info-page__container">
      <h1 class="info-page__title">Доставка</h1>

      <div class="info-page__grid">
        <div class="info-card">
          <div class="info-card__icon">🚗</div>
          <h2 class="info-card__title">Самовывоз</h2>
          <p class="info-card__text">
            Бесплатно. Забирайте заказ в нашем магазине по адресу:
            <strong>Ленинградский микрорайон, 26/1н, Саяногорск</strong>.
          </p>
          <div class="info-card__badge info-card__badge--free">Бесплатно</div>
        </div>

        <div class="info-card info-card--disabled">
          <div class="info-card__badge-disabled">Скоро станет доступно</div>
          <div class="info-card__icon">🏠</div>
          <h2 class="info-card__title">Доставка по городу</h2>
          <p class="info-card__text">
            Доставляем по Саяногорску в день заказа или на следующий день.
            Уточните стоимость у менеджера при оформлении.
          </p>
          <div class="info-card__badge">По договорённости</div>
        </div>
      </div>

      <div class="info-page__section">
        <h2 class="info-page__section-title">Часы работы</h2>
        <div class="schedule">
          <div class="schedule__row">
            <span class="schedule__day">Понедельник — Пятница</span>
            <span class="schedule__time">9:00 — 19:00</span>
          </div>
          <div class="schedule__row">
            <span class="schedule__day">Суббота — Воскресенье</span>
            <span class="schedule__time">9:00 — 18:00</span>
          </div>
        </div>
      </div>

      <div class="info-page__section">
        <h2 class="info-page__section-title">Условия доставки</h2>
        <ul class="info-page__list">
          <li>Перед доставкой менеджер свяжется с вами для подтверждения времени.</li>
          <li>При получении обязательно проверьте целостность товара.</li>
        </ul>
      </div>

      <div class="info-page__map">
        <h2 class="info-page__section-title">Как нас найти</h2>
        <div class="map-placeholder">
          <p>📍 Ленинградский микрорайон, 26/1н, Саяногорск, Республика Хакасия</p>
          <a
            href="https://yandex.ru/maps/-/CTVq4Dnp"
            target="_blank"
            rel="noopener"
            class="map-link"
          >Открыть в Яндекс.Картах</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.info-page {
  @include page-layout;

  &__container {
    @include container;
    max-width: 900px;
  }

  &__title {
    font-size: $font-3xl;
    font-weight: 700;
    color: $color-dark;
    margin: 0 0 $gap-xl;

    @include below-sm { font-size: $font-2xl; }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $gap-lg;
    margin-bottom: $gap-xl;

    @include below-sm { grid-template-columns: 1fr; }
  }

  &__section {
    margin-bottom: $gap-xl;
  }

  &__section-title {
    font-size: $font-xl;
    font-weight: 700;
    color: $color-dark;
    margin: 0 0 $gap-md;
  }

  &__list {
    padding-left: 20px;
    margin: 0;

    li {
      font-size: $font-md;
      color: $color-text;
      line-height: 1.7;
      margin-bottom: $gap-sm;
    }
  }

  &__map { margin-bottom: $gap-xl; }
}

.info-card {
  @include card;
  padding: $gap-lg;
  position: relative;
  transition: box-shadow 0.2s;
  overflow: hidden;

  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }

  &--disabled {
    opacity: 0.55;
    &:hover { box-shadow: none; }
  }

  &__badge-disabled {
    position: absolute;
    top: 12px;
    right: 12px;
    font-size: 11px;
    font-weight: 600;
    color: $color-text-muted;
    background: $color-bg-light;
    border: 1px solid $color-border;
    border-radius: $radius-sm;
    padding: 3px 8px;
    white-space: nowrap;
  }

  &__icon {
    font-size: 36px;
    margin-bottom: $gap-sm;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 600;
    color: $color-dark;
    margin: 0 0 $gap-sm;
  }

  &__text {
    font-size: $font-md;
    color: $color-text-muted;
    line-height: 1.65;
    margin: 0 0 $gap-md;

    strong { color: $color-dark; }
  }

  &__badge {
    display: inline-block;
    padding: 4px 12px;
    border-radius: $radius-full;
    font-size: $font-sm;
    font-weight: 600;
    background: $color-bg;
    color: $color-text-muted;

    &--free {
      background: #f0fdf4;
      color: $color-success;
    }
  }
}

.schedule {
  @include card;
  overflow: hidden;

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $gap-md $gap-lg;
    border-bottom: 1px solid $color-border-light;

    &:last-child { border-bottom: none; }

    @include below-xs {
      flex-direction: column;
      align-items: flex-start;
      gap: $gap-xs;
    }
  }

  &__day {
    font-size: $font-md;
    color: $color-text;
  }

  &__time {
    font-size: $font-md;
    font-weight: 700;
    color: $color-dark;
  }
}

.map-placeholder {
  @include card;
  padding: $gap-lg;
  text-align: center;

  p {
    font-size: $font-md;
    color: $color-text;
    margin: 0 0 $gap-md;
  }
}

.map-link {
  display: inline-block;
  padding: 10px $gap-lg;
  background: $color-primary;
  color: $color-dark;
  border-radius: $radius-md;
  font-weight: 600;
  font-size: $font-md;
  text-decoration: none;
  transition: filter 0.2s;

  &:hover { filter: brightness(0.9); }
}
</style>
