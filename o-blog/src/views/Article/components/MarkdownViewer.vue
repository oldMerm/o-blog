<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue';
import MarkdownIt from 'markdown-it';
import anchor from 'markdown-it-anchor';
import container from 'markdown-it-container';
import hljs from 'highlight.js';
import 'highlight.js/styles/vs.css'
import router from '@/router/index.ts'
import { useRoute } from 'vue-router'
import { httpInstance, type Response } from '@/utils/http';
import mermaid from 'mermaid';
import { useAiSummary } from '@/composables/useAiSummary';
import Toast from '@/utils/toast/Toast.vue';
import LikeButton from './LikeButton.vue';

// --- 类型定义 ---
interface Heading {
  id: string;
  text: string;
  level: number;
}

// --- 响应式数据 ---
const renderedHtml = ref('');
const allHeadings = ref<Heading[]>([]);
const showAboutPopup = ref(false);

// Mermaid 初始化
mermaid.initialize({ startOnLoad: false, theme: 'default' });
// 作者信息数据（在渲染前更新）
const authorMeta = { writer: '', createdAt: '', len: 0 };

// 作者信息注入插件（只注册一次，数据从 authorMeta 读取）
const authorPlugin = (md: any) => {
  md.core.ruler.push('inject_author', (state: any) => {
    const h1Index = state.tokens.findIndex((t: any) => t.type === 'heading_open' && t.tag === 'h1');
    if (h1Index === -1) return
    const h1CloseIndex = state.tokens.findIndex((t: any, i: any) => i > h1Index && t.type === 'heading_close');
    const authorInfoHtml = `
      <div class="author-info">
        🖊︎<span class="author-info-span"> ${authorMeta.writer}</span>
        🕮<span class="author-info-span"> 约${authorMeta.len}字</span>
        ⏰︎<span class="author-info-span"> ${authorMeta.createdAt.substring(0, 10)}</span>
      </div>
    `
    state.tokens.splice(h1CloseIndex + 1, 0, {
      type: 'html_block',
      content: authorInfoHtml,
      block: true,
      level: 0
    })
  })
}
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
  .use(container as any, 'danger')
  .use(authorPlugin);
// --- Mermaid 渲染拦截 ---
const defaultRender = md.renderer.rules.fence;
md.renderer.rules.fence = (tokens, idx, options, env, self) => {
  const token: any = tokens[idx];
  if (token.info === 'mermaid') {
    const code = token.content.trim();
    return `<div class="mermaid">${code}</div>`;
  }
  return defaultRender!(tokens, idx, options, env, self);
};
// --- Mermaid 渲染触发函数 ---
const triggerMermaid = async () => {
  await nextTick();
  // 渲染所有 .mermaid 元素
  const elements = document.querySelectorAll('.mermaid');
  if (elements.length > 0) {
    await mermaid.run({ nodes: elements as any });
  }
};

// 文章信息
interface ArticleInfo {
  url: string;
  articleWriter: string;
  articleDecr: string;
  like: number;
  createdAt: string;
}

const articleInfo = ref<ArticleInfo>(
  {
    url: '',
    articleWriter: 'oldmerman',
    articleDecr: '无',
    like: 0,
    createdAt: '2026-1-1'
  }
);

const articleLength = ref<number>(0);
const rawMarkdown = ref('');

// 左侧栏：分组文章
interface GroupInfo {
  id: number;
  groupName: string;
  number: number;
  expanded: boolean;
  articles?: GroupArticle[];
}

interface GroupArticle {
  id: string;
  articleName: string;
}

const articleGroups = ref<GroupInfo[]>([]);
// 路由
const route = useRoute();
const isPublic = route.query.isPublic;

// 文章内容加载
const loadArticle = async (id: string | string[] | undefined) => {
  if (!id) {
    fetchDocument(1);
    return;
  }
  if (isPublic === 'private') {
    try {
      const res = await httpInstance.get<any, Response>(`/article/private/${id}`);
      if (res.code !== 200) {
        alert(`错误消息: ${res.message}, 将渲染默认文本`);
        fetchDocument(1);
        return;
      }
      const text: string = await httpInstance.get(res.data);
      rawMarkdown.value = text;
      renderedHtml.value = md.render(text);
      extractHeadings(text);
      triggerMermaid();
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
      articleInfo.value = res.data;
      if (articleInfo.value && articleInfo.value.url !== '') {
        const text: string = await httpInstance.get(articleInfo.value.url);
        rawMarkdown.value = text;
        articleLength.value = text.length;
        authorMeta.writer = articleInfo.value.articleWriter;
        authorMeta.createdAt = articleInfo.value.createdAt;
        authorMeta.len = articleLength.value;
        renderedHtml.value = md.render(text);
        extractHeadings(text);
        triggerMermaid();
      }
    } catch (error) {
      alert(error);
    }
  }
};

// 分组加载
const loadGroups = async () => {
  const articleId = route.params.id;
  if (articleId) {
    try {
      const res = await httpInstance.get<any, Response>(`/article/group/public/group_info/${articleId}`);
      if (res.code === 200 && res.data) {
        articleGroups.value = res.data.map((g: any) => ({ ...g, expanded: false }));
      }
    } catch (e) {
      console.error('Failed to load groups', e);
    }
  }
};

onMounted(async () => {
  await loadArticle(route.params.id);
  await loadGroups();
});

watch(() => route.params.id, async (newId) => {
  if (!newId) return;
  resetSummary();
  articleGroups.value = [];
  await loadArticle(newId);
  await loadGroups();
});

// 导航到分组文章
const goAndReRenderArticle = (id: string) => {
  router.push({ name: 'markdown', params: { id }, query: { isPublic: 'public' } });
};

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
  rawMarkdown.value = mockMd;
  extractHeadings(mockMd);
};

const extractHeadings = (content: string) => {
  // 1. 先过滤掉所有代码块的内容，防止误匹配其中的 # 注释
  // 匹配 ```...``` 之间的所有内容，替换为空格
  const cleanContent = content.replace(/```[\s\S]*?```/g, '');

  // 匹配行首 # 标题
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

const extractArticleName = (content: string): string => {
  const cleanContent = content.replace(/```[\s\S]*?```/g, '');
  const match = /^#\s+(.*)$/m.exec(cleanContent);
  return match?.[1]?.trim() ?? '';
};

// 右侧栏：过滤出 H2 作为"本页重点"
const leafHeadings = computed(() => {
  return allHeadings.value.filter(h => h.level === 2);
});

// 平滑滚动到锚点
const scrollTo = (id: string) => {
  const el = document.getElementById(id);
  if (el) {
    const y = el.getBoundingClientRect().top + window.pageYOffset - 80;
    window.scrollTo({ top: y, behavior: 'smooth' });
  }
};

const scrollProgress = ref(0);
const isNearBottom = ref(false);

const R = 18;
const CIRCUMFERENCE = 2 * Math.PI * R;

const dashOffset = computed(() => {
  return CIRCUMFERENCE * (1 - scrollProgress.value);
});

const handleScroll = () => {
  const scrollTop = window.scrollY;
  const docHeight = document.documentElement.scrollHeight - window.innerHeight;
  if (docHeight <= 0) {
    scrollProgress.value = 0;
    isNearBottom.value = false;
    return;
  }
  const progress = Math.min(Math.max(scrollTop / docHeight, 0), 1);
  scrollProgress.value = progress;
  isNearBottom.value = progress > 0.92;
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 左侧栏：展开/收起分组，首次展开时懒加载文章列表
const toggleGroup = async (groupId: number) => {
  const group = articleGroups.value.find(g => g.id === groupId);
  if (!group) return;
  group.expanded = !group.expanded;
  if (group.expanded && !group.articles) {
    try {
      const res = await httpInstance.get<any, Response>(`/article/group/public/${groupId}`);
      if (res.code === 200) {
        const currentId = route.params.id;
        group.articles = (res.data || []).filter(
          (a: any) => String(a.id) !== String(currentId)
        );
      }
    } catch (e) {
      console.error('Failed to load group articles', e);
    }
  }
};


// 跳转首页
const goToHome = () => {
  router.push({ name: 'home' });
}

// --- AI 智能摘要 ---
const {
  text: summaryText,
  generating: summaryGenerating,
  finished: summaryFinished,
  error: summaryError,
  generate: generateSummaryStream,
  stop: stopSummaryStream,
  reset: resetSummary,
} = useAiSummary();
const toastRef = ref<InstanceType<typeof Toast> | null>(null);
const toastMsg = ref('');
const toastType = ref<'success' | 'error'>('success');

const showToast = (type: 'success' | 'error', message: string) => {
  toastType.value = type;
  toastMsg.value = message;
  toastRef.value?.show();
};

const generateSummary = async () => {
  if (summaryGenerating.value) return;
  if (!rawMarkdown.value) {
    showToast('error', '文章内容为空，无法生成摘要');
    return;
  }
  await generateSummaryStream({
    articleId: String(route.params.id ?? ''),
    articleName: extractArticleName(rawMarkdown.value),
    content: rawMarkdown.value,
  });
  if (summaryError.value) {
    showToast('error', summaryError.value);
  }
};

const stopSummary = () => {
  stopSummaryStream();
};

// 点击外部关闭弹出层
const handleClickOutside = (e: Event) => {
  if (showAboutPopup.value) {
    showAboutPopup.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  window.addEventListener('scroll', handleScroll, { passive: true });
  handleScroll();
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
  window.removeEventListener('scroll', handleScroll);
});

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
          <div><a @click="goToHome" style="cursor: pointer;">主页</a></div>
          <div><a href="https://github.com" target="_blank">GitHub</a></div>
        </div>
      </div>
    </nav>

    <div class="vp-body">
      <!-- 2. 左侧栏：关联分组文章 -->
      <aside class="vp-sidebar-left">
        <div class="sidebar-content">
          <div class="sidebar-title">同分类文章</div>
          <ul v-if="articleGroups.length > 0" class="toc-tree">
            <li v-for="group in articleGroups" :key="group.id" class="group-item">
              <div class="group-header" @click="toggleGroup(group.id)">
                <span class="group-name">{{ group.groupName }}</span>
                <span class="group-meta">
                  <span class="group-arrow" :class="{ expanded: group.expanded }">▸</span>
                </span>
              </div>
              <transition name="expand">
                <ul v-if="group.expanded" class="group-articles">
                  <li v-for="article in group.articles" :key="article.id" class="article-item">
                    <div class="article-link" @click="goAndReRenderArticle(article.id)">{{ article.articleName }}</div>
                  </li>
                </ul>
              </transition>
            </li>
          </ul>
          <div v-else class="sidebar-empty">暂无关联分组</div>

          <div class="ai-summary-card" v-if="isPublic === 'public'">
            <div class="ai-summary-head">
              <span class="ai-summary-title">AI 智能摘要</span>
              <button
                v-if="!summaryText || summaryGenerating"
                class="ai-summary-btn"
                @click="summaryGenerating ? stopSummary() : generateSummary()"
              >{{ summaryGenerating ? '停止' : '生成摘要' }}</button>
            </div>
            <div v-if="summaryText" class="ai-summary-body">{{ summaryText }}<span v-if="summaryGenerating" class="ai-caret" /></div>
            <div v-else-if="summaryGenerating" class="ai-summary-body ai-summary-wait">正在理解文章内容…<span class="ai-caret" /></div>
            <div v-else class="ai-summary-body ai-summary-empty">点击"生成摘要"，获取由老鱼人智能体撰写的文章摘要</div>
            <div class="ai-summary-foot">生成自老鱼人智能体，仅供参考</div>
          </div>
        </div>
      </aside>

      <!-- 3. 中间内容区：VitePress 渲染引擎 -->
      <main class="vp-content">
        <div class="vp-doc-container">
          <article class="vp-doc" v-html="renderedHtml"></article>
          <LikeButton
            :article-id="String(route.params.id ?? '')"
            :count="articleInfo.like ?? 0"
          />
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
        <div
          class="scroll-top-btn"
          :class="{ 'near-bottom': isNearBottom }"
          @click="scrollToTop"
          role="button"
          tabindex="0"
          aria-label="回到顶部"
        >
          <svg viewBox="0 0 44 44" class="progress-ring">
            <circle class="ring-bg" cx="22" cy="22" :r="R" />
            <circle
              class="ring-fg"
              cx="22" cy="22" :r="R"
              :stroke-dasharray="CIRCUMFERENCE"
              :stroke-dashoffset="dashOffset"
            />
          </svg>
          <span class="arrow-up">↑</span>
        </div>
      </aside>
    </div>

    <Toast ref="toastRef" :message="toastMsg" :type="toastType" />
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
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
  font-size: 20px;
  font-weight: 500;
  color: #3b82f6;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.nav-links {
  display: flex;

}

.nav-links a {
  margin-left: 35px;
  text-decoration: none;
  color: #4b5563;
  font-size: 14px;
}

.nav-links a:hover {
  color: #3b82f6;
}

/* 🗂 布局 */
.vp-body {
  max-width: 1440px;
  margin: 0 auto;
  padding-top: 64px;
}

/* 📂 左侧栏 */
.vp-sidebar-left {
  width: 280px;
  border-right: 1px solid #f1f1f1;
  padding: 16px 12px;
  background: #fcfdfe;
  position: fixed;
  top: 64px;
  left: max(calc((100vw - 1440px) / 2), 0px);
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  margin: 12px 0;
}

.toc-tree {
  list-style: none;
  padding: 0;
}

.group-item {
  margin-bottom: 2px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
  user-select: none;
}

.group-header:hover {
  background: #eef2f6;
}

.group-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.group-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.group-arrow {
  font-size: 15px;
  color: #94a3b8;
  transition: transform 0.2s ease;
  display: inline-block;
  line-height: 1;
}

.group-arrow.expanded {
  transform: rotate(90deg);
}

.group-articles {
  list-style: none;
  padding: 2px 0 4px 0;
  margin: 0;
}

.article-item {
  padding: 0;
}

.article-link {
  display: block;
  padding: 6px 10px 6px 24px;
  font-size: 14px;
  color: #64748b;
  text-decoration: none;
  border-radius: 4px;
  transition: color 0.15s, background 0.15s;
  cursor: pointer;
}

.article-link:hover {
  color: #3b82f6;
  background: #eef2f6;
}

.sidebar-empty {
  font-size: 13px;
  color: #94a3b8;
  padding: 16px 10px;
}

/* AI 智能摘要卡片 */
.ai-summary-card {
  position: absolute;
  left: 12px;
  right: 28px;
  bottom: 16px;
  z-index: 5;
  display: flex;
  flex-direction: column;
  max-height: 40%;
  padding: 14px;
  border: 1px solid #d8e6f6;
  border-radius: 10px;
  background: linear-gradient(180deg, #f4f9ff, #fbfdff);
}

.ai-summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.ai-summary-title {
  font-size: 13px;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.02em;
}

.ai-summary-btn {
  border: none;
  background: #2563eb;
  color: #fff;
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
  white-space: nowrap;
}

.ai-summary-btn:hover {
  background: #1d4ed8;
}

.ai-summary-btn:active {
  transform: scale(0.95);
}

.ai-summary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.ai-summary-body {
  font-size: 13px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 2px;
}

.ai-summary-body::-webkit-scrollbar {
  width: 2px;
}

.ai-summary-body::-webkit-scrollbar-track {
  background: transparent;
}

.ai-summary-body::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.ai-summary-wait,
.ai-summary-empty {
  color: #94a3b8;
}

.ai-caret {
  display: inline-block;
  width: 7px;
  height: 14px;
  margin-left: 2px;
  vertical-align: -2px;
  background: #2563eb;
  animation: aiCaret 0.9s step-end infinite;
}

@keyframes aiCaret {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.ai-summary-foot {
  margin-top: 10px;
  padding-top: 8px;
  font-size: 11px;
  color: #7f9dbd;
  text-align: right;
}

.expand-enter-active,
.expand-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  overflow: hidden;
}

.expand-enter-from {
  opacity: 0;
  transform: translateY(-6px);
}

.expand-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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
  padding: 48px 64px;
  padding-left: calc(64px + 280px);
  padding-right: calc(64px + 240px);
}

.vp-doc-container {
  max-width: 768px;
  margin: 0 auto;
}

/* 📍 右侧栏 */
.vp-sidebar-right {
  width: 240px;
  padding: 32px 24px 24px;
  background: #fcfdfe;
  position: fixed;
  top: 64px;
  right: max(calc((100vw - 1440px) / 2), 0px);
  height: calc(100vh - 64px);
}

.aside-content {
  height: 100%;
  overflow-y: auto;
  padding-bottom: 72px;
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

/* 回到顶部按钮 */
.scroll-top-btn {
  position: absolute;
  right: 16px;
  bottom: 16px;
  width: 48px;
  height: 48px;
  cursor: pointer;
  transition: opacity 0.3s ease, transform 0.2s ease;
}

.scroll-top-btn.near-bottom {
  opacity: 0.3;
}

.scroll-top-btn:active {
  transform: scale(0.92);
}

.progress-ring {
  width: 100%;
  height: 100%;
  display: block;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #e2e8f0;
  stroke-width: 3.5;
}

.ring-fg {
  fill: none;
  stroke: #3b82f6;
  stroke-width: 3.5;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.08s linear;
}

.arrow-up {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 28px;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1;
  pointer-events: none;
}

/* author介绍 */
:deep(.author-info) {
  margin-top: 5px;
  margin-bottom: 15px;
  width: 60%;
  border-bottom: 2px solid rgb(170, 180, 182);
  font-size: 17px;
}

:deep(.author-info-span) {
  margin-right: 12px;
  font-style: italic;
}

:deep(.vp-doc) {
  line-height: 1.6;
  font-size: 16px;
}

:deep(.vp-doc h1) {
  font-size: 32px;
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
  margin-bottom: 20px;
}

:deep(pre>code) {
  color: #363636 !important;
  font-weight: 300;
}

:deep(p) {
  margin-bottom: 10px;
  color: black;
}

:deep(img) {
  width: 96%;
}

:deep(table) {
  width: 90%;
  margin: 15px auto;
  border-collapse: collapse;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  font-size: 14px;
}

:deep(th) {
  text-align: start;
  padding: 14px 16px;
  background: #f1f5f9;
  font-weight: 600;
  color: #1e293b;
}

:deep(td) {
  text-align: start;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}

:deep(tr:last-child td) {
  border-bottom: none;
}

:deep(.mermaid) {
  display: flex;
  justify-content: center;
  margin: 20px 0;
  overflow-x: auto;
}

/* 确保 SVG 渲染后不会溢出 */
:deep(.mermaid svg) {
  max-width: 100%;
  height: auto;
}

/* 超链接渲染 */
:deep(p>a) {
  color: rgb(0, 0, 0);
  background-color: rgb(246, 246, 247);
  padding: 3px;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  border-bottom: 1px solid black;
  box-shadow: #1e293b;
  transition: all 0.2s ease;
}

:deep(p>a:hover) {
  box-shadow: 0 1px black;
}

:deep(p>code) {
  background-color: rgb(246, 246, 247);
  padding: 5px 5px;
  border-radius: 8px;
  font-weight: 300;
  margin: 0 5px;
}

:deep(ol) {
  margin-left: 2.1%;
}
</style>
