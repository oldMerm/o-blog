<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(async () => {
    const isMobile = () => {
        return /Android|webOS|iPhone|iPod|BlackBerry|iPad/i.test(navigator.userAgent);
    }

    if (!isMobile() || sessionStorage.getItem('redirected')) {
        return;
    }

    // 等待首次路由解析完成，避免访问不存在的地址时被跳转到移动端首页
    await router.isReady();
    if (router.currentRoute.value.name === 'not-found') {
        return;
    }

    sessionStorage.setItem('redirected', 'true');
    router.push('/mobile-home');
})

</script>

<template>
    <router-view />
</template>

<style scoped></style>
