<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

/**
 * 404 页面 —— 由 D:\SecondCode\qianduan\404.html 移植为 Vue3 + TS 组件。
 * 原页面使用 Tailwind CDN 与全局 html/body 样式，这里全部改写为 scoped CSS，
 * 并以 position: fixed 铺满视口，避免与本项目全局样式（body min-width、滚动条）互相影响。
 */

interface Bubble {
    id: number;
    left: number;
    top: number;
    size: number;
    drift: number;
}

const BUBBLE_LIFETIME = 2300; // 与气泡动画时长保持一致
const FONT_LINK_ID = 'nf-404-google-fonts';
const FONT_PRECONNECT_IDS = ['nf-404-preconnect-1', 'nf-404-preconnect-2'] as const;
const BODY_CLASS = 'nf-404-active';

const sceneRef = ref<HTMLElement | null>(null);
const fishRef = ref<HTMLElement | null>(null);

const bubbles = ref<Bubble[]>([]);
const isSqueezed = ref(false);

let bubbleSeed = 0;
const timers: number[] = [];

const later = (fn: () => void, delay: number) => {
    timers.push(window.setTimeout(fn, delay));
};

// 气泡样式：以 CSS 变量 --tx 控制上浮时的水平漂移
const bubbleStyle = (bubble: Bubble): Record<string, string> => ({
    width: `${bubble.size}px`,
    height: `${bubble.size}px`,
    left: `${bubble.left}px`,
    top: `${bubble.top}px`,
    '--tx': `${bubble.drift}px`
});

const createBubble = () => {
    const fish = fishRef.value;
    const scene = sceneRef.value;
    if (!fish || !scene) return;

    const fishRect = fish.getBoundingClientRect();
    const sceneRect = scene.getBoundingClientRect();

    const bubble: Bubble = {
        id: bubbleSeed++,
        left: fishRect.left - sceneRect.left + fishRect.width / 2 + (Math.random() * 16 - 8),
        top: fishRect.top - sceneRect.top + 10,
        size: Math.floor(Math.random() * 11) + 9,
        drift: (Math.random() - 0.5) * 40
    };

    bubbles.value.push(bubble);

    later(() => {
        bubbles.value = bubbles.value.filter((item) => item.id !== bubble.id);
    }, BUBBLE_LIFETIME);
};

const handleFishClick = () => {
    // 轻轻捏一下的挤压反馈
    isSqueezed.value = true;
    later(() => {
        isSqueezed.value = false;
    }, 240);

    // 泛起 2 ~ 3 个梦幻气泡
    const count = Math.floor(Math.random() * 2) + 2;
    for (let i = 0; i < count; i++) {
        later(createBubble, i * 160);
    }
};

/* ---- Google Fonts 仅在本页面挂载时注入 ---- */
const loadFonts = () => {
    if (document.getElementById(FONT_LINK_ID)) return;

    const pre1 = document.createElement('link');
    pre1.id = FONT_PRECONNECT_IDS[0];
    pre1.rel = 'preconnect';
    pre1.href = 'https://fonts.googleapis.com';

    const pre2 = document.createElement('link');
    pre2.id = FONT_PRECONNECT_IDS[1];
    pre2.rel = 'preconnect';
    pre2.href = 'https://fonts.gstatic.com';
    pre2.crossOrigin = 'anonymous';

    const link = document.createElement('link');
    link.id = FONT_LINK_ID;
    link.rel = 'stylesheet';
    link.href =
        'https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@300;400;600&family=Playfair+Display:ital,wght@0,500;0,700;1,400&family=Plus+Jakarta+Sans:wght@300;400&display=swap';

    document.head.append(pre1, pre2, link);
};

const removeFonts = () => {
    document.getElementById(FONT_LINK_ID)?.remove();
    FONT_PRECONNECT_IDS.forEach((id) => document.getElementById(id)?.remove());
};

onMounted(() => {
    loadFonts();
    // 本页需要独占视口，挂载期间抹平 cover.css 里的 body 最小宽度与横向滚动
    document.body.classList.add(BODY_CLASS);
});

onUnmounted(() => {
    removeFonts();
    document.body.classList.remove(BODY_CLASS);
    timers.forEach((timer) => window.clearTimeout(timer));
    timers.length = 0;
});
</script>

<template>
    <div class="nf-page nf-grain">
        <!-- 顶部轻描淡写的氛围提示（无按钮，保持留白） -->
        <header class="nf-header">Page Not Found</header>

        <!-- 主体：漂浮的 404、羽化的海景与诗意文案 -->
        <main class="nf-main">
            <div class="nf-emblem">
                <h1 class="nf-emblem-text nf-font-art">404</h1>
            </div>

            <!-- 羽化边缘的海景画布（无边框，直接融入留白） -->
            <div class="nf-scene-wrap">
                <div ref="sceneRef" class="nf-sky">
                    <!-- 远处的暖阳 -->
                    <div class="nf-sun"></div>

                    <!-- 大气里的云 -->
                    <div class="nf-cloud nf-cloud-1"></div>
                    <div class="nf-cloud nf-cloud-2"></div>

                    <!-- 远处的海鸟 -->
                    <div class="nf-seagull nf-seagull-1"></div>
                    <div class="nf-seagull nf-seagull-2"></div>

                    <!-- 有呼吸感的分层海浪 -->
                    <div class="nf-ocean">
                        <div class="nf-shimmer"></div>

                        <svg class="nf-wave nf-wave-deep" viewBox="0 0 1200 120" preserveAspectRatio="none">
                            <path d="M0,0 C150,45 350,-25 500,20 C650,65 900,-20 1200,15 L1200,120 L0,120 Z"
                                fill="rgba(82, 142, 187, 0.45)"></path>
                        </svg>

                        <svg class="nf-wave nf-wave-center" viewBox="0 0 1200 120" preserveAspectRatio="none">
                            <path d="M0,20 C200,75 450,-10 650,40 C850,90 1050,10 1200,35 L1200,120 L0,120 Z"
                                fill="rgba(43, 92, 143, 0.6)"></path>
                        </svg>

                        <svg class="nf-wave nf-wave-near" viewBox="0 0 1200 120" preserveAspectRatio="none">
                            <path d="M0,35 C180,80 380,10 600,55 C820,100 1020,25 1200,50 L1200,120 L0,120 Z" fill="#183a5c">
                            </path>
                        </svg>
                    </div>

                    <!-- 温暖的金色沙滩 -->
                    <div class="nf-beach">
                        <div class="nf-tide-glow"></div>
                    </div>

                    <!-- 胖鱼：点击泛起气泡 -->
                    <div ref="fishRef" class="nf-fish-anchor" :class="{ 'is-squeezed': isSqueezed }" title="轻触胖鱼，泛起泡泡"
                        @click="handleFishClick">
                        <div class="nf-fish-shadow"></div>

                        <div class="nf-fish-sway">
                            <!-- 背鳍 -->
                            <div class="nf-fish-top-fin"></div>

                            <!-- 圆润的鱼身（背面视角） -->
                            <div class="nf-fish-body">
                                <div class="nf-fish-ridge"></div>
                            </div>

                            <!-- 撑在沙滩上的侧鳍 -->
                            <div class="nf-fish-fin-prop"></div>

                            <!-- 安放在岸边的尾鳍 -->
                            <div class="nf-fish-tail">
                                <div class="nf-tail-blade-1"></div>
                                <div class="nf-tail-blade-2"></div>
                            </div>
                        </div>
                    </div>

                    <!-- 点击胖鱼后升起的气泡 -->
                    <div v-for="bubble in bubbles" :key="bubble.id" class="nf-bubble" :style="bubbleStyle(bubble)"></div>
                </div>
            </div>

            <!-- 下方的诗意文案 -->
            <div class="nf-poetic">
                <h2 class="nf-font-poetic nf-poetic-line">页面跑丢了...</h2>
            </div>
        </main>

        <footer class="nf-footer">&copy; 2026 oldmerman and o-blog</footer>
    </div>
</template>

<!-- 非 scoped：仅在 404 页面挂载期间覆盖全局 body 样式 -->
<style>
body.nf-404-active {
    min-width: 0;
    overflow: hidden;
    background-color: #faf8f5;
}
</style>

<style scoped>
/* ================= 页面骨架 ================= */
.nf-page {
    --nf-canvas: #faf8f5;
    --nf-sand-main: #f5cf9e;
    --nf-sand-warm: #e3b379;
    --nf-sand-deep: #ca985c;

    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100dvh;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    overflow: hidden;
    padding: 20px 16px;
    background-color: var(--nf-canvas);
    color: #3b332d;
    font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
    user-select: none;
    -webkit-user-select: none;
}

/* 整屏淡淡的画纸颗粒 */
.nf-grain {
    background-image: radial-gradient(rgba(45, 35, 25, 0.035) 1px, transparent 0);
    background-size: 28px 28px;
}

.nf-font-poetic {
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
}

.nf-font-art {
    font-family: 'Playfair Display', Georgia, serif;
}

/* 顶部提示 */
.nf-header {
    width: 100%;
    text-align: center;
    flex-shrink: 0;
    padding-top: 8px;
    opacity: 0.5;
    font-size: 10px;
    letter-spacing: 0.35em;
    text-transform: uppercase;
    color: #78716c;
    font-weight: 300;
}

/* 主体 */
.nf-main {
    width: 100%;
    max-width: 672px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin-block: auto;
    padding-inline: 8px;
}

.nf-emblem {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 4px;
}

.nf-emblem-text {
    margin: 0;
    font-size: 96px;
    font-weight: 700;
    background: linear-gradient(180deg,
        rgba(40, 34, 30, 0.85) 0%,
        rgba(75, 62, 54, 0.7) 45%,
        rgba(150, 130, 118, 0.35) 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    line-height: 0.86;
    letter-spacing: -0.04em;
    filter: drop-shadow(0 15px 30px rgba(0, 0, 0, 0.04));
}

/* 海景画布外框：径向羽化遮罩，让画面融入留白 */
.nf-scene-wrap {
    position: relative;
    width: 100%;
    max-width: 540px;
    height: 210px;
    margin-top: -16px;
    -webkit-mask-image: radial-gradient(ellipse 65% 58% at 50% 50%,
        #000 35%,
        rgba(0, 0, 0, 0.75) 60%,
        rgba(0, 0, 0, 0.25) 82%,
        transparent 100%);
    mask-image: radial-gradient(ellipse 65% 58% at 50% 50%,
        #000 35%,
        rgba(0, 0, 0, 0.75) 60%,
        rgba(0, 0, 0, 0.25) 82%,
        transparent 100%);
}

/* 天空与暖色黄昏渐变 */
.nf-sky {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background: linear-gradient(180deg,
        #ffd6ba 0%,
        #fed1be 20%,
        #eecdc2 48%,
        #fae3d9 75%,
        #fff1e6 100%);
}

/* ================= 远处的暖阳 ================= */
.nf-sun {
    position: absolute;
    width: 90px;
    height: 90px;
    bottom: 74px;
    left: 38%;
    background: radial-gradient(circle, #ffffff 18%, #ffefa6 55%, rgba(255, 239, 166, 0) 100%);
    border-radius: 50%;
    filter: blur(1.5px);
    box-shadow: 0 0 50px rgba(255, 225, 130, 0.65);
    animation: nf-sun-breathing 6s ease-in-out infinite alternate;
}

@keyframes nf-sun-breathing {
    0% {
        transform: translateY(0) scale(1);
        opacity: 0.9;
    }

    100% {
        transform: translateY(-4px) scale(1.05);
        opacity: 1;
    }
}

/* ================= 漂浮的云 ================= */
.nf-cloud {
    position: absolute;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 999px;
    filter: blur(2.5px);
    animation: nf-cloud-drift linear infinite;
}

.nf-cloud-1 {
    width: 130px;
    height: 18px;
    top: 26px;
    left: -140px;
    animation-duration: 34s;
}

.nf-cloud-2 {
    width: 85px;
    height: 14px;
    top: 52px;
    left: -90px;
    animation-duration: 24s;
    animation-delay: 5s;
    opacity: 0.5;
}

@keyframes nf-cloud-drift {
    0% {
        transform: translateX(0);
    }

    100% {
        transform: translateX(780px);
    }
}

/* ================= 远处的海鸟 ================= */
.nf-seagull {
    position: absolute;
    width: 16px;
    height: 7px;
    opacity: 0.4;
    animation: nf-gull-wander 20s ease-in-out infinite;
}

.nf-seagull-1 {
    top: 32px;
    left: 18%;
}

.nf-seagull-2 {
    top: 48px;
    left: 26%;
    animation-delay: -7s;
}

.nf-seagull::before,
.nf-seagull::after {
    content: '';
    position: absolute;
    width: 8px;
    height: 5px;
    border: 1.5px solid #6c4b37;
    border-bottom: transparent;
    border-left: transparent;
    border-radius: 50% 50% 0 0;
}

.nf-seagull::after {
    left: 7px;
    border-left: transparent;
    border-right: 1.5px solid #6c4b37;
}

@keyframes nf-gull-wander {
    0% {
        transform: translate(30px, 35px) scale(0.7);
    }

    50% {
        transform: translate(170px, 28px) scale(0.85);
    }

    100% {
        transform: translate(320px, 18px) scale(0.7);
    }
}

/* ================= 分层海浪 ================= */
.nf-ocean {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 110px;
    background: linear-gradient(180deg, #376a95 0%, #234f78 45%, #183a5c 100%);
    overflow: hidden;
}

.nf-shimmer {
    position: absolute;
    top: 0;
    left: 37%;
    width: 58px;
    height: 100%;
    background: linear-gradient(180deg, rgba(255, 245, 195, 0.5) 0%, rgba(255, 230, 150, 0.12) 65%, transparent 100%);
    filter: blur(4px);
    clip-path: polygon(35% 0%, 65% 0%, 100% 100%, 0% 100%);
    pointer-events: none;
    animation: nf-shimmer-wave 4s ease-in-out infinite alternate;
}

@keyframes nf-shimmer-wave {
    0% {
        opacity: 0.55;
        transform: scaleX(0.85);
    }

    100% {
        opacity: 0.85;
        transform: scaleX(1.15);
    }
}

.nf-wave {
    position: absolute;
    left: 0;
    width: 200%;
    height: 100%;
    pointer-events: none;
}

.nf-wave-deep {
    top: -3px;
    opacity: 0.42;
    animation: nf-wave-flow 8.5s cubic-bezier(0.36, 0.45, 0.63, 0.53) infinite;
}

.nf-wave-center {
    top: 10px;
    opacity: 0.55;
    animation: nf-wave-flow 6s cubic-bezier(0.36, 0.45, 0.63, 0.53) -2s infinite reverse;
}

.nf-wave-near {
    top: 24px;
    opacity: 0.75;
    animation: nf-wave-flow 4.5s cubic-bezier(0.36, 0.45, 0.63, 0.53) -1s infinite;
}

@keyframes nf-wave-flow {
    0% {
        transform: translateX(0);
    }

    50% {
        transform: translateX(-25%);
    }

    100% {
        transform: translateX(-50%);
    }
}

/* ================= 金色沙滩 ================= */
.nf-beach {
    position: absolute;
    bottom: -15px;
    left: -20px;
    right: -20px;
    height: 90px;
    background: radial-gradient(ellipse at 50% 100%, #fbedd8 0%, var(--nf-sand-main) 48%, var(--nf-sand-warm) 82%, var(--nf-sand-deep) 100%);
    border-radius: 65% 55% 0 0 / 60% 50% 0 0;
    z-index: 10;
}

.nf-tide-glow {
    position: absolute;
    top: 1px;
    left: 0;
    right: 0;
    height: 7px;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.45) 30%, rgba(255, 255, 255, 0.75) 55%, transparent 100%);
    filter: blur(1.5px);
    animation: nf-tide-breath 4s ease-in-out infinite;
}

@keyframes nf-tide-breath {

    0%,
    100% {
        opacity: 0.3;
        transform: scaleY(0.7);
    }

    50% {
        opacity: 0.85;
        transform: scaleY(1.3);
    }
}

/* ================= 沙滩上的胖鱼 ================= */
.nf-fish-anchor {
    position: absolute;
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
    transform-origin: 50% 88%;
    z-index: 20;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 点击瞬间的挤压反馈（呼吸动画在子元素上，二者互不覆盖） */
.nf-fish-anchor.is-squeezed {
    transform: translateX(-50%) scale(1.12, 0.88);
}

.nf-fish-sway {
    position: relative;
    animation: nf-calm-breathe 5s ease-in-out infinite;
    transform-origin: 50% 88%;
    transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nf-fish-anchor:hover .nf-fish-sway {
    transform: scale(1.04) translateY(-2px);
}

@keyframes nf-calm-breathe {

    0%,
    100% {
        transform: scale(1) rotate(0deg);
    }

    35% {
        transform: scale(1.025, 0.98) rotate(-0.7deg);
    }

    70% {
        transform: scale(0.985, 1.02) rotate(0.7deg);
    }
}

/* 金色沙滩上温暖的柔和阴影 */
.nf-fish-shadow {
    position: absolute;
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%);
    width: 86px;
    height: 18px;
    background: rgba(138, 92, 45, 0.42);
    border-radius: 50%;
    filter: blur(3px);
}

/* 胖胖的、梨形的圆润鱼身（背面视角） */
.nf-fish-body {
    width: 82px;
    height: 88px;
    border-radius: 47% 53% 45% 55% / 58% 57% 43% 42%;
    background: linear-gradient(145deg,
        #ff9d7f 0%,
        #fa6f6f 35%,
        #e25454 70%,
        #ba3535 100%);
    position: relative;
    box-shadow:
        inset 5px 6px 14px rgba(255, 235, 225, 0.65),
        inset -6px -8px 18px rgba(115, 20, 20, 0.42),
        0 8px 22px rgba(186, 53, 53, 0.22);
    overflow: hidden;
}

/* 背部高光，凸显圆润感 */
.nf-fish-body::before {
    content: '';
    position: absolute;
    top: 8px;
    left: 14px;
    width: 40px;
    height: 50px;
    background: radial-gradient(ellipse at 38% 30%, rgba(255, 255, 255, 0.52) 0%, rgba(255, 235, 225, 0.15) 60%, transparent 80%);
    border-radius: 50%;
    transform: rotate(-15deg);
}

/* 细腻的背脊高光线 */
.nf-fish-ridge {
    position: absolute;
    top: 16px;
    left: 46%;
    width: 2.5px;
    height: 48px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, rgba(255, 210, 200, 0.1) 85%, transparent 100%);
    border-radius: 2px;
    transform: rotate(-3deg);
}

/* 轻轻抖动的小背鳍 */
.nf-fish-top-fin {
    position: absolute;
    top: -11px;
    left: 38px;
    width: 18px;
    height: 19px;
    background: linear-gradient(135deg, #ffab91 0%, #ef5350 100%);
    border-radius: 70% 30% 60% 40% / 80% 40% 60% 20%;
    transform: rotate(20deg);
    box-shadow: inset 1px 2px 4px rgba(255, 255, 255, 0.4);
    animation: nf-dorsal-flutter 4s ease-in-out infinite;
    transform-origin: bottom center;
}

@keyframes nf-dorsal-flutter {

    0%,
    100% {
        transform: rotate(18deg) scaleY(1);
    }

    50% {
        transform: rotate(25deg) scaleY(1.08);
    }
}

/* 直接撑在沙滩上的侧鳍 */
.nf-fish-fin-prop {
    position: absolute;
    bottom: 12px;
    left: -7px;
    width: 23px;
    height: 19px;
    background: linear-gradient(130deg, #ffab91 0%, #e53935 100%);
    border-radius: 60% 40% 70% 30% / 70% 30% 70% 30%;
    transform: rotate(-35deg);
    box-shadow: -2px 3px 6px rgba(0, 0, 0, 0.18);
    animation: nf-fin-wiggle 6s ease-in-out infinite;
    transform-origin: 85% 50%;
}

@keyframes nf-fin-wiggle {

    0%,
    85%,
    100% {
        transform: rotate(-35deg);
    }

    90% {
        transform: rotate(-24deg);
    }

    95% {
        transform: rotate(-38deg);
    }
}

/* 安稳地搭在岸边的尾鳍 */
.nf-fish-tail {
    position: absolute;
    bottom: -6px;
    right: 4px;
    width: 36px;
    height: 28px;
    z-index: 18;
    transform-origin: left center;
    animation: nf-tail-motion 3.5s ease-in-out infinite;
}

.nf-tail-blade-1,
.nf-tail-blade-2 {
    position: absolute;
    width: 23px;
    height: 19px;
    background: linear-gradient(140deg, #ff8a65 0%, #e53935 90%);
    border-radius: 50% 50% 20% 70%;
    box-shadow: inset 1px 1px 3px rgba(255, 255, 255, 0.4);
}

.nf-tail-blade-1 {
    transform: rotate(28deg);
    top: 0px;
    left: 4px;
}

.nf-tail-blade-2 {
    transform: rotate(62deg);
    top: 8px;
    left: 10px;
    width: 21px;
    height: 17px;
    opacity: 0.92;
}

@keyframes nf-tail-motion {

    0%,
    100% {
        transform: rotate(0deg);
    }

    45% {
        transform: rotate(8deg) scaleX(1.06);
    }

    80% {
        transform: rotate(-6deg) scaleX(0.96);
    }
}

/* ================= 气泡 ================= */
.nf-bubble {
    position: absolute;
    background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.95), rgba(215, 238, 255, 0.4));
    border: 1px solid rgba(255, 255, 255, 0.85);
    border-radius: 50%;
    pointer-events: none;
    animation: nf-bubble-ascend 2.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

@keyframes nf-bubble-ascend {
    0% {
        transform: translate(0, 0) scale(0.3);
        opacity: 0.95;
    }

    70% {
        opacity: 0.8;
    }

    100% {
        transform: translate(var(--tx, 15px), -95px) scale(1.1);
        opacity: 0;
    }
}

/* ================= 文案与页脚 ================= */
.nf-poetic {
    text-align: center;
    margin-top: 12px;
}

.nf-poetic-line {
    color: #3b332d;
    font-size: 18px;
    font-weight: 400;
    line-height: 1.625;
    letter-spacing: 0.28em;
    text-indent: 0.28em; /* 字距视觉平衡 */
}

.nf-footer {
    width: 100%;
    text-align: center;
    flex-shrink: 0;
    padding-bottom: 4px;
    font-size: 11px;
    color: rgba(120, 113, 108, 0.8);
    letter-spacing: 0.1em;
    font-weight: 300;
}

/* ================= 响应式（对应原 Tailwind sm / md 断点） ================= */
@media (min-width: 640px) {
    .nf-page {
        padding-block: 28px;
    }

    .nf-header {
        font-size: 12px;
    }

    .nf-emblem {
        margin-bottom: 8px;
    }

    .nf-emblem-text {
        font-size: 128px;
    }

    .nf-scene-wrap {
        height: 245px;
        margin-top: -24px;
    }

    .nf-poetic {
        margin-top: 20px;
    }

    .nf-poetic-line {
        font-size: 24px;
    }
}

@media (min-width: 768px) {
    .nf-emblem-text {
        font-size: 168px;
    }

    .nf-scene-wrap {
        height: 265px;
    }
}
</style>
