export interface Product {
    id: number
    name: string
    price: number
    description?: string
    quantity: number
    catalogId?: number | null
    subCatalogId?: number | null
    imageUrls: string[]
    characteristics: Record<string, string>
}

export interface CartItem extends Product {
    cartQuantity: number
}

export interface PaginatedResponse<T> {
    content: T[]
    totalElements: number
    totalPages: number
    number: number
    size: number
}
