<script setup lang="ts">
import { watch } from 'vue'

export interface Article {
  id: string
  articleName: string
  articleStatus: number
  like: number
  createdAt: string
}

interface Props {
  visible: boolean
  articles: Article[]
  modalTitle?: string
}

const props = withDefaults(defineProps<Props>(), {
  modalTitle: '文章列表'
})

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'delete', article: Article): void
}>()

const handleClose = () => {
  emit('update:visible', false)
}

const handleDelete = (article: Article) => {
  emit('delete', article)
}

// 时间格式化：兼容 ISO 字符串和时间戳
const formatTime = (time: string): string => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time

  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

watch(
  () => props.visible,
  (val) => {
    document.body.style.overflow = val ? 'hidden' : ''
  }
)

</script>

<template>
  
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-mask" @click.self="handleClose">
        <Transition name="pop">
          <div v-if="visible" class="modal-box" role="dialog" aria-modal="true">
            <!-- 头部 -->
            <div class="modal-header">
              <h3 class="modal-title">{{ modalTitle }}</h3>
              <span class="modal-count">共 {{ articles.length }} 篇</span>
            </div>

            <!-- 列表区 -->
            <div class="article-list">
              <template v-if="articles.length">
                <TransitionGroup name="list">
                  <div
                    v-for="item in articles"
                    :key="item.id"
                    class="article-item"
                  >
                    <div class="article-info">
                      <div class="article-name" :title="item.articleName">
                        {{ item.articleName }}
                      </div>
                      <div class="article-time">
                        <svg viewBox="0 0 24 24" width="14" height="14">
                          <circle
                            cx="12" cy="12" r="9"
                            stroke="currentColor" stroke-width="1.6" fill="none"
                          />
                          <path
                            d="M12 7v5l3 2"
                            stroke="currentColor" stroke-width="1.6"
                            stroke-linecap="round" fill="none"
                          />
                        </svg>
                        <span>创建于 {{ formatTime(item.createdAt) }}</span>
                      </div>
                    </div>

                    <button class="delete-btn" @click="handleDelete(item)">
                      <svg viewBox="0 0 24 24" width="14" height="14">
                        <path
                          d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14"
                          stroke="currentColor" stroke-width="1.6"
                          stroke-linecap="round" fill="none"
                        />
                      </svg>
                      删除
                    </button>
                  </div>
                </TransitionGroup>
              </template>

              <!-- 空状态 -->
              <div v-else class="empty-state">
                <svg viewBox="0 0 24 24" width="36" height="36">
                  <path
                    d="M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9l-6-6z M14 3v6h6 M9 13h6 M9 17h4"
                    stroke="currentColor" stroke-width="1.5"
                    stroke-linecap="round" stroke-linejoin="round" fill="none"
                  />
                </svg>
                <p>暂无文章</p>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ================= 遮罩层 ================= */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(234, 244, 252, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* ================= 弹窗主体 ================= */
.modal-box {
  position: relative;
  width: 480px;
  max-width: calc(100vw - 32px);
  max-height: calc(100vh - 240px);
  background: #ffffff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 12px 40px rgba(0, 85, 153, 0.1);
  border: 1px solid #e0ecff;
  display: flex;
  flex-direction: column;
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}
.close-btn:hover {
  background: #eff6ff;
  color: #2563eb;
  transform: rotate(90deg);
}

/* 头部 */
.modal-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eff6ff;
}
.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a4a75;
}
.modal-count {
  font-size: 12px;
  color: #64748b;
}

/* ================= 列表区 ================= */
.article-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 4px;
}
.article-list::-webkit-scrollbar {
  width: 6px;
}
.article-list::-webkit-scrollbar-thumb {
  background: #dbeafe;
  border-radius: 3px;
}
.article-list::-webkit-scrollbar-thumb:hover {
  background: #93c5fd;
}

/* ================= 文章项 ================= */
.article-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  border: 1px solid #dbeafe;
  border-radius: 10px;
  transition: all 0.25s ease;
}
.article-item:hover {
  border-color: #93c5fd;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12);
  transform: translateY(-1px);
}

.article-info {
  flex: 1;
  min-width: 0;
}
.article-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

/* 删除按钮：默认隐藏，hover 当前项时显示 */
.delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 12px;
  color: #ffffff;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  opacity: 0;
  transform: translateX(8px);
  transition: opacity 0.25s ease, transform 0.25s ease, background 0.2s ease;
  pointer-events: none;
  flex-shrink: 0;
}
.article-item:hover .delete-btn {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
}
.delete-btn:hover {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

/* ================= 空状态 ================= */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: #94a3b8;
}
.empty-state p {
  margin: 12px 0 0;
  font-size: 13px;
}

/* ================= 动画效果 ================= */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.pop-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.pop-leave-active {
  transition: all 0.25s ease-in;
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: scale(0.85) translateY(10px);
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
.list-leave-active {
  position: absolute;
  width: calc(100% - 8px);
}
</style>