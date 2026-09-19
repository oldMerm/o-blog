<script setup lang="ts">
import type { Article } from '@/views/public/Article';
import ArticleCard from './ArticleCard.vue';

defineProps<{
    index: string;
    icon: string;
    title: string;
    variant: 'notice' | 'tech' | 'daily';
    articles: Article[];
}>();
</script>

<template>
    <div class="px-col">
        <div class="px-card px-col-head" :class="variant">
            <div class="px-col-head-left">
                <span class="px-col-index">{{ icon }} {{ index }}</span>
                <h3 class="px-col-name">{{ title }}</h3>
            </div>
            <span class="px-col-count">{{ articles.length }} POSTS</span>
        </div>

        <ArticleCard v-for="item in articles" :key="item.id" :article="item" :variant="variant" />

        <div v-if="articles.length === 0" class="px-card px-empty">暂无内容</div>
    </div>
</template>
