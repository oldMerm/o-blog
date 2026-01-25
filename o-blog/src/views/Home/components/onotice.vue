<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import { type Article, articleType, goToArticle } from '@/views/public/Article';

let articleList = ref<Article[]>([]);
const getNotice = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/article/public/info',{
      params: {
        id: articleType.Notice
      }
    });
    articleList.value = res.data;
  } catch (error) {
    alert(`错误:${error}`);
  }
}

onMounted(() => {
  getNotice();
});
</script>

<template>
  <div class="notice-container">
    <!-- 顶部标题区 -->
    <div class="header">
      <span class="title">须知推送</span>
    </div>
    
    <!-- 内容滚动区 -->
    <div class="content-scroll">
      <div 
        v-for="item in articleList" 
        :key="item.id" 
        class="list-item"
        @click="goToArticle(item.id)"
      >
        {{ item.articleName }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.notice-container {
  /* --- 核心尺寸约束 (保持不变) --- */
  width: 100%;
  height: 12rem;

  /* --- 整体色调：高亮灰白 --- */
  background-color: #ffffff;      /* 纯白背景 */
  border: 1px solid #dadee5;      /* 极浅的灰色边框 */
  border-radius: 8px;             
  /* 阴影调整得非常轻柔，增加“悬浮感”和亮度 */
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05); 
  
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 顶部标题样式 */
.header {
  flex-shrink: 0;
  padding: 14px 10px;
  background-color: #ffffff; /* 保持纯白，显得干净 */
  /* 下划线使用明亮的银灰色，不压抑 */
  border-bottom: 2px solid #e4e7ed; 
}

.title {
  font-weight: 300;
  font-size: 1rem;
  color: #303133; /* 柔和的深灰，比纯黑更护眼 */
  letter-spacing: 4px;
}

/* 滚动区域 */
.content-scroll {
  flex: 1;
  overflow-y: auto;
  background-color: #ffffff;
}

/* 极简滚动条样式 */
.content-scroll::-webkit-scrollbar {
  width: 2px;
}
.content-scroll::-webkit-scrollbar-thumb {
  background-color: #dcdfe6; /* 浅灰滑块 */
  border-radius: 2px;
}
.content-scroll::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 列表项样式 */
.list-item {
  padding: 12px 16px;
  font-size: 0.9rem;
  color: #606266; /* 标准灰，清晰但不刺眼 */
  border-bottom: 1px solid #f2f6fc; /* 几乎看不见的分割线，保持整洁 */
  cursor: pointer;
  transition: all 0.2s ease;
  
  /* 文本处理 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 交互效果：不高亮反转，而是微微变灰，保持“亮”的感觉 */
.list-item:hover {
  background-color: #f5f7fa; /* 悬停时变成非常浅的灰 */
  color: #409eff;           
  padding-left: 20px;        /* 增加一点滑动感 */
}

.list-item:last-child {
  border-bottom: none;
}
</style>
