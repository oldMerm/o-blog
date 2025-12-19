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
      const level = match[1]!.length
      const text = match[2]!.trim()
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

// 文章标题（取首个 H1）
const title = computed(() => {
  const m = source.value.match(/^#\s+(.+)$/m)
  return m?.[1]?.trim() ?? '文章'
})

// 聊天/留言区域的数据结构（支持一级回复）
interface Reply {
  id: number
  author: string
  content: string
  time: string
}
interface Comment {
  id: number
  author: string
  content: string
  time: string
  replies: Reply[]
}

const comments = ref<Comment[]>([])
const newComment = ref('')
const sending = ref(false)

// 只允许对一级评论回复：维护哪个评论正在回复中，以及输入内容
const openReplyId = ref<number | null>(null)
const replyTexts = ref<Record<number, string>>({})

// 文章点赞状态（本地缓存）
const likes = ref<number>(0)
const liked = ref<boolean>(false)

const emit = defineEmits<{
  (e: 'comment', payload: { content: string }): void
  (e: 'like', payload: { liked: boolean }): void
}>()

const storageKeyFor = (t: string) => `article_like_${encodeURIComponent(t)}`

const loadLikeState = () => {
  try {
    const key = storageKeyFor(title.value)
    const raw = localStorage.getItem(key)
    if (raw) {
      const s = JSON.parse(raw)
      likes.value = s.likes ?? 0
      liked.value = !!s.liked
    }
  } catch (e) { /* ignore */ }
}

loadLikeState()
watch(title, loadLikeState)

const saveLikeState = () => {
  try {
    const key = storageKeyFor(title.value)
    localStorage.setItem(key, JSON.stringify({ likes: likes.value, liked: liked.value }))
  } catch (e) { /* ignore */ }
}

const toggleLike = () => {
  if (!liked.value) {
    likes.value += 1
    liked.value = true
  } else {
    likes.value = Math.max(0, likes.value - 1)
    liked.value = false
  }
  saveLikeState()
  emit('like', { liked: liked.value })
}

const formatTime = (d = new Date()) => d.toLocaleString()

const handleSend = async () => {
  const content = newComment.value.trim()
  if (!content) return
  sending.value = true
  // 小模拟延迟以提供更好的 UX
  await new Promise(r => setTimeout(r, 250))
  const c: Comment = { id: Date.now(), author: '匿名', content, time: formatTime(), replies: [] }
  comments.value.push(c)
  emit('comment', { content })
  newComment.value = ''
  sending.value = false
  await nextTick()
  const el = document.querySelector('.comment-list')
  if (el) el.scrollTop = el.scrollHeight
}

// 打开某条一级评论的回复输入（如果从回复操作触发，会 prefill @用户名）
const openReply = (parentId: number, suggested?: string) => {
  openReplyId.value = parentId
  if (suggested) replyTexts.value[parentId] = `@${suggested} `
  nextTick(() => {
    const el = document.querySelector(`.reply-input[data-for="${parentId}"]`) as HTMLTextAreaElement | null
    if (el) el.focus()
  })
}

const handleSendReply = async (parentId: number) => {
  const content = (replyTexts.value[parentId] || '').trim()
  if (!content) return
  sending.value = true
  await new Promise(r => setTimeout(r, 250))
  const r: Reply = { id: Date.now(), author: '匿名', content, time: formatTime() }
  const parent = comments.value.find(c => c.id === parentId)
  if (parent) {
    parent.replies.push(r)
  }
  replyTexts.value[parentId] = ''
  openReplyId.value = null
  sending.value = false
  await nextTick()
  // 滚动到该父评论位置
  const el = document.querySelector(`.comment-item[data-comment-id="${parentId}"]`)
  if (el) (el as HTMLElement).scrollIntoView({ behavior: 'smooth', block: 'nearest' })
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
      <div class="content-inner">
        <div class="markdown-body" v-html="html"></div>

        <!-- 评论/聊天栏（与文章内容同宽） -->
        <section class="comment-panel" aria-label="评论区">
          <div class="comment-container">
            <div class="comment-header">
              <strong>留言与讨论</strong>
              <small class="comment-note">支持 Markdown 渲染（后续接入后端）</small>
            </div>

            <ul class="comment-list" role="list">
              <li v-for="c in comments" :key="c.id" class="comment-item" :data-comment-id="c.id">
                <div class="avatar">{{ c.author.charAt(0) }}</div>
                <div class="comment-body">
                  <div class="comment-top">
                    <span class="author">{{ c.author }}</span>
                    <span class="time">{{ c.time }}</span>
                    <button class="btn-reply" @click="openReply(c.id, c.author)">回复</button>
                  </div>

                  <div class="comment-content">{{ c.content }}</div>

                  <ul class="reply-list" v-if="c.replies && c.replies.length">
                    <li v-for="r in c.replies" :key="r.id" class="reply-item">
                      <div class="reply-meta">
                        <span class="reply-author">{{ r.author }}</span>
                        <span class="reply-time">{{ r.time }}</span>
                        <button class="btn-reply-tiny" @click="openReply(c.id, r.author)">回复</button>
                      </div>
                      <div class="reply-content">{{ r.content }}</div>
                    </li>
                  </ul>

                  <div v-if="openReplyId === c.id" class="reply-input-row">
                    <textarea
                      v-model="replyTexts[c.id]"
                      class="reply-input"
                      :data-for="c.id"
                      placeholder="写回复…（按 Enter 发送，Shift+Enter 换行）"
                      @keydown.enter.exact.prevent="handleSendReply(c.id)"
                      @keydown.enter.shift.stop
                      rows="2"
                    ></textarea>
                    <button class="btn-send-reply" :disabled="sending || !(replyTexts[c.id] || '').trim()" @click="handleSendReply(c.id)">
                      {{ sending ? '发送中...' : '回复' }}
                    </button>
                  </div>
                </div>
              </li>

              <li v-if="comments.length === 0" class="comment-empty">暂无留言，成为第一个留言的人吧～</li>
            </ul>

            <div class="comment-input-row">
              <textarea
                v-model="newComment"
                class="comment-input"
                placeholder="写下你的留言…（按 Enter 发送，Shift+Enter 换行）"
                @keydown.enter.exact.prevent="handleSend"
                @keydown.enter.shift.stop
                rows="2"
              ></textarea>
              <button class="btn-send" :disabled="sending || !newComment.trim()" @click="handleSend">
                {{ sending ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<style scoped>
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
  /* 左移一点：减小左侧内边距，让内容更靠近侧边栏 */
  padding: 20px 30px 24px 64px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* 简单的 Markdown 内容样式微调 */
.content-inner {
  max-width: 900px;
  margin: 0  auto 0 0; /* 左对齐，靠近侧边 */
  width: 100%;
}

.like-btn {
  border: 1px solid transparent;
  background: linear-gradient(180deg,#f7fdfd,#f0fbfb);
  color: #007b6b;
  padding: 6px 10px;
  border-radius: 8px;
  display: inline-flex;
  gap: 6px;
  align-items: center;
  cursor: pointer;
  transition: all 0.18s ease;
}
.like-btn .heart { font-size: 1.05rem }
.like-btn.liked {
  background: linear-gradient(180deg,#ffdde1,#ffc5cc);
  color: #c62828;
  box-shadow: 0 4px 12px rgba(198,40,40,0.08);
  transform: translateY(-1px);
}

/* 回复相关样式 */
.reply-list {
  margin-top: 10px;
  padding-left: 48px; /* 与 avatar 对齐 */
}
.reply-item {
  padding: 8px 0;
  border-left: 2px dashed rgba(132, 201, 194, 0.15);
  margin-bottom: 6px;
  padding-left: 12px;
}
.reply-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}
.reply-author { font-weight: 700; color: #23333a }
.reply-time { color: #88959a; font-size: 0.82rem }
.btn-reply, .btn-reply-tiny {
  margin-left: auto;
  background: transparent;
  border: none;
  color: #007b6b;
  cursor: pointer;
  font-size: 0.85rem;
}

.reply-input-row {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  align-items: flex-end;
}
.reply-input { flex: 1; min-height: 36px; padding: 8px 10px; border: 1px solid #E6F3F3; border-radius: 6px }
.btn-send-reply { background: #00bfa5; color: white; border: none; padding: 8px 10px; border-radius: 6px }
.btn-send-reply:disabled { opacity: 0.6; cursor: not-allowed }


.markdown-body {
  max-width: 900px;
  word-break: break-all;     /* 允许在单词内换行 */
  overflow-wrap: break-word; /* 长单词强制换行 */
  margin: 0 auto;
  background: rgba(250, 255, 255, 0.6);
  padding: 28px 32px;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(61, 80, 87, 0.06);
}

/* 评论区域 */
.comment-panel {
  margin-top: 26px;
}
.comment-container {
  background: #ffffff;
  border: 1px solid #EAF6F6;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 6px 18px rgba(61, 80, 87, 0.03);
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
}
.comment-note {
  color: #8f9aa0;
  font-size: 0.8rem;
}
.comment-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px 4px;
  margin: 0 0 12px 0;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 8px 6px;
  align-items: flex-start;
}
.avatar {
  min-width: 36px;
  height: 36px;
  background: linear-gradient(180deg,#80cbc4,#4db6ac);
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.comment-body {
  flex: 1;
}
.comment-top {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 6px;
}
.author {
  font-weight: 700;
  color: #23333a;
}
.time {
  font-size: 0.82rem;
  color: #88959a;
}
.comment-content {
  line-height: 1.5;
}
.comment-empty {
  color: #8f9aa0;
  padding: 12px;
  text-align: center;
}
.comment-input-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.comment-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #E6F3F3;
  border-radius: 8px;
  resize: vertical;
  min-height: 44px;
  font-size: 0.95rem;
}
.btn-send {
  background: linear-gradient(180deg,#00bfa5,#00a28a);
  color: #fff;
  border: none;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
}
.btn-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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