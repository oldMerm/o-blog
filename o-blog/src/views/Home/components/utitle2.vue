<script setup lang="ts">
import { ref } from 'vue';

const map = new Map([
    [0, "一"],
    [1, "七"],
    [2, "六"],
    [3, "五"],
    [4, "四"],
    [5, "三"],
    [6, "二"]
])

let time = ref<string>();
const getDay = () => {
    const day = new Date().getDay();
    return map.get(day);
}
time.value = getDay();

setInterval(getDay, 600000);
</script>

<template>
    <div class="ut">
        文章刷新倒计时：{{ time }}天
    </div>
</template>

<style scoped>
.ut {
    width: 100%;
    height: 3rem;
    line-height: 3rem;
    border: 1px solid #dadee5;
    border-radius: 12px;
    padding-left: 10px;
    font-size: 1rem;
    font-weight: 300;
    margin-top: 10px;
}
</style>