<script setup lang="ts">
</script>

<template>
  <div class="info-page">
    <div class="info-page__container">
      <h1 class="info-page__title">Политика конфиденциальности</h1>
      <p class="info-page__date">Дата вступления в силу: 1 января 2025 г.</p>

      <div class="privacy-section">
        <h2>1. Общие положения</h2>
        <p>
          Настоящая Политика конфиденциальности регулирует порядок обработки персональных
          данных пользователей интернет-магазина Factura (далее — «Магазин»), расположенного
          по адресу: Ленинградский микрорайон, 26/1н, Саяногорск, Республика Хакасия.
        </p>
        <p>
          Используя сайт, вы подтверждаете своё согласие с условиями настоящей Политики.
        </p>
      </div>

      <div class="privacy-section">
        <h2>2. Собираемые данные</h2>
        <p>В процессе работы сайта мы можем собирать следующие данные:</p>
        <ul>
          <li>Имя и контактные данные (email, номер телефона);</li>
          <li>Адрес доставки;</li>
          <li>Данные об оформленных заказах;</li>
          <li>Технические данные: IP-адрес, тип браузера, время посещения.</li>
        </ul>
      </div>

      <div class="privacy-section">
        <h2>3. Цели обработки</h2>
        <p>Данные используются исключительно для:</p>
        <ul>
          <li>Оформления и выполнения заказов;</li>
          <li>Связи с пользователем по вопросам заказа;</li>
          <li>Улучшения работы сайта и сервиса;</li>
          <li>Исполнения требований законодательства РФ.</li>
        </ul>
      </div>

      <div class="privacy-section">
        <h2>4. Передача данных третьим лицам</h2>
        <p>
          Мы не передаём персональные данные третьим лицам без вашего согласия, за
          исключением случаев, предусмотренных законодательством Российской Федерации
          (например, по запросу государственных органов).
        </p>
      </div>

      <div class="privacy-section">
        <h2>5. Безопасность данных</h2>
        <p>
          Для защиты ваших данных применяются технические и организационные меры:
          шифрование передачи данных (HTTPS), ограниченный доступ персонала к
          персональным данным, регулярный аудит систем безопасности.
        </p>
      </div>

      <div class="privacy-section">
        <h2>6. Cookies</h2>
        <p>
          Сайт использует файлы cookie для корректной работы функций (например, корзины
          и авторизации). Вы можете отключить cookie в настройках браузера, однако это
          может ограничить функциональность сайта.
        </p>
      </div>

      <div class="privacy-section">
        <h2>7. Права пользователя</h2>
        <p>Вы вправе:</p>
        <ul>
          <li>Получить информацию о хранимых данных;</li>
          <li>Запросить исправление или удаление своих данных;</li>
          <li>Отозвать согласие на обработку данных.</li>
        </ul>
        <p>Для реализации прав обратитесь по адресу: <a href="mailto:info@factura.ru">info@factura.ru</a></p>
      </div>

      <div class="privacy-section">
        <h2>8. Изменения политики</h2>
        <p>
          Мы оставляем за собой право вносить изменения в данную Политику.
          Актуальная версия всегда доступна на этой странице. При существенных изменениях
          пользователи будут уведомлены по email.
        </p>
      </div>

      <div class="privacy-contact">
        <h3>Контакты</h3>
        <p>По вопросам конфиденциальности:</p>
        <p><a href="mailto:info@factura.ru">info@factura.ru</a></p>
        <p><a href="tel:+79235997489">+7 (923) 599-74-89</a></p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.info-page {
  @include page-layout;

  &__container {
    @include container;
    max-width: 800px;
  }

  &__title {
    font-size: $font-3xl;
    font-weight: 700;
    color: $color-dark;
    margin: 0 0 $gap-sm;

    @include below-sm { font-size: $font-2xl; }
  }

  &__date {
    font-size: $font-sm;
    color: $color-text-muted;
    margin: 0 0 $gap-xl;
  }
}

.privacy-section {
  margin-bottom: $gap-xl;

  h2 {
    font-size: $font-xl;
    font-weight: 700;
    color: $color-dark;
    margin: 0 0 $gap-md;
    padding-bottom: $gap-sm;
    border-bottom: 2px solid $color-primary;
  }

  p {
    font-size: $font-md;
    color: $color-text;
    line-height: 1.75;
    margin: 0 0 $gap-sm;

    a {
      color: $color-primary-dark;
      text-decoration: underline;
      &:hover { color: $color-dark; }
    }
  }

  ul {
    padding-left: 24px;
    margin: $gap-sm 0;

    li {
      font-size: $font-md;
      color: $color-text;
      line-height: 1.7;
      margin-bottom: $gap-xs;
    }
  }
}

.privacy-contact {
  @include card;
  padding: $gap-lg;
  background: $color-bg;

  h3 {
    font-size: $font-lg;
    font-weight: 700;
    color: $color-dark;
    margin: 0 0 $gap-sm;
  }

  p {
    font-size: $font-md;
    color: $color-text;
    margin: 0 0 $gap-xs;

    a {
      color: $color-primary-dark;
      text-decoration: none;
      font-weight: 500;
      &:hover { text-decoration: underline; }
    }
  }
}
</style>
