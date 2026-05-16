<script setup lang="ts">
import { ref } from 'vue'
import type { Order } from '@/types/order'
import api from '@/api/auth'

const props = defineProps<{ order: Order }>()
const emit = defineEmits<{
  close: []
  cancelled: [order: Order]
}>()

const cancelling = ref(false)
const error = ref('')

const statusLabel: Record<string, string> = {
  pending:          'Ожидает',
  processing:       'В обработке',
  shipped:          'В доставке',
  delivered:        'Доставлен',
  ready_for_pickup: 'Готов к выдаче',
  picked_up:        'Выдан',
  cancelled:        'Отменён',
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 2 }).format(price)
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('ru-RU', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

async function cancelOrder() {
  if (!confirm('Вы уверены, что хотите отменить заказ?')) return
  cancelling.value = true
  error.value = ''
  try {
    const res = await api.patch(`/buyer/orders/${props.order.id}/cancel`)
    emit('cancelled', res.data)
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Ошибка при отмене заказа'
  } finally {
    cancelling.value = false
  }
}
</script>

<template>
  <div class="modal-backdrop" @click.self="emit('close')">
    <div class="modal">
      <button class="modal__close" @click="emit('close')" aria-label="Закрыть">✕</button>

      <h3 class="modal__title">Заказ #{{ order.id }}</h3>

      <div class="modal__meta">
        <span class="modal__date">{{ formatDate(order.createdAt) }}</span>
        <span class="modal__status" :class="`modal__status--${order.status}`">
          {{ statusLabel[order.status] ?? order.status }}
        </span>
      </div>

      <ul class="modal__items">
        <li v-for="item in order.items" :key="item.id" class="modal-item">
          <img v-if="item.imageUrl" :src="item.imageUrl" class="modal-item__img" alt="" />
          <div v-else class="modal-item__img modal-item__img--empty"></div>
          <span class="modal-item__name">{{ item.name }}</span>
          <span class="modal-item__qty">{{ item.quantity }} шт.</span>
          <span class="modal-item__price">{{ formatPrice(item.price * item.quantity) }}</span>
        </li>
      </ul>

      <div class="modal__delivery">
        <span class="modal__delivery-method">
          {{ order.deliveryMethod === 'pickup' ? 'Самовывоз' : 'Доставка' }}
        </span>
        <span v-if="order.deliveryAddress" class="modal__delivery-address">
          {{ order.deliveryAddress }}
        </span>
        <span class="modal__delivery-cost">Доставка: {{ formatPrice(order.deliveryCost) }}</span>
      </div>

      <div class="modal__total">Итого: {{ formatPrice(order.total) }}</div>

      <p v-if="error" class="modal__error">{{ error }}</p>

      <div class="modal__actions">
        <button
          v-if="order.status === 'pending'"
          class="btn btn--cancel"
          :disabled="cancelling"
          @click="cancelOrder"
        >
          <span v-if="cancelling" class="btn-spinner"></span>
          {{ cancelling ? 'Отмена...' : 'Отменить заказ' }}
        </button>
        <button class="btn btn--close" @click="emit('close')">Закрыть</button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 16px;
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 28px 28px 24px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);

  &__close {
    position: absolute;
    top: 14px;
    right: 16px;
    background: none;
    border: none;
    font-size: 18px;
    color: #aaa;
    cursor: pointer;
    line-height: 1;
    padding: 4px;
    transition: color 0.15s;
    &:hover { color: #333; }
  }

  &__title {
    font-size: 18px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 12px;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
  }

  &__date {
    font-size: 13px;
    color: #888;
  }

  &__status {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;

    &--pending           { background: #fff3cd; color: #856404; }
    &--processing        { background: #cce5ff; color: #004085; }
    &--shipped           { background: #d4edda; color: #155724; }
    &--delivered         { background: #d1ecf1; color: #0c5460; }
    &--ready_for_pickup  { background: #fff3e0; color: #e65100; }
    &--picked_up         { background: #d1ecf1; color: #0c5460; }
    &--cancelled         { background: #f8d7da; color: #721c24; }
  }

  &__items {
    list-style: none;
    margin: 0 0 20px;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__delivery {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 0;
    border-top: 1px solid #eee;
    border-bottom: 1px solid #eee;
    margin-bottom: 16px;
  }

  &__delivery-method {
    font-size: 14px;
    font-weight: 600;
    color: #2c3e50;
  }

  &__delivery-address {
    font-size: 13px;
    color: #555;
  }

  &__delivery-cost {
    font-size: 13px;
    color: #888;
  }

  &__total {
    font-size: 17px;
    font-weight: 700;
    color: #2c3e50;
    text-align: right;
    margin-bottom: 20px;
  }

  &__error {
    font-size: 13px;
    color: #e74c3c;
    margin: 0 0 12px;
    text-align: center;
  }

  &__actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
}

.modal-item {
  display: flex;
  align-items: center;
  gap: 12px;

  &__img {
    width: 52px;
    height: 52px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #eee;
    flex-shrink: 0;

    &--empty {
      background: #e0e0e0;
    }
  }

  &__name {
    flex: 1;
    font-size: 13px;
    color: #2c3e50;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__qty {
    font-size: 12px;
    color: #888;
    flex-shrink: 0;
  }

  &__price {
    font-size: 13px;
    font-weight: 600;
    color: #333;
    flex-shrink: 0;
    min-width: 80px;
    text-align: right;
  }
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.15s;

  &:disabled { opacity: 0.5; cursor: not-allowed; }
  &:not(:disabled):hover { filter: brightness(0.9); }

  &--cancel { background: #e74c3c; color: white; }
  &--close  { background: #f0f0f0; color: #555; }
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.55s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
