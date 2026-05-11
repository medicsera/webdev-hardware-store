import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { Product } from '@/types/product'

export interface CartItem {
    id: number
    name: string
    price: number
    imageUrls: string[]
    cartQuantity: number
    stock: number
}

const CART_KEY_PREFIX = 'cart_'
let currentKey = `${CART_KEY_PREFIX}guest`

function loadFromStorage(key: string): CartItem[] {
    try {
        const raw = localStorage.getItem(key)
        return raw ? JSON.parse(raw) : []
    } catch {
        return []
    }
}

export const useCartStore = defineStore('cart', () => {
    const items = ref<CartItem[]>(loadFromStorage(currentKey))

    watch(items, (val) => {
        localStorage.setItem(currentKey, JSON.stringify(val))
    }, { deep: true })

    const totalItems = computed(() =>
        items.value.reduce((sum, item) => sum + item.cartQuantity, 0)
    )

    const totalPrice = computed(() =>
        items.value.reduce((sum, item) => sum + item.price * item.cartQuantity, 0)
    )

    function initForUser(userId: string | null) {
        localStorage.setItem(currentKey, JSON.stringify(items.value))
        currentKey = `${CART_KEY_PREFIX}${userId ?? 'guest'}`
        items.value = loadFromStorage(currentKey)
    }

    function addToCart(product: Product, qty = 1) {
        const existing = items.value.find(item => item.id === product.id)
        if (existing) {
            existing.cartQuantity = Math.min(existing.cartQuantity + qty, existing.stock)
        } else {
            items.value.push({
                id: product.id,
                name: product.name,
                price: product.price,
                imageUrls: product.imageUrls,
                cartQuantity: Math.min(qty, product.quantity),
                stock: product.quantity,
            })
        }
    }

    function removeFromCart(productId: number) {
        items.value = items.value.filter(item => item.id !== productId)
    }

    function updateQuantity(productId: number, quantity: number) {
        const item = items.value.find(item => item.id === productId)
        if (item) item.cartQuantity = Math.max(1, Math.min(quantity, item.stock))
    }

    function clearCart() {
        items.value = []
    }

    return { items, totalItems, totalPrice, initForUser, addToCart, removeFromCart, updateQuantity, clearCart }
})
