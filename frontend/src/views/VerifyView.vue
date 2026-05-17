<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = (route.query.email as string) || ''
const code = ref('')
const isLoading = ref(false)
const error = ref('')
const resendCooldown = ref(0)
const resendSuccess = ref(false)

let cooldownTimer: ReturnType<typeof setInterval> | null = null

const handleVerify = async () => {
  error.value = ''
  if (code.value.trim().length !== 6) {
    error.value = 'Введите 6-значный код из письма'
    return
  }
  isLoading.value = true
  const result = await authStore.verifyEmail(email, code.value.trim())
  isLoading.value = false
  if (result.success) {
    router.push('/')
  } else {
    error.value = result.error || 'Ошибка подтверждения'
  }
}

const handleResend = async () => {
  if (resendCooldown.value > 0) return
  error.value = ''
  resendSuccess.value = false
  const result = await authStore.resendCode(email)
  if (result.success) {
    resendSuccess.value = true
    resendCooldown.value = 60
    cooldownTimer = setInterval(() => {
      resendCooldown.value--
      if (resendCooldown.value <= 0 && cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }, 1000)
  } else {
    error.value = result.error || 'Ошибка отправки'
  }
}

onUnmounted(() => {
  if (cooldownTimer) clearInterval(cooldownTimer)
})
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <div class="auth-card__icon">✉</div>
      <h1 class="auth-card__title">Подтверждение email</h1>
      <p class="auth-card__subtitle">
        Мы отправили 6-значный код на<br/>
        <strong>{{ email }}</strong>
      </p>

      <form class="auth-card__form" @submit.prevent="handleVerify">
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

        <div v-if="error" class="form-error">{{ error }}</div>
        <div v-if="resendSuccess" class="form-success">Код отправлен повторно</div>

        <button
          type="submit"
          class="auth-card__submit"
          :disabled="isLoading"
        >
          <span v-if="isLoading">Проверка...</span>
          <span v-else>Подтвердить</span>
        </button>
      </form>

      <p class="auth-card__footer">
        Не пришло письмо?
        <button
          class="resend-btn"
          :disabled="resendCooldown > 0"
          @click="handleResend"
        >
          <span v-if="resendCooldown > 0">Отправить повторно ({{ resendCooldown }}с)</span>
          <span v-else>Отправить повторно</span>
        </button>
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

  &__icon {
    font-size: 40px;
    margin-bottom: $gap-md;
    display: block;
  }

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
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: $font-base;
    color: $color-text-secondary;
    margin: 20px 0 0;
    flex-wrap: wrap;
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

.resend-btn {
  background: none;
  border: none;
  padding: 0;
  font-size: $font-base;
  color: $color-success;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  &:hover:not(:disabled) { text-decoration: underline; }
  &:disabled { opacity: 0.5; cursor: default; }
}
</style>
