<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { ArticlePageDetailVO } from './types'

const props = defineProps<{
  article: ArticlePageDetailVO
}>()

const router = useRouter()

const groupNames = computed(() => props.article.groupNameList ?? [])

// 多个集合时只展示第一个，并用省略号提示还有更多
const groupLabel = computed(() => {
  if (groupNames.value.length === 0) return ''
  if (groupNames.value.length === 1) return groupNames.value[0]
  return `${groupNames.value[0]}...`
})

const goDetail = () => {
  router.push({ name: 'mobile_markdown', params: { id: props.article.id } })
}
</script>

<template>
  <article class="article-item" @click="goDetail">
    <div class="title-row">
      <h3 class="title">{{ article.articleName }}</h3>
      <span v-if="article.isTop" class="top-badge">
        <svg class="pin-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 3.6 19 11h-4v9h-6v-9H5l7-7.4Z" />
        </svg>
        置顶
      </span>
    </div>
    <p class="desc">{{ article.articleDecr }}</p>
    <span v-if="groupLabel" class="group-tag" :title="groupNames.join('、')">{{ groupLabel }}</span>
  </article>
</template>

<style scoped>
.article-item {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(6px);
  border-radius: 14px;
  padding: 16px 18px 14px;
  margin: 0 16px 12px;
  box-shadow: 0 2px 16px rgba(60, 120, 180, 0.08);
  border: 1px solid rgba(180, 210, 240, 0.25);
  min-height: 110px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-item:active {
  transform: scale(0.98);
  box-shadow: 0 1px 4px rgba(60, 120, 180, 0.05);
}

.title-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.title {
  flex: 1;
  min-width: 0;
  font-size: 1rem;
  font-weight: 600;
  color: rgb(40, 50, 65);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.top-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 0.66rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, rgb(96, 178, 232), rgb(48, 118, 184));
  padding: 2px 8px 2px 6px;
  border-radius: 7px;
  margin-top: 2px;
  letter-spacing: 0.06em;
}

.pin-icon {
  width: 9px;
  height: 9px;
  fill: currentColor;
}

.desc {
  font-size: 0.82rem;
  color: rgb(115, 138, 158);
  line-height: 1.55;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
}

.group-tag {
  align-self: flex-end;
  font-size: 0.72rem;
  color: rgb(75, 155, 210);
  background: rgba(75, 155, 210, 0.1);
  padding: 3px 11px;
  border-radius: 20px;
  letter-spacing: 0.02em;
  font-weight: 500;
}
</style>
