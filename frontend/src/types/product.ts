export interface Product {
    id: number
    name: string
    price: number
    image?: string
    description?: string
    category?: string
    inStock?: boolean
    quantity?: number
}

export interface CartItem extends Product {
    quantity: number
}

export interface PaginatedResponse<T> {
    content: T[]
    totalElements: number
    totalPages: number
    number: number
    size: number
}
