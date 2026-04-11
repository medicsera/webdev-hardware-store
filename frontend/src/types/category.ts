export interface Category {
    id: number
    name: string
    slug: string
    image?: string
    productsCount?: number
    description?: string
    parentId?: number
    order?: number
}

export interface PopularCategory extends Category {
    productsCount: number
    image?: string
}

export interface Subcategory {
    id: number
    name: string
    slug: string
}

export interface CategoryWithSubcategories {
    id: number
    name: string
    slug: string
    image?: string
    subcategories?: Subcategory[]
}