<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/vs.css'

const props = withDefaults(
  defineProps<{
    content: string
    streaming?: boolean
  }>(),
  { streaming: false }
)

const emit = defineEmits<{
  (e: 'tick'): void
}>()

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        const highlighted = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
        return `<pre><code class="hljs">${highlighted}</code></pre>`
      } catch {
        // 高亮失败时回退为纯文本
      }
    }
    return `<pre><code class="hljs">${escapeHtml(str)}</code></pre>`
  },
})

const displayed = ref('')
const pending = ref('')
let timer: ReturnType<typeof setInterval> | null = null

const stopTimer = () => {
  if (timer !== null) {
    clearInterval(timer)
    timer = null
  }
}

const drain = () => {
  if (pending.value.length > 0) {
    const step = Math.max(1, Math.ceil(pending.value.length / 10))
    displayed.value += pending.value.slice(0, step)
    pending.value = pending.value.slice(step)
    emit('tick')
  }
  if (pending.value.length === 0) stopTimer()
}

const ensureTimer = () => {
  if (timer === null) timer = setInterval(drain, 16)
}

watch(
  () => props.content,
  (value) => {
    if (!value) {
      displayed.value = ''
      pending.value = ''
      stopTimer()
      return
    }

    // 历史消息直接展示，不重新播放打字机效果
    if (!displayed.value && !pending.value && !props.streaming) {
      displayed.value = value
      return
    }

    const current = displayed.value + pending.value
    if (value.startsWith(current)) {
      pending.value += value.slice(current.length)
    } else {
      displayed.value = value
      pending.value = ''
    }

    if (pending.value.length > 0) ensureTimer()
  },
  { immediate: true }
)

watch(
  () => props.streaming,
  (value) => {
    if (!value && pending.value.length === 0) stopTimer()
    if (value && pending.value.length > 0) ensureTimer()
  }
)

onUnmounted(stopTimer)

const html = computed(() => md.render(displayed.value))
</script>

<template>
  <div class="streaming-markdown">
    <div class="markdown-body" v-html="html"></div>
    <span v-if="streaming || pending.length > 0" class="stream-caret" aria-hidden="true"></span>
  </div>
</template>

<style scoped>
.streaming-markdown {
  font-size: 14px;
  line-height: 1.75;
  color: #303133;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.markdown-body :deep(p) {
  margin: 0 0 8px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5) {
  margin: 12px 0 6px;
  color: #303133;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body :deep(h1) {
  font-size: 18px;
}

.markdown-body :deep(h2) {
  font-size: 16px;
}

.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5) {
  font-size: 15px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 4px 0 8px;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 3px 0;
}

.markdown-body :deep(code) {
  padding: 1px 5px;
  border-radius: 4px;
  background: #f1f5f9;
  color: #409eff;
  font-family: var(--font-mono);
  font-size: 12px;
}

.markdown-body :deep(pre) {
  margin: 8px 0;
  padding: 10px 12px;
  overflow-x: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f6f8fa;
}

.markdown-body :deep(pre code),
.markdown-body :deep(pre code.hljs) {
  padding: 0;
  background: transparent;
  color: #303133;
  font-size: 12px;
  line-height: 1.6;
}

.markdown-body :deep(blockquote) {
  margin: 8px 0;
  padding: 6px 10px;
  border-left: 3px solid #c6e2ff;
  background: #f7fbff;
  color: #606266;
}

.markdown-body :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(table) {
  width: 100%;
  margin: 8px 0;
  border-collapse: collapse;
  font-size: 13px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 6px 8px;
  border: 1px solid #e4e7ed;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.markdown-body :deep(hr) {
  margin: 12px 0;
  border: none;
  border-top: 1px solid #e4e7ed;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}

.stream-caret {
  display: inline-block;
  width: 6px;
  height: 14px;
  margin-left: 2px;
  vertical-align: -2px;
  background: #409eff;
  animation: stream-caret-blink 1s step-end infinite;
}

@keyframes stream-caret-blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
