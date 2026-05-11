import { ref } from 'vue'
import { categoryService } from '@/services/categoryApi'
import type { Category } from '@/types/category'

export function useCategories() {
    const popularCategories = ref<Category[]>([])
    const allCategories = ref<Category[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    const fetchPopularCategories = async (limit = 6) => {
        loading.value = true
        error.value = null
        try {
            popularCategories.value = await categoryService.getPopularCategories(limit)
        } catch {
            error.value = 'Не удалось загрузить категории'
            popularCategories.value = []
        } finally {
            loading.value = false
        }
    }

    const fetchAllCategories = async () => {
        loading.value = true
        error.value = null
        try {
            allCategories.value = await categoryService.getAllCategories()
        } catch {
            error.value = 'Не удалось загрузить категории'
            allCategories.value = []
        } finally {
            loading.value = false
        }
    }

    return { popularCategories, allCategories, loading, error, fetchPopularCategories, fetchAllCategories }
}
