<script setup lang="ts">
import { ref, computed } from 'vue';
import router from '@/router';

// --- 类型定义 ---
interface MenuItem {
  id: number;
  name: string;
  path: string;
  icon?: string;
}

// --- 假数据 (Mock Data) ---
// TODO: 实际开发中，这些数据通常来自后端 API (/api/menus)
const menuItems = ref<MenuItem[]>([
  { id: 1, name: '控制台', path: '/dashboard' },
  { id: 2, name: '用户管理', path: '/users' },
  { id: 3, name: '信息反馈', path: '/feedback' },
  { id: 4, name: '文章管理', path: '/article' },
]);

// --- 状态管理 ---
const currentRoute = ref<string>('/dashboard'); // 模拟当前路由

// 计算当前页面标题
const pageTitle = computed(() => {
  const item = menuItems.value.find(i => i.path === currentRoute.value);
  return item ? item.name : '未知页面';
});

// --- 事件处理 ---
const handleNavigate = (path: string) => {
  console.log(`正在跳转到: ${path}`);
  currentRoute.value = path;
  router.push(`/admin${path}`);
};

const goToHome = () => {
  router.push({name: 'home'});
}
</script>

<template>
  <div class="layout-container">
    <!-- 🟢 左侧侧边栏 Sidebar -->
    <aside class="sidebar">
      <div class="logo-area">
        <h2>鱼人博客管理系统</h2>
      </div>
      
      <nav class="nav-menu">
        <!-- 假数据循环渲染菜单 -->
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          class="nav-item"
          :class="{ 'active': currentRoute === item.path }"
          @click="handleNavigate(item.path)"
        >
          <!-- <span class="icon">📂</span> -->
          <span class="label">{{ item.name }}</span>
        </div>
      </nav>
      
      <div class="sidebar-footer">
        <p>© 2026 鱼人博客</p>
      </div>
    </aside>

    <!-- 🔵 右侧主体区域 -->
    <main class="main-content">
      <!-- 🔝 顶部栏 Topbar -->
      <header class="topbar">
        <div class="breadcrumb">
          <span>首页</span> / <span class="current">{{ pageTitle }}</span>
        </div>
        
        <div class="user-actions">
          <div class="action-item" @click="goToHome">首页</div>
          <div class="user-profile">
            <span>管理员</span>
            <div class="avatar">Admin</div>
          </div>
        </div>
      </header>

      <!-- 🖥️ 内容展示区域 (Router View) -->
      <div class="content-wrapper">
        <!-- 这里是用来展示各个子组件的地方 -->
        <router-view /> <!-- 在实际项目中请取消注释 -->
        
        <!-- 演示用占位符，模拟子组件加载 -->
        <!-- <div class="demo-component-slot">
          <h3>{{ pageTitle }} 组件内容区域</h3>
          <p>此处将动态加载对应侧边栏的 Vue 组件。</p>
          <div class="demo-card">
             📊 数据概览图表 (占位)
          </div>
        </div> -->
      </div>
    </main>
  </div>
</template>

<style scoped>
/* --- 🎨 颜色变量定义 (蓝白风格) --- */
.layout-container {
  --primary-color: #97dae6;   /* 深蓝：侧边栏背景 */
  --accent-color: #3b82f6;    /* 亮蓝：选中状态/高亮 */
  --bg-color: #f3f4f6;        /* 灰白：页面整体背景 */
  --white: #ffffff;           /* 纯白：卡片/TopBar背景 */
  --text-main: #1f2937;       /* 深灰：主要文字 */
  --text-light: #9ca3af;      /* 浅灰：次要文字 */
  
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  font-family: 'Segoe UI', sans-serif;
  background-color: var(--bg-color);
  color: var(--text-main);
}

/* --- 🟢 侧边栏样式 --- */
.sidebar {
  width: 240px;
  background-color: var(--primary-color);
  color: rgb(75, 75, 75);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  flex-shrink: 0; /* 防止被压缩 */
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  font-size: 0.9rem;
}

.nav-menu {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.2s;
  color: rgba(30, 30, 30, 0.7);
  border-left: 4px solid transparent;
}

.nav-item:hover {
  background-color: rgba(255,255,255,0.05);
  color: var(--white);
}

.nav-item.active {
  background-color: rgba(255,255,255,0.1);
  color: var(--white);
  border-left-color: var(--accent-color);
}

.nav-item .icon {
  margin-right: 12px;
}

.sidebar-footer {
  padding: 20px;
  text-align: center;
  font-size: 12px;
  color: rgba(255,255,255,0.4);
}

/* --- 🔵 主体区域样式 --- */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 内容过多时内部滚动 */
}

/* --- 🔝 顶部栏样式 --- */
.topbar {
  height: 64px;
  background-color: var(--white);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  z-index: 10;
}

.breadcrumb {
  font-size: 14px;
  color: var(--text-light);
}

.breadcrumb .current {
  color: var(--text-main);
  font-weight: 600;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-item {
  position: relative;
  cursor: pointer;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  margin-left: 5px;
}

.avatar {
  width: 32px;
  height: 32px;
  background-color: var(--accent-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* --- 🖥️ 内容包裹层样式 --- */
.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto; /* 让内容区域可以独立滚动 */
}

/* 演示用组件插槽样式 */
.demo-component-slot {
  background-color: var(--white);
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  min-height: 400px;
}

.demo-card {
  margin-top: 20px;
  height: 200px;
  border: 2px dashed #e5e7eb;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
}
</style>
