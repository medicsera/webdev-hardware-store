<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = (route.query.email as string) || ''
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const error = ref('')
const success = ref(false)

const handleReset = async () => {
  error.value = ''
  if (!code.value.trim()) { error.value = 'Введите код из письма'; return }
  if (newPassword.value.length < 6) { error.value = 'Пароль должен содержать минимум 6 символов'; return }
  if (newPassword.value !== confirmPassword.value) { error.value = 'Пароли не совпадают'; return }

  isLoading.value = true
  const result = await authStore.resetPassword(email, code.value.trim(), newPassword.value)
  isLoading.value = false

  if (result.success) {
    success.value = true
    setTimeout(() => { router.push('/login') }, 2000)
  } else if (result.error) {
    error.value = result.error
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1 class="auth-card__title">Новый пароль</h1>
      <p class="auth-card__subtitle">
        Введите код из письма и придумайте новый пароль для
        <strong>{{ email }}</strong>
      </p>

      <form class="auth-card__form" @submit.prevent="handleReset">
        <div class="form-group">
          <label class="form-group__label" for="code">Код из письма</label>
          <input
            id="code"
            v-model="code"
            type="text"
            class="form-group__input form-group__input--code"
            placeholder="000000"
            maxlength="6"
            autocomplete="one-time-code"
            inputmode="numeric"
          />
        </div>

        <div class="form-group">
          <label class="form-group__label" for="newPassword">Новый пароль</label>
          <PasswordInput
            id="newPassword"
            v-model="newPassword"
            placeholder="Минимум 6 символов"
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label class="form-group__label" for="confirmPassword">Повторите пароль</label>
          <PasswordInput
            id="confirmPassword"
            v-model="confirmPassword"
            placeholder="Повторите пароль"
            autocomplete="new-password"
          />
        </div>

        <div v-if="error" class="form-error">{{ error }}</div>
        <div v-if="success" class="form-success">Пароль изменён! Перенаправление на вход...</div>

        <button type="submit" class="auth-card__submit" :disabled="isLoading || success">
          <span v-if="isLoading">Сохранение...</span>
          <span v-else>Сохранить пароль</span>
        </button>
      </form>

      <p class="auth-card__footer">
        <a href="#" class="auth-card__link" @click.prevent="$router.push('/login')">Вернуться ко входу</a>
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
  text-align: center;

  @include below-xs { padding: $gap-lg $gap-md; }

  &__title {
    font-size: $font-2xl;
    font-weight: 700;
    color: $color-success;
    margin: 0 0 $gap-sm;
  }

  &__subtitle {
    font-size: $font-base;
    color: $color-text-secondary;
    margin: 0 0 $gap-lg;
    line-height: 1.5;
    strong { color: $color-dark; }
  }

  &__form { display: flex; flex-direction: column; gap: $gap-md; text-align: left; }

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
  &__input {
    padding: 10px 14px;
    border: 1px solid #ccc;
    border-radius: $radius-pill;
    font-size: $font-md;
    background: #fff;
    transition: border-color 0.2s;
    &:focus { outline: none; border-color: $color-primary; }
    &::placeholder { color: $color-text-faint; }

    &--code {
      text-align: center;
      font-size: $font-2xl;
      font-weight: 700;
      letter-spacing: 8px;
    }
  }
}

.form-error {
  background: $color-danger-bg;
  border: 1px solid $color-danger;
  border-radius: $radius-md;
  padding: 10px 14px;
  font-size: $font-base;
  color: $color-danger;
}

.form-success {
  background: #e8f5e9;
  border: 1px solid $color-success;
  border-radius: $radius-md;
  padding: 10px 14px;
  font-size: $font-base;
  color: $color-success;
}
</style>
