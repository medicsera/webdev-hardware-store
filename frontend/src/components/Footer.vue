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
            <li>
              <a href="https://t.me/factura" target="_blank" rel="noopener">Телеграмм</a>
            </li>
            <li>
              <a href="https://wa.me/79235997489" target="_blank" rel="noopener">WhatsApp</a>
            </li>
          </ul>
        </div>

        <div class="footer-column">
          <h3 class="footer-title">Адрес</h3>
          <div class="footer-address">
            <p>Микрорайон Ленинградский, 26Б,</p>
            <p>Саяногорск, Республика Хакасия</p>
            <p class="footer-hours">Ежедневно с 9:00 до 18:00</p>
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
  background-color: #3a3a3a;
  color: #ffffff;
  padding: 40px 0 20px;
  margin-top: 60px;
  width: 100%;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 40px;

  @media (max-width: 968px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 30px;
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}

.footer-column {
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #ffffff;
  }
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 10px;

    &:last-child {
      margin-bottom: 0;
    }

    a {
      color: #cccccc;
      text-decoration: none;
      font-size: 14px;
      transition: color 0.2s;

      &:hover {
        color: #42b983;
      }
    }
  }
}

.footer-address {
  p {
    margin: 0 0 8px;
    font-size: 14px;
    color: #cccccc;
    line-height: 1.5;
  }
}

.footer-hours {
  margin-top: 12px !important;
  color: #ffffff;
  font-weight: 500;
}

.footer-bottom {
  border-top: 1px solid #555555;
  padding-top: 20px;
  text-align: center;

  p {
    margin: 0;
    font-size: 13px;
    color: #999999;
  }
}
</style>