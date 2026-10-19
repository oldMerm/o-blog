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
  <article
    class="article-item"
    :class="{ 'is-pinned': article.isTop }"
    @click="goDetail"
  >
    <h3 class="title">
      <span v-if="article.isTop" class="top-badge">
        <svg class="pin-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 3.6 19 11h-4v9h-6v-9H5l7-7.4Z" />
        </svg>
        置顶
      </span>
      {{ article.articleName }}
    </h3>
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

/* --- 置顶卡片：淡蓝玻璃质感 --- */
.article-item.is-pinned {
  background:
    radial-gradient(
      120% 160% at 100% 0%,
      rgba(132, 196, 238, 0.28) 0%,
      rgba(255, 255, 255, 0) 55%
    ),
    linear-gradient(
      135deg,
      rgba(248, 252, 255, 0.96) 0%,
      rgba(231, 244, 253, 0.9) 100%
    );
  border-color: rgba(112, 175, 225, 0.45);
  box-shadow:
    0 4px 22px rgba(60, 130, 190, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.article-item.is-pinned:active {
  box-shadow:
    0 2px 10px rgba(60, 130, 190, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.article-item.is-pinned .title {
  color: rgb(36, 74, 110);
}

.article-item.is-pinned .group-tag {
  background: rgba(255, 255, 255, 0.78);
  color: rgb(50, 120, 185);
  box-shadow: 0 1px 4px rgba(75, 155, 210, 0.14);
}

.title {
  font-size: 1rem;
  font-weight: 600;
  color: rgb(40, 50, 65);
  margin-bottom: 8px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.top-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 0.66rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, rgb(96, 178, 232), rgb(48, 118, 184));
  padding: 2px 8px 2px 6px;
  border-radius: 7px;
  margin-right: 6px;
  vertical-align: 2px;
  letter-spacing: 0.06em;
  box-shadow: 0 2px 6px rgba(48, 118, 184, 0.32);
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
