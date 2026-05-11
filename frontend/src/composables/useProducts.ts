import { ref } from 'vue'
import { productService } from '@/services/productApi'
import type { Product } from '@/types/product'

export function useProducts() {
    const products = ref<Product[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)
    const totalPages = ref(0)
    const currentPage = ref(0)

    const fetchProducts = async (page = 0, size = 20, catalogId?: number, subCatalogId?: number) => {
        loading.value = true
        error.value = null
        try {
            const response = await productService.getProducts(page, size, catalogId, subCatalogId)
            products.value = response.content
            totalPages.value = response.totalPages
            currentPage.value = response.number
        } catch (e) {
            error.value = 'Не удалось загрузить товары'
            products.value = []
        } finally {
            loading.value = false
        }
    }

    return { products, loading, error, totalPages, currentPage, fetchProducts }
}
