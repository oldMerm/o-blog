<script setup lang="ts">
import { onMounted, ref } from 'vue';
import topbar from './components/topbar.vue';
import ontice from './components/onotice.vue';
import mcontent from './components/mcontent.vue';
import userInfo from './components/userInfo.vue';
import ucontent from './components/ucontent.vue';
import utitle from './components/utitle.vue';
import utitle2 from './components/utitle2.vue';
import bcontent from './components/bcontent.vue';
import chatCard from './components/chatCard.vue';
import PixelHome from './style/index.vue';
import { usePinnedStore } from '@/stores/articleTop.ts';

const STORAGE_KEY = 'home_style';
const storedStyle = localStorage.getItem(STORAGE_KEY);
// 首次访问（无本地记录）默认使用像素风格
const isPixel = ref(storedStyle ? storedStyle === 'pixel' : true);

const setPixel = (value: boolean) => {
    isPixel.value = value;
    localStorage.setItem(STORAGE_KEY, value ? 'pixel' : 'normal');
};

// 初始化置顶状态
const store = usePinnedStore();

onMounted(() => {
    console.log("index mounted");
    store.load()
});
</script>

<template>
    <PixelHome v-if="isPixel" @switch-style="setPixel(false)" />

    <div v-else class="main">
        <topbar class="topbar" @toggle-style="setPixel(true)" />
        <div class="content">
            <div class="c">
                <ontice class="notice" />
                <utitle2 class="utitle2" />
                <chat-card class="chat" />
            </div>
            <mcontent class="m-article" />
            <div class="a">
                <user-info class="user-info" />
                <utitle class="utitle" />
                <ucontent class="ucontent" />
            </div>
        </div>
        <div class="b">
            <bcontent />
        </div>
    </div>
</template>

<style scoped>
.main {
    width: 100%;
    height: 100vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.topbar {
    flex: 0 0 auto;
}

/* 三栏内容区：占满顶部栏与 footer 之间的剩余高度 */
.content {
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    align-items: stretch;
    gap: 20px;
    padding: 20px 24px 16px;
    overflow: hidden;
}

.c {
    flex: 0 0 15%;
    min-width: 0;
    min-height: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.c>.notice {
    flex: 1 1 0;
    min-height: 0;
    height: auto;
    max-height: none;
}

.c>.utitle2 {
    flex: 0 0 3rem;
    margin-top: 0;
}

.c>.chat {
    flex: 1 1 0;
    min-height: 0;
    height: auto;
    margin-top: 0;
    line-height: normal;
    justify-content: center;
}

.content>.m-article {
    flex: 1 1 0;
    min-width: 0;
    min-height: 0;
    width: auto;
    height: auto;
    max-height: none;
    margin: 0;
    align-self: stretch;
}

.a {
    flex: 0 0 28%;
    min-width: 0;
    min-height: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.a>.user-info {
    flex: 0 0 auto;
    width: 100%;
    margin: 0;
}

.a>.utitle {
    flex: 0 0 3rem;
    width: auto;
    margin-left: 0;
}

.a>.ucontent {
    flex: 1 1 0;
    min-height: 0;
    height: auto;
    max-height: none;
    margin: 0;
}

/* footer：始终位于视口底部 */
.b {
    flex: 0 0 auto;
    width: 100%;
}
</style>