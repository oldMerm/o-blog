<template>
  <div class="app-container">
    <!-- 基础用法 -->
    <JumpingText text="Hello Vue3!" />

    <!-- 进阶用法：自定义波浪速度和周期 -->
    <JumpingText 
      text="TypeScript is awesome!" 
      :delay="0.1" 
      duration="2s" 
      style="color: #42b883; font-size: 3rem; margin-top: 50px;" 
    />
  </div>
</template>

<script setup lang="ts">
import JumpingText from './JumpingText.vue';
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50px;
}
</style>
