<script setup lang="ts">
import { httpInstance, type Response } from '@/utils/http';
import { onMounted, ref } from 'vue';

// --- 类型定义 ---
interface ChartConfig {
  id: number;
  title: string;
  maxValue: number; // 用于计算百分比高度
  data: Counter[];
}

// 1. 运行天数
const runDays = ref<number>();
const getRunDays = async () => {
  const {data} = await httpInstance.get<any, Response>('/counter/time');
  runDays.value = data;
}

const userCounter = ref<Counter[]>([]);
const articleCounter = ref<Counter[]>([]);
interface Counter{
  id: number;
  countType: number;
  count: number;
  createdAt: string;
}
const getCount = async (type: number) => {
  try {
    const res = await httpInstance.get<any, Response>(`/counter/incr/${type}`);
    if(res.code !== 200) alert(`出现错误:${res.message}`);
    return res.data;
  } catch (error) {
    alert(error);    
  }
}

// 2. 四个图表的数据
// 你可以在这里修改 label 和 value 来测试不同的展示效果
const chartsData:any = ref<ChartConfig[]>([
  {
    id: 1,
    title: '近五日访问量 (PV)',
    maxValue: 1000, // 设定一个最大值作为100%高度参考
    data: []
  },
  {
    id: 2,
    title: '新增用户注册数',
    maxValue: 10,
    data: userCounter.value
  },
  {
    id: 3,
    title: '文章发布统计',
    maxValue: 10,
    data: articleCounter.value
  },
  {
    id: 4,
    title: '服务器负载 (%)',
    maxValue: 100,
    data: []
  }
]);

/* 立即执行代码 */
onMounted(async () => {
  getRunDays();
  userCounter.value = await getCount(1);
  articleCounter.value = await getCount(2);

  // 手动更新 chartsData 中的数据
  chartsData.value[1].data = userCounter.value;
  chartsData.value[2].data = articleCounter.value;
})
</script>

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
              :style="{ height: (item.count / chart.maxValue) * 100 + '%' }"
              :title="`${item.createdAt}: ${item.count}`"
            >
              <span class="bar-value">{{ item.count }}</span>
            </div>
            <!-- X轴标签 -->
            <div class="bar-label">{{ item.createdAt.replace(':', '_').slice(2, 7)}}</div>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

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
