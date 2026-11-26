<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AgentStreamView from '@/components/agent-chat/AgentStreamView.vue'
import { useAgentChatStream } from '@/components/agent-chat/useAgentChatStream'
import MobileSheet from './MobileSheet.vue'

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

const onVisibleChange = (value: boolean) => {
  if (!value) abort()
  emit('update:modelValue', value)
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
  close()
  router.push({ name: 'login', query: { from: 'mobile' } })
}

watch(
  () => props.modelValue,
  async (open) => {
    if (open) {
      reset()
      input.value = ''
      await nextTick()
      inputRef.value?.focus()
    } else {
      abort()
    }
  }
)
</script>

<template>
  <MobileSheet
    :model-value="modelValue"
    height="88dvh"
    :scrollable="false"
    @update:model-value="onVisibleChange"
  >
    <template #title>
      <span>网页助手</span>
      <span class="status-tag" :class="statusClass">{{ statusText }}</span>
    </template>

    <div class="assistant-body">
      <div class="stream-wrap">
        <AgentStreamView :turns="turns" />
      </div>

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
          <svg v-else class="send-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
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
  </MobileSheet>
</template>

<style scoped>
.status-tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: #f4f4f5;
  color: #909399;
  font-size: 0.68rem;
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

.assistant-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stream-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.stream-wrap :deep(.agent-stream) {
  padding: 12px;
  border-radius: 12px;
  font-size: 13px;
}

.stream-wrap :deep(.stream-placeholder) {
  min-height: 140px;
}

.stream-wrap :deep(.placeholder-topbar) {
  font-size: 20px;
}

.login-hint {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid #faecd8;
  border-radius: 8px;
  background: #fdf6ec;
  color: #e6a23c;
  font-size: 0.72rem;
  line-height: 1.5;
}

.login-hint button {
  flex-shrink: 0;
  padding: 0;
  border: none;
  background: transparent;
  color: #409eff;
  font-size: 0.72rem;
  font-weight: 600;
  cursor: pointer;
}

.input-section {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 12px;
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
  padding: 8px 6px;
  border: none;
  outline: none;
  background: transparent;
  color: #303133;
  font-size: 16px;
}

.chat-input::placeholder {
  color: #c0c4cc;
}

.action-btn {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 1.5px solid transparent;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.1s ease;
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
  border-radius: 50%;
  background: transparent;
}

.action-btn.abort-mode {
  border-color: #dcdfe6;
  border-radius: 10px;
  background: #fff;
  color: #409eff;
}

.action-btn.abort-mode:active {
  background: #ecf5ff;
}
</style>
