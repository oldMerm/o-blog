<script setup lang="ts">
import { httpInstance, type Response } from '@/utils/http';
import { ref, watch } from 'vue';
import Dialog from '@/utils/dia/Dialog.vue';
import { goToArticle } from '@/views/public/Article';

// --- 状态映射 (用于显示中文) ---
const statusIdMap: Record<number, string> = {
    1: 'checked',
    3: 'published',
    4: 'offline'
};
const statusMap: Record<string, string> = {
    checked: '待审核',
    published: '已发布',
    offline: '已下架'
};
const typeMap: Record<number, string> = {
    0: '公告',
    1: '技术相关',
    2: '日常吐槽',
    3: '其它'
}

// --- 事件处理 (占位) ---
const handleEdit = async (id: string) => {
    try {
        const res = await httpInstance.post<any, Response>(`/article/status/${id}`);
        if(res.code !== 200){
            alert(`系统错误:${res.message}`);
        }else{
            location.reload();
        }
    } catch (error) {
        alert(error);
    }
};

// 分页逻辑
const currentPage = ref(1);
const pageSize = ref(10);
const pages = ref(1);
const total = ref(0);
interface ArticlePageVO{
    id: string;
    articleName: string;
    articleWriter: string;
    articleType: number;
    articleStatus: any;
    createdAt: string;
    visable: boolean;
}
const articleList = ref<ArticlePageVO[]>([]);
watch(
    currentPage, 
    async (newVal) => {
        try {
            const res = await httpInstance.get<any, Response>('/article/page', {
                params: {
                    current: newVal,
                    size: pageSize.value
                }
            });
            if(res.code !== 200){
                alert(`系统错误${res.message}`);
                return;
            }
            articleList.value = res.data.records;
            articleList.value.map((item): ArticlePageVO => {
                item.articleStatus = statusIdMap[item.articleStatus];
                return item;
            });
            pages.value = res.data.pages;
            total.value = res.data.total;
        } catch (error) {
            alert(`出现错误${error}`);
        }
        
    }, {immediate: true});
</script>

<template>
    <div class="article-manage-container">

        <!-- 1. 顶部操作栏 -->
        <div class="toolbar">
            <div class="title-section">
                <h3>文章管理</h3>
                <span class="subtitle">共 {{ total }} 篇</span>
            </div>
            <div class="actions">
                <!-- 预留搜索框位置，保持布局平衡，暂不实现功能 -->
                <button class="btn-primary">
                    <span>+ 发布新文章</span>
                </button>
            </div>
        </div>

        <!-- 2. 表格区域 -->
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 80px;">ID</th>
                        <th style="width: 30%;">文章标题</th>
                        <th>作者</th>
                        <th>文章状态</th>
                        <th>文章类型</th>
                        <th>发布时间</th>
                        <th style="text-align: right;">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in articleList">
                        <td class="col-id">{{ item.id.slice(0, 12) }}...</td>
                        <td class="col-title">
                            <span class="title-text">{{ item.articleName }}</span>
                        </td>
                        <td>
                            <div class="author-info">
                                <span class="avatar-placeholder">{{ item.articleWriter[0] }}</span>
                                {{ item.articleWriter }}
                            </div>
                        </td>
                        <td>
                            <!-- 状态标签：根据状态动态改变颜色 -->
                            <span class="status-badge" :class="item.articleStatus">
                                {{ statusMap[item.articleStatus] }}
                            </span>
                        </td>
                        <td>
                            <span>{{ typeMap[item.articleType] }}</span>
                        </td>
                        <td class="col-date">{{ item.createdAt }}</td>
                        <td class="col-actions">
                            <button class="btn-text" @click="item.visable = true" :disabled="item.articleStatus === 'offline'">
                                {{ statusMap[item.articleStatus] }}
                            </button>
                            <Dialog v-model="item.visable " title="确认修改" 
                            content="状态（未发布或已发布）修改后将不能撤回，确定执行吗？" 
                            @confirm="handleEdit(item.id)" />
                            <span class="divider">|</span>
                            <button class="btn-text" @click="goToArticle(item.id, true)">
                                查看详细
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- 3. 分页栏 (UI展示，逻辑由你实现) -->
        <div class="pagination-bar">
            <button class="page-btn" :disabled="currentPage === 1">上一页</button>
            <div class="page-numbers">
                <span class="page-num">1</span>/
                <span class="page-num">{{ pages }}</span>
            </div>
            <button class="page-btn" :disabled="currentPage >= 10">下一页</button>
        </div>

    </div>
</template>

<style scoped>
/* 复用之前的颜色变量概念 */
.article-manage-container {
    background-color: #ffffff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    min-height: 100%;
    display: flex;
    flex-direction: column;
}

/* --- 顶部栏 --- */
.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.title-section h3 {
    margin: 0;
    font-size: 18px;
    color: #1f2937;
    display: inline-block;
    margin-right: 12px;
}

.subtitle {
    font-size: 13px;
    color: #9ca3af;
}

.btn-primary {
    background-color: #3b82f6;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-primary:hover {
    background-color: #2563eb;
}

/* --- 表格样式 --- */
.table-wrapper {
    flex: 1;
    /* 撑满剩余空间 */
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
}

.data-table th {
    text-align: left;
    padding: 12px 16px;
    background-color: #f9fafb;
    color: #6b7280;
    font-weight: 600;
    border-bottom: 1px solid #e5e7eb;
}

.data-table td {
    padding: 16px;
    border-bottom: 1px solid #f3f4f6;
    color: #374151;
    vertical-align: middle;
}

.data-table tr:hover td {
    background-color: #f9fafb;
}

/* 列样式细节 */
.col-id {
    color: #9ca3af;
    font-family: monospace;
}

.title-text {
    font-weight: 500;
    color: #1f2937;
}

.author-info {
    display: flex;
    align-items: center;
    gap: 8px;
}

.avatar-placeholder {
    width: 24px;
    height: 24px;
    background-color: #dbeafe;
    /* 浅蓝背景 */
    color: #1e40af;
    /* 深蓝文字 */
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: bold;
}

.col-date {
    color: #6b7280;
    font-size: 13px;
}

/* 状态标签样式 */
.status-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

.status-badge.published {
    background-color: #d1fae5;
    /* 浅绿背景 */
    color: #059669;
    /* 深绿文字 */
}

.status-badge.draft {
    background-color: #f3f4f6;
    /* 浅灰背景 */
    color: #6b7280;
    /* 深灰文字 */
}

.status-badge.offline {
    background-color: #fee2e2;
    /* 浅红背景 */
    color: #dc2626;
    /* 深红文字 */
}

/* 操作按钮 */
.col-actions {
    text-align: right;
}

.btn-text {
    background: none;
    border: none;
    color: #3b82f6;
    cursor: pointer;
    font-size: 13px;
    padding: 4px 8px;
}

.btn-text:hover {
    text-decoration: underline;
}

.divider {
    color: #e5e7eb;
    font-size: 12px;
}

/* --- 分页栏样式 --- */
.pagination-bar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;
}

.page-btn {
    background-color: white;
    border: 1px solid #e5e7eb;
    padding: 6px 12px;
    border-radius: 4px;
    color: #6b7280;
    cursor: pointer;
    font-size: 13px;
    margin: 0 4px;
}

.page-btn:hover:not(:disabled) {
    border-color: #3b82f6;
    color: #3b82f6;
}

.page-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.page-numbers {
    display: flex;
    align-items: center;
    margin: 0 8px;
}

.page-num {
    padding: 6px 12px;
    margin: 0 2px;
    cursor: pointer;
    border-radius: 4px;
    font-size: 13px;
    color: #6b7280;
}

.page-num:hover {
    background-color: #f3f4f6;
}

.dots {
    color: #9ca3af;
    margin: 0 4px;
}
</style>
