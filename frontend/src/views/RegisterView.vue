<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const firstName = ref('')
const lastName = ref('')
const phone = ref('')
const email = ref('')
const password = ref('')
const isLoading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  error.value = ''

  // …validation omitted for brevity (same as before) …

  isLoading.value = true

  const result = await authStore.register(
      firstName.value.trim(),
      lastName.value.trim(),
      phone.value.trim(),
      email.value.trim(),
      password.value
  )

  isLoading.value = false

  if (result.success) {
    router.push('/')
  } else if (result.error) {
    error.value = result.error
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1 class="auth-card__title">Регистрация</h1>

      <form class="auth-card__form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-group__label" for="firstName">Имя</label>
          <input
            id="firstName"
            v-model="firstName"
            type="text"
            class="form-group__input"
            placeholder="Введите имя"
            autocomplete="given-name"
          />
        </div>

        <div class="form-group">
          <label class="form-group__label" for="lastName">Фамилия</label>
          <input
            id="lastName"
            v-model="lastName"
            type="text"
            class="form-group__input"
            placeholder="Введите фамилию"
            autocomplete="family-name"
          />
        </div>

        <div class="form-group">
          <label class="form-group__label" for="phone">Номер телефона</label>
          <input
            id="phone"
            v-model="phone"
            type="tel"
            class="form-group__input"
            placeholder="+7 (___) ___-__-__"
            autocomplete="tel"
          />
        </div>

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
          <input
            id="password"
            v-model="password"
            type="password"
            class="form-group__input"
            placeholder="Введите пароль"
            autocomplete="new-password"
          />
        </div>

        <div v-if="error" class="form-error">{{ error }}</div>

        <button
          type="submit"
          class="auth-card__submit"
          :disabled="isLoading"
        >
          <span v-if="isLoading">Регистрация...</span>
          <span v-else>Зарегистрироваться</span>
        </button>
      </form>

      <p class="auth-card__footer">
        Уже есть аккаунт?
        <a href="#" class="auth-card__link" @click.prevent="goToLogin">
          Войдите
        </a>
      </p>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.auth-page {
  min-height: calc(100vh - 110px);
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.auth-card {
  background: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 12px;
  padding: 32px;
  width: 100%;
  max-width: 400px;

  &__title {
    font-size: 22px;
    font-weight: 700;
    color: #27ae60;
    margin: 0 0 24px;
    text-align: center;
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__submit {
    padding: 12px 24px;
    background: #f4b942;
    color: #2c3e50;
    border: none;
    border-radius: 24px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;

    &:hover:not(:disabled) {
      background: #e0a830;
    }

    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
  }

  &__footer {
    text-align: center;
    font-size: 13px;
    color: #666;
    margin: 20px 0 0;
  }

  &__link {
    color: #27ae60;
    text-decoration: none;
    font-weight: 600;

    &:hover {
      text-decoration: underline;
    }
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__label {
    font-size: 13px;
    color: #555;
    font-weight: 500;
  }

  &__input {
    padding: 10px 14px;
    border: 1px solid #ccc;
    border-radius: 20px;
    font-size: 14px;
    background: #fff;
    transition: border-color 0.2s;

    &:focus {
      outline: none;
      border-color: #f4b942;
    }

    &::placeholder {
      color: #aaa;
    }
  }
}

.form-error {
  background: #fdecea;
  border: 1px solid #e74c3c;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: #e74c3c;
}
</style>
