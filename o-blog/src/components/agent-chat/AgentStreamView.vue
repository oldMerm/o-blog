<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import StreamingMarkdown from './StreamingMarkdown.vue'
import type { AgentTurn } from './types.ts'

const props = defineProps<{
  turns: AgentTurn[]
}>()

const scrollRef = ref<HTMLElement | null>(null)
const nearBottom = ref(true)
let scrollFrame: number | null = null

const onScroll = () => {
  const el = scrollRef.value
  if (!el) return
  nearBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 48
}

const scrollToBottom = async () => {
  await nextTick()
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const scheduleScroll = () => {
  if (scrollFrame !== null) return
  scrollFrame = requestAnimationFrame(() => {
    scrollFrame = null
    const el = scrollRef.value
    if (el && nearBottom.value) el.scrollTop = el.scrollHeight
  })
}

const handleStreamTick = () => {
  if (nearBottom.value) scheduleScroll()
}

const streamSignature = computed(() =>
  props.turns
    .map(
      (turn) =>
        `${turn.status}:${turn.content.length}:${turn.reasoning.length}:${turn.tools.length}:${turn.error.length}`
    )
    .join('|')
)

watch(streamSignature, () => {
  if (nearBottom.value) scheduleScroll()
})

onMounted(() => {
  void scrollToBottom()
})

onUnmounted(() => {
  if (scrollFrame !== null) {
    cancelAnimationFrame(scrollFrame)
    scrollFrame = null
  }
})

const toggle = (turn: AgentTurn) => {
  turn.expanded = !turn.expanded
}

const toolLabel = (turn: AgentTurn) => {
  if (turn.tools.length > 0) {
    const prefix = turn.status === 'streaming' ? '正在调用工具' : '已调用工具'
    return `${prefix}：${turn.tools.join('、')}`
  }
  if (turn.reasoning) {
    return turn.status === 'streaming' ? '思考中…' : '思考过程'
  }
  if (turn.status === 'pending' || turn.status === 'streaming') return '正在连接智能体…'
  return '思考过程'
}

const showToolSection = (turn: AgentTurn) =>
  Boolean(
    turn.reasoning ||
      turn.tools.length > 0 ||
      turn.status === 'pending' ||
      turn.status === 'streaming'
  )
</script>

<template>
  <div ref="scrollRef" class="agent-stream" @scroll.passive="onScroll">
    <div v-if="turns.length === 0" class="stream-placeholder">
      <div class="placeholder-topbar">欢迎使用老鱼人博客助手</div>
      <p class="placeholder-title">输入问题，开始和博客智能体对话</p>
      <span class="placeholder-desc">支持多轮追问，回答内容由 AI 生成，仅供参考</span>
    </div>

    <div v-for="turn in turns" :key="turn.id" class="turn">
      <div class="user-prompt">
        <span class="prompt-label">你</span>
        <span class="prompt-text">{{ turn.question }}</span>
      </div>

      <div class="assistant-block">
        <div
          v-if="showToolSection(turn)"
          class="tool-section"
          :class="{ expanded: turn.expanded }"
        >
          <div class="tool-header" @click="toggle(turn)">
            <span class="tool-name">{{ toolLabel(turn) }}</span>
            <span class="dropdown-arrow">▼</span>
          </div>
          <div v-show="turn.expanded" class="thinking-body">
            <div v-if="turn.tools.length > 0" class="tool-tags">
              <span v-for="tool in turn.tools" :key="tool" class="tool-tag">{{ tool }}</span>
            </div>
            <div v-if="turn.reasoning" class="reasoning-text">{{ turn.reasoning }}</div>
            <div
              v-else-if="turn.status === 'pending' || turn.status === 'streaming'"
              class="thinking-wait"
            >
              正在理解你的问题…
            </div>
            <div v-else class="thinking-wait">本轮没有输出思考内容</div>
          </div>
        </div>

        <div v-if="turn.content" class="answer-content">
          <StreamingMarkdown
            :content="turn.content"
            :streaming="turn.status === 'streaming'"
            @tick="handleStreamTick"
          />
        </div>
        <div
          v-else-if="turn.status === 'pending' || turn.status === 'streaming'"
          class="answer-wait"
        >
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
        </div>
        <div v-else-if="turn.status === 'done'" class="answer-empty">本轮没有返回内容</div>

        <div v-if="turn.error" class="answer-error">{{ turn.error }}</div>
        <div v-if="turn.status === 'aborted'" class="answer-aborted">已手动截断本轮生成</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.agent-stream {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  color: #303133;
  font-size: 14px;
  line-height: 1.7;
}

.agent-stream::-webkit-scrollbar {
  width: 4px;
}

.agent-stream::-webkit-scrollbar-thumb {
  border-radius: 4px;
  background: #dcdfe6;
}

.agent-stream::-webkit-scrollbar-track {
  background: transparent;
}

.stream-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 180px;
  text-align: center;
  color: #909399;
}

.placeholder-topbar {
  font-size: 24px;
  color: rgb(64, 158, 255);
}

.placeholder-title {
  margin: 0 0 6px;
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.placeholder-desc {
  font-size: 12px;
  color: #a8abb2;
}

.turn + .turn {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed #ebeef5;
}

.user-prompt {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  border-left: 3px solid #409eff;
  border-radius: 0 6px 6px 0;
  background: #ecf5ff;
  color: #303133;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.prompt-label {
  flex-shrink: 0;
  margin-top: 1px;
  padding: 0 5px;
  border-radius: 4px;
  background: #409eff;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
}

.prompt-text {
  min-width: 0;
  white-space: pre-wrap;
}

.assistant-block {
  margin-top: 10px;
}

.tool-section {
  margin-bottom: 10px;
  overflow: hidden;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  background: #f5f9ff;
}

.tool-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  color: #303133;
  font-size: 13px;
  cursor: pointer;
  user-select: none;
}

.tool-name {
  overflow: hidden;
  color: #409eff;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  flex-shrink: 0;
  color: #409eff;
  font-size: 11px;
  transition: transform 0.2s ease;
}

.tool-section.expanded .dropdown-arrow {
  transform: rotate(180deg);
}

.thinking-body {
  max-height: 170px;
  padding: 10px 12px;
  overflow-y: auto;
  border-top: 1px solid #d9ecff;
  background: #fbfdff;
  color: #606266;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.thinking-body::-webkit-scrollbar {
  width: 3px;
}

.thinking-body::-webkit-scrollbar-thumb {
  border-radius: 3px;
  background: #dcdfe6;
}

.tool-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.tool-tag {
  padding: 1px 8px;
  border-radius: 999px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 11px;
}

.reasoning-text {
  white-space: pre-wrap;
}

.thinking-wait {
  color: #a8abb2;
}

.answer-content {
  color: #303133;
}

.answer-wait {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  animation: answer-dot 1.2s ease-in-out infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.15s;
}

.dot:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes answer-dot {
  0%,
  100% {
    transform: scale(0.6);
    opacity: 0.3;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

.answer-empty {
  color: #a8abb2;
  font-size: 13px;
}

.answer-error {
  margin-top: 8px;
  padding: 8px 12px;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 13px;
  line-height: 1.6;
}

.answer-aborted {
  margin-top: 8px;
  color: #e6a23c;
  font-size: 12px;
}
</style>
