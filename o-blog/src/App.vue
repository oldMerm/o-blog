<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
    const isMobile = () => {
        return /Android|webOS|iPhone|iPod|BlackBerry|iPad/i.test(navigator.userAgent);
    }

    if (isMobile() && !sessionStorage.getItem('redirected')) {
        sessionStorage.setItem('redirected', 'true');
        router.push('/mobile-home');
    }
})

</script>

<template>
    <router-view />
</template>

<style scoped></style>
