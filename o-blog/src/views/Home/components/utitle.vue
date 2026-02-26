<script setup lang="ts">
import { httpInstance, type Response } from '@/utils/http';
import { goToArticle, type Article } from '@/views/public/Article';
import { ref } from 'vue';

const newMess = ref<Article>();

const getNewMess = async () => {
    try {
        const res = await httpInstance.get<any, Response>('/counter/newArt');
        if (res.code !== 200) {
            alert(`系统出现错误:${res.message}`);
            return;
        }
        newMess.value = res.data;
    } catch (error) {
        alert(error);
    }
}

getNewMess();
setInterval(
    getNewMess, 10800 * 1000
)


</script>

<template>
    <div class="ut" v-if="newMess">
        oldmerman<strong>更新推送:</strong>
        <span class="newMess" @click="goToArticle(newMess.id)">{{ newMess.articleName.slice(0, 8) }}</span>
        <span class="time">于{{ newMess.createdAt.slice(0, 11) }}</span>
    </div>
</template>

<style scoped>
.ut {
    width: 100%;
    height: 3rem;
    line-height: 3rem;
    margin-left: 10px;
    border: 1px solid #dadee5;
    border-radius: 12px;
    padding-left: 10px;
    font-size: 1rem;
    font-weight: 300;
    cursor: default;
}

.newMess {
    margin: 0 5px;
    font-style: italic;
    text-decoration: underline;
    cursor: pointer;
    color: skyblue;
}

.time {
    font-size: smaller;
}
</style>