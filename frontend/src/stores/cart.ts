import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { CartItem, Product } from '@/types/product'

const CART_KEY = 'cart_items'

function loadFromStorage(): CartItem[] {
    try {
        const raw = localStorage.getItem(CART_KEY)
        return raw ? JSON.parse(raw) : []
    } catch {
        return []
    }
}

export const useCartStore = defineStore('cart', () => {
    const items = ref<CartItem[]>(loadFromStorage())

    watch(items, (val) => {
        localStorage.setItem(CART_KEY, JSON.stringify(val))
    }, { deep: true })

    const totalItems = computed(() =>
        items.value.reduce((sum, item) => sum + item.quantity, 0)
    )

    const totalPrice = computed(() =>
        items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
    )

    function addToCart(product: Product) {
        const existingItem = items.value.find(item => item.id === product.id)
        if (existingItem) {
            existingItem.quantity += product.quantity || 1
        } else {
            items.value.push({ ...product, quantity: product.quantity || 1 })
        }
    }

    function removeFromCart(productId: number) {
        items.value = items.value.filter(item => item.id !== productId)
    }

    function updateQuantity(productId: number, quantity: number) {
        const item = items.value.find(item => item.id === productId)
        if (item) item.quantity = Math.max(1, quantity)
    }

    function clearCart() {
        items.value = []
    }

    return { items, totalItems, totalPrice, addToCart, removeFromCart, updateQuantity, clearCart }
})
