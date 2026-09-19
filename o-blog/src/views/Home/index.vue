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
        <topbar @toggle-style="setPixel(true)" />
        <div class="c">
            <ontice />
            <utitle2 />
            <chat-card />
        </div>
        <mcontent />
        <div class="a">
            <user-info />
            <utitle />
            <ucontent />
        </div>
        <div class="b">
            <bcontent />
        </div>
    </div>
</template>

<style scoped>
.main {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
}

.c {
    width: 15%;
    margin: 10px 10px;
}

.a {
    width: 28%;
}

.b {
    position: absolute;
    width: 100%;
    bottom: 0;
}
</style>