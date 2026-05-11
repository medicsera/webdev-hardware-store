import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/auth'
import { jwtDecode } from 'jwt-decode'
import { useCartStore } from '@/stores/cart'

export interface User {
    id: number
    firstName: string
    lastName: string
    phone: string
    email: string
    role: string
}

const TOKEN_KEY = 'auth_token'

function applyToken(token: string) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

function clearToken() {
    localStorage.removeItem(TOKEN_KEY)
    delete api.defaults.headers.common['Authorization']
}

export const useAuthStore = defineStore('auth', () => {
    const currentUser = ref<User | null>(null)
    const isAuthenticated = computed(() => !!currentUser.value)

    function setUserFromToken(token: string) {
        try {
            const payload: any = jwtDecode(token)
            currentUser.value = {
                id: payload.id,
                firstName: payload.firstName ?? '',
                lastName: payload.lastName ?? '',
                phone: payload.phone ?? '',
                email: payload.sub,
                role: payload.role,
            }
            applyToken(token)
        } catch (e) {
            console.error('Invalid token', e)
        }
    }

    function init() {
        const token = localStorage.getItem(TOKEN_KEY)
        if (token) {
            setUserFromToken(token)
            if (currentUser.value?.id) {
                useCartStore().initForUser(String(currentUser.value.id))
            }
        }
    }

    init()

    async function login(email: string, password: string): Promise<{ success: boolean; error?: string }> {
        try {
            const resp = await api.post('/auth/login', { username: email, password })
            const token = resp.data.token
            localStorage.setItem(TOKEN_KEY, token)
            setUserFromToken(token)
            if (currentUser.value?.id) {
                useCartStore().initForUser(String(currentUser.value.id))
            }
            return { success: true }
        } catch (err: any) {
            const msg = err?.response?.data?.message ?? 'Ошибка входа'
            return { success: false, error: msg }
        }
    }

    async function register(
        firstName: string,
        lastName: string,
        phone: string,
        email: string,
        password: string
    ): Promise<{ success: boolean; error?: string }> {
        try {
            const resp = await api.post('/auth/register', { username: email, password, firstName, lastName, phone })
            const token = resp.data.token
            localStorage.setItem(TOKEN_KEY, token)
            setUserFromToken(token)
            if (currentUser.value?.id) {
                useCartStore().initForUser(String(currentUser.value.id))
            }
            return { success: true }
        } catch (err: any) {
            const msg = err?.response?.data?.message ?? 'Ошибка регистрации'
            return { success: false, error: msg }
        }
    }

    function logout() {
        useCartStore().initForUser(null)
        currentUser.value = null
        clearToken()
    }

    async function updateProfile(data: { firstName?: string; lastName?: string; phone?: string; address?: string; email?: string; password?: string }) {
        if (!currentUser.value) return
        try {
            const token = localStorage.getItem(TOKEN_KEY) ?? ''
            await api.put('/buyer/profile', data, {
                headers: { Authorization: `Bearer ${token}` },
            })
            currentUser.value = {
                ...currentUser.value,
                firstName: data.firstName ?? currentUser.value.firstName,
                lastName: data.lastName ?? currentUser.value.lastName,
                phone: data.phone ?? currentUser.value.phone,
            }
        } catch (e) {
            console.error('Profile update failed', e)
        }
    }

    return { currentUser, isAuthenticated, login, register, logout, updateProfile }
})
