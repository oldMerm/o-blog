<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import UpdateModel from '../utils/UpdateModel.vue';

interface Version {
    id: number;
    objectName: string;
    versionContent: string;
    versionId: string;
    createdAt: string;
}

const versionData = ref<Version>();
const isModalVisible = ref(false); // 单独控制弹窗显隐

onMounted(async () => {
    try {
        const res = await httpInstance.get<any, Response>("/version");

        if (res.code !== 200) {
            console.error(`服务错误:${res.message}`);
            alert(`服务错误:${res.message}`);
            return;
        }

        versionData.value = res.data;

        if (versionData.value?.versionId) {
            const currentVersionId = versionData.value.versionId;
            const localVersion = localStorage.getItem("version_news");

            if (localVersion !== currentVersionId) {
                isModalVisible.value = true;
                localStorage.setItem("version_news", currentVersionId);
            }
        }

    } catch (error) {
        console.error("版本获取失败:", error);
        alert(`系统错误:${error}`);
    }
})
</script>

<template>
    <UpdateModel v-model="isModalVisible" :title="versionData?.versionId" :content="versionData?.versionContent" />

    <div class="bc">
        该网页不获取任何个人信息，请注意甄别！
        <br>
        <a href="http://beian.miit.gov.cn" class="beian" target="_blank" rel="noopener noreferrer">
            粤ICP备2026023638号-1
        </a>
        <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44196202000120" class="beian">
            <img src="https://beian.mps.gov.cn/img/logo01.dd7ff50e.png">粤公网安备44196202000120号</a>
        ©版本号: {{ versionData?.versionId || '获取中...' }}
    </div>
</template>

<style scoped>
.bc {
    width: 100%;
    font-size: 0.8rem;
    background: linear-gradient(0deg, #e9e9e9, #ffffff);
    text-align: center;
}

.beian {
    color: black;
    transition: all 0.2s;
    text-decoration: none;
}

.beian:hover {
    color: rgb(8, 0, 255);
}

img {
    width: 16px;
    height: 17px;
    vertical-align: middle;
}
</style>
