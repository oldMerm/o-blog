<script setup lang="ts">
const props = defineProps<{
    versionId?: string;
}>();

const emit = defineEmits<{
    (e: 'toast', msg: string, icon?: string): void;
}>();

const showVersion = () => {
    emit('toast', `当前版本：${props.versionId || '获取中...'}`, '📦');
};
</script>

<template>
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
                    <span>{{ versionId || 'Vx.x.x' }}</span>
                </a>
                <div class="px-footer-note">MINECRAFT ASSETS ARE PARODY PIXEL FAN ART</div>
            </div>
        </div>
    </footer>
</template>
