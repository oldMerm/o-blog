<script setup lang="ts">
import LeftSidebar from './components/LeftSidebar.vue';
import MainContent from './components/MainContent.vue';
</script>

<template>
  <div class="dashboard-container">
    <!-- 左侧区域：占宽 30% -->
    <div class="left-panel">
      <LeftSidebar />
    </div>
    
    <!-- 右侧区域：占宽 70% -->
    <div class="right-panel">
      <MainContent />
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  display: flex;
  width: 100%;
  height: 100vh; /* 占满整个视口高度 */
  background-color: #f7fbfe; /* 这里的背景色作为底色，AliceBlue */
  overflow: hidden;
}

.left-panel {
  width: 30%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-around; /* 让两个子块在垂直方向均匀分布 */
}

.right-panel {
  width: 70%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
</style>
