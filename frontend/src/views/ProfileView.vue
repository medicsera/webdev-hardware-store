<script setup lang="ts">
import { ref } from 'vue'
import PersonalDataForm from '@/components/PersonalDataForm.vue'
import OrderHistory from '@/components/OrderHistory.vue'

type Tab = 'personal' | 'orders'

const activeTab = ref<Tab>('personal')
</script>

<template>
  <main class="profile-page">
    <div class="container">
      <div class="profile-layout">
        <!-- Sidebar -->
        <aside class="profile-sidebar">
          <button
            class="sidebar-tab"
            :class="{ 'sidebar-tab--active': activeTab === 'personal' }"
            @click="activeTab = 'personal'"
          >
            Личные данные
          </button>
          <button
            class="sidebar-tab"
            :class="{ 'sidebar-tab--active': activeTab === 'orders' }"
            @click="activeTab = 'orders'"
          >
            История покупок
          </button>
        </aside>

        <!-- Content -->
        <div class="profile-content">
          <PersonalDataForm v-if="activeTab === 'personal'" />
          <OrderHistory v-else />
        </div>
      </div>
    </div>
  </main>
</template>

<style lang="scss" scoped>
.profile-page {
  background: #ececec;
  min-height: calc(100vh - 110px);
  padding: 20px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.profile-layout {
  display: flex;
  gap: 0;
  background: white;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  overflow: hidden;
  min-height: 400px;
}

.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: white;
  border-right: 1px solid #d0d0d0;
}

.sidebar-tab {
  display: block;
  width: 100%;
  padding: 12px 16px;
  background: white;
  border: none;
  border-bottom: 1px solid #e8e8e8;
  text-align: left;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #f5f5f5;
  }

  &--active {
    background: #ececec;
    font-weight: 600;
  }
}

.profile-content {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
}
</style>
