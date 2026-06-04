<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { httpInstance, type Response } from '@/utils/http'
import { articleType } from '@/views/public/Article'
import type { ArticlePageDetailVO, PageResult } from './types'
import ArticleItem from './ArticleItem.vue'
import FooterBar from './FooterBar.vue'

const tabList = [
  { label: '公告', type: articleType.Notice },
  { label: '技术分享', type: articleType.TECNO },
  { label: '日常生活', type: articleType.LIFE },
]

const currentTab = ref(articleType.Notice)
const currentPage = ref(1)
const pageResult = ref<PageResult<ArticlePageDetailVO> | null>(null)
const articles = ref<ArticlePageDetailVO[]>([])
const loading = ref(false)
const transitionKey = ref(0)
const slideDir = ref<'next' | 'prev'>('next')

const touchStartX = ref(0)
const touchStartY = ref(0)
const isSwiping = ref(false)

const totalPages = computed(() => pageResult.value?.pages || 1)
const hasNext = computed(() => currentPage.value < totalPages.value)
const hasPrev = computed(() => currentPage.value > 1)

const fetchArticles = async (page: number, type: number) => {
  loading.value = true
  try {
    const res = await httpInstance.get<any, Response>('/article/public/page', {
      params: { current: page, size: 8, type }
    })
    if (res.code === 200 && res.data) {
      pageResult.value = res.data
      articles.value = res.data.records || []
    } else {
      articles.value = []
    }
  } catch (e) {
    console.error('获取文章失败:', e)
    articles.value = []
  } finally {
    loading.value = false
  }
}

const switchTab = (type: number) => {
  if (type === currentTab.value) return
  currentTab.value = type
  currentPage.value = 1
  slideDir.value = 'next'
  transitionKey.value++
  fetchArticles(currentPage.value, type)
}

const goNextPage = () => {
  if (!hasNext.value) return
  currentPage.value++
  slideDir.value = 'next'
  transitionKey.value++
  fetchArticles(currentPage.value, currentTab.value)
}

const goPrevPage = () => {
  if (!hasPrev.value) return
  currentPage.value--
  slideDir.value = 'prev'
  transitionKey.value++
  fetchArticles(currentPage.value, currentTab.value)
}

const handleTouchStart = (e: TouchEvent) => {
  const touch = e.touches[0]
  if (!touch) return
  touchStartX.value = touch.clientX
  touchStartY.value = touch.clientY
  isSwiping.value = true
}

const handleTouchEnd = (e: TouchEvent) => {
  if (!isSwiping.value) return
  const touch = e.changedTouches[0]
  if (!touch) return
  isSwiping.value = false
  const dx = touch.clientX - touchStartX.value
  const dy = touch.clientY - touchStartY.value
  if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 50) {
    if (dx < 0) goNextPage()
    else goPrevPage()
  }
}

onMounted(() => {
  fetchArticles(1, articleType.Notice)
})
</script>

<template>
  <section class="article-section">
    <nav class="tabs-bar">
      <button
        v-for="tab in tabList"
        :key="tab.type"
        :class="['tab-btn', { active: currentTab === tab.type }]"
        @click="switchTab(tab.type)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <div
      class="swipe-area"
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
    >
      <Transition :name="slideDir === 'next' ? 'slide-next' : 'slide-prev'" mode="out-in">
        <div class="article-list" :key="`page-${transitionKey}`">
          <div v-if="loading" class="state-box">
            <div class="wave-loader">
              <span v-for="i in 3" :key="i" class="dot" :style="{ animationDelay: `${i * 0.15}s` }" />
            </div>
          </div>
          <template v-else-if="articles.length > 0">
            <ArticleItem
              v-for="(article, i) in articles"
              :key="article.id"
              :article="article"
              :style="{ animationDelay: `${i * 0.07}s` }"
              class="item-enter"
            />
          </template>
          <div v-else class="state-box empty">
            <span class="empty-icon">~</span>
            <span>暂无文章</span>
          </div>
        </div>
      </Transition>

      <div v-if="!loading && totalPages > 1" class="pagination">
        <button
          :class="['page-btn', { disabled: !hasPrev }]"
          :disabled="!hasPrev"
          @click="goPrevPage"
        >
          ‹
        </button>
        <div class="page-dots">
          <span
            v-for="p in totalPages"
            :key="p"
            :class="['dot', { active: p === currentPage }]"
          />
        </div>
        <button
          :class="['page-btn', { disabled: !hasNext }]"
          :disabled="!hasNext"
          @click="goNextPage"
        >
          ›
        </button>
      </div>

      <div v-if="!loading" class="footer-wrapper">
        <FooterBar />
      </div>
    </div>
  </section>
</template>

<style scoped>
.article-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.tabs-bar {
  display: flex;
  padding: 2px 16px 8px;
  gap: 2px;
}

.tab-btn {
  flex: 1;
  padding: 10px 0;
  border: none;
  background: transparent;
  font-size: 0.88rem;
  color: rgb(110, 140, 165);
  font-weight: 500;
  border-bottom: 2.5px solid transparent;
  transition: color 0.3s, border-color 0.3s;
  cursor: pointer;
  position: relative;
}

.tab-btn.active {
  color: rgb(50, 120, 185);
  border-bottom-color: rgb(50, 120, 185);
  font-weight: 600;
}

.tab-btn:active {
  opacity: 0.6;
}

.swipe-area {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
  overscroll-behavior: contain;
  display: flex;
  flex-direction: column;
  background: linear-gradient(
    180deg,
    rgb(225, 237, 248) 0%,
    rgb(215, 228, 242) 50%,
    rgb(210, 224, 240) 100%
  );
}

.footer-wrapper {
  margin-top: auto;
}

.swipe-area::-webkit-scrollbar {
  display: none;
}

.article-list {
  padding: 4px 0 0;
}

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 70px 0;
  gap: 12px;
  color: rgb(140, 170, 190);
  font-size: 0.88rem;
}

.state-box.empty .empty-icon {
  font-size: 2rem;
  opacity: 0.4;
}

.wave-loader {
  display: flex;
  gap: 7px;
}

.wave-loader .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgb(75, 155, 210);
  animation: waveBounce 1s ease-in-out infinite;
}

@keyframes waveBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-14px); }
}

.item-enter {
  animation: slideUpFade 0.5s ease-out both;
}

@keyframes slideUpFade {
  from {
    transform: translateY(22px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.slide-next-enter-active {
  animation: slideInRight 0.35s ease-out;
}

.slide-next-leave-active {
  animation: slideOutLeft 0.25s ease-in;
}

.slide-prev-enter-active {
  animation: slideInLeft 0.35s ease-out;
}

.slide-prev-leave-active {
  animation: slideOutRight 0.25s ease-in;
}

@keyframes slideInRight {
  from { transform: translateX(40px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@keyframes slideOutLeft {
  from { transform: translateX(0); opacity: 1; }
  to { transform: translateX(-40px); opacity: 0; }
}

@keyframes slideInLeft {
  from { transform: translateX(-40px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@keyframes slideOutRight {
  from { transform: translateX(0); opacity: 1; }
  to { transform: translateX(40px); opacity: 0; }
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 10px 0 16px;
}

.page-btn {
  font-size: 1.3rem;
  color: rgb(75, 155, 210);
  cursor: pointer;
  padding: 4px 8px;
  background: none;
  border: none;
  user-select: none;
  transition: opacity 0.2s;
  line-height: 1;
}

.page-btn:active:not(.disabled) {
  opacity: 0.5;
}

.page-btn.disabled {
  opacity: 0.2;
  cursor: default;
}

.page-dots {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgb(180, 200, 220);
  transition: all 0.35s ease;
}

.page-dots .dot.active {
  background: rgb(50, 120, 185);
  width: 20px;
  border-radius: 3px;
}
</style>
