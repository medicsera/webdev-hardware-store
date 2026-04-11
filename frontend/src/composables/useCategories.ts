import { ref } from 'vue'
import { categoryService } from '@/services/categoryApi'
import type { PopularCategory } from '@/types/category'

const API_BASE_URL = import.meta.env.VITE_API_URL // ← Добавьте дефолт!

export function useCategories() {
    const popularCategories = ref<PopularCategory[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    const mockPopularCategories: PopularCategory[] = [
        {
            id: 1,
            name: 'Электроинструмент',
            slug: 'electro-tools',
            productsCount: 156
        },
        {
            id: 2,
            name: 'Ручной инструмент',
            slug: 'hand-tools',
            productsCount: 243
        },
        {
            id: 3,
            name: 'Садовая техника',
            slug: 'garden',
            productsCount: 89
        },
        {
            id: 4,
            name: 'Автоинструмент',
            slug: 'auto-tools',
            productsCount: 124
        },
        {
            id: 5,
            name: 'Крепеж и метизы',
            slug: 'fasteners',
            productsCount: 312
        },
        {
            id: 6,
            name: 'Расходные материалы',
            slug: 'consumables',
            productsCount: 478
        }
    ]

    const fetchPopularCategories = async (limit = 6) => {
        console.log('🔄 fetchPopularCategories вызван, limit:', limit)
        loading.value = true
        error.value = null

        try {
            console.log('API_BASE_URL:', API_BASE_URL)
            const url = `${API_BASE_URL}/categories/popular?limit=${limit}`
            console.log('Запрос к:', url)

            const response = await fetch(url)

            if (response.ok) {
                const data: PopularCategory[] = await response.json()
                popularCategories.value = data
                console.log('Загружено категорий с бэкенда:', popularCategories.value.length)
                return
            }

            throw new Error(`Backend returned ${response.status}`)

        } catch (err) {
            console.warn('Ошибка загрузки категорий:', err)
            console.warn('Используем mock-данные')

            await new Promise(resolve => setTimeout(resolve, 500))

            popularCategories.value = mockPopularCategories.slice(0, limit)
            console.log('Загружено mock-категорий:', popularCategories.value.length)
            console.log('Первая категория:', popularCategories.value[0])

        } finally {
            loading.value = false
            console.log('loading установлен в ', loading.value)
        }
    }

    return {
        popularCategories,
        loading,
        error,
        fetchPopularCategories
    }
}