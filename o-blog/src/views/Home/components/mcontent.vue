<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import { type Article, articleType, goToArticle } from '@/views/public/Article';

let articleList = ref<Article[]>([]);

onMounted(() => {
  renderHomePage();
})

const renderHomePage = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/article/public/info',{
      params: {
        id: articleType.TECNO,
        size: 11
      }
    });
    articleList.value = res.data;
  } catch (error) {
    alert(`错误:${error}`);
  }
}
</script>

<template>
  <div class="core-article-box">
    
    <!-- 顶部标题区 -->
    <div class="header">
      <span class="title">技术内容分享</span>
    </div>

    <!-- 标题列表区域 -->
    <div class="title-list">
      <div v-for="(item, index) in articleList" :key="index" class="article-item" @click="goToArticle(item.id)">
        
        <!-- 左侧：标题 (自动截断) -->
        <span class="title-text">{{ item.articleName }}</span>
        
        <!-- 右侧：数据 (固定不换行) -->
        <p class="stats-info">
          <!-- <span class="stat-box">点赞 {{ item.like }}</span>
          <span class="divider">·</span> -->
          <span class="stat-box">发布于 {{ item.createdAt }}</span>
        </p>

      </div>
    </div>

  </div>
</template>

<style scoped>
.core-article-box {
  /* --- 尺寸设定 --- */
  width: 53%;
  height: 36.9rem;
  margin: 10px 10px;

  /* --- 整体色调 --- */
  background-color: #ffffff;
  border: 1px solid #dadee5;
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.05);
  
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

/* --- 列表区域 --- */
.title-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* --- 单个文章行 (Flex布局核心) --- */
.article-item {
  display: flex;              /* 启用Flex布局 */
  align-items: center;        /* 垂直居中 */
  justify-content: space-between; /* 左右两端对齐 */
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent; 
}

.article-item:hover {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

/* --- 标题样式 --- */
.title-text {
  font-size: 1rem;
  font-weight: 600;
  color: #303133;
  
  /* 关键：防止标题过长把右边顶出去 */
  flex: 1;                /* 占据剩余空间 */
  min-width: 0;           /* 允许flex子项小于内容宽度（实现截断） */
  white-space: nowrap;    /* 不换行 */
  overflow: hidden;       /* 超出隐藏 */
  text-overflow: ellipsis;/* 显示省略号 */
  margin-right: 20px;     /* 和右边数据的间距 */
  transition: all 0.2s ease;
}

/* hover时标题变色 */
.article-item:hover .title-text {
  margin-left: 5px;
  color: #409eff;
}

/* --- P标签：数据统计样式 --- */
.stats-info {
  margin: 0; 
  font-size: 0.85rem;
  color: #909399;
  flex-shrink: 0; /* 关键：禁止数据区域被压缩 */
  white-space: nowrap; /* 保持数据在一行 */
  display: flex;
  align-items: center;
}

.stat-box {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.divider {
  margin: 0 8px;
  color: #dcdfe6;
}


</style>
