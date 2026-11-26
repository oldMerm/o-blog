<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { httpInstance, type Response } from '@/utils/http'
import Dialog from '@/utils/dia/Dialog.vue'
import MobileSheet from './MobileSheet.vue'

interface ArticleHistory {
  userId: string
  articleId: string
  articleName: string
  updatedAt: string
}

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const router = useRouter()

const loading = ref(false)
const logged = ref(false)
const articleHistoryList = ref<ArticleHistory[]>([])
const showClearDialog = ref(false)

// 弹窗只展示最近 5 条，避免内容过多撑高弹层
const visibleHistory = computed(() => articleHistoryList.value.slice(0, 5))

const hasToken = () => Boolean(localStorage.getItem('token'))

const loadHistory = async () => {
  if (!hasToken()) {
    logged.value = false
    articleHistoryList.value = []
    loading.value = false
    return
  }

  loading.value = true
  try {
    const res = await httpInstance.get<any, Response>('/article/history')
    if (res.code !== 200) {
      logged.value = res.code !== 401 && hasToken()
      articleHistoryList.value = []
      return
    }
    logged.value = true
    articleHistoryList.value = Array.isArray(res.data) ? res.data : []
  } catch {
    articleHistoryList.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) void loadHistory()
  }
)

const close = () => emit('update:modelValue', false)

const goLogin = () => {
  close()
  router.push({ name: 'login', query: { from: 'mobile' } })
}

const goDetail = (item: ArticleHistory) => {
  close()
  router.push({ name: 'mobile_markdown', params: { id: item.articleId } })
}

const clearHistory = async () => {
  const res = await httpInstance.delete<any, Response>('/article/history')
  if (res.code === 200) {
    articleHistoryList.value = []
  }
}

const formatTime = (value?: string) => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <MobileSheet
    :model-value="modelValue"
    title="浏览历史"
    centered
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="loading" class="state-box">
      <span class="loading-dot" />
      <span>正在加载浏览历史...</span>
    </div>

    <div v-else-if="!logged" class="login-state">
      <div class="history-icon">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path
            d="M13 3a9 9 0 1 0 8.94 10.06 1 1 0 0 0-1.99-.2A7 7 0 1 1 13 5v2.5a.5.5 0 0 0 .82.38l3.68-3a.5.5 0 0 0 0-.76l-3.68-3A.5.5 0 0 0 13 1.5V3Z"
          />
          <path d="M12 8a1 1 0 0 1 1 1v3.38l2.3 1.53a1 1 0 1 1-1.1 1.67l-2.7-1.8A1 1 0 0 1 11 13V9a1 1 0 0 1 1-1Z" />
        </svg>
      </div>
      <p class="login-title">登录后查看浏览历史</p>
      <p class="login-desc">你浏览过的文章会保存在这里，方便随时找回</p>
      <button class="primary-btn" type="button" @click="goLogin">去登录</button>
    </div>

    <template v-else>
      <div v-if="articleHistoryList.length === 0" class="empty-state">
        <span class="empty-icon">~</span>
        <span>去浏览几篇文章吧！</span>
      </div>

      <template v-else>
        <div class="history-toolbar">
          <span>共 {{ articleHistoryList.length }} 篇</span>
          <button class="clear-btn" type="button" @click="showClearDialog = true">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M9 3a1 1 0 0 0-1 1v1H4a1 1 0 0 0 0 2h1v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7h1a1 1 0 1 0 0-2h-4V4a1 1 0 0 0-1-1H9Zm1 2h4v1h-4V5Zm-3 2h10v12H7V7Zm3 3a1 1 0 0 1 1 1v5a1 1 0 1 1-2 0v-5a1 1 0 0 1 1-1Zm4 0a1 1 0 0 1 1 1v5a1 1 0 1 1-2 0v-5a1 1 0 0 1 1-1Z"
              />
            </svg>
            清空
          </button>
        </div>

        <div class="history-list">
          <button
            v-for="item in visibleHistory"
            :key="`${item.articleId}-${item.updatedAt}`"
            class="history-item"
            type="button"
            @click="goDetail(item)"
          >
            <span class="item-title">{{ item.articleName }}</span>
            <span class="item-time">浏览时间：{{ formatTime(item.updatedAt) }}</span>
          </button>
        </div>
      </template>
    </template>

    <Dialog
      v-model="showClearDialog"
      title="清空浏览历史"
      content="确定要清空所有浏览历史吗？"
      width="min(88vw, 340px)"
      @confirm="clearHistory"
    />
  </MobileSheet>
</template>

<style scoped>
.state-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 0;
  color: #8a9bab;
  font-size: 0.86rem;
}

.loading-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgb(75, 155, 210);
  animation: history-pulse 1s ease-in-out infinite;
}

@keyframes history-pulse {
  0%,
  100% {
    transform: scale(0.7);
    opacity: 0.4;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

.login-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 8px 10px;
  text-align: center;
}

.history-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: rgba(75, 155, 210, 0.12);
  color: rgb(75, 155, 210);
}

.history-icon svg {
  width: 40px;
  height: 40px;
  fill: currentColor;
}

.login-title {
  margin: 14px 0 6px;
  color: #2b3a4a;
  font-size: 1.05rem;
  font-weight: 600;
}

.login-desc {
  margin: 0 0 20px;
  max-width: 250px;
  color: #8a9bab;
  font-size: 0.82rem;
  line-height: 1.6;
}

.primary-btn {
  width: 100%;
  max-width: 260px;
  padding: 12px 0;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(96, 178, 232), rgb(48, 118, 184));
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
}

.primary-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 54px 0;
  color: #8a9bab;
  font-size: 0.86rem;
}

.empty-icon {
  font-size: 2rem;
  opacity: 0.4;
}

.history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 0 2px;
  color: #8a9bab;
  font-size: 0.74rem;
}

.clear-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border: 1px solid rgba(245, 108, 108, 0.25);
  border-radius: 999px;
  background: rgba(245, 108, 108, 0.06);
  color: #e46a6a;
  font-size: 0.72rem;
  cursor: pointer;
}

.clear-btn:active {
  background: rgba(245, 108, 108, 0.14);
}

.clear-btn svg {
  width: 13px;
  height: 13px;
  fill: currentColor;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(180, 210, 240, 0.35);
  border-radius: 12px;
  background: rgba(244, 249, 255, 0.8);
  text-align: left;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.history-item:active {
  transform: scale(0.99);
  background: rgba(225, 238, 250, 0.9);
}

.item-title {
  display: block;
  overflow: hidden;
  color: #2b3a4a;
  font-size: 0.88rem;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  color: #8a9bab;
  font-size: 0.7rem;
}
</style>
