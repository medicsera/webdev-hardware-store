import { ref } from 'vue'
import { categoryService } from '@/services/categoryApi'
import type { CategoryWithSubcategories } from '@/types/category'

export function useCatalogMenu() {
    const categories = ref<CategoryWithSubcategories[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    const fetchCategoriesTree = async () => {
        loading.value = true
        error.value = null

        try {
            const data = await categoryService.getCategoriesTree()
            categories.value = data
        } catch {
            categories.value = []
        } finally {
            loading.value = false
        }
    }

    return {
        categories,
        loading,
        error,
        fetchCategoriesTree
    }
}
