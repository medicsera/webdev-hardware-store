import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
  id: number
  firstName: string
  lastName: string
  phone: string
  email: string
  role: string
}

const USERS_STORAGE_KEY = 'app_users'
const SESSION_STORAGE_KEY = 'app_session'

function getUsers(): Record<string, { user: User; password: string }> {
  const raw = localStorage.getItem(USERS_STORAGE_KEY)
  if (!raw) return {}
  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
}

function saveUsers(users: Record<string, { user: User; password: string }>) {
  localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(users))
}

function getSession(): { email: string; password: string } | null {
  const raw = sessionStorage.getItem(SESSION_STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

function saveSession(email: string, password: string) {
  sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify({ email, password }))
}

function clearSession() {
  sessionStorage.removeItem(SESSION_STORAGE_KEY)
}

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref<User | null>(null)
  const isAuthenticated = computed(() => currentUser.value !== null)

  function init() {
    const session = getSession()
    if (!session) return

    const users = getUsers()
    const record = users[session.email.toLowerCase()]
    if (record && record.password === session.password) {
      currentUser.value = { ...record.user }
    }
  }

  init()

  function login(email: string, password: string): { success: boolean; error?: string } {
    const normalizedEmail = email.toLowerCase().trim()

    const users = getUsers()
    const record = users[normalizedEmail]

    if (!record) {
      return { success: false, error: 'Пользователь не найден' }
    }

    if (record.password !== password) {
      return { success: false, error: 'Неверный пароль' }
    }

    currentUser.value = { ...record.user }
    saveSession(normalizedEmail, password)
    return { success: true }
  }

  function register(
    firstName: string,
    lastName: string,
    phone: string,
    email: string,
    password: string
  ): { success: boolean; error?: string } {
    const normalizedEmail = email.toLowerCase().trim()
    const users = getUsers()

    if (users[normalizedEmail]) {
      return { success: false, error: 'Пользователь с такой почтой уже существует' }
    }

    const newUser: User = {
      id: Date.now(),
      firstName,
      lastName,
      phone,
      email: normalizedEmail,
      role: 'user'
    }

    users[normalizedEmail] = { user: newUser, password }
    saveUsers(users)

    currentUser.value = { ...newUser }
    saveSession(normalizedEmail, password)
    return { success: true }
  }

  function logout() {
    currentUser.value = null
    clearSession()
  }

  function updateProfile(data: { firstName: string; lastName: string; phone: string; email: string }) {
    if (!currentUser.value) return

    const session = getSession()
    if (!session) return

    const users = getUsers()
    const record = users[session.email.toLowerCase()]
    if (!record) return

    currentUser.value = {
      ...currentUser.value,
      ...data
    }

    record.user = {
      ...record.user,
      ...data
    }

    saveUsers(users)
  }

  return { currentUser, isAuthenticated, init, login, register, logout, updateProfile }
})
