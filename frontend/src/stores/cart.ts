import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { Product } from '@/types/product'

export interface CartItem {
    id: number
    name: string
    price: number
    imageUrls: string[]
    cartQuantity: number
}

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
        items.value.reduce((sum, item) => sum + item.cartQuantity, 0)
    )

    const totalPrice = computed(() =>
        items.value.reduce((sum, item) => sum + item.price * item.cartQuantity, 0)
    )

    function addToCart(product: Product, qty = 1) {
        const existing = items.value.find(item => item.id === product.id)
        if (existing) {
            existing.cartQuantity += qty
        } else {
            items.value.push({
                id: product.id,
                name: product.name,
                price: product.price,
                imageUrls: product.imageUrls,
                cartQuantity: qty,
            })
        }
    }

    function removeFromCart(productId: number) {
        items.value = items.value.filter(item => item.id !== productId)
    }

    function updateQuantity(productId: number, quantity: number) {
        const item = items.value.find(item => item.id === productId)
        if (item) item.cartQuantity = Math.max(1, quantity)
    }

    function clearCart() {
        items.value = []
    }

    return { items, totalItems, totalPrice, addToCart, removeFromCart, updateQuantity, clearCart }
})
