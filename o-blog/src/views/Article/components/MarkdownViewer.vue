<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import MarkdownIt from 'markdown-it';
import anchor from 'markdown-it-anchor';
import container from 'markdown-it-container';
import hljs from 'highlight.js';
import 'highlight.js/styles/vs.css'
import router from '@/router/index.ts'
import { useRoute } from 'vue-router'
import { httpInstance, type Response } from '@/utils/http';

// --- 类型定义 ---
interface Heading {
  id: string;
  text: string;
  level: number;
}

// --- 响应式数据 ---
const renderedHtml = ref('');
const allHeadings = ref<Heading[]>([]);

// --- Markdown-It 配置 ---
const md = new MarkdownIt({
  html: true,
  linkify: true,
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch (__) { }
    }
    return ''; // 使用默认转义
  },
  typographer: true,
})
  .use(anchor, {
    permalink: anchor.permalink.ariaHidden({ placement: 'before' }),
    slugify: (s) => encodeURIComponent(String(s).trim().toLowerCase().replace(/\s+/g, '-'))
  })
  // 模拟 VitePress 的 ::: tip/warning 容器
  .use(container as any, 'tip')
  .use(container as any, 'warning')
  .use(container as any, 'danger');

const route = useRoute();

onMounted(async () => {
  const id = route.params.id;
  const isPublic = route.query.isPublic;
  if (id === null) {
    alert('未传入数据，文章为空，将渲染默认文本');
    fetchDocument(1);
  } else if (isPublic === 'private') { 
    try {
      const res = await httpInstance.get<any, Response>(`/article/private/${id}`);
      if (res.code !== 200) {
        alert(`错误消息: ${res.message}, 将渲染默认文本`);
        fetchDocument(1);
        return;
      }
      const text: string = await httpInstance.get(res.data);
      renderedHtml.value = md.render(text);
      extractHeadings(text);
    } catch (error) {
      alert(error);
    }
  } else {
    try {
      const res = await httpInstance.get<any, Response>(`/article/public/${id}`);
      if (res.code !== 200) {
        alert(`错误消息: ${res.message}, 将渲染默认文本`);
        fetchDocument(1);
        return;
      }
      const text: string = await httpInstance.get(res.data);
      renderedHtml.value = md.render(text);
      extractHeadings(text);
    } catch (error) {
      alert(error);
    }
  }

})

// --- 逻辑：获取数据并解析 ---
const fetchDocument = async (id: number) => {
  // 模拟后端返回的数据
  const mockMd = `
# 快速上手 Vue + TS
这是你的文档首页。

::: tip 提示
这是一个复刻 VitePress 的 Tip 容器。
:::

## 环境配置
### 安装 Node.js
确保版本在 16.0 以上。
### 配置镜像源
使用 npm 或 yarn。

## 核心功能
### 自动渲染
### 动态目录提取

::: warning 警告
请勿直接在生产环境修改 Mock 数据。
:::
  `;

  renderedHtml.value = md.render(mockMd);
  extractHeadings(mockMd);
};

const extractHeadings = (content: string) => {
  // 1. 先过滤掉所有代码块的内容，防止误匹配其中的 # 注释
  // 匹配 ```...``` 之间的所有内容，替换为空格
  const cleanContent = content.replace(/```[\s\S]*?```/g, '');

  // 2. 使用增强后的正则：确保标题必须在行首，且前面没有被代码块干扰
  const headingRegex = /^(#{1,6})\s+(.*)$/gm;
  const list: Heading[] = [];
  let match: any;

  while ((match = headingRegex.exec(cleanContent)) !== null) {
    const text = match[2].trim();
    list.push({
      level: match[1].length,
      text: text,
      id: encodeURIComponent(text.toLowerCase().replace(/\s+/g, '-'))
    });
  }
  allHeadings.value = list;
};

// --- 计算属性：过滤出末级标题 ---
// 简单高效：直接过滤出所有 H2 标题作为“本页重点”
const leafHeadings = computed(() => {
  return allHeadings.value.filter(h => h.level === 2);
});

// 丝滑滚动
const scrollTo = (id: string) => {
  const el = document.getElementById(id);
  if (el) {
    const y = el.getBoundingClientRect().top + window.pageYOffset - 80; // 80 是导航栏高度 + 一点余量
    window.scrollTo({ top: y, behavior: 'smooth' });
  }
};

const goToHome = () => {
  router.push({ name: 'home' });
}

</script>

<template>
  <div class="vp-wrapper">
    <!-- 1. 顶部栏 (VitePress 风格) -->
    <nav class="vp-nav">
      <div class="nav-layout">
        <div class="brand">
          <span class="logo"></span>
          <span class="site-name">老鱼人文档中心</span>
        </div>
        <div class="nav-links">
          <a @click="goToHome" style="cursor: pointer;">主页</a>
          <a href="https://github.com" target="_blank">GitHub</a>
        </div>
      </div>
    </nav>

    <div class="vp-body">
      <!-- 2. 左侧栏：完整目录树 -->
      <aside class="vp-sidebar-left">
        <div class="sidebar-content">
          <div class="sidebar-title">章节目录</div>
          <ul class="toc-tree">
            <li v-for="h in allHeadings" :key="h.id" :class="['toc-item', `depth-${h.level}`]">
              <a :href="`#${h.id}`" @click.prevent="scrollTo(h.id)">{{ h.text }}</a>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 3. 中间内容区：VitePress 渲染引擎 -->
      <main class="vp-content">
        <!-- vp-doc 是 VitePress 样式的核心入口类 -->
        <div class="vp-doc-container">
          <article class="vp-doc" v-html="renderedHtml"></article>
        </div>
      </main>

      <!-- 4. 右侧栏：仅展示末级标题 (Leaf Nodes) -->
      <aside class="vp-sidebar-right">
        <div class="aside-content">
          <div class="aside-title">本页重点</div>
          <ul class="leaf-list">
            <li v-for="leaf in leafHeadings" :key="leaf.id">
              <a :href="`#${leaf.id}`" @click.prevent="scrollTo(leaf.id)">
                {{ leaf.text }}
              </a>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
/* 🎨 VitePress 极简淡蓝配色方案 */
:root {
  --vp-c-brand: #3b82f6;
  --vp-c-bg: #ffffff;
  --vp-c-bg-alt: #f6f8fa;
  --vp-c-border: #e2e8f0;
  --vp-c-text: #2c3e50;
  --vp-nav-height: 64px;
}

.vp-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #fff;
  color: #3c3c43;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
}

/* 🏗 顶部导航 */
.vp-nav {
  height: 64px;
  border-bottom: 1px solid #f1f1f1;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-layout {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #3b82f6;
}

.nav-links a {
  margin-left: 20px;
  text-decoration: none;
  color: #4b5563;
  font-size: 14px;
}

.nav-links a:hover {
  color: #3b82f6;
}

/* 🗂 布局 */
.vp-body {
  display: flex;
  flex: 1;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
}

/* 📂 左侧栏 */
.vp-sidebar-left {
  width: 280px;
  border-right: 1px solid #f1f1f1;
  padding: 32px 24px;
  background: #fcfdfe;
  height: calc(100vh - 64px);
  position: sticky;
  top: 64px;
  overflow-y: auto;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  margin-bottom: 16px;
}

.toc-tree {
  list-style: none;
  padding: 0;
}

.toc-item {
  margin: 8px 0;
}

.toc-item a {
  text-decoration: none;
  color: #475569;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.toc-item a:hover {
  color: #3b82f6;
}

.depth-2 {
  padding-left: 12px;
  font-weight: 600;
}

.depth-3 {
  padding-left: 12px;
}

/* 📝 内容区 */
.vp-content {
  flex: 1;
  padding: 48px 64px;
  min-width: 0;
}

.vp-doc-container {
  max-width: 768px;
  margin: 0 auto;
}

/* 📍 右侧栏 */
.vp-sidebar-right {
  width: 240px;
  padding: 32px 24px;
  height: calc(100vh - 64px);
  position: sticky;
  top: 64px;
}

.aside-title {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 12px;
}

.leaf-list {
  list-style: none;
  padding: 0;
  border-left: 1px solid #e2e8f0;
}

.leaf-list li {
  padding-left: 16px;
  margin-bottom: 10px;
}

.leaf-list a {
  text-decoration: none;
  color: #64748b;
  font-size: 13px;
}

.leaf-list a:hover {
  color: #3b82f6;
}

:deep(.vp-doc) {
  line-height: 1.7;
  font-size: 16px;
}

:deep(.vp-doc h1) {
  font-size: 32px;
  margin-bottom: 20px;
  font-weight: 700;
}

:deep(.vp-doc h2) {
  font-size: 28px;
  margin: 48px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f1f1;
}

:deep(.vp-doc h3) {
  font-size: 24px;
  margin: 32px 0 16px;
}

:deep(.vp-doc h4) {
  font-size: 20px;
  margin: 12px 0 16px;
}

:deep(.vp-doc h5) {
  font-size: 16px;
  margin: 12px 0 16px;
}

/* Container 样式 */
:deep(.custom-block) {
  margin: 16px 0;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid transparent;
}

:deep(.custom-block.tip) {
  background-color: #f0f9ff;
  border-color: #bae6fd;
  color: #0369a1;
}

:deep(.custom-block.tip::before) {
  content: "TIP";
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
}

:deep(.custom-block.warning) {
  background-color: #fffbeb;
  border-color: #fef3c7;
  color: #92400e;
}

/* 代码块样式 */
:deep(pre) {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  border: 1px solid #e2e8f0;
}

:deep(pre>code) {
  color: #000000 !important;
  font-weight: 300;
}

:deep(p) {
  margin-bottom: 14px;
}

:deep(img) {
  width: 100%;
}
</style>
