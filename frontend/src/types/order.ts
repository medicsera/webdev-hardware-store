export interface OrderItem {
  id: number
  name: string
  price: number
  image?: string
  quantity: number
}

export interface Order {
  id: number
  items: OrderItem[]
  total: number
  date: string
  time: string
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled'
}
