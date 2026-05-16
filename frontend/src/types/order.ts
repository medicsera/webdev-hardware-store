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
  deliveryMethod: string
  deliveryAddress: string | null
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'ready_for_pickup' | 'picked_up' | 'cancelled'
  createdAt: string
}
