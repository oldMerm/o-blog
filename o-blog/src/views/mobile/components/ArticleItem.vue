<script setup lang="ts">
import type { ArticlePageDetailVO } from './types'

defineProps<{
  article: ArticlePageDetailVO
}>()
</script>

<template>
  <article class="article-item">
    <h3 class="title">{{ article.articleName }}</h3>
    <p class="desc">{{ article.articleDecr }}</p>
    <span v-if="article.groupName" class="group-tag">{{ article.groupName }}</span>
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
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-item:active {
  transform: scale(0.98);
  box-shadow: 0 1px 4px rgba(60, 120, 180, 0.05);
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
