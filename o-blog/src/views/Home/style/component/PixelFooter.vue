<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import PixelUpdateModel from '@/views/Home/utils/PixelUpdateModel.vue';

interface Version {
    id: number;
    objectName: string;
    versionContent: string;
    versionId: string;
    createdAt: string;
}

const props = defineProps<{
    versionId?: string;
}>();

const emit = defineEmits<{
    (e: 'toast', msg: string, icon?: string): void;
}>();

/* ---- 版本弹框：与 bcontent 保持一致的显隐逻辑 ---- */
const VERSION_NEWS_KEY = 'version_news';

const versionData = ref<Version | null>(null);
const isModalVisible = ref(false);

// 优先使用接口返回的版本号，其次回退到父组件传入的版本号
const displayVersion = computed(() => versionData.value?.versionId || props.versionId || '');

onMounted(async () => {
    try {
        const res = await httpInstance.get<any, Response>('/version');

        if (res.code !== 200) {
            return;
        }

        versionData.value = res.data;

        const currentVersionId = versionData.value?.versionId;
        if (!currentVersionId) {
            return;
        }

        // 本地记录的版本号与线上版本号不一致 => 用户没查看过，直接弹出
        if (localStorage.getItem(VERSION_NEWS_KEY) !== currentVersionId) {
            isModalVisible.value = true;
            localStorage.setItem(VERSION_NEWS_KEY, currentVersionId);
        }
    } catch {
        // 忽略版本获取失败，不阻塞页面
    }
});

const showVersion = () => {
    isModalVisible.value = true;
    emit('toast', `当前版本：${displayVersion.value || '获取中...'}`, '📦');
};
</script>

<template>
    <!-- 像素风格版本更新弹框：由版本号与本地记录共同控制显隐 -->
    <PixelUpdateModel
        v-model="isModalVisible"
        :title="versionData?.versionId || 'WORLD PATCH'"
        :content="versionData?.versionContent"
    />

    <footer class="px-footer">
        <div class="px-footer-inner">
            <div class="px-footer-left">
                <div class="px-footer-brand">
                    <span>oldmerman's blog</span>
                    <span class="px-online"></span>
                </div>
                <p class="px-footer-desc">方块世界的避风港 · 记录代码、像素艺术与生存探险。</p>
                <div class="px-footer-server">SERVER TPS: 20.0 | TICK DELAY: 0MS | MESH: ACTIVE</div>
            </div>

            <div class="px-footer-mid">
                <div class="px-footer-made">
                    MADE WITH <span class="px-heart">❤</span> IN PIXELS
                </div>
                <div class="px-footer-copy">&copy; 2026 oldmerman. All rights reserved.</div>
            </div>

            <div class="px-footer-right">
                <a href="http://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer" class="px-footer-link">
                    <span class="px-icp">[ICP]</span>
                    <span>粤ICP备2026023638号-1</span>
                </a>
                <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44196202000120" target="_blank"
                    rel="noopener noreferrer" class="px-footer-link">
                    <img src="https://beian.mps.gov.cn/img/logo01.dd7ff50e.png" alt="beian" />
                    <span>粤公网安备44196202000120号</span>
                </a>
                <a class="px-footer-link" @click="showVersion">
                    <span class="px-icp">[VERSION]</span>
                    <span>{{ displayVersion || 'Vx.x.x' }}</span>
                </a>
                <div class="px-footer-note">MINECRAFT ASSETS ARE PARODY PIXEL FAN ART</div>
            </div>
        </div>
    </footer>
</template>
