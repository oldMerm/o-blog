<script setup lang="ts">
import { ref } from 'vue';

interface Article {
  id: number;
  title: string;
  date: string;
}

// 假数据：生成多一点以展示滚动条效果
const articleList = ref<Article[]>([
  { id: 1, title: 'Vue3 组合式 API 最佳实践指南', date: '2023-10-01' },
  { id: 2, title: 'TypeScript 在前端项目中的深度应用', date: '2023-10-05' },
  { id: 3, title: 'CSS Grid 与 Flexbox 布局对比', date: '2023-10-12' },
  { id: 4, title: '前端性能优化：从入门到精通', date: '2023-10-15' },
  { id: 5, title: 'Webpack 与 Vite 构建工具解析', date: '2023-10-20' },
  { id: 6, title: 'React 与 Vue 的哲学差异', date: '2023-10-22' },
  { id: 7, title: 'Node.js 中间件原理解析', date: '2023-10-25' },
  { id: 8, title: 'WebAssembly 的未来展望', date: '2023-10-28' },
  { id: 9, title: '微前端架构落地实战', date: '2023-11-01' },
  { id: 10, title: 'Serverless 架构在前端的应用', date: '2023-11-05' },
]);
</script>

<template>
  <!-- 区块3：文章列表 -->
  <div class="main-block article-list-container">
    <h3>我的文章列表</h3>
    <div class="scroll-area">
      <ul class="article-list">
        <li v-for="article in articleList" :key="article.id" class="article-item">
          <span class="article-title">{{ article.title }}</span>
          <span class="article-date">{{ article.date }}</span>
        </li>
      </ul>
    </div>
  </div>

  <!-- 区块4：信息反馈 -->
  <!-- <div class="main-block feedback-container">
    <div class="add-content">
      <h3>添加文章</h3>
      <div class="add">
        <img src="../../../static/add.svg" alt="">
      </div>
    </div>
    <div class="feedback-content">
      <h3>信息反馈</h3>
    </div>
  </div> -->
  <div class="add-feedback-block">
    <div class="main-block" style="margin-right: 30px;">
      <div class="add-content">
        <h3>添加文章</h3>
        <div class="add">
          <img src="../../../static/add.svg" alt="">
        </div>
      </div>
    </div>
    <div class="main-block" style="margin-right: 30px;">
      <div class="o-content">
        <h3>功能待开发</h3>
      </div>
    </div>
    <div class="main-block">
      <div class="feedback-content">
        <h3>信息反馈</h3>
      </div>
    </div>
  </div>

</template>

<style scoped>
.main-block {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(173, 216, 230, 0.3);
  padding: 15px;
  box-sizing: border-box;
  border-bottom: 2px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

h3 {
  color: #303133;
  font-size: 1.1rem;
  font-weight: 300;
  margin-bottom: 15px;
  margin-top: 0;
}

/* 文章列表区域：占视口 60% */
.article-list-container {
  height: 60vh;
  margin-bottom: 20px;
  /* 与下方区块的间距 */
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  /* 关键：内容过多显示下拉条 */
  padding-right: 10px;
  border-top: 2px solid #e4e7ed;
}

/* 自定义滚动条样式 (WebKit内核) */
.scroll-area::-webkit-scrollbar {
  width: 4px;
}

.scroll-area::-webkit-scrollbar-thumb {
  background-color: #b3e5fc;
  border-radius: 6px;
}

.scroll-area::-webkit-scrollbar-track {
  background-color: #f0f8ff;
}

.article-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.article-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 15px;
  border-bottom: 1px solid #e1f5fe;
  transition: background-color 0.2s;
  color: #455a64;
  transition: all 0.2s ease;
}

.article-item:hover {
  margin-left: 5px;
  color: #409eff;
  cursor: pointer;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

.article-date {
  color: #90a4ae;
  font-size: 0.85rem;
}

.add-content {
}

.feedback-content {
  color: #546e7a;
  line-height: 1.6;
  width: 33rem;
}

.add {
  width: 11rem;
  height: 10rem;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 3px solid rgb(80, 195, 236);
  border-radius: 8px;
  cursor: pointer;
}

.o-content {
  width: 11rem;
}

.add-feedback-block {
  display: flex;
}
</style>
