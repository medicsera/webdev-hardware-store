<script setup lang="ts">
import NavigationBar from "@/components/NavigationBar.vue";

</script>

<template>
  <div class="home-view">
    <img src="/banner.jpg" alt="Баннер" class="banner">
    <NavigationBar></NavigationBar>
  </div>
</template>

<style lang="scss" scoped>
.home-view {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.banner {
  margin: -10px;
}

</style>