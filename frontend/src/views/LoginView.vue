<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const route  = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  error.value = ''

  if (!email.value.trim()) {
    error.value = 'Введите почту'
    return
  }

  if (!password.value) {
    error.value = 'Введите пароль'
    return
  }

  isLoading.value = true

  const result = await authStore.login(email.value.trim(), password.value)

  isLoading.value = false

  if (result.success) {
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' ? redirect : '/')
  } else if (result.needsVerification) {
    router.push(`/verify?email=${encodeURIComponent(email.value.trim())}`)
  } else if (result.error) {
    error.value = result.error
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1 class="auth-card__title">Вход</h1>

      <form class="auth-card__form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-group__label" for="email">Почта</label>
          <input
            id="email"
            v-model="email"
            type="email"
            class="form-group__input"
            placeholder="example@mail.com"
            autocomplete="email"
          />
        </div>

        <div class="form-group">
          <label class="form-group__label" for="password">Пароль</label>
          <PasswordInput
            id="password"
            v-model="password"
            placeholder="Введите пароль"
            autocomplete="current-password"
            input-class="form-group__input"
          />
          <router-link to="/forgot-password" class="forgot-link">Забыли пароль?</router-link>
        </div>

        <div v-if="error" class="form-error">{{ error }}</div>

        <button
          type="submit"
          class="auth-card__submit"
          :disabled="isLoading"
        >
          <span v-if="isLoading">Вход...</span>
          <span v-else>Войти</span>
        </button>
      </form>

      <p class="auth-card__footer">
        Нет аккаунта?
        <a href="#" class="auth-card__link" @click.prevent="goToRegister">
          Зарегистрируйтесь
        </a>
      </p>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.auth-page {
  min-height: calc(100vh - #{$nav-height});
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $gap-xl $container-pad;

  @include below-sm { padding: $gap-lg $gap-md; }
}

.auth-card {
  background: $color-bg;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  padding: 32px;
  width: 100%;
  max-width: 400px;

  @include below-xs { padding: $gap-lg $gap-md; }

  &__title {
    font-size: $font-2xl;
    font-weight: 700;
    color: $color-success;
    margin: 0 0 $gap-lg;
    text-align: center;
  }

  &__form { display: flex; flex-direction: column; gap: $gap-md; }

  &__submit {
    padding: 12px $gap-lg;
    background: $color-primary;
    color: $color-dark;
    border: none;
    border-radius: $radius-pill;
    font-size: $font-md;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
    &:hover:not(:disabled) { background: $color-primary-dark; }
    &:disabled { opacity: 0.7; cursor: not-allowed; }
  }

  &__footer {
    text-align: center;
    font-size: $font-base;
    color: $color-text-secondary;
    margin: 20px 0 0;
  }

  &__link {
    color: $color-success;
    text-decoration: none;
    font-weight: 600;
    &:hover { text-decoration: underline; }
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__label { font-size: $font-base; color: $color-text; font-weight: 500; }

  &__input { @include input-field; }
}

.form-error {
  background: $color-danger-bg;
  border: 1px solid $color-danger;
  border-radius: $radius-md;
  padding: 10px 14px;
  font-size: $font-base;
  color: $color-danger;
}

.forgot-link {
  display: block;
  text-align: right;
  font-size: $font-sm;
  color: $color-success;
  text-decoration: none;
  margin-top: 4px;
  &:hover { text-decoration: underline; }
}
</style>
