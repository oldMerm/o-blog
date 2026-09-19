<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import { type Article, articleType } from '@/views/public/Article';
import PixelHeader from './component/PixelHeader.vue';
import PixelColumn from './component/PixelColumn.vue';
import PlayerCard from './component/PlayerCard.vue';
import PixelSteve from './component/PixelSteve.vue';
import PixelFooter from './component/PixelFooter.vue';
import PixelToast from './component/PixelToast.vue';
import './pixel.css';

const emit = defineEmits<{
    (e: 'switch-style'): void;
}>();

const notices = ref<Article[]>([]);
const techs = ref<Article[]>([]);
const lives = ref<Article[]>([]);
const versionId = ref('');

const totalModules = computed(() => notices.value.length + techs.value.length + lives.value.length);

const toastRef = ref<InstanceType<typeof PixelToast> | null>(null);
const showToast = (msg: string, icon = '💡') => {
    toastRef.value?.show(msg, icon);
};

/* ---- 自定义缓动滚动（先慢后快再慢） ---- */
const easeInOutCubic = (t: number) => {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
};

const smoothScrollTo = (el: HTMLElement | null, duration = 900, offset = 0) => {
    if (!el) return;
    const startY = window.scrollY;
    const targetY = el.getBoundingClientRect().top + startY - offset;
    const distance = targetY - startY;
    if (Math.abs(distance) < 1) return;

    let startTime: number | null = null;
    const step = (ts: number) => {
        if (startTime === null) startTime = ts;
        const progress = Math.min((ts - startTime) / duration, 1);
        window.scrollTo(0, startY + distance * easeInOutCubic(progress));
        if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
};

const contentRef = ref<HTMLElement | null>(null);
const scrollToContent = () => {
    smoothScrollTo(contentRef.value, 900, 80);
};

const playerRef = ref<HTMLElement | null>(null);
const scrollToUser = () => {
    smoothScrollTo(playerRef.value, 1800, 160);
    showToast('已定位至玩家状态档案卡！', '👤');
};

/* ---- Google Fonts 仅在像素模式下注入 ---- */
const FONT_ID = 'px-google-fonts';
const loadFonts = () => {
    if (document.getElementById(FONT_ID)) return;
    const pre1 = document.createElement('link');
    pre1.rel = 'preconnect';
    pre1.href = 'https://fonts.googleapis.com';
    const pre2 = document.createElement('link');
    pre2.rel = 'preconnect';
    pre2.href = 'https://fonts.gstatic.com';
    pre2.crossOrigin = 'anonymous';
    const link = document.createElement('link');
    link.id = FONT_ID;
    link.rel = 'stylesheet';
    link.href = 'https://fonts.googleapis.com/css2?family=Press+Start+2P&family=Silkscreen:wght@400;700&family=Noto+Sans+SC:wght@400;500;700;900&display=swap';
    document.head.append(pre1, pre2, link);
};
const removeFonts = () => {
    document.getElementById(FONT_ID)?.remove();
};

const fetchList = async (type: articleType, size?: number): Promise<Article[]> => {
    try {
        const res = await httpInstance.get<any, Response>('/article/public/info', {
            params: size ? { id: type, size } : { id: type }
        });
        return (res.data ?? []) as Article[];
    } catch {
        return [];
    }
};

onMounted(async () => {
    loadFonts();

    const [n, t, l] = await Promise.all([
        fetchList(articleType.Notice),
        fetchList(articleType.TECNO, 11),
        fetchList(articleType.LIFE)
    ]);
    notices.value = n;
    techs.value = t;
    lives.value = l;

    try {
        const res = await httpInstance.get<any, Response>('/version');
        if (res.code === 200) {
            versionId.value = res.data?.versionId ?? '';
        }
    } catch {
        // 忽略版本获取失败
    }
});

onUnmounted(removeFonts);
</script>

<template>
    <div class="pixel-home mc-grid">
        <PixelHeader @switch-style="emit('switch-style')" @scroll-user="scrollToUser" />

        <main id="top" class="px-main">
            <!-- UPPER HERO SECTION -->
            <section class="px-hero">
                <!-- Sky Floating Pixel Clouds & Sun Block -->
                <div class="px-sky">
                    <div class="px-cloud px-cloud-1">
                        <div class="px-cloud-base">
                            <div class="px-cloud-top"></div>
                        </div>
                    </div>
                    <div class="px-cloud px-cloud-2">
                        <div class="px-cloud-base">
                            <div class="px-cloud-top"></div>
                        </div>
                    </div>
                    <div class="px-sun"></div>
                </div>

                <!-- Top Biome Tag -->
                <div class="px-hero-top">
                    <div class="px-biome">
                        <span class="px-biome-dot"></span>
                        <span>BIOME: OVERWORLD OAK PLAINS &amp; SUNSET MEADOW</span>
                    </div>
                </div>

                <!-- Central Greeting -->
                <div class="px-hero-center">
                    <div class="px-hello">
                        <span>HELLO, TRAVELER !</span>
                    </div>

                    <h1 class="px-hero-title">
                        你好，旅行者！<br />
                        <span>欢迎来到 oldmerman 的方块世界。</span>
                    </h1>

                    <p class="px-quote">
                        “方块筑起坚不可摧的城堡，阳光洒落在静谧的草甸。<br />
                        敲下每一行代码、放下每一个方块，都为与你更好地相遇。”
                    </p>

                    <div class="px-cta-wrap">
                        <a href="#content-section" class="px-btn px-cta" @click.prevent="scrollToContent">
                            <span>EXPLORE POSTS / 开始探索</span>
                            <span class="px-bounce">▼</span>
                        </a>
                        <span class="px-cta-hint">滚动向下滑动查阅档案</span>
                    </div>
                </div>

                <!-- Steve 像素角色 -->
                <PixelSteve @toast="showToast" />

                <!-- Ground Trim Layer -->
                <div class="px-ground">
                    <div class="px-ground-inner"></div>
                </div>
            </section>

            <!-- 三栏内容 -->
            <section id="content-section" ref="contentRef" class="px-content">
                <div class="px-section-head">
                    <div class="px-section-title-wrap">
                        <div class="px-section-mark"></div>
                        <h2 class="px-section-title">WORLD ARCHIVES / 领地档案</h2>
                    </div>
                    <div class="px-section-meta">
                        <span class="px-section-meta-dot"></span>
                        <span>3 CATEGORIES • {{ totalModules }} MODULES</span>
                    </div>
                </div>

                <div class="px-grid">
                    <!-- 公告 -->
                    <div class="px-col">
                        <PixelColumn index="01." icon="📜" title="公告 (NOTICES)" variant="notice" :articles="notices" />
                        <div ref="playerRef">
                            <PlayerCard @toast="showToast" />
                        </div>
                    </div>

                    <!-- 技术 -->
                    <PixelColumn index="02." icon="⚡" title="技术 (TECH)" variant="tech" :articles="techs" />

                    <!-- 日常 -->
                    <PixelColumn index="03." icon="☕" title="日常 (DAILY)" variant="daily" :articles="lives" />
                </div>
            </section>
        </main>

        <PixelFooter :version-id="versionId" @toast="showToast" />

        <PixelToast ref="toastRef" />
    </div>
</template>
