<script setup lang="ts">
import { computed } from 'vue';
import { goToArticle, type Article } from '@/views/public/Article';

const props = defineProps<{
    article: Article;
    variant: 'notice' | 'tech' | 'daily';
}>();

const date = computed(() => (props.article.createdAt || '').slice(0, 10));

const labelMap: Record<string, string> = {
    notice: 'ADMIN',
    tech: 'TECH',
    daily: 'DAILY'
};
const label = computed(() => labelMap[props.variant]);
</script>

<template>
    <article class="px-card px-article" :class="variant" @click="goToArticle(article.id)">
        <div>
            <div class="px-article-meta">
                <span>{{ date }}</span>
            </div>
            <h4 class="px-article-title">{{ article.articleName }}</h4>
            <p v-if="article.articleDecr" class="px-article-desc">{{ article.articleDecr }}</p>
        </div>
        <div class="px-article-foot">
            <span>{{ label }}</span>
            <span class="px-read">READ →</span>
        </div>
    </article>
</template>
