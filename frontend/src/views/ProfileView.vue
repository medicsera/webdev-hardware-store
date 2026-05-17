<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import PersonalDataForm from '@/components/PersonalDataForm.vue'
import OrderHistory from '@/components/OrderHistory.vue'
import AdminDashboard from '@/views/AdminDashboard.vue'

type Tab = 'personal' | 'orders'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.currentUser?.role === 'ADMIN')

const activeTab = ref<Tab>('personal')
</script>

<template>
  <AdminDashboard v-if="isAdmin" />
  <main v-else class="profile-page">
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
  @include page-layout;
  background: #ececec;
}

.container { @include container; }

.profile-layout {
  display: flex;
  background: #fff;
  border: 1px solid #d0d0d0;
  border-radius: $radius-sm;
  overflow: hidden;
  min-height: 400px;

  @include below-md { flex-direction: column; }
}

.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #d0d0d0;

  @include below-md {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #d0d0d0;
    display: flex;
    flex-wrap: wrap;
  }
}

.sidebar-tab {
  display: block;
  width: 100%;
  padding: 12px $gap-md;
  background: #fff;
  border: none;
  border-bottom: 1px solid $color-border-light;
  text-align: left;
  font-size: $font-base;
  color: #333;
  cursor: pointer;
  transition: background 0.15s;

  &:hover { background: $color-bg-light; }
  &--active { background: #ececec; font-weight: 600; }

  @include below-md {
    width: auto;
    flex: 1;
    border-bottom: none;
    border-right: 1px solid $color-border-light;
    text-align: center;
    &:last-child { border-right: none; }
  }
}

.profile-content {
  flex: 1;
  min-width: 0;
  padding: $gap-lg;
  background: $color-bg-light;
  overflow: hidden;

  @include below-sm { padding: $gap-md; }
}
</style>
