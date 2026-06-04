<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import Topbar from './components/Topbar.vue'
import ArticleSection from './components/ArticleSection.vue'

onMounted(() => {
  document.documentElement.style.overflow = 'hidden'
  document.documentElement.style.height = '100%'
  document.body.style.overflow = 'hidden'
  document.body.style.height = '100%'
  document.body.style.minWidth = 'unset'
})

onUnmounted(() => {
  document.documentElement.style.overflow = ''
  document.documentElement.style.height = ''
  document.body.style.overflow = ''
  document.body.style.height = ''
  document.body.style.minWidth = ''
})
</script>

<template>
  <div class="mobile-root">
    <Topbar />
    <ArticleSection />
  </div>
</template>

<style>
.mobile-root {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  max-width: 100vw;
  overflow-x: hidden;
  background: linear-gradient(
    180deg,
    rgb(225, 237, 248) 0%,
    rgb(215, 228, 242) 50%,
    rgb(210, 224, 240) 100%
  );
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  touch-action: manipulation;
  overscroll-behavior: none;
}

.mobile-root * {
  -webkit-tap-highlight-color: transparent;
}
</style>
