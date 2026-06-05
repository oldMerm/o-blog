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

interface Heading {
  id: string;
  text: string;
  level: number;
}

const renderedHtml = ref('');
const loading = ref(true);
const allHeadings = ref<Heading[]>([]);
const scrollProgress = ref(0);
const isNearBottom = ref(false);

mermaid.initialize({ startOnLoad: false, theme: 'default' });

const authorMeta = { writer: '', createdAt: '', len: 0 };

const authorPlugin = (md: any) => {
  md.core.ruler.push('inject_author', (state: any) => {
    const h1Index = state.tokens.findIndex((t: any) => t.type === 'heading_open' && t.tag === 'h1');
    if (h1Index === -1) return
    const h1CloseIndex = state.tokens.findIndex((t: any, i: any) => i > h1Index && t.type === 'heading_close');
    const authorInfoHtml = `
      <div class="author-info">
        <span class="author-info-span"> ${authorMeta.writer}</span>
        <span class="author-info-span"> 约${authorMeta.len}字</span>
        <span class="author-info-span"> ${authorMeta.createdAt.substring(0, 10)}</span>
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

const md = new MarkdownIt({
  html: true,
  linkify: true,
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch (__) { }
    }
    return '';
  },
  typographer: true,
})
  .use(anchor, {
    slugify: (s) => encodeURIComponent(String(s).trim().toLowerCase().replace(/\s+/g, '-'))
  })
  .use(container as any, 'tip')
  .use(container as any, 'warning')
  .use(container as any, 'danger')
  .use(authorPlugin);

const defaultRender = md.renderer.rules.fence;
md.renderer.rules.fence = (tokens, idx, options, env, self) => {
  const token: any = tokens[idx];
  if (token.info === 'mermaid') {
    const code = token.content.trim();
    return `<div class="mermaid">${code}</div>`;
  }
  return defaultRender!(tokens, idx, options, env, self);
};

const triggerMermaid = async () => {
  await nextTick();
  const elements = document.querySelectorAll('.mermaid');
  if (elements.length > 0) {
    await mermaid.run({ nodes: elements as any });
  }
};

interface ArticleInfo {
  url: string;
  articleWriter: string;
  articleDecr: string;
  createdAt: string;
}

const articleInfo = ref<ArticleInfo>({
  url: '',
  articleWriter: 'oldmerman',
  articleDecr: '无',
  createdAt: '2026-1-1'
});

const articleLength = ref(0);
const route = useRoute();

const loadArticle = async (id: string | string[] | undefined) => {
  loading.value = true;
  if (!id) return;
  try {
    const res = await httpInstance.get<any, Response>(`/article/public/${id}`);
    if (res.code !== 200) {
      renderedHtml.value = `<div class="error-msg">${res.message || '文章加载失败'}</div>`;
      return;
    }
    articleInfo.value = res.data;
    if (articleInfo.value && articleInfo.value.url !== '') {
      const text: string = await httpInstance.get(articleInfo.value.url);
      articleLength.value = text.length;
      authorMeta.writer = articleInfo.value.articleWriter;
      authorMeta.createdAt = articleInfo.value.createdAt;
      authorMeta.len = articleLength.value;
      renderedHtml.value = md.render(text);
      extractHeadings(text);
      await triggerMermaid();
    }
  } catch (error) {
    renderedHtml.value = `<div class="error-msg">网络错误，请稍后重试</div>`;
  } finally {
    loading.value = false;
  }
};

const extractHeadings = (content: string) => {
  const cleanContent = content.replace(/```[\s\S]*?```/g, '');
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

const goToHome = () => {
  router.push({ name: 'moblie_home' });
};

onMounted(async () => {
  await loadArticle(route.params.id);
  window.addEventListener('scroll', handleScroll, { passive: true });
  handleScroll();
});

watch(() => route.params.id, async (newId) => {
  if (!newId) return;
  window.scrollTo(0, 0);
  await loadArticle(newId);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<template>
  <div class="mobile-doc-root">
    <nav class="mob-topbar">
      <span class="mob-brand">老鱼人文档中心</span>
      <div class="mob-nav-items">
        <a class="nav-link" @click="goToHome">首页</a>
      </div>
    </nav>

    <main class="mob-content">
      <div v-if="loading" class="loading-wrap">
        <div class="loading-dots">
          <span v-for="i in 3" :key="i" class="ldot" :style="{ animationDelay: `${i * 0.2}s` }" />
        </div>
      </div>
      <article v-else class="mob-doc" v-html="renderedHtml" />
    </main>

    <div
      class="scroll-btn"
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
  </div>
</template>

<style>
html, body {
  margin: 0 !important;
  padding: 0 !important;
  width: 100% !important;
  min-width: unset !important;
  overflow-x: hidden !important;
}
</style>

<style scoped>
.mobile-doc-root {
  min-height: 100vh;
  min-height: 100dvh;
  width: 100vw;
  overflow-x: hidden;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  touch-action: manipulation;
  color: #1e293b;
}

.mobile-doc-root * {
  -webkit-tap-highlight-color: transparent;
}

.mob-topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
}

.mob-brand {
  font-size: 1.05rem;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.03em;
}

.mob-nav-items {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2563eb;
  text-decoration: none;
  border-bottom: 2.5px solid #2563eb;
  padding-bottom: 2px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.nav-link:active {
  opacity: 0.6;
}

.mob-content {
  padding: 0;
  width: 100%;
  max-width: 100%;
  min-height: calc(100vh - 52px);
  min-height: calc(100dvh - 52px);
}

.loading-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80px 0;
}

.loading-dots {
  display: flex;
  gap: 8px;
}

.ldot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #3b82f6;
  animation: dotPulse 1.2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { transform: scale(0.6); opacity: 0.3; }
  50% { transform: scale(1); opacity: 1; }
}

.mob-doc {
  padding: 20px 16px 80px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  line-height: 1.7;
  font-size: 16px;
  overflow-x: hidden;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.mob-doc :deep(h1) {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #0f172a;
  line-height: 1.35;
}

.mob-doc :deep(h2) {
  font-size: 20px;
  font-weight: 700;
  margin: 28px 0 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e2e8f0;
  color: #1e293b;
}

.mob-doc :deep(h3) {
  font-size: 17px;
  font-weight: 600;
  margin: 20px 0 8px;
  color: #1e293b;
}

.mob-doc :deep(h4) {
  font-size: 16px;
  font-weight: 600;
  margin: 14px 0 6px;
  color: #334155;
}

.mob-doc :deep(h5) {
  font-size: 15px;
  font-weight: 600;
  margin: 10px 0 6px;
  color: #475569;
}

.mob-doc :deep(p) {
  margin-bottom: 14px;
  color: #1e293b;
}

.mob-doc :deep(a) {
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px solid #93c5fd;
  transition: border-color 0.2s;
}

.mob-doc :deep(a:active) {
  border-bottom-color: #2563eb;
}

.mob-doc :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 16px 0;
}

.mob-doc :deep(blockquote) {
  margin: 16px 0;
  padding: 10px 14px;
  border-left: 3px solid #94a3b8;
  background: #f8fafc;
  color: #475569;
}

.mob-doc :deep(ul),
.mob-doc :deep(ol) {
  padding-left: 24px;
  margin-bottom: 14px;
}

.mob-doc :deep(li) {
  margin-bottom: 6px;
  color: #1e293b;
}

.mob-doc :deep(hr) {
  border: none;
  border-top: 1px solid #e2e8f0;
  margin: 28px 0;
}

.mob-doc :deep(pre) {
  background: #f1f5f9;
  padding: 14px 12px;
  border-radius: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  margin: 16px 0;
  border: 1px solid #e2e8f0;
  font-size: 13px;
  line-height: 1.55;
  max-width: 100%;
  box-sizing: border-box;
}

.mob-doc :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #1e293b !important;
  font-weight: 400;
  font-size: 13px;
}

.mob-doc :deep(p code) {
  background: #f1f5f9;
  padding: 2px 7px;
  border-radius: 5px;
  font-size: 14px;
  color: #2563eb;
  font-weight: 500;
}

.mob-doc :deep(table) {
  width: 100%;
  margin: 16px 0;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  font-size: 14px;
  display: block;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  max-width: 100%;
}

.mob-doc :deep(th) {
  text-align: left;
  padding: 10px 14px;
  background: #f1f5f9;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
}

.mob-doc :deep(td) {
  text-align: left;
  padding: 10px 14px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}

.mob-doc :deep(tr:last-child td) {
  border-bottom: none;
}

.mob-doc :deep(.mermaid) {
  display: flex;
  justify-content: center;
  margin: 20px 0;
  overflow-x: auto;
}

.mob-doc :deep(.mermaid svg) {
  max-width: 100%;
  height: auto;
}

.mob-doc :deep(.author-info) {
  margin-top: 2px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1.5px solid #dce4ee;
  font-size: 14px;
  color: #64748b;
  display: flex;
  flex-wrap: wrap;
  gap: 2px 8px;
}

.mob-doc :deep(.author-info-span) {
  margin-right: 6px;
  font-style: italic;
}

.mob-doc :deep(.custom-block) {
  margin: 16px 0;
  padding: 12px 14px;
  border-radius: 6px;
  font-size: 14px;
}

.mob-doc :deep(.custom-block.tip) {
  background: #f0f7ff;
  border-left: 3px solid #3b82f6;
  color: #1e40af;
}

.mob-doc :deep(.custom-block.warning) {
  background: #fffbeb;
  border-left: 3px solid #f59e0b;
  color: #92400e;
}

.mob-doc :deep(.custom-block.danger) {
  background: #fef2f2;
  border-left: 3px solid #ef4444;
  color: #991b1b;
}

.mob-doc :deep(.error-msg) {
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
  font-size: 15px;
}

.scroll-btn {
  position: fixed;
  bottom: 28px;
  right: 16px;
  width: 46px;
  height: 46px;
  cursor: pointer;
  z-index: 200;
  transition: opacity 0.3s ease, transform 0.2s ease;
}

.scroll-btn.near-bottom {
  opacity: 0.3;
}

.scroll-btn:active {
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
  font-size: 18px;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1;
  pointer-events: none;
}

</style>
