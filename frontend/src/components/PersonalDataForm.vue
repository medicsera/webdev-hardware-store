<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { ProfileData } from '@/types/profile'

const authStore = useAuthStore()

const form = ref<ProfileData>({
  firstName: '',
  lastName: '',
  phone: '',
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
      email: user.email,
      password: ''
    }
  }
})

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

  authStore.updateProfile({
    firstName: form.value.firstName,
    lastName: form.value.lastName,
    phone: form.value.phone,
    email: form.value.email
  })

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
      <input
        id="password"
        ref="passwordInput"
        v-model="form.password"
        type="password"
        class="form-group__input"
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
}

.form-group {
  display: flex;
  align-items: center;
  gap: 12px;

  &__label {
    width: 130px;
    flex-shrink: 0;
    font-size: 13px;
    color: #333;
    font-weight: 500;
    text-align: right;
  }

  &__input {
    flex: 1;
    max-width: 220px;
    height: 28px;
    padding: 0 10px;
    border: 1px solid #ccc;
    border-radius: 3px;
    font-size: 13px;
    background: #fff;
    transition: border-color 0.2s;

    &:focus {
      outline: none;
      border-color: #f4b942;
    }

    &:disabled {
      background: #f0f0f0;
      color: #666;
      cursor: default;
    }
  }
}

.change-btn {
  padding: 4px 14px;
  background: none;
  border: 1px solid #f4b942;
  border-radius: 4px;
  font-size: 12px;
  color: #f4b942;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover {
    background: #f4b942;
    color: #fff;
  }
}

.form-error {
  margin-left: 146px;
  background: #fdecea;
  border: 1px solid #e74c3c;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 13px;
  color: #e74c3c;
}

.form-success {
  margin-left: 146px;
  background: #e8f5e9;
  border: 1px solid #27ae60;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 13px;
  color: #27ae60;
}

.form-submit {
  padding: 8px 28px;
  background: #f4b942;
  color: #2c3e50;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  align-self: flex-start;
  margin-left: 146px;

  &:hover:not(:disabled) {
    background: #e0a830;
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}
</style>
