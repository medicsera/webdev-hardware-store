import { ref } from 'vue'
import type { PopularCategory } from '@/types/category'

const API_BASE_URL = import.meta.env.VITE_API_URL

const mockPopularCategories: PopularCategory[] = [
    { id: 1, name: 'Электроинструмент', slug: 'electro-tools', productsCount: 156 },
    { id: 2, name: 'Ручной инструмент', slug: 'hand-tools', productsCount: 243 },
    { id: 3, name: 'Садовая техника', slug: 'garden', productsCount: 89 },
    { id: 4, name: 'Автоинструмент', slug: 'auto-tools', productsCount: 124 },
    { id: 5, name: 'Крепеж и метизы', slug: 'fasteners', productsCount: 312 },
    { id: 6, name: 'Расходные материалы', slug: 'consumables', productsCount: 478 }
]

export function useCategories() {
    const popularCategories = ref<PopularCategory[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    const fetchPopularCategories = async (limit = 6) => {
        loading.value = true
        error.value = null

        try {
            const response = await fetch(`${API_BASE_URL}/categories/popular?limit=${limit}`)
            if (response.ok) {
                const data: PopularCategory[] = await response.json()
                popularCategories.value = data
                return
            }
            throw new Error(`Backend returned ${response.status}`)
        } catch {
            await new Promise(resolve => setTimeout(resolve, 500))
            popularCategories.value = mockPopularCategories.slice(0, limit)
        } finally {
            loading.value = false
        }
    }

    return {
        popularCategories,
        loading,
        error,
        fetchPopularCategories
    }
}