import { ref } from 'vue'
import { productService } from '@/services/productApi'
import type { Product, PaginatedResponse } from '@/types/product'

/**
export function useProducts() {
    const products = ref<Product[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)
    const totalPages = ref(0)
    const currentPage = ref(0)

    const fetchProducts = async (page = 0, size = 20) => {
        loading.value = true
        error.value = null

        try {
            const response: PaginatedResponse<Product> = await productService.getProducts(page, size)
            products.value = response.content
            totalPages.value = response.totalPages
            currentPage.value = response.number
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Failed to load products'
            console.error('Error fetching products:', error.value)
        } finally {
            loading.value = false
        }
    }
    const searchProducts = async (query: string) => {
        if (!query.trim()) {
            await fetchProducts()
            return
        }

        loading.value = true
        error.value = null

        try {
            const response: PaginatedResponse<Product> = await productService.searchProducts(query)
            products.value = response.content
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Search failed'
        } finally {
            loading.value = false
        }
    }
*/
export function useProducts() {
    const products = ref<Product[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)
    const totalPages = ref(0)
    const currentPage = ref(0)

    const mockProducts: Product[] = [
        { id: 1, name: 'Дрель ударная Bosch', price: 5490, inStock: true },
        { id: 2, name: 'Набор отвёрток 12 шт', price: 890, inStock: true },
        { id: 3, name: 'Перчатки рабочие', price: 150, inStock: false },
        { id: 4, name: 'Молоток столярный', price: 450, inStock: true },
        { id: 5, name: 'Рулетка 5м', price: 320, inStock: true },
        { id: 6, name: 'Ключ гаечный', price: 280, inStock: true },
        { id: 7, name: 'Отвёртка крестовая', price: 120, inStock: true },
        { id: 8, name: 'Пассатижи', price: 380, inStock: true },
        { id: 9, name: 'Ножовка по металлу', price: 650, inStock: false },
        { id: 10, name: 'Уровень строительный', price: 890, inStock: true },
    ]

    const fetchProducts = async (page = 0, size = 20) => {
        loading.value = true
        error.value = null

        try {
            const response = await fetch(`${API_BASE_URL}/products?page=${page}&size=${size}`)

            if (response.ok) {
                const data: PaginatedResponse<Product> = await response.json()
                products.value = data.content
                totalPages.value = data.totalPages
                currentPage.value = data.number
                console.log('Загружено с бэкенда:', products.value.length)
                return
            }

            throw new Error('Backend not available')

        } catch (err) {
            console.warn('Бэкенд недоступен, используем mock-данные для products')

            await new Promise(resolve => setTimeout(resolve, 500))

            const start = page * size
            const end = start + size
            products.value = mockProducts.slice(start, end)
            totalPages.value = Math.ceil(mockProducts.length / size)
            currentPage.value = page

        } finally {
            loading.value = false
        }
    }
    return {
        products,
        loading,
        error,
        totalPages,
        currentPage,
        fetchProducts,
        //searchProducts
    }
}

