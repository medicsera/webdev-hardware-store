import type { Category } from '@/types/category'

const API_BASE_URL = import.meta.env.VITE_API_URL

export const categoryService = {
    async getPopularCategories(limit = 6): Promise<Category[]> {
        const response = await fetch(`${API_BASE_URL}/categories/popular?limit=${limit}`)
        if (!response.ok) throw new Error('Failed to fetch popular categories')
        return response.json()
    },

    async getAllCategories(): Promise<Category[]> {
        const response = await fetch(`${API_BASE_URL}/categories`)
        if (!response.ok) throw new Error('Failed to fetch categories')
        return response.json()
    },

    async getCategoriesTree(): Promise<Category[]> {
        const response = await fetch(`${API_BASE_URL}/categories/tree`)
        if (!response.ok) throw new Error('Failed to fetch categories tree')
        return response.json()
    }
}
