<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PixelAgentStreamView from './PixelAgentStreamView.vue'
import { useAgentChatStream } from '@/components/agent-chat/useAgentChatStream'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const router = useRouter()
const { turns, streaming, status, error, send, abort, reset } = useAgentChatStream()

const input = ref('')
const inputRef = ref<HTMLInputElement | null>(null)

let previousBodyOverflow = ''
let bodyLocked = false

const lockBody = () => {
  if (bodyLocked) return
  previousBodyOverflow = document.body.style.overflow
  document.body.style.overflow = 'hidden'
  bodyLocked = true
}

const unlockBody = () => {
  if (!bodyLocked) return
  document.body.style.overflow = previousBodyOverflow
  bodyLocked = false
}

const busy = computed(() => status.value === 'connecting' || status.value === 'streaming')

const statusText = computed(() => {
  switch (status.value) {
    case 'connecting':
      return 'PIXEL_AI_OS // CONNECTING...'
    case 'streaming':
      return 'PIXEL_AI_OS // STREAMING...'
    case 'done':
      return 'PIXEL_AI_OS // COMPLETE'
    case 'aborted':
      return 'PIXEL_AI_OS // ABORTED'
    case 'error':
      return 'PIXEL_AI_OS // ERROR'
    default:
      return 'PIXEL_AI_OS // READY'
  }
})

const dotClass = computed(() => ({
  busy: busy.value,
  error: status.value === 'error',
}))

const needLogin = computed(() => /登录|401|未授权|UNAUTHORIZED/i.test(error.value))

const close = () => {
  abort()
  emit('update:modelValue', false)
}

const onAction = () => {
  if (streaming.value) {
    abort()
    return
  }
  const question = input.value.trim()
  if (!question) return
  input.value = ''
  void send(question)
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (!streaming.value) onAction()
  }
}

const goLogin = () => {
  emit('update:modelValue', false)
  void router.push({ name: 'login' })
}

const onEsc = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && props.modelValue) close()
}

watch(
  () => props.modelValue,
  async (open) => {
    if (open) {
      reset()
      input.value = ''
      lockBody()
      await nextTick()
      inputRef.value?.focus()
    } else {
      unlockBody()
      abort()
    }
  },
  { immediate: true }
)

onMounted(() => {
  window.addEventListener('keydown', onEsc)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onEsc)
  unlockBody()
})
</script>

<template>
  <Teleport to="body">
    <Transition name="pixel-dialog">
      <div v-if="modelValue" class="pixel-chat-overlay" @click.self="close">
        <div
          class="pixel-chat-modal"
          role="dialog"
          aria-modal="true"
          aria-label="像素 AI 对话助手"
        >
          <!-- 顶部像素状态栏 -->
          <div class="pixel-chat-header">
            <div class="pixel-chat-title">
              <span class="pixel-chat-dot" :class="dotClass"></span>
              <span class="pixel-chat-status">{{ statusText }}</span>
            </div>
            <button class="pixel-chat-close" type="button" title="关闭" @click="close">X</button>
          </div>

          <!-- 中部：对话流 -->
          <PixelAgentStreamView :turns="turns" class="pixel-chat-stream" />

          <!-- 登录失效提示 -->
          <div v-if="needLogin" class="pixel-chat-login">
            <span>登录状态已失效，请重新登录后继续对话。</span>
            <button type="button" @click="goLogin">去登录</button>
          </div>

          <!-- 底部：输入栏 -->
          <div class="pixel-input-bar">
            <input
              ref="inputRef"
              v-model="input"
              class="pixel-input"
              type="text"
              placeholder="输入指令（Enter 发送，支持流式截断）..."
              autocomplete="off"
              @keydown="handleKeydown"
            />
            <button
              class="pixel-send-btn"
              :class="{ 'stop-state': streaming }"
              type="button"
              :title="streaming ? '截断生成' : '发送'"
              @click="onAction"
            >
              {{ streaming ? '截断 ■' : '发送 ▶' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.pixel-chat-overlay {
  --pc-ink: #11111b;
  --pc-panel: #fffdf7;
  --pc-grass: #6aa82f;
  --pc-yellow: #facc15;
  --pc-green: #22c55e;
  --pc-red: #f43f5e;
  --pc-thinking-bg: #eef7e0;
  --pc-font-mono: 'Courier New', Consolas, 'Liberation Mono', 'PingFang SC', 'Microsoft YaHei',
    monospace;
  --pc-font-pixel: 'Silkscreen', 'Press Start 2P', 'Courier New', monospace;

  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background-color: rgba(17, 17, 27, 0.7);
  backdrop-filter: blur(4px);
}

.pixel-chat-overlay *,
.pixel-chat-overlay *::before,
.pixel-chat-overlay *::after {
  box-sizing: border-box;
}

/* 弹框主体（带弹性缩放弹出效果） */
.pixel-chat-modal {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: min(560px, calc(100vw - 32px));
  height: min(640px, calc(100vh - 32px));
  padding: 18px;
  overflow: hidden;
  border: 4px solid var(--pc-ink);
  background-color: var(--pc-panel);
  box-shadow: 10px 10px 0 0 var(--pc-grass), 10px 10px 0 4px var(--pc-ink);
  font-family: var(--pc-font-mono);
}

/* 顶部像素状态栏 */
.pixel-chat-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  border: 3px solid var(--pc-ink);
  background-color: var(--pc-grass);
  box-shadow: 4px 4px 0 0 var(--pc-ink);
  color: var(--pc-ink);
}

.pixel-chat-title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.pixel-chat-dot {
  display: inline-block;
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border: 2px solid var(--pc-ink);
  background-color: var(--pc-green);
}

.pixel-chat-dot.busy {
  background-color: var(--pc-yellow);
  animation: pixel-chat-blink 0.6s infinite alternate;
}

.pixel-chat-dot.error {
  background-color: var(--pc-red);
}

.pixel-chat-status {
  overflow: hidden;
  font-family: var(--pc-font-pixel);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pixel-chat-close {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  padding: 0;
  border: 2px solid var(--pc-ink);
  background-color: var(--pc-red);
  color: #ffffff;
  font-family: var(--pc-font-mono);
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.1s ease, background-color 0.15s ease;
}

.pixel-chat-close:hover {
  background-color: #e11d48;
}

.pixel-chat-close:active {
  transform: translate(2px, 2px);
}

.pixel-chat-stream {
  flex: 1;
  min-height: 0;
}

/* 登录失效提示 */
.pixel-chat-login {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border: 2px solid var(--pc-ink);
  background: #fef9c3;
  box-shadow: 3px 3px 0 0 var(--pc-ink);
  color: #854d0e;
  font-size: 12px;
  line-height: 1.5;
}

.pixel-chat-login button {
  flex-shrink: 0;
  padding: 0;
  border: none;
  background: transparent;
  color: #3f6212;
  font-family: var(--pc-font-mono);
  font-size: 12px;
  font-weight: 900;
  text-decoration: underline;
  cursor: pointer;
}

/* 底部对话输入栏 */
.pixel-input-bar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
  padding: 6px;
  border: 3px solid var(--pc-ink);
  background-color: #ffffff;
  box-shadow: 4px 4px 0 0 var(--pc-ink);
}

.pixel-input {
  flex: 1;
  min-width: 0;
  padding: 6px 8px;
  border: none;
  outline: none;
  background: transparent;
  color: var(--pc-ink);
  font-family: var(--pc-font-mono);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
}

.pixel-input::placeholder {
  color: #9ca3af;
}

.pixel-send-btn {
  flex-shrink: 0;
  padding: 8px 14px;
  border: 2px solid var(--pc-ink);
  background-color: var(--pc-green);
  box-shadow: 2px 2px 0 0 var(--pc-ink);
  color: var(--pc-ink);
  font-family: var(--pc-font-pixel);
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.1s ease, background-color 0.15s ease;
  white-space: nowrap;
}

.pixel-send-btn:hover {
  filter: brightness(1.05);
}

.pixel-send-btn:active {
  transform: translate(2px, 2px);
  box-shadow: none;
}

.pixel-send-btn.stop-state {
  background-color: var(--pc-red);
  color: #ffffff;
}

@keyframes pixel-chat-blink {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

/* 弹性弹出 / 收起动画 */
.pixel-dialog-enter-active,
.pixel-dialog-leave-active {
  transition: opacity 0.25s ease;
}

.pixel-dialog-enter-active .pixel-chat-modal,
.pixel-dialog-leave-active .pixel-chat-modal {
  transition: transform 0.38s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.25s ease;
}

.pixel-dialog-enter-from,
.pixel-dialog-leave-to {
  opacity: 0;
}

.pixel-dialog-enter-from .pixel-chat-modal,
.pixel-dialog-leave-to .pixel-chat-modal {
  opacity: 0;
  transform: scale(0.75) translateY(30px);
}

@media (max-width: 640px) {
  .pixel-chat-overlay {
    padding: 12px;
  }

  .pixel-chat-modal {
    gap: 10px;
    width: calc(100vw - 24px);
    height: calc(100vh - 24px);
    padding: 12px;
    box-shadow: 6px 6px 0 0 var(--pc-grass), 6px 6px 0 3px var(--pc-ink);
  }

  .pixel-chat-status {
    font-size: 10px;
  }

  .pixel-send-btn {
    padding: 8px 10px;
    font-size: 11px;
  }
}
</style>
