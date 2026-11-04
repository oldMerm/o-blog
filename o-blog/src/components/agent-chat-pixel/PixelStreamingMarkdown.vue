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
  <div class="pixel-markdown">
    <div class="pixel-markdown-body" v-html="html"></div>
    <span v-if="streaming || pending.length > 0" class="pixel-stream-caret" aria-hidden="true"></span>
  </div>
</template>

<style scoped>
.pixel-markdown {
  color: var(--pc-ink, #11111b);
  font-family: var(--pc-font-mono, 'Courier New', Consolas, 'Liberation Mono', 'PingFang SC', 'Microsoft YaHei', monospace);
  font-size: 13px;
  line-height: 1.8;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.pixel-markdown-body :deep(p) {
  margin: 0 0 10px;
}

.pixel-markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.pixel-markdown-body :deep(h1),
.pixel-markdown-body :deep(h2),
.pixel-markdown-body :deep(h3),
.pixel-markdown-body :deep(h4),
.pixel-markdown-body :deep(h5) {
  margin: 14px 0 8px;
  padding: 4px 8px;
  border-left: 5px solid var(--pc-grass, #6aa82f);
  background: var(--pc-thinking-bg, #eef7e0);
  color: var(--pc-ink, #11111b);
  font-weight: 900;
  line-height: 1.45;
}

.pixel-markdown-body :deep(h1) {
  font-size: 17px;
}

.pixel-markdown-body :deep(h2) {
  font-size: 15px;
}

.pixel-markdown-body :deep(h3),
.pixel-markdown-body :deep(h4),
.pixel-markdown-body :deep(h5) {
  font-size: 14px;
}

.pixel-markdown-body :deep(ul),
.pixel-markdown-body :deep(ol) {
  margin: 6px 0 10px;
  padding-left: 22px;
}

.pixel-markdown-body :deep(li) {
  margin: 4px 0;
}

.pixel-markdown-body :deep(li::marker) {
  color: var(--pc-grass, #6aa82f);
  font-weight: 900;
}

.pixel-markdown-body :deep(code) {
  padding: 1px 5px;
  border: 2px solid var(--pc-ink, #11111b);
  background: var(--pc-thinking-bg, #eef7e0);
  box-shadow: 2px 2px 0 0 var(--pc-ink, #11111b);
  color: #3f6212;
  font-family: var(--pc-font-mono, 'Courier New', Consolas, monospace);
  font-size: 12px;
}

.pixel-markdown-body :deep(pre) {
  margin: 10px 0;
  padding: 12px;
  overflow-x: auto;
  border: 3px solid var(--pc-ink, #11111b);
  background: #f8fafc;
  box-shadow: 4px 4px 0 0 var(--pc-ink, #11111b);
}

.pixel-markdown-body :deep(pre code),
.pixel-markdown-body :deep(pre code.hljs) {
  padding: 0;
  border: none;
  background: transparent;
  box-shadow: none;
  color: var(--pc-ink, #11111b);
  font-size: 12px;
  line-height: 1.65;
}

.pixel-markdown-body :deep(blockquote) {
  margin: 10px 0;
  padding: 8px 12px;
  border: 2px solid var(--pc-ink, #11111b);
  border-left: 6px solid var(--pc-yellow, #facc15);
  background: #fffbea;
  box-shadow: 3px 3px 0 0 var(--pc-ink, #11111b);
  color: #4b5563;
}

.pixel-markdown-body :deep(blockquote p) {
  margin: 0;
}

.pixel-markdown-body :deep(a) {
  color: #3f6212;
  font-weight: 700;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.pixel-markdown-body :deep(a:hover) {
  background: var(--pc-thinking-bg, #eef7e0);
}

.pixel-markdown-body :deep(table) {
  width: 100%;
  margin: 10px 0;
  border-collapse: collapse;
  font-size: 12px;
}

.pixel-markdown-body :deep(th),
.pixel-markdown-body :deep(td) {
  padding: 6px 8px;
  border: 2px solid var(--pc-ink, #11111b);
  text-align: left;
}

.pixel-markdown-body :deep(th) {
  background: var(--pc-thinking-bg, #eef7e0);
  font-weight: 900;
}

.pixel-markdown-body :deep(hr) {
  margin: 12px 0;
  border: none;
  border-top: 3px dashed var(--pc-ink, #11111b);
}

.pixel-markdown-body :deep(img) {
  max-width: 100%;
  border: 3px solid var(--pc-ink, #11111b);
  box-shadow: 4px 4px 0 0 var(--pc-ink, #11111b);
}

.pixel-stream-caret {
  display: inline-block;
  width: 8px;
  height: 15px;
  margin-left: 3px;
  vertical-align: -2px;
  background: var(--pc-grass, #6aa82f);
  animation: pixel-caret-blink 0.8s steps(2) infinite;
}

@keyframes pixel-caret-blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
