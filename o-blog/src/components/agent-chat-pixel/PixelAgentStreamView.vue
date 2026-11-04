<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import PixelStreamingMarkdown from './PixelStreamingMarkdown.vue'
import type { AgentTurn } from '@/components/agent-chat/types'

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
  <div ref="scrollRef" class="pixel-stream" @scroll.passive="onScroll">
    <div v-if="turns.length === 0" class="pixel-placeholder">
      <div class="pixel-placeholder-avatar" aria-hidden="true">
        <span class="pixel-placeholder-eye left"></span>
        <span class="pixel-placeholder-eye right"></span>
        <span class="pixel-placeholder-mouth"></span>
      </div>
      <div class="pixel-placeholder-title">PIXEL_AI_OS // STANDBY</div>
      <p class="pixel-placeholder-desc">输入问题，开始和博客智能体对话</p>
      <span class="pixel-placeholder-hint">支持多轮追问 · 回答由 AI 生成，仅供参考</span>
    </div>

    <div v-for="turn in turns" :key="turn.id" class="pixel-turn">
      <div class="pixel-question">
        <span class="pixel-question-label">PLAYER &gt;</span>
        <span class="pixel-question-text">{{ turn.question }}</span>
      </div>

      <div class="pixel-assistant">
        <div
          v-if="showToolSection(turn)"
          class="pixel-tool-card"
          :class="{ open: turn.expanded }"
        >
          <div class="pixel-tool-bar" @click="toggle(turn)">
            <span class="pixel-tool-name">{{ toolLabel(turn) }}</span>
            <span class="pixel-arrow" aria-hidden="true">▼</span>
          </div>
          <div class="pixel-thinking-drawer">
            <div class="pixel-thinking-inner">
              <div v-if="turn.tools.length > 0" class="pixel-tool-tags">
                <span v-for="tool in turn.tools" :key="tool" class="pixel-tool-tag">{{ tool }}</span>
              </div>
              <div v-if="turn.reasoning" class="pixel-reasoning">{{ turn.reasoning }}</div>
              <div
                v-else-if="turn.status === 'pending' || turn.status === 'streaming'"
                class="pixel-thinking-wait"
              >
                正在理解你的问题…
              </div>
              <div v-else class="pixel-thinking-wait">本轮没有输出思考内容</div>
            </div>
          </div>
        </div>

        <div v-if="turn.content" class="pixel-answer">
          <PixelStreamingMarkdown
            :content="turn.content"
            :streaming="turn.status === 'streaming'"
            @tick="handleStreamTick"
          />
        </div>
        <div
          v-else-if="turn.status === 'pending' || turn.status === 'streaming'"
          class="pixel-answer-wait"
        >
          <span class="pixel-wait-dot" />
          <span class="pixel-wait-dot" />
          <span class="pixel-wait-dot" />
        </div>
        <div v-else-if="turn.status === 'done'" class="pixel-answer-empty">本轮没有返回内容</div>

        <div v-if="turn.error" class="pixel-answer-error">⚠ {{ turn.error }}</div>
        <div v-if="turn.status === 'aborted'" class="pixel-answer-aborted">
          ⚠ [SYSTEM] 已手动截断本轮生成
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pixel-stream {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px;
  border: 3px solid var(--pc-ink, #11111b);
  background: #ffffff;
  box-shadow: inset 3px 3px 0 rgba(0, 0, 0, 0.06);
  color: var(--pc-ink, #11111b);
  font-family: var(--pc-font-mono, 'Courier New', Consolas, 'Liberation Mono', 'PingFang SC', 'Microsoft YaHei', monospace);
  font-size: 13px;
  line-height: 1.7;
}

.pixel-stream::-webkit-scrollbar,
.pixel-thinking-inner::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.pixel-stream::-webkit-scrollbar-thumb,
.pixel-thinking-inner::-webkit-scrollbar-thumb {
  background: var(--pc-grass, #6aa82f);
}

.pixel-stream::-webkit-scrollbar-track,
.pixel-thinking-inner::-webkit-scrollbar-track {
  background: #e8f5d8;
}

/* ---------- 空状态 ---------- */
.pixel-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
  padding: 16px;
  text-align: center;
}

.pixel-placeholder-avatar {
  position: relative;
  width: 56px;
  height: 44px;
  margin-bottom: 16px;
  border: 3px solid var(--pc-ink, #11111b);
  background: var(--pc-grass, #6aa82f);
  box-shadow: 4px 4px 0 0 var(--pc-ink, #11111b);
}

.pixel-placeholder-avatar::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 50%;
  width: 4px;
  height: 7px;
  background: var(--pc-ink, #11111b);
  transform: translateX(-50%);
}

.pixel-placeholder-eye {
  position: absolute;
  top: 12px;
  width: 10px;
  height: 8px;
  background: var(--pc-green, #22c55e);
  border: 2px solid var(--pc-ink, #11111b);
}

.pixel-placeholder-eye.left {
  left: 10px;
}

.pixel-placeholder-eye.right {
  right: 10px;
}

.pixel-placeholder-mouth {
  position: absolute;
  bottom: 8px;
  left: 50%;
  width: 18px;
  height: 4px;
  background: var(--pc-ink, #11111b);
  transform: translateX(-50%);
}

.pixel-placeholder-title {
  margin-bottom: 8px;
  padding: 4px 10px;
  border: 2px solid var(--pc-ink, #11111b);
  background: var(--pc-yellow, #facc15);
  box-shadow: 2px 2px 0 0 var(--pc-ink, #11111b);
  color: var(--pc-ink, #11111b);
  font-family: var(--pc-font-pixel, 'Silkscreen', 'Courier New', monospace);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.pixel-placeholder-desc {
  margin: 0 0 6px;
  color: var(--pc-ink, #11111b);
  font-size: 13px;
  font-weight: 700;
}

.pixel-placeholder-hint {
  color: #94a3b8;
  font-size: 12px;
}

/* ---------- 轮次 ---------- */
.pixel-turn + .pixel-turn {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 3px dashed #d4d4d8;
}

.pixel-question {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
  padding: 6px 10px;
  border: 2px solid var(--pc-ink, #11111b);
  background: var(--pc-yellow, #facc15);
  box-shadow: 3px 3px 0 0 var(--pc-ink, #11111b);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.6;
  word-break: break-word;
}

.pixel-question-label {
  flex-shrink: 0;
  margin-top: 1px;
  padding: 0 5px;
  background: var(--pc-ink, #11111b);
  color: #ffffff;
  font-family: var(--pc-font-pixel, 'Silkscreen', 'Courier New', monospace);
  font-size: 9px;
  line-height: 17px;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.pixel-question-text {
  min-width: 0;
  white-space: pre-wrap;
}

/* ---------- 工具调用 / 思考 ---------- */
.pixel-tool-card {
  margin-bottom: 10px;
  overflow: hidden;
  border: 3px solid var(--pc-ink, #11111b);
  background: var(--pc-thinking-bg, #eef7e0);
  box-shadow: 4px 4px 0 0 var(--pc-ink, #11111b);
}

.pixel-tool-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  color: var(--pc-ink, #11111b);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  user-select: none;
}

.pixel-tool-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pixel-arrow {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: 2px solid var(--pc-ink, #11111b);
  background: var(--pc-yellow, #facc15);
  color: var(--pc-ink, #11111b);
  font-size: 8px;
  transition: transform 0.2s steps(4);
}

.pixel-tool-card.open .pixel-arrow {
  transform: rotate(180deg);
}

.pixel-thinking-drawer {
  max-height: 0;
  overflow: hidden;
  background: #ffffff;
  transition: max-height 0.3s ease-out;
}

.pixel-tool-card.open .pixel-thinking-drawer {
  max-height: 180px;
  border-top: 3px dashed var(--pc-ink, #11111b);
}

.pixel-thinking-inner {
  max-height: 174px;
  padding: 10px 12px;
  overflow-y: auto;
  color: #4b5563;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.pixel-tool-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.pixel-tool-tag {
  padding: 1px 7px;
  border: 2px solid var(--pc-ink, #11111b);
  background: #e8f5d8;
  box-shadow: 2px 2px 0 0 var(--pc-ink, #11111b);
  color: #3f6212;
  font-family: var(--pc-font-pixel, 'Silkscreen', 'Courier New', monospace);
  font-size: 9px;
  font-weight: 700;
}

.pixel-reasoning {
  white-space: pre-wrap;
}

.pixel-thinking-wait {
  color: #94a3b8;
}

/* ---------- 回答 ---------- */
.pixel-answer {
  padding: 2px 0;
}

.pixel-answer-wait {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 0;
}

.pixel-wait-dot {
  width: 8px;
  height: 8px;
  border: 2px solid var(--pc-ink, #11111b);
  background: var(--pc-grass, #6aa82f);
  animation: pixel-wait 1.2s steps(2) infinite;
}

.pixel-wait-dot:nth-child(2) {
  animation-delay: 0.15s;
}

.pixel-wait-dot:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes pixel-wait {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.35;
  }
  50% {
    transform: translateY(-3px);
    opacity: 1;
  }
}

.pixel-answer-empty {
  color: #94a3b8;
  font-size: 12px;
}

.pixel-answer-error {
  margin-top: 10px;
  padding: 8px 10px;
  border: 2px solid var(--pc-ink, #11111b);
  background: #ffe4e6;
  box-shadow: 3px 3px 0 0 var(--pc-ink, #11111b);
  color: #be123c;
  font-size: 12px;
  line-height: 1.6;
}

.pixel-answer-aborted {
  margin-top: 10px;
  padding: 6px 10px;
  border: 2px dashed var(--pc-ink, #11111b);
  background: #fef9c3;
  color: #a16207;
  font-size: 12px;
}
</style>
