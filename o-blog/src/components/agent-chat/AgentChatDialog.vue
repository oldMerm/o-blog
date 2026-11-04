<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AgentStreamView from './AgentStreamView.vue'
import { useAgentChatStream } from './useAgentChatStream.ts'

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

const statusText = computed(() => {
  switch (status.value) {
    case 'connecting':
      return '连接中'
    case 'streaming':
      return '生成中'
    case 'done':
      return '已完成'
    case 'aborted':
      return '已截断'
    case 'error':
      return '出错'
    default:
      return '待命'
  }
})

const statusClass = computed(() => `is-${status.value}`)
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
    <Transition name="agent-dialog">
      <div v-if="modelValue" class="agent-overlay" @click.self="close">
        <div class="agent-dialog" role="dialog" aria-modal="true" aria-label="AI 对话助手">
          <div class="dialog-header">
            <div class="dialog-title">
              <span>AI 对话助手</span>
              <span class="status-tag" :class="statusClass">{{ statusText }}</span>
            </div>
            <button class="close-btn" type="button" title="关闭" @click="close">×</button>
          </div>

          <AgentStreamView :turns="turns" class="stream-view" />

          <div v-if="needLogin" class="login-hint">
            <span>登录状态已失效，请重新登录后继续对话。</span>
            <button type="button" @click="goLogin">去登录</button>
          </div>

          <div class="input-section">
            <input
              ref="inputRef"
              v-model="input"
              class="chat-input"
              type="text"
              placeholder="输入你的问题，Enter 发送..."
              autocomplete="off"
              @keydown="handleKeydown"
            />
            <button
              class="action-btn"
              :class="{ 'abort-mode': streaming }"
              type="button"
              :title="streaming ? '截断生成' : '发送'"
              :aria-label="streaming ? '截断生成' : '发送'"
              @click="onAction"
            >
              <span v-if="streaming" class="abort-icon" aria-hidden="true">
                <span class="abort-dot" />
              </span>
              <svg
                v-else
                class="send-icon"
                viewBox="0 0 24 24"
                fill="none"
                aria-hidden="true"
              >
                <path
                  d="M12 19V5M5 12l7-7 7 7"
                  stroke="currentColor"
                  stroke-width="2.2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.agent-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(2px);
}

.agent-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: min(560px, 100%);
  height: min(640px, calc(100vh - 32px));
  padding: 16px;
  overflow: hidden;
  border: 1px solid #dadee5;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 16px 48px rgba(64, 158, 255, 0.16), 0 4px 16px rgba(0, 0, 0, 0.08);
}

.dialog-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-size: 15px;
  font-weight: 600;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: #f4f4f5;
  color: #909399;
  font-size: 12px;
  font-weight: 400;
  line-height: 18px;
  white-space: nowrap;
}

.status-tag.is-connecting,
.status-tag.is-streaming {
  background: #ecf5ff;
  color: #409eff;
}

.status-tag.is-done {
  background: #f0f9eb;
  color: #67c23a;
}

.status-tag.is-aborted {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-tag.is-error {
  background: #fef0f0;
  color: #f56c6c;
}

.close-btn {
  padding: 0 2px;
  border: none;
  background: none;
  color: #909399;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #409eff;
}

.stream-view {
  flex: 1;
  min-height: 0;
}

.login-hint {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid #faecd8;
  border-radius: 6px;
  background: #fdf6ec;
  color: #e6a23c;
  font-size: 12px;
  line-height: 1.5;
}

.login-hint button {
  flex-shrink: 0;
  padding: 0;
  border: none;
  background: transparent;
  color: #409eff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.login-hint button:hover {
  text-decoration: underline;
}

.input-section {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input-section:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12);
}

.chat-input {
  flex: 1;
  min-width: 0;
  padding: 6px;
  border: none;
  outline: none;
  background: transparent;
  color: #303133;
  font-size: 14px;
}

.chat-input::placeholder {
  color: #c0c4cc;
}

.action-btn {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1.5px solid transparent;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.1s ease;
}

.action-btn:hover {
  background: #66b1ff;
}

.action-btn:active {
  transform: scale(0.94);
}

.send-icon {
  width: 18px;
  height: 18px;
}

.abort-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

.abort-dot {
  width: 12px;
  height: 12px;
  border: 2px solid #409eff;
  background-color: #409eff;
  border-radius: 50%;
  background: transparent;
}

.action-btn.abort-mode {
  border-color: #dcdfe6;
  border-radius: 8px;
  background: #fff;
  color: #409eff;
}

.action-btn.abort-mode:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.agent-dialog-enter-active,
.agent-dialog-leave-active {
  transition: opacity 0.2s ease;
}

.agent-dialog-enter-active .agent-dialog,
.agent-dialog-leave-active .agent-dialog {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.agent-dialog-enter-from,
.agent-dialog-leave-to {
  opacity: 0;
}

.agent-dialog-enter-from .agent-dialog,
.agent-dialog-leave-to .agent-dialog {
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
}

@media (max-width: 640px) {
  .agent-overlay {
    padding: 12px;
  }

  .agent-dialog {
    gap: 10px;
    height: calc(100vh - 24px);
    padding: 12px;
    border-radius: 10px;
  }
}
</style>
