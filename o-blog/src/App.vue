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
    const current = router.currentRoute.value;
    if (current.name === 'not-found') {
        return;
    }

    // 目标本身已经是移动端页面（移动端首页 / 移动端文章）时保留原地址，
    // 否则转发出去的 /mobile/markdown/:id 链接会被重定向到移动端首页。
    if (current.path.startsWith('/mobile')) {
        return;
    }

    sessionStorage.setItem('redirected', 'true');

    // 手机打开桌面文章分享链接时，转成对应的移动端文章地址
    if (current.name === 'markdown' && current.params.id) {
        // 私有文章交由桌面文章页处理，避免移动端公开接口取不到内容
        if (current.query.isPublic === 'private') {
            return;
        }
        router.replace({
            name: 'mobile_markdown',
            params: { id: current.params.id },
            query: current.query,
            hash: current.hash,
        });
        return;
    }

    router.push('/mobile-home');
})

</script>

<template>
    <router-view />
</template>

<style scoped></style>
