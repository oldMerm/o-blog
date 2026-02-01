<template>
  <div class="dashboard-page">
    
    <!-- 1️⃣ 顶部状态栏：网站运行时间 -->
    <div class="status-banner">
      <div class="banner-text">
        系统已平稳运行了 <span class="highlight-days">{{ runDays }}</span> 天
      </div>
    </div>

    <!-- 2️⃣ 图表区域：2x2 网格布局 -->
    <div class="charts-grid">
      <!-- 循环渲染4个图表卡片 -->
      <div 
        v-for="chart in chartsData" 
        :key="chart.id" 
        class="chart-card"
      >
        <h3 class="chart-title">{{ chart.title }}</h3>
        
        <!-- 模拟柱状图容器 -->
        <div class="bar-chart-container">
          <div 
            v-for="(item, index) in chart.data" 
            :key="index" 
            class="bar-group"
          >
            <!-- 柱子：高度动态计算 -->
            <div 
              class="bar" 
              :style="{ height: (item.value / chart.maxValue) * 100 + '%' }"
              :title="`${item.label}: ${item.value}`"
            >
              <span class="bar-value">{{ item.value }}</span>
            </div>
            <!-- X轴标签 -->
            <div class="bar-label">{{ item.label }}</div>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// --- 类型定义 ---
interface DataPoint {
  label: string;
  value: number;
}

interface ChartConfig {
  id: number;
  title: string;
  maxValue: number; // 用于计算百分比高度
  data: DataPoint[];
}

// --- 假数据 (Mock Data) ---

// 1. 运行天数
const runDays = ref<number>(365);

// 2. 四个图表的数据
// 你可以在这里修改 label 和 value 来测试不同的展示效果
const chartsData = ref<ChartConfig[]>([
  {
    id: 1,
    title: '近五日访问量 (PV)',
    maxValue: 1000, // 设定一个最大值作为100%高度参考
    data: [
      { label: '周一', value: 450 },
      { label: '周二', value: 620 },
      { label: '周三', value: 800 },
      { label: '周四', value: 550 },
      { label: '周五', value: 920 },
    ]
  },
  {
    id: 2,
    title: '新增用户注册数',
    maxValue: 100,
    data: [
      { label: '1月', value: 20 },
      { label: '2月', value: 35 },
      { label: '3月', value: 60 },
      { label: '4月', value: 45 },
      { label: '5月', value: 88 },
    ]
  },
  {
    id: 3,
    title: '文章发布统计',
    maxValue: 20,
    data: [
      { label: '技术', value: 12 },
      { label: '生活', value: 5 },
      { label: '转载', value: 8 },
      { label: '公告', value: 15 },
      { label: '其他', value: 3 },
    ]
  },
  {
    id: 4,
    title: '服务器负载 (%)',
    maxValue: 100,
    data: [
      { label: '08:00', value: 15 },
      { label: '12:00', value: 65 },
      { label: '16:00', value: 45 },
      { label: '20:00', value: 80 },
      { label: '00:00', value: 30 },
    ]
  }
]);

</script>

<style scoped>
/* 使用与 MainLayout 一致的变量，保持风格统一 */
.dashboard-page {
  /* 基础布局 */
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* --- 顶部状态栏 --- */
.status-banner {
  background: linear-gradient(to right, #eff6ff, #ffffff); /* 极淡的蓝到白渐变 */
  border-left: 5px solid #3b82f6; /* 强调色左边框 */
  padding: 20px 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  display: flex;
  align-items: center;
  gap: 15px;
  color: #1e3a8a; /* 深蓝文字 */
}

.banner-icon {
  font-size: 24px;
}

.banner-text {
  font-size: 18px;
  font-weight: 500;
}

.highlight-days {
  font-size: 24px;
  font-weight: bold;
  color: #3b82f6;
  margin: 0 5px;
}

/* --- 图表网格区域 --- */
.charts-grid {
  display: grid;
  /* 2列布局，每列等宽 */
  grid-template-columns: repeat(2, 1fr); 
  gap: 24px;
}

/* 响应式：屏幕变窄时变为单列 */
@media (max-width: 1000px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

/* --- 单个图表卡片 --- */
.chart-card {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  height: 320px; /* 固定卡片高度，保证整齐 */
}

.chart-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #1f2937;
  font-weight: 600;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 10px;
}

/* --- CSS 模拟柱状图样式 --- */
.bar-chart-container {
  flex: 1;
  display: flex;
  align-items: flex-end; /* 底部对齐 */
  justify-content: space-around; /* 均匀分布 */
  padding-bottom: 10px;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  height: 100%;
  justify-content: flex-end;
}

.bar {
  width: 100%;
  background-color: #93c5fd; /* 浅蓝：默认柱子颜色 */
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  position: relative;
  cursor: pointer;
}

.bar:hover {
  background-color: #3b82f6; /* 亮蓝：悬停高亮 */
  transform: scaleY(1.05); /* 微微放大 */
  transform-origin: bottom;
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #6b7280;
  opacity: 0; /* 默认隐藏数值 */
  transition: opacity 0.2s;
}

.bar:hover .bar-value {
  opacity: 1; /* 悬停显示数值 */
}

.bar-label {
  margin-top: 10px;
  font-size: 12px;
  color: #6b7280;
}
</style>
