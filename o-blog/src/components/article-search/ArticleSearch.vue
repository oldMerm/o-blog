<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import { goToArticle } from '@/views/public/Article';
import { buildSnippetParts } from './snippet';

interface ArticleSearchItem {
    id?: number;
    articleId: number | string;
    title: string;
    summary?: string | null;
    createdAt?: string;
}

const props = withDefaults(defineProps<{
    /** pixel: 像素首页；plain: 普通首页 */
    variant?: 'pixel' | 'plain';
}>(), {
    variant: 'plain'
});

const isPixel = computed(() => props.variant === 'pixel');

const isOpen = ref(false);
const keyword = ref('');
const results = ref<ArticleSearchItem[]>([]);
const status = ref<'idle' | 'loading' | 'done' | 'error'>('idle');
const inputRef = ref<HTMLInputElement | null>(null);

let inputTimer: ReturnType<typeof setTimeout> | null = null;
let requestSeq = 0;

/** 后端默认返回 10 条；前端再兜底限制，避免一次渲染过多结果 */
const MAX_RENDER_COUNT = 10;

const countText = computed(() =>
    isPixel.value ? `${results.value.length} RESULTS` : `共 ${results.value.length} 条结果`
);

const statusLabel = computed(() => {
    switch (status.value) {
        case 'loading':
            return 'LOADING...';
        case 'done':
            return 'DONE';
        case 'error':
            return 'ERROR';
        default:
            return 'READY';
    }
});

const emptyText = computed(() => {
    if (status.value === 'loading') {
        return isPixel.value ? 'SEARCHING...' : '搜索中...';
    }
    if (status.value === 'error') {
        return isPixel.value ? 'SEARCH FAILED.' : '搜索出错，请稍后重试';
    }
    if (status.value === 'done') {
        return isPixel.value ? 'NO MATCHING ARCHIVES FOUND.' : '未找到相关文章';
    }
    return isPixel.value ? 'TYPE TO EXPLORE ARCHIVES...' : '输入关键词搜索文章';
});

const clearTimer = () => {
    if (inputTimer) {
        clearTimeout(inputTimer);
        inputTimer = null;
    }
};

/**
 * articleId 是后端 Long / 雪花 ID，直接 JSON.parse 会丢失精度。
 * 解析前先把该字段转成字符串，保证跳转文章时 ID 不被截断。
 */
const parseSearchResponse = (data: unknown): unknown => {
    if (typeof data !== 'string') return data;

    const safeJson = data.replace(/("articleId"\s*:\s*)(-?\d{16,})(?=\s*[,}])/g, '$1"$2"');

    try {
        return JSON.parse(safeJson);
    } catch {
        return data;
    }
};

const focusInput = () => {
    if (!isOpen.value) return;
    inputRef.value?.focus();
};

const open = () => {
    isOpen.value = true;
    document.body.style.overflow = 'hidden';
    window.setTimeout(focusInput, isPixel.value ? 120 : 30);
};

const close = () => {
    isOpen.value = false;
    document.body.style.overflow = '';
};

const runSearch = async () => {
    const query = keyword.value.trim();

    if (!query) {
        results.value = [];
        status.value = 'idle';
        return;
    }

    const currentSeq = ++requestSeq;
    status.value = 'loading';

    try {
        const res = await httpInstance.get<any, Response>(
            `/article/search/${encodeURIComponent(query)}`,
            {
                transformResponse: [parseSearchResponse]
            }
        );

        // 丢弃过期请求，避免旧关键词覆盖新结果
        if (currentSeq !== requestSeq) return;

        const list = Array.isArray(res?.data) ? (res.data as ArticleSearchItem[]) : [];
        results.value = list.slice(0, MAX_RENDER_COUNT);
        status.value = 'done';
    } catch {
        if (currentSeq !== requestSeq) return;
        results.value = [];
        status.value = 'error';
    }
};

const onInput = () => {
    clearTimer();
    requestSeq++;

    if (!keyword.value.trim()) {
        results.value = [];
        status.value = 'idle';
        return;
    }

    results.value = [];
    status.value = 'loading';
    inputTimer = setTimeout(() => {
        void runSearch();
    }, 250);
};

const searchNow = () => {
    clearTimer();
    void runSearch();
};

const openResult = (item: ArticleSearchItem) => {
    close();
    void goToArticle(String(item.articleId));
};

const formatDate = (value?: string) => (value ?? '').slice(0, 10);

const titleParts = (item: ArticleSearchItem) =>
    buildSnippetParts(item.title, keyword.value, 50);

const summaryParts = (item: ArticleSearchItem) =>
    buildSnippetParts(item.summary, keyword.value, 60);

const onKeydown = (event: KeyboardEvent) => {
    if (event.key === 'Escape' && isOpen.value) {
        event.preventDefault();
        close();
    }
};

onMounted(() => {
    document.addEventListener('keydown', onKeydown);
});

onBeforeUnmount(() => {
    document.removeEventListener('keydown', onKeydown);
    clearTimer();
    requestSeq++;
    if (isOpen.value) {
        document.body.style.overflow = '';
    }
});
</script>

<template>
    <button
        type="button"
        class="as-trigger"
        :class="isPixel ? 'as-trigger--pixel px-btn' : 'as-trigger--plain'"
        :title="isPixel ? '搜索文章' : '搜索文章（弹窗内按 ESC 关闭）'"
        aria-label="搜索文章"
        @click="open"
    >
        <svg
            v-if="isPixel"
            class="as-trigger-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
            aria-hidden="true"
        >
            <path
                d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
            />
        </svg>
        <svg
            v-else
            class="as-trigger-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            aria-hidden="true"
        >
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" />
        </svg>
        <span v-if="isPixel">Search</span>
    </button>

    <Teleport to="body">
        <Transition :name="isPixel ? 'px-search' : 'plain-search'">
            <div
                v-if="isOpen"
                class="as-overlay"
                :class="props.variant"
                @click.self="close"
            >
                <div class="as-panel" role="dialog" aria-modal="true" aria-label="文章搜索">
                    <!-- 像素风顶部标题栏 -->
                    <div v-if="isPixel" class="as-titlebar">
                        <div class="as-title">
                            <span class="as-title-dot"></span>
                            <span>ARCHIVE SEARCH</span>
                        </div>
                        <button
                            type="button"
                            class="as-pixel-close"
                            title="关闭 (ESC)"
                            aria-label="关闭 (ESC)"
                            @click="close"
                        >
                            X
                        </button>
                    </div>

                    <!-- 输入区 -->
                    <div class="as-inputbar">
                        <template v-if="isPixel">
                            <div class="as-inputbox">
                                <span class="as-prompt">&gt;_</span>
                                <input
                                    ref="inputRef"
                                    v-model="keyword"
                                    class="as-input"
                                    type="text"
                                    autocomplete="off"
                                    placeholder="输入关键词搜索文章标题或摘要..."
                                    @input="onInput"
                                    @keydown.enter.prevent="searchNow"
                                />
                                <span class="as-status">{{ statusLabel }}</span>
                            </div>
                        </template>

                        <template v-else>
                            <svg
                                class="as-plain-icon"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                aria-hidden="true"
                            >
                                <circle cx="11" cy="11" r="7" />
                                <path d="M20 20l-3.5-3.5" />
                            </svg>
                            <input
                                ref="inputRef"
                                v-model="keyword"
                                class="as-input"
                                type="text"
                                autocomplete="off"
                                placeholder="搜索文章..."
                                @input="onInput"
                                @keydown.enter.prevent="searchNow"
                            />
                            <button
                                type="button"
                                class="as-plain-close"
                                title="关闭 (ESC)"
                                aria-label="关闭 (ESC)"
                                @click="close"
                            >
                                <svg
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    stroke-width="2"
                                    aria-hidden="true"
                                >
                                    <path d="M6 6l12 12M18 6L6 18" />
                                </svg>
                            </button>
                        </template>
                    </div>

                    <!-- 结果列表 -->
                    <div class="as-results">
                        <button
                            v-for="item in results"
                            :key="item.id ?? item.articleId"
                            type="button"
                            class="as-result"
                            @click="openResult(item)"
                        >
                            <div class="as-result-top">
                                <h4 class="as-result-title">
                                    <template v-for="(part, index) in titleParts(item)" :key="index">
                                        <mark
                                            v-if="part.match"
                                            :class="isPixel ? 'as-mark-pixel' : 'as-mark-plain'"
                                        >{{ part.text }}</mark>
                                        <template v-else>{{ part.text }}</template>
                                    </template>
                                </h4>
                                <span class="as-result-date">{{ formatDate(item.createdAt) }}</span>
                            </div>
                            <p v-if="item.summary" class="as-result-summary">
                                <template v-for="(part, index) in summaryParts(item)" :key="index">
                                    <mark
                                        v-if="part.match"
                                        :class="isPixel ? 'as-mark-pixel' : 'as-mark-plain'"
                                    >{{ part.text }}</mark>
                                    <template v-else>{{ part.text }}</template>
                                </template>
                            </p>
                        </button>

                        <div v-if="!results.length" class="as-empty">{{ emptyText }}</div>
                    </div>

                    <!-- 底部状态栏 -->
                    <div class="as-footer">
                        <span>{{ isPixel ? 'ESC TO CLOSE' : '按 ESC 关闭' }}</span>
                        <span>{{ countText }}</span>
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<style scoped>
/* ==================== 触发按钮 ==================== */
.as-trigger {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    cursor: pointer;
}

.as-trigger--pixel {
    white-space: nowrap;
}

.as-trigger--plain {
    width: auto;
    height: 2.25rem;
    justify-content: center;
    padding: 0;
    border: none;
    background: transparent;
    color: rgb(46, 46, 46);
    font-family: inherit;
    font-size: 1rem;
}

.as-trigger--plain .as-trigger-icon {
    width: 32px;
    height: 32px;
    flex-shrink: 0;
}

/* ==================== 弹框基础 ==================== */
.as-overlay,
.as-overlay *,
.as-overlay *::before,
.as-overlay *::after {
    box-sizing: border-box;
}

.as-overlay {
    position: fixed;
    inset: 0;
    z-index: 200;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    padding: 12vh 16px;
    overflow-y: auto;
}

.as-panel {
    display: flex;
    flex-direction: column;
    width: 100%;
    max-width: 480px;
    /* 弹框最高占视口 3/4，上下留白基本一致 */
    max-height: 75vh;
    overflow: hidden;
}

.as-titlebar,
.as-inputbar,
.as-footer {
    flex-shrink: 0;
}

.as-results {
    flex: 1 1 auto;
    min-height: 0;
    overflow-y: auto;
    overscroll-behavior: contain;
}

/* ==================== 像素风格 ==================== */
.as-overlay.pixel {
    background-color: rgba(18, 38, 58, 0.5);
    backdrop-filter: blur(2px);
    font-family: 'Press Start 2P', 'Silkscreen', 'Noto Sans SC', monospace;
}

.pixel .as-panel {
    background-color: #ffffff;
    border: 4px solid #12263a;
    box-shadow: 6px 6px 0 0 #12263a;
}

.pixel .as-titlebar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 10px 16px;
    background-color: #d9eeff;
    border-bottom: 4px solid #12263a;
}

.pixel .as-title {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #12263a;
    font-size: 12px;
    letter-spacing: 2px;
}

.pixel .as-title-dot {
    display: inline-block;
    width: 10px;
    height: 10px;
    background-color: #1e3a8a;
    border: 1px solid #12263a;
    flex-shrink: 0;
}

.pixel .as-pixel-close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    background-color: #ffffff;
    border: 3px solid #12263a;
    box-shadow: 3px 3px 0 0 #12263a;
    color: #12263a;
    font-family: inherit;
    font-size: 12px;
    cursor: pointer;
    transition: transform 0.1s ease, box-shadow 0.1s ease, background-color 0.1s ease;
}

.pixel .as-pixel-close:hover {
    background-color: #fee2e2;
    transform: translate(-2px, -2px);
    box-shadow: 5px 5px 0 0 #12263a;
}

.pixel .as-pixel-close:active {
    transform: translate(2px, 2px);
    box-shadow: 1px 1px 0 0 #12263a;
}

.pixel .as-inputbar {
    padding: 16px;
    background-color: #f0f7ff;
    border-bottom: 3px solid #12263a;
}

.pixel .as-inputbox {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background-color: #ffffff;
    border: 3px solid #12263a;
    box-shadow: 2px 2px 0 0 #12263a;
    transition: border-color 0.15s ease;
}

.pixel .as-inputbox:focus-within {
    border-color: #1e3a8a;
}

.pixel .as-prompt {
    flex-shrink: 0;
    color: #1e3a8a;
    font-size: 12px;
}

.pixel .as-input {
    flex: 1;
    min-width: 0;
    padding: 0;
    border: none;
    outline: none;
    background: transparent;
    color: #12263a;
    font-family: 'Noto Sans SC', system-ui, sans-serif;
    font-size: 13px;
}

.pixel .as-input::placeholder {
    color: #94a3b8;
}

.pixel .as-status {
    flex-shrink: 0;
    color: #94a3b8;
    font-size: 10px;
}

.pixel .as-results {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    background-color: #ffffff;
}

.pixel .as-result {
    display: block;
    width: 100%;
    padding: 14px;
    text-align: left;
    background-color: #ffffff;
    border: 2px solid #12263a;
    box-shadow: 2px 2px 0 0 #12263a;
    cursor: pointer;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.pixel .as-result:hover {
    transform: translate(-2px, -2px);
    box-shadow: 4px 4px 0 0 #12263a;
}

.pixel .as-result-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 6px;
}

.pixel .as-result-title {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    color: #12263a;
    font-family: 'Noto Sans SC', system-ui, sans-serif;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.6;
    word-break: break-word;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.pixel .as-result-date {
    flex-shrink: 0;
    color: #94a3b8;
    font-size: 9px;
}

.pixel .as-result-summary {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    color: #475569;
    font-family: 'Noto Sans SC', system-ui, sans-serif;
    font-size: 12px;
    line-height: 1.7;
    word-break: break-word;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.pixel .as-empty {
    padding: 32px 8px;
    color: #94a3b8;
    font-size: 11px;
    line-height: 1.8;
    text-align: center;
}

.pixel .as-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 8px 16px;
    background-color: #eaf4ff;
    border-top: 2px solid #12263a;
    color: #64748b;
    font-size: 10px;
}

/* ==================== 普通风格（与普通首页配色对齐） ==================== */
.as-overlay.plain {
    background-color: rgba(0, 0, 0, 0.35);
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.plain .as-panel {
    background-color: #ffffff;
    border: 1px solid #dadee5;
    border-radius: 12px;
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.08);
}

.plain .as-inputbar {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
}

.plain .as-plain-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: #909399;
}

.plain .as-input {
    flex: 1;
    min-width: 0;
    padding: 0;
    border: none;
    outline: none;
    background: transparent;
    color: #303133;
    font-family: inherit;
    font-size: 14px;
}

.plain .as-input::placeholder {
    color: #c0c4cc;
}

.plain .as-plain-close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    flex-shrink: 0;
    border: none;
    border-radius: 6px;
    background: transparent;
    color: #909399;
    cursor: pointer;
    transition: color 0.2s ease, background-color 0.2s ease;
}

.plain .as-plain-close:hover {
    color: #409eff;
    background-color: #f5f7fa;
}

.plain .as-plain-close svg {
    width: 16px;
    height: 16px;
}

.plain .as-results {
    background-color: #ffffff;
}

.plain .as-result {
    display: block;
    width: 100%;
    padding: 12px 16px;
    text-align: left;
    background-color: #ffffff;
    border: none;
    border-bottom: 1px solid #f2f6fc;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.plain .as-result:last-child {
    border-bottom: none;
}

.plain .as-result:hover {
    background-color: #f5f7fa;
}

.plain .as-result-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 4px;
}

.plain .as-result-title {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    color: #303133;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.5;
    word-break: break-word;
    transition: color 0.2s ease;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.plain .as-result:hover .as-result-title {
    color: #409eff;
}

.plain .as-result-date {
    flex-shrink: 0;
    color: #909399;
    font-size: 12px;
}

.plain .as-result-summary {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    color: #909399;
    font-size: 12px;
    line-height: 1.7;
    word-break: break-word;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.plain .as-empty {
    padding: 36px 8px;
    color: #909399;
    font-size: 14px;
    text-align: center;
}

.plain .as-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 8px 16px;
    background-color: #fafafa;
    border-top: 1px solid #e4e7ed;
    border-radius: 0 0 12px 12px;
    color: #909399;
    font-size: 12px;
}

/* ==================== 关键词高亮 ==================== */
:deep(.as-mark-pixel) {
    padding: 0 3px;
    background-color: #ffeaa7;
    border-bottom: 2px solid #e1b12c;
    color: #12263a;
    font-weight: 700;
}

:deep(.as-mark-plain) {
    padding: 0 2px;
    border-radius: 2px;
    background-color: #ecf5ff;
    color: #409eff;
    font-weight: 600;
}

/* ==================== 滚动条 ==================== */
.as-results::-webkit-scrollbar {
    width: 6px;
}

.pixel .as-results::-webkit-scrollbar-thumb {
    background-color: #a8d8ea;
}

.plain .as-results::-webkit-scrollbar-thumb {
    background-color: #dcdfe6;
    border-radius: 3px;
}

.as-results::-webkit-scrollbar-track {
    background-color: transparent;
}

/* ==================== 动画 ==================== */
.px-search-enter-active,
.px-search-leave-active,
.plain-search-enter-active,
.plain-search-leave-active {
    transition: opacity 0.2s ease;
}

.px-search-enter-from,
.px-search-leave-to,
.plain-search-enter-from,
.plain-search-leave-to {
    opacity: 0;
}

.px-search-enter-active .as-panel {
    transition: transform 0.24s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.18s ease;
}

.px-search-enter-from .as-panel {
    transform: translateY(-24px) scale(0.92);
    opacity: 0;
}

.px-search-leave-active .as-panel {
    transition: transform 0.15s ease, opacity 0.15s ease;
}

.px-search-leave-to .as-panel {
    transform: translateY(-16px) scale(0.96);
    opacity: 0;
}

.plain-search-enter-active .as-panel {
    transition: transform 0.18s ease;
}

.plain-search-enter-from .as-panel {
    transform: translateY(-8px);
}

/* ==================== 响应式 ==================== */
@media (max-width: 720px) {
    .as-trigger--pixel span {
        display: none;
    }

    .as-overlay {
        padding-top: 8vh;
    }

    .pixel .as-results {
        padding: 12px;
    }
}
</style>
