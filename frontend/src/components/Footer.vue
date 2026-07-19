<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-grid">
        <!-- Товар -->
        <div class="footer-column">
          <h3 class="footer-title">Товар</h3>
          <ul class="footer-links">
            <li>
              <router-link to="/payment">Способы оплаты</router-link>
            </li>
            <li>
              <router-link to="/delivery">Доставка</router-link>
            </li>
          </ul>
        </div>

        <!-- Сайт -->
        <div class="footer-column">
          <h3 class="footer-title">Сайт</h3>
          <ul class="footer-links">
            <li>
              <router-link to="/privacy">Политика конфиденциальности</router-link>
            </li>
          </ul>
        </div>

        <!-- Контакты -->
        <div class="footer-column">
          <h3 class="footer-title">Контакты</h3>
          <ul class="footer-links">
            <li>
              <a href="tel:+79235997489">Сотовый</a>
            </li>
            <li>
              <a href="mailto:info@factura.ru">Почта</a>
            </li>
            <li>
              <a href="https://vk.com/facturasayan" target="_blank" rel="noopener">ВКонтакте</a>
            </li>
          </ul>
        </div>

        <div class="footer-column">
          <h3 class="footer-title">Адрес</h3>
          <div class="footer-address">
            <p>Ленинградский микрорайон, 26/1 н,</p>
            <p>Саяногорск, Республика Хакасия</p>
            <p class="footer-hours">Пн-пт с 9:00 до 19:00</p>
            <p class="footer-hours">Сб-вс с 9:00 до 18:00</p>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; {{ currentYear }} Factura. Все права защищены.</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style lang="scss" scoped>
.app-footer {
  background-color: $color-bg-dark;
  color: #fff;
  padding: $gap-xl 0 $gap-md;
  margin-top: 60px;
  width: 100%;

  @include below-sm { padding: $gap-lg 0 $gap-md; margin-top: 40px; }
}

.footer-container {
  @include container;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $gap-xl;
  margin-bottom: $gap-xl;

  @include below-lg { grid-template-columns: repeat(2, 1fr); gap: 30px; }
  @include below-sm { grid-template-columns: repeat(2, 1fr); gap: $gap-md; }
  @include below-xs { grid-template-columns: 1fr; gap: $gap-lg; }
}

.footer-column {
  h3 {
    font-size: $font-lg;
    font-weight: 600;
    margin-bottom: $gap-md;
    color: #fff;

    @include below-sm { font-size: $font-md; margin-bottom: $gap-sm; }
  }
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 10px;
    &:last-child { margin-bottom: 0; }

    a {
      color: #cccccc;
      text-decoration: none;
      font-size: $font-md;
      transition: color 0.2s;
      &:hover { color: $color-success-light; }

      @include below-sm { font-size: $font-base; }
    }
  }
}

.footer-address {
  p {
    margin: 0 0 $gap-sm;
    font-size: $font-md;
    color: #cccccc;
    line-height: 1.5;

    @include below-sm { font-size: $font-base; }
  }
}

.footer-hours {
  margin-top: 12px !important;
  color: #fff;
  font-weight: 500;
}

.footer-bottom {
  border-top: 1px solid #555555;
  padding-top: $gap-md;
  text-align: center;

  p {
    margin: 0;
    font-size: $font-base;
    color: #999999;
  }
}
</style>