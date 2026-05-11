export interface Subcategory {
    id: number
    name: string
    slug: string
}

export interface Category {
    id: number
    name: string
    slug: string
    imageUrl?: string
    productsCount?: number
    subcategories?: Subcategory[]
}

export type PopularCategory = Category
export type CategoryWithSubcategories = Category
