<script setup lang="ts">
import { RouterView } from 'vue-router'
import NavigationBar from '@/components/NavigationBar.vue'
import AppFooter from '@/components/Footer.vue'
</script>

<template>
  <div id="app">
    <NavigationBar />

    <main class="main-content">
      <RouterView />
    </main>

    <AppFooter />
  </div>
</template>

<style>

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f5f5;
  overflow-x: hidden;
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  width: 100%;
}
</style>