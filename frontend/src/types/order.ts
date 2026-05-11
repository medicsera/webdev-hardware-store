export interface OrderItem {
  id: number
  productId: number
  name: string
  price: number
  quantity: number
  imageUrl?: string
}

export interface Order {
  id: number
  items: OrderItem[]
  total: number
  deliveryCost: number
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled'
  createdAt: string
}
