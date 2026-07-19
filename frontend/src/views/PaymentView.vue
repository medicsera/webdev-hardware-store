<script setup lang="ts">
</script>

<template>
  <div class="info-page">
    <div class="info-page__container">
      <h1 class="info-page__title">Способы оплаты</h1>

      <div class="info-page__grid">
        <div class="info-card info-card--disabled">
          <div class="info-card__badge">Скоро станет доступно</div>
          <div class="info-card__icon">💳</div>
          <h2 class="info-card__title">Банковская карта</h2>
          <p class="info-card__text">
            Оплата картами Visa, MasterCard, МИР онлайн при оформлении заказа.
            Платёж проходит через защищённый шлюз — данные карты не хранятся на сервере.
          </p>
        </div>

        <div class="info-card">
          <div class="info-card__icon">🏦</div>
          <h2 class="info-card__title">Наличными при получении</h2>
          <p class="info-card__text">
            Оплачивайте заказ наличными в момент получения в магазине или курьеру.
            Сдача обеспечивается.
          </p>
        </div>

        <div class="info-card info-card--disabled">
          <div class="info-card__badge">Скоро станет доступно</div>
          <div class="info-card__icon">📱</div>
          <h2 class="info-card__title">СБП (Система быстрых платежей)</h2>
          <p class="info-card__text">
            Переводите по номеру телефона через мобильный банк без комиссии.
            Подтверждение оплаты поступает мгновенно.
          </p>
        </div>

        <div class="info-card info-card--disabled">
          <div class="info-card__badge">Скоро станет доступно</div>
          <div class="info-card__icon">🧾</div>
          <h2 class="info-card__title">Безналичный расчёт для юр. лиц</h2>
          <p class="info-card__text">
            Выставляем счёт на реквизиты организации. Отгрузка производится после
            поступления оплаты. Предоставляем полный пакет документов.
          </p>
        </div>
      </div>

      <div class="info-page__note">
        <h3>Важно</h3>
        <ul>
          <li>При оплате картой онлайн заказ подтверждается автоматически.</li>
          <li>При оплате наличными резервирование товара действует 24 часа.</li>
          <li>Чек отправляется на email, указанный при регистрации.</li>
        </ul>
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

  &__note {
    @include card;
    padding: $gap-lg;
    background: #fffbe6;
    border-color: #ffe58f;

    h3 {
      font-size: $font-lg;
      font-weight: 600;
      color: $color-dark;
      margin: 0 0 $gap-md;
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        font-size: $font-md;
        color: $color-text;
        line-height: 1.7;
        margin-bottom: $gap-xs;
      }
    }
  }
}

.info-card {
  @include card;
  padding: $gap-lg;
  transition: box-shadow 0.2s;
  position: relative;
  overflow: hidden;

  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }

  &--disabled {
    opacity: 0.55;
    &:hover { box-shadow: none; }
  }

  &__badge {
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
    margin: 0;
  }
}
</style>
