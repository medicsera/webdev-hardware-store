<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { ProfileData } from '@/types/profile'
import PasswordInput from '@/components/PasswordInput.vue'

const authStore = useAuthStore()

const form = ref<ProfileData>({
  firstName: '',
  lastName: '',
  phone: '',
  address: '',
  email: '',
  password: ''
})

const isSaving = ref(false)
const savedMessage = ref('')
const error = ref('')

const editingPhone = ref(false)
const editingEmail = ref(false)
const editingPassword = ref(false)

const phoneInput = ref<HTMLInputElement | null>(null)
const emailInput = ref<HTMLInputElement | null>(null)
const passwordInput = ref<HTMLInputElement | null>(null)

onMounted(() => {
  const user = authStore.currentUser
  if (user) {
    form.value = {
      firstName: user.firstName,
      lastName: user.lastName,
      phone: user.phone,
      address: user.address,
      email: user.email,
      password: ''
    }
  }
})

function isValidPhone(value: string): boolean {
  const digits = value.replace(/\D/g, '')
  return digits.length === 11 && (digits[0] === '7' || digits[0] === '8')
}

const handleSubmit = async () => {
  error.value = ''
  savedMessage.value = ''

  if (!form.value.firstName.trim()) {
    error.value = 'Введите имя'
    return
  }
  if (!form.value.lastName.trim()) {
    error.value = 'Введите фамилию'
    return
  }
  if (!form.value.phone.trim()) {
    error.value = 'Введите номер телефона'
    return
  }
  if (!isValidPhone(form.value.phone)) {
    error.value = 'Некорректный номер телефона'
    return
  }
  if (!form.value.email.trim()) {
    error.value = 'Введите почту'
    return
  }
  if (editingPassword.value && form.value.password.length > 0 && form.value.password.length < 6) {
    error.value = 'Пароль должен содержать минимум 6 символов'
    return
  }

  isSaving.value = true

  await new Promise(r => setTimeout(r, 500))

  await authStore.updateProfile({
    firstName: form.value.firstName,
    lastName: form.value.lastName,
    phone: form.value.phone,
    address: form.value.address,
    email: form.value.email,
    ...(form.value.password ? { password: form.value.password } : {})
  })

  if (form.value.password) {
    form.value.password = ''
    editingPassword.value = false
  }

  isSaving.value = false
  savedMessage.value = 'Данные сохранены'

  setTimeout(() => {
    savedMessage.value = ''
  }, 3000)
}

const startEditPhone = () => {
  editingPhone.value = true
  setTimeout(() => phoneInput.value?.focus(), 50)
}

const startEditEmail = () => {
  editingEmail.value = true
  setTimeout(() => emailInput.value?.focus(), 50)
}

const startEditPassword = () => {
  editingPassword.value = true
  setTimeout(() => passwordInput.value?.focus(), 50)
}
</script>

<template>
  <form class="personal-data-form" @submit.prevent="handleSubmit">
    <div class="form-group">
      <label class="form-group__label" for="firstName">Имя:</label>
      <input
        id="firstName"
        v-model="form.firstName"
        type="text"
        class="form-group__input"
      />
    </div>

    <div class="form-group">
      <label class="form-group__label" for="lastName">Фамилия:</label>
      <input
        id="lastName"
        v-model="form.lastName"
        type="text"
        class="form-group__input"
      />
    </div>

    <div class="form-group">
      <label class="form-group__label" for="phone">Номер телефона:</label>
      <input
        id="phone"
        ref="phoneInput"
        v-model="form.phone"
        type="tel"
        class="form-group__input"
        :disabled="!editingPhone"
      />
      <button
        v-if="!editingPhone"
        type="button"
        class="change-btn"
        @click="startEditPhone"
      >
        Изменить
      </button>
    </div>

    <div class="form-group">
      <label class="form-group__label" for="address">Адрес доставки:</label>
      <input
        id="address"
        v-model="form.address"
        type="text"
        class="form-group__input"
        placeholder="ул. Примерная, д. 1, кв. 10"
      />
    </div>

    <div class="form-group">
      <label class="form-group__label" for="email">Почта:</label>
      <input
        id="email"
        ref="emailInput"
        v-model="form.email"
        type="email"
        class="form-group__input"
        :disabled="!editingEmail"
      />
      <button
        v-if="!editingEmail"
        type="button"
        class="change-btn"
        @click="startEditEmail"
      >
        Изменить
      </button>
    </div>

    <div class="form-group">
      <label class="form-group__label" for="password">Пароль:</label>
      <PasswordInput
        id="password"
        v-model="form.password"
        :disabled="!editingPassword"
      />
      <button
        v-if="!editingPassword"
        type="button"
        class="change-btn"
        @click="startEditPassword"
      >
        Изменить
      </button>
    </div>

    <div v-if="error" class="form-error">{{ error }}</div>
    <div v-if="savedMessage" class="form-success">{{ savedMessage }}</div>

    <button
      type="submit"
      class="form-submit"
      :disabled="isSaving"
    >
      <span v-if="isSaving">Сохранение...</span>
      <span v-else>Сохранить</span>
    </button>
  </form>
</template>

<style lang="scss" scoped>
.personal-data-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 500px;

  @include below-sm { max-width: 100%; }
}

.form-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  @include below-xs { flex-direction: column; align-items: flex-start; }

  &__label {
    width: 130px;
    flex-shrink: 0;
    font-size: $font-base;
    color: #333;
    font-weight: 500;
    text-align: right;

    @include below-xs { width: auto; text-align: left; }
  }

  &__input {
    flex: 1;
    max-width: 220px;
    @include input-field;

    @include below-xs { max-width: 100%; width: 100%; }
  }
}

:deep(.password-field) {
  flex: 1;
  max-width: 220px;
  @include below-xs { max-width: 100%; width: 100%; }
}

.change-btn {
  padding: 6px 14px;
  background: none;
  border: 1px solid $color-primary;
  border-radius: $radius-sm;
  font-size: $font-sm;
  color: $color-primary;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover { background: $color-primary; color: #fff; }
}

.form-error {
  margin-left: 146px;
  background: $color-danger-bg;
  border: 1px solid $color-danger;
  border-radius: $radius-sm;
  padding: $gap-sm 12px;
  font-size: $font-base;
  color: $color-danger;

  @include below-xs { margin-left: 0; }
}

.form-success {
  margin-left: 146px;
  background: #e8f5e9;
  border: 1px solid $color-success;
  border-radius: $radius-sm;
  padding: $gap-sm 12px;
  font-size: $font-base;
  color: $color-success;

  @include below-xs { margin-left: 0; }
}

.form-submit {
  padding: $gap-sm 28px;
  background: $color-primary;
  color: $color-dark;
  border: none;
  border-radius: $radius-pill;
  font-size: $font-md;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  align-self: flex-start;
  margin-left: 146px;

  @include below-xs { margin-left: 0; align-self: stretch; }

  &:hover:not(:disabled) { background: $color-primary-dark; }
  &:disabled { opacity: 0.7; cursor: not-allowed; }
}
</style>
