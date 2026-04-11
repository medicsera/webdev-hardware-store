import { ref } from 'vue'
import { productService } from '@/services/productApi'
import type { Product, PaginatedResponse } from '@/types/product'

const mockProducts: Product[] = [
    { id: 1, name: 'Дрель ударная Bosch', price: 5490, inStock: true, image: '/placeholder-product.jpg' },
    { id: 2, name: 'Набор отвёрток 12 шт', price: 890, inStock: true, image: '/placeholder-product.jpg' },
    { id: 3, name: 'Перчатки рабочие', price: 150, inStock: false, image: '/placeholder-product.jpg' },
    { id: 4, name: 'Молоток столярный', price: 450, inStock: true, image: '/placeholder-product.jpg' },
    { id: 5, name: 'Рулетка 5м', price: 320, inStock: true, image: '/placeholder-product.jpg' },
    { id: 6, name: 'Ключ гаечный', price: 280, inStock: true, image: '/placeholder-product.jpg' },
    { id: 7, name: 'Отвёртка крестовая', price: 120, inStock: true, image: '/placeholder-product.jpg' },
    { id: 8, name: 'Пассатижи', price: 380, inStock: true, image: '/placeholder-product.jpg' },
    { id: 9, name: 'Ножовка по металлу', price: 650, inStock: false, image: '/placeholder-product.jpg' },
    { id: 10, name: 'Уровень строительный', price: 890, inStock: true, image: '/placeholder-product.jpg' },
]

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
            console.log('Загружено с бэкенда:', products.value.length)
        } catch {
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
    }
}

