<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const isLoading = ref(false)
const error = ref('')
const success = ref(false)

const handleSubmit = async () => {
  error.value = ''
  if (!email.value.trim()) { error.value = 'Введите почту'; return }

  isLoading.value = true
  const result = await authStore.forgotPassword(email.value.trim())
  isLoading.value = false

  if (result.success) {
    success.value = true
    setTimeout(() => {
      router.push(`/reset-password?email=${encodeURIComponent(email.value.trim())}`)
    }, 1500)
  } else if (result.error) {
    error.value = result.error
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1 class="auth-card__title">Забыли пароль?</h1>
      <p class="auth-card__subtitle">
        Укажите почту, и мы отправим код для сброса пароля.
      </p>

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

        <div v-if="error" class="form-error">{{ error }}</div>
        <div v-if="success" class="form-success">Код отправлен! Перенаправление...</div>

        <button type="submit" class="auth-card__submit" :disabled="isLoading || success">
          <span v-if="isLoading">Отправка...</span>
          <span v-else>Отправить код</span>
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

.form-success {
  background: #e8f5e9;
  border: 1px solid $color-success;
  border-radius: $radius-md;
  padding: 10px 14px;
  font-size: $font-base;
  color: $color-success;
}
</style>
