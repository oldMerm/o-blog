<script setup lang="ts">
import { onMounted, ref } from 'vue';
import router from '@/router';
import { httpInstance, type Response } from '@/utils/http';
import { goToArticle } from '@/views/public/Article';

const time = new Date();
const day = `${time.getFullYear()}年${time.getMonth() + 1}月${time.getDate()}日`;

const showbox = ref(false)

onMounted(() => {
    const token = localStorage.getItem('token');
    if (token) {
        renderUsrInfo();
    }
});
interface UserInfo {
    username: string;
    article: number;
    like: number;
    attrURL: string;
}
const username = ref();
const article = ref();
const like = ref();
const url = ref("https://picsum.photos/200");
const renderUsrInfo = async () => {
    try {
        const res = await httpInstance.get<any, Response>('/usr/info');
        if (res.code !== 200) {
            return;
        }
        const data: UserInfo = res.data;
        username.value = data.username;
        article.value = data.article;
        like.value = data.like;
        if (data.attrURL !== null) {
            url.value = data.attrURL;
        }
        showbox.value = !showbox.value;

    } catch (error) {
        const data = await httpInstance.get<any, Response>('/auth/refresh');
        if (data.code !== 200) {
            return;
        }
        const { token, refreshToken, timeout } = <any>data.data;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('timeout', timeout);
        location.reload();
    }
}

// 打开图片管理器
const fileRef: any = ref(null);
function openFile() {
    if (fileRef.value != null) {
        fileRef.value.click();
    }
}

// 上传图片
function handleSelect(e: any) {
    const file = e.target.files[0];
    if (!file) return;
    url.value = URL.createObjectURL(file);
    const formdata = new FormData();
    formdata.append("img", file);
    httpInstance.put<any, Response>('/oss/upload', formdata);
}

const loginPage = ref(() => {
    router.push({ name: 'login' })
})

const goToManagePage = () => {
    showHistory.value = false;
    router.push({ name: 'manage' });
    // const routeUrl = router.resolve({ name: 'manage' });
    // window.open(routeUrl.href, '_blank');
}

/* 浏览历史 */
interface ArticleHistory {
    userId: string;
    articleId: string;
    articleName: string;
    updatedAt: string;
}
const showHistory = ref(false);
const articleHistoryList = ref<ArticleHistory[]>([]);
const hasHistory = ref(false);
onMounted(async() => {
    if(localStorage.getItem('token') === null) return;
    const res = await httpInstance.get<any, Response>("/article/history");
    if(res.code !== 200){
        return;
    }
    articleHistoryList.value = res.data;
    hasHistory.value = articleHistoryList.value.length !== 0;
})

</script>

<template>
    <div class="uc">
        <!-- 1. 最上方展示时间 -->
        <div class="header">
            <span class="time">{{ day }}</span>
        </div>

        <!-- 虚线分割在 CSS 中实现 (border-bottom) -->

        <!-- 2. 下方主体区域 -->
        <div class="content-body" v-show="showbox">

            <!-- 左边区域：占总宽度的 3/4 -->
            <div class="left-section">
                <!-- 圆形头像 -->
                <div class="avatar-wrapper" @click="openFile" title="点击修改头像">
                    <img :src="url" alt="Avatar" class="avatar" />
                    <input ref="fileRef" type="file" style="display: none;" @change="handleSelect">
                </div>

                <!-- 右边展示数据：用户名、文章数、点赞数 -->
                <div class="user-info">
                    <div class="info-item username">用户名：{{ username }}</div>
                    <div class="info-item">文章数：{{ article }}</div>
                    <div class="info-item">点赞数：{{ like }}</div>
                </div>
            </div>

            <!-- 实线分割在 CSS 中实现 (border-right) -->

            <!-- 右边区域：占总宽度的 1/4 -->
            <div class="right-section">
                <!-- 三个功能响应块 -->
                <div class="action-btn" @click="goToManagePage">用户设置</div>
                <div class="action-btn" @click="goToManagePage">内容创作</div>
                <div class="action-btn" @click="showHistory = true">浏览历史</div>
                <!-- 历史浏览组件 -->
                <div v-if="showHistory" class="history-popover">
                    <div class="popover-title">
                        文章浏览历史
                        <span class="popover-close" @click="showHistory = false">&times;</span>
                    </div>
                    <div class="popover-content">
                        <div class="history-item" v-if="hasHistory" v-for="item in articleHistoryList" @click="goToArticle(item.articleId)">
                            <span>{{ item.articleName }}</span>
                            <p>浏览时间:{{ item.updatedAt }}</p>
                        </div>
                        <div v-if="!hasHistory" style="text-align: center; margin-top: 20px; ">
                            去浏览几篇文章吧！
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <div class="content-body not" v-show="!showbox">
            <button class="login-r" @click="loginPage">
                请先登录!
            </button>
        </div>
    </div>
</template>

<style scoped>
.uc {
    margin: 10px 10px;
    width: 100%;
    /* 保持你原有的宽度设置 */
    /* height: 10rem;  建议去掉固定高度，或者设为 min-height，防止内容溢出 */
    min-height: 10rem;
    background-color: rgb(238, 255, 255);
    /* 稍微调淡一点背景色，aquamarine有点刺眼 */
    border-radius: 8px;
    /* 加一点圆角好看些 */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    /* 防止子元素溢出圆角 */
}

/* --- 顶部区域 --- */
.header {
    padding: 8px 12px;
    font-size: 0.9rem;
    color: #2c2c2c;
    /* 核心需求：和长一样长的虚线分割 */
    border-bottom: 2px solid #999;
    text-align: left;
    /* 一般时间放在右边好看点，你可以改成 left */
}

/* --- 下方主体 Flex 容器 --- */
.content-body {
    flex: 1;
    /* 占满剩余高度 */
    display: flex;
    /* 开启 flex 布局 */
}

.not {
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
}

.login-r {
    font-size: larger;
    border: none;
    outline: none;
    background-color: transparent;
    text-decoration: underline;
}

.login-r:hover {
    color: #409eff;
    cursor: pointer;
}

/* --- 左边区域 (3/4) --- */
.left-section {
    flex: 3;
    /* 占比 3 份 */
    display: flex;
    /* 内部也用 flex 来排头像和文字 */
    align-items: center;
    /* 垂直居中 */
    padding: 10px;
    /* 核心需求：右边实线分割 */
    border-right: 2px solid #ccc;
}

/* 头像区域 */
.avatar-wrapper {
    width: 60px;
    height: 60px;
    margin-right: 15px;
    flex-shrink: 0;
    /* 防止被挤压 */
    cursor: pointer;
}

.avatar {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    /* 圆形 */
    object-fit: cover;
    border: 2px solid #fff;
}

/* 用户信息区域 */
.user-info {
    display: flex;
    flex-direction: column;
    /* 由上到下排列 */
    justify-content: center;
    font-size: 0.85rem;
    color: #333;
    gap: 4px;
    /* 行间距 */
}

.username {
    font-weight: bold;
    font-size: 1rem;
    color: #000;
}

/* --- 右边区域 (1/4) --- */
.right-section {
    flex: 1;
    /* 占比 1 份 */
    display: flex;
    flex-direction: column;
    /* 垂直排列三个按钮 */
    justify-content: space-around;
    /* 均匀分布 */
    align-items: center;
}

.action-btn {
    color: black;
    font-size: 1rem;
    cursor: pointer;
    width: 100%;
    /* 按钮宽度 */
    height: 33.3%;
    line-height: 35px;
    text-align: center;
    font-weight: 400;
    transition: all 0.2s ease;
    border-bottom: 1px solid #ccc;
}

.action-btn:hover {
    color: #409eff;
}

/* 浏览历史 */
.history-popover {
    position: fixed;
    top: 40%;
    right: 0;
    width: 250px;
    /* 适中的宽度 */
    max-height: 300px;
    min-height: 100px;
    background-color: #ffffff;
    border: 1px solid #d9ecff;
    /* 浅蓝边框 */
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-radius: 4px;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    text-align: left;
    /* 确保内部文字左对齐 */
    font-size: 13px;
    line-height: 1.5;

}

.popover-title {
    padding: 8px 12px;
    background-color: #ecf5ff;
    /* 极浅的蓝色背景 */
    color: #409EFF;
    font-weight: bold;
    border-bottom: 1px solid #d9ecff;
}

.popover-content {
    overflow-y: auto;
    /* 内容过多可滚动 */
    padding: 0;
}

/* 极简滚动条样式 */
.popover-content::-webkit-scrollbar {
    width: 3px;
}

.popover-content::-webkit-scrollbar-thumb {
    background-color: rgb(211, 211, 211);
    /* 浅灰滑块 */
    border-radius: 2px;
}

.popover-content::-webkit-scrollbar-track {
    background-color: transparent;
}

/* 单条数据样式 */
.history-item {
    padding: 5px 6px;
    border-bottom: 1px solid #f0f0f0;
    transition: background-color 0.2s;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.history-item:hover {
    color: #409eff;
}

.history-item:last-child {
    border-bottom: none;
}

.history-item:hover {
    background-color: #fafafa;
}

.popover-close {
    position: absolute;
    right: 12px;
    top: 10px;
    font-size: 16px;
    /* 叉叉大一点 */
    color: #999;
    cursor: pointer;
    line-height: 1;
    transition: color 0.2s;
    z-index: 10;
}

.popover-close:hover {
    color: #409eff;
    /* 悬停变为浅蓝色 */
}
</style>
