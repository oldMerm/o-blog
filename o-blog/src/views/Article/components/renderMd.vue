<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import MarkdownIt from 'markdown-it'
import markdownItPrism from 'markdown-it-prism'
import DOMPurify from 'dompurify'

// 引入 prism 的样式 (你可以根据喜好换主题，如 'prismjs/themes/prism-tomorrow.css')
import 'prismjs/themes/prism.css' 

// ---------------------------------------------------------
// 1. 模拟数据 (实际使用时可从 props 或 store 获取)
// ---------------------------------------------------------
const str = `
# 浅海航行日志
欢迎来到新的文档系统。

## 1. 启航准备
### 1.1 检查仪表盘
这里有一些代码示例:
\`\`\`javascript
const ocean = "blue";
console.log(ocean);
\`\`\`

### 1.2 升起风帆
风向正常，可以出发。
#### 1.2.1 调整角度
这是四级标题，也会归纳在父级下。

## 2. 航行路线
### 2.1 穿越珊瑚礁
注意避让。
### 2.2 抵达深蓝
这里水深超过 200 米。

# 🐋 遇见鲸鱼
这是一个新的一级标题。
## 附录：海洋生物图鉴
`

const source = ref(str)

// ---------------------------------------------------------
// 2. Markdown 解析配置
// ---------------------------------------------------------
const mdParser = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
})

// 使用 Prism 插件进行代码高亮
mdParser.use(markdownItPrism, {})

// ---------------------------------------------------------
// 3. 核心逻辑：HTML 生成与 TOC 树形结构构建
// ---------------------------------------------------------

// 目录节点接口
interface TocNode {
  level: number
  text: string
  slug: string
  children: TocNode[] // 存放 H3+ 的子标题
  expanded: boolean   // 控制展开/折叠
}

const tocTree = ref<TocNode[]>([])

// 生成安全的 HTML
const html = computed(() => {
  const rawHtml = mdParser.render(source.value)
  
  // 1. 注入 ID (简单实现，生产环境建议 markdown-it-anchor)
  // 2. 使用 DOMPurify 清洗，防止 XSS
  const htmlWithIds = rawHtml.replace(/<(h[1-6])>(.*?)<\/\1>/g, (match, tag, content) => {
    // 移除 HTML 标签保留纯文本作为 ID
    const slug = content.replace(/<[^>]+>/g, '').trim()
    return `<${tag} id="${slug}">${content}</${tag}>`
  })

  return DOMPurify.sanitize(htmlWithIds)
})

// 提取并构建树形目录
const buildTocTree = (mdStr: string) => {
  const lines = mdStr.split('\n')
  const regex = /^(#{1,6})\s+(.+)$/
  
  const tree: TocNode[] = []
  let currentParent: TocNode | null = null

  lines.forEach(line => {
    const match = line.match(regex)

    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const slug = text // 简单 ID 生成

      const node: TocNode = {
        level,
        text,
        slug,
        children: [],
        expanded: false // 默认折叠
      }

      // 逻辑：H1 和 H2 视为“父节点”，H3 及以上视为“子节点”
      if (level <= 2) {
        tree.push(node)
        currentParent = node
      } else {
        // 如果有父节点，则加入父节点的 children
        if (currentParent) {
          currentParent.children.push(node)
        } else {
          // 极端情况：文档以 H3 开头，暂时作为根节点处理
          tree.push(node)
          currentParent = node // 把它当作临时父节点
        }
      }
    }
  })
  
  tocTree.value = tree
}

// 监听内容变化
watch(source, (newVal) => {
  buildTocTree(newVal)
}, { immediate: true })

// ---------------------------------------------------------
// 4. 交互逻辑
// ---------------------------------------------------------

// 切换展开状态
const toggleExpand = (item: TocNode) => {
  if (item.children.length > 0) {
    item.expanded = !item.expanded
  }
}

// 平滑滚动
const scrollTo = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 点击父标题：既要跳转，又要展开
const handleParentClick = (item: TocNode) => {
  scrollTo(item.slug)
  // 如果有子项且当前未展开，则自动展开；如果已展开则保持（或者你也可以选择 toggle）
  if (item.children.length > 0 && !item.expanded) {
    item.expanded = true
  } else if (item.children.length > 0 && item.expanded) {
    // 可选：再次点击是否折叠？这里我设定为仅切换折叠，但也跳转
    item.expanded = !item.expanded
  }
}

</script>

<template>
  <div class="light-ocean-layout">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <div class="sidebar-title">导航目录</div>
      
      <ul class="toc-root">
        <li v-for="(item, index) in tocTree" :key="index" class="toc-group">
          
          <!-- 父标题 (H1, H2) -->
          <div 
            class="parent-title" 
            :class="{ 'has-children': item.children.length > 0, 'active': item.expanded }"
            @click="handleParentClick(item)"
          >
            <!-- 展开/折叠 箭头 -->
            <span 
              v-if="item.children.length > 0" 
              class="arrow-icon"
              :class="{ 'rotated': item.expanded }"
            >▶</span>
            <span v-else class="arrow-placeholder"></span>

            <span class="text">{{ item.text }}</span>
          </div>

          <!-- 子标题列表 (H3+) -->
          <transition name="slide-fade">
            <ul v-show="item.expanded" class="child-list">
              <li 
                v-for="(child, cIndex) in item.children" 
                :key="cIndex"
                class="child-item"
                @click.stop="scrollTo(child.slug)"
              >
                <span class="level-dot"></span>
                {{ child.text }}
              </li>
            </ul>
          </transition>
        </li>
      </ul>
    </aside>

    <!-- 右侧 Markdown 内容 -->
    <main class="content-wrapper">
      <div class="markdown-body" v-html="html"></div>
    </main>
  </div>
</template>

<style scoped>
/* 
  🎨 浅色温带海洋色系 (Light Temperate Ocean)
  Background: #EAF6F6 (极浅青) ~ #FFFFFF
  Sidebar Bg: #F0FBFC
  Text Dark: #2C3E50 (深海灰)
  Text Light: #5D7A88 (浅海灰)
  Accent: #4DB6AC (海藻绿/青)
  Hover: #D1EFF2
*/

.light-ocean-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #fff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #2C3E50;
}

/* --- 侧边栏 --- */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  /* 极浅的渐变背景，像清晨的海面 */
  background: linear-gradient(180deg, #F0FBFC 0%, #E0F7FA 100%);
  border-right: 1px solid #B2EBF2;
  overflow-y: auto;
  box-sizing: border-box;
}

.sidebar-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #00838F; /* 深青色 */
  padding:10px 10px;
  border-bottom: 2px solid #B2EBF2;
  letter-spacing: 1px;
}

.toc-root {
  list-style: none;
  padding: 0;
  margin: 0;
}


/* 父标题样式 */
.parent-title {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 600;
  font-size: 0.95rem;
  color: #37474F;
}

.parent-title:hover {
  background-color: #B2EBF2; /* 浅蓝高亮 */
  color: #006064;
}

.parent-title.active {
  background-color: #E0F2F1;
  color: #00695C;
}

/* 箭头图标 */
.arrow-icon {
  font-size: 0.7rem;
  margin-right: 8px;
  color: #80CBC4;
  transition: transform 0.3s ease;
  display: inline-block;
}

.arrow-icon.rotated {
  transform: rotate(90deg);
}

.arrow-placeholder {
  width: 14px; /* 占位，保持对齐 */
  margin-right: 8px;
}

.text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 子标题列表 */
.child-list {
  list-style: none;
  padding: 0;
  margin: 0 0 8px 22px; /* 缩进 */
  border-left: 2px solid #E0F2F1; /* 左侧连接线 */
}

.child-item {
  padding: 6px 12px;
  font-size: 0.85rem;
  color: #546E7A;
  cursor: pointer;
  border-radius: 0 4px 4px 0;
  transition: color 0.2s;
  display: flex;
  align-items: center;
}

.child-item:hover {
  color: #00838F;
  background-color: rgba(77, 182, 172, 0.1);
}

.level-dot {
  width: 4px;
  height: 4px;
  background-color: #80CBC4;
  border-radius: 50%;
  margin-right: 8px;
  opacity: 0.6;
}

/* --- 右侧内容 --- */
.content-wrapper {
  flex: 1;
  padding: 30px 30px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* 简单的 Markdown 内容样式微调 */
.markdown-body {
  max-width: 900px;
  word-break: break-all;     /* 允许在单词内换行 */
  overflow-wrap: break-word; /* 长单词强制换行 */
  margin-left: 30px;
}

/* 针对 Prism 代码块的微调，让它融入浅色主题 */
/* 修复：给代码块增加垂直间距，解决"下面没有空行"的问题 */
.markdown-body :deep(pre) {
  /* 原有的样式保持不变 */
  background-color: #f5f7f9 !important;
  border-radius: 8px;
  border: 1px solid #e1e4e8;
  
  /* 新增：增加上下间距 */
  margin-top: 1rem;    /* 视情况调整 */
  margin-bottom: 2rem; /* 让代码块下面和段落一样有呼吸感 */
}
.markdown-body :deep(code) {
  font-family: 'Fira Code', Consolas, monospace;
}

/* 针对 Markdown 内容的排版微调 */
.markdown-body :deep(p) {
  margin-bottom: 1.5em; /*稍微改小一点，平衡一下*/
  line-height: 1.6;
}

/* 修复：消除因 breaks: true 导致的段落末尾多余空行 */
.markdown-body :deep(p) br:last-child {
  display: none;
}

/* 如果你希望列表内的换行也不要太挤 */
.markdown-body :deep(li) {
  margin-bottom: 0.5em;
}

/* 动画效果 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
  max-height: 500px; /* 估算最大高度 */
  overflow: hidden;
  opacity: 1;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-5px);
}
</style>