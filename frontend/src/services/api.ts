import type { Product, PaginatedResponse } from '@/types/product'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export const productService = {
    async getProducts(page = 0, size = 20): Promise<PaginatedResponse<Product>> {
        const response = await fetch(`${API_BASE_URL}/products?page=${page}&size=${size}`)
        if (!response.ok) throw new Error('Failed to fetch products')
        return response.json()
    },

    async getProductById(id: number): Promise<Product> {
        const response = await fetch(`${API_BASE_URL}/products/${id}`)
        if (!response.ok) throw new Error('Failed to fetch product')
        return response.json()
    },

    async searchProducts(query: string, page = 0, size = 20): Promise<PaginatedResponse<Product>> {
        const response = await fetch(
            `${API_BASE_URL}/products/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`
        )
        if (!response.ok) throw new Error('Search failed')
        return response.json()
    }
}