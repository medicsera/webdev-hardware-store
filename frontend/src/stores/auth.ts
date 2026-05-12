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
    address: string
    email: string
    role: string
}

const TOKEN_KEY = 'auth_token'

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
                email: payload.sub,
                role: payload.role,
                firstName: '',
                lastName: '',
                phone: '',
                address: '',
            }
        } catch (e) {
            console.error('Invalid token', e)
        }
    }

    async function fetchProfile() {
        try {
            const { data } = await api.get('/buyer/profile')
            if (currentUser.value) {
                currentUser.value = {
                    ...currentUser.value,
                    firstName: data.firstName ?? '',
                    lastName: data.lastName ?? '',
                    phone: data.phone ?? '',
                    address: data.address ?? '',
                }
            }
        } catch {
            // токен протух или недействителен — разлогиниваем
            clearToken()
            currentUser.value = null
        }
    }

    function init() {
        const token = localStorage.getItem(TOKEN_KEY)
        if (token) {
            setUserFromToken(token)
            if (currentUser.value?.id) {
                useCartStore().initForUser(String(currentUser.value.id))
                fetchProfile()
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
            await fetchProfile()
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
            await fetchProfile()
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
        await api.put('/buyer/profile', data)
        await fetchProfile()
    }

    return { currentUser, isAuthenticated, login, register, logout, updateProfile }
})
