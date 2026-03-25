<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';
import JumpingText from '@/utils/JumpingText.vue'; // 假设你的工具组件路径
import { httpInstance, type Response } from '@/utils/http';

// --- 弹窗状态管理 ---
const isChatOpen = ref(false);

// 2. 右侧对话内容假数据
interface Session {
    id: number;
    userId: string;
    sessionId: string;
    sessionDecr: string;
}
interface Message {
    session_id: string;
    role: 'human' | 'ai';
    content: string;
}

const sessionHistory = ref<Session[]>([]);

const messages = ref<Message[]>([
    { session_id: '111', role: 'ai', content: '你好！我是老鱼人智能体，有什么我可以帮你的吗？' },
    { session_id: '111', role: 'human', content: '我想了解一下这篇博客的核心内容。' },
    { session_id: '111', role: 'ai', content: '好的，这篇博客主要介绍了如何使用 Vue3 和 TypeScript 构建一个内嵌的 AI 聊天界面，包括布局设计、状态管理和样式美化等内容。' }
]);

const inputText = ref('');
const chatScrollRef = ref<HTMLElement | null>(null);

// --- 交互逻辑 ---
// 打开弹窗
const openChat = async () => {
    const flag = await isLoginUser();
    if (!flag) {
        alert("未登录或ai服务错误");
        return;
    }
    gethistroySession();
    selectedId.value = sessionHistory.value[0]?.sessionId;
    selectedDecr.value = sessionHistory.value[0]?.sessionDecr;
    scrollToBottom();
    isChatOpen.value = true;
};

const isLoginUser = async () => {
    try {
        const res1 = await httpInstance.get<any, Response>("/usr/info");
        const res2 = await httpInstance.get<any, Response>("/agent/health");

        if (res1.code === 200 && res2.code === 200) {
            return true;
        } else {
            return false;
        }
    } catch (error) {
        return false;
    }
}

// 渲染历史会话
const selectedId = ref<string>();
const selectedDecr = ref<string>();

const selectSession = (sessionId: string, decr: string) => {
    selectedId.value = sessionId;
    selectedDecr.value = decr;
}

const gethistroySession = async () => {
    const res = await httpInstance.get<any, Response>("/agent");
    sessionHistory.value = res.data;
}

// // 获取当前选中的详情
// const selectedSession = computed(() => {
//   return sessionHistory.value.find(item => item.id === selectedId.value)
// })
// 加载会话
watch(selectedId, async (newVal) => {
    try {
        if(newVal === undefined) return;
        const formData = new FormData();
        formData.append('sessionId', newVal);
        const res = await httpInstance<any, Response>(`/agent/chatInfo?sessionId=${newVal}`);
        if(res.code !== 200){
            alert(`服务错误：${res.message}`);
        }
        console.log(res.data);
    } catch (error) {
        alert(`系统错误:${error}`);
    }
}, {immediate: true})

// 新建会话
const createSession = async () => {

}

// 发送消息
const sendMessage = () => {
    if (!inputText.value.trim()) return;

    // 添加用户消息
    messages.value.push({
        role: 'human',
        session_id: '111',
        content: inputText.value
    });

    const userText = inputText.value;
    inputText.value = ''; // 清空输入框
    scrollToBottom();

    // 模拟 AI 回复 (延迟1秒)
    setTimeout(() => {
        messages.value.push({
            session_id: '111',
            role: 'ai',
            content: `你刚才说：“${userText}”。这是一个很好的问题，但我目前只是一个模拟的前端界面哦！`
        });
        scrollToBottom();
    }, 1000);
};

// 自动滚动到最新消息
const scrollToBottom = async () => {
    await nextTick();
    if (chatScrollRef.value) {
        chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight;
    }
};
</script>

<template>
    <!-- 原有页面触发区域 -->
    <div class="cc">
        <div class="title">老鱼人的专属智能体</div>
        <JumpingText text="分析本站文章内容" :delay="0.1" duration="2.5s"
            style="color: black; font-size: 1rem; margin-top: 30px;" />
        <JumpingText text="粗略实用的介绍文章" :delay="0.1" duration="2.5s"
            style="color: black; font-size: 1rem; margin-top: 20px;" />
        <JumpingText text="更多功能，请看公告" :delay="0.1" duration="2.5s"
            style="color: black; font-size: 1rem; margin-top: 20px;" />
        <!-- 绑定点击事件 -->
        <div class="btn" @click="openChat">发起会话</div>
    </div>

    <!-- AI 聊天弹窗 (使用 Teleport 挂载到 body 防止样式被父级截断) -->
    <Teleport to="body">
        <!-- 遮罩层，点击空白处关闭 -->
        <div v-if="isChatOpen" class="chat-overlay" @click.self="isChatOpen = false">
            <!-- 弹窗主体 -->
            <div class="chat-modal">

                <!-- 顶部 Header -->
                <div class="chat-header">
                    <span class="header-title">老鱼人智能体</span>
                    <span class="header-close" @click="isChatOpen = false">&times;</span>
                </div>

                <!-- 身体部分：左右分栏 -->
                <div class="chat-body">
                    <!-- 左侧：对话历史 -->
                    <div class="chat-history">
                        <div class="chat-history-topbar">
                            <img class="topbar-item" src="../../../static/添加.svg" title="新建会话" @click="createSession">
                            <img class="topbar-item" src="../../../static/删除.svg" title="删除所有会话">
                            <img class="topbar-item" src="../../../static/公告.svg" title="须知">
                        </div>
                        <div v-for="item in sessionHistory" 
                            class="history-item"
                            :class="{ active: selectedId === item.sessionId }"
                            @click="selectSession(item.sessionId, item.sessionDecr)">
                            {{ item.sessionDecr }}
                        </div>
                    </div>

                    <!-- 右侧：对话内容与输入框 -->
                    <div class="chat-main">

                        <!-- 聊天记录展示区 -->
                        <div class="chat-messages" ref="chatScrollRef">
                            <h5 style="text-align: center;">{{ selectedDecr }}</h5>
                            <div v-for="msg in messages" :class="['message-wrapper', msg.role]">
                                <div class="message-bubble">{{ msg.content }}</div>
                            </div>
                        </div>

                        <!-- 底部输入处 (相对右侧居中) -->
                        <div class="chat-input-area">
                            <div class="input-container">
                                <input v-model="inputText" type="text" placeholder="请输入你的问题吧..."
                                    @keyup.enter="sendMessage" />
                                <button @click="sendMessage">发送</button>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>

<style scoped>
/* ========== 原有样式 ========== */
.cc {
    width: 100%;
    margin-top: 10px;
    height: 16rem;
    border: 1px solid #dadee5;
    border-radius: 12px;
    font-size: 1rem;
    display: flex;
    align-items: center;
    flex-direction: column;
    padding: 10px 20px;
}

.title {
    margin-top: 10px;
    font-weight: 300;
}

.btn {
    margin-top: 25px;
    font-weight: 400;
    text-decoration: underline;
    cursor: pointer;
    transition: all 0.1s ease-in-out;
    font-size: 1.1rem;
}

.btn:hover {
    color: rgb(19, 160, 216);
}

/* ========== 弹窗新增样式 (灰白黑简洁风) ========== */

/* 全屏遮罩层：垂直水平居中 */
.chat-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.4);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

/* 弹窗主体：高70vh，宽60vw(为了左右分栏不拥挤，比50vw略宽) */
.chat-modal {
    width: 60vw;
    height: 70vh;
    min-width: 700px;
    min-height: 500px;
    background-color: #ffffff;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    font-family: sans-serif;
    animation: fadenum 1.5s 1;
}

@keyframes fadenum {
    0% {
        opacity: 0;
    }

    100% {
        opacity: 1;
    }
}

/* --- 头部布局 --- */
.chat-header {
    height: 55px;
    background-color: #f8f8f8;
    border-bottom: 1px solid #e5e5e5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
}

.header-title {
    font-weight: bold;
    color: #111111;
    font-size: 1.1rem;
}

.header-close {
    font-size: 1.5rem;
    color: #666666;
    cursor: pointer;
    line-height: 1;
    transition: color 0.2s;
}

.header-close:hover {
    color: #000000;
}

/* --- 主体部分：Flex 左右分栏 --- */
.chat-body {
    flex: 1;
    display: flex;
    overflow: hidden;
}

/* 左侧：对话历史 */
.chat-history {
    width: 25%;
    min-width: 180px;
    background-color: #fafafa;
    border-right: 1px solid #e5e5e5;
    padding: 10px 10px;
    overflow-y: auto;
}

.chat-history-topbar {
    padding: 12px 15px;
    background-color: #ffffff;
    border: 2px solid #868686;
    border-radius: 3px;
    font-size: 0.9rem;
    color: #333333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-around;

}


.topbar-item {
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.5s ease;
}

.topbar-item:nth-child(1):hover {
    background-color: skyblue;
}

.topbar-item:nth-child(2):hover {
    background-color: red;
}

.topbar-item:nth-child(3):hover {
    background-color: gold;
}

.history-item {
    padding: 12px 15px;
    background-color: #ffffff;
    border: 1px solid #e5e5e5;
    border-radius: 6px;
    font-size: 0.9rem;
    color: #333333;
    cursor: pointer;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: background-color 0.3s;
}

.history-item:hover {
    background-color: #f1f3f4;
}

.active {
    background-color: #eceeef;
}

/* 右侧：对话主区 */
.chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: #ffffff;
}

/* 消息展示区 */
.chat-messages {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

/* 极简滚动条样式 */
.chat-messages::-webkit-scrollbar {
    width: 5px;
}

.chat-messages::-webkit-scrollbar-thumb {
    background-color: rgb(211, 211, 211);
    /* 浅灰滑块 */
    border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-track {
    background-color: transparent;
}

.message-wrapper {
    display: flex;
    width: 100%;
}

/* 用户消息靠右 */
.message-wrapper.human {
    justify-content: flex-end;
}

/* AI消息靠左 */
.message-wrapper.ai {
    justify-content: flex-start;
}

.message-bubble {
    max-width: 75%;
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 0.95rem;
    line-height: 1.5;
    word-wrap: break-word;
}

/* 黑白灰配色方案 */
.message-wrapper.user .message-bubble {
    background-color: #222222;
    color: #ffffff;
    border-bottom-right-radius: 2px;
}

.message-wrapper.ai .message-bubble {
    background-color: #f2f2f2;
    color: #222222;
    border-bottom-left-radius: 2px;
}

/* 底部发送区 */
.chat-input-area {
    padding: 15px 20px;
    border-top: 1px solid #e5e5e5;
    background-color: #fafafa;
    display: flex;
    justify-content: center;
    /* 居中对齐 */
}

/* 控制输入框在右侧布局中的宽度占比 */
.input-container {
    width: 85%;
    display: flex;
    gap: 10px;
}

.input-container input {
    flex: 1;
    padding: 10px 15px;
    border: 1px solid #cccccc;
    border-radius: 20px;
    outline: none;
    font-size: 0.95rem;
    transition: border-color 0.2s;
}

.input-container input:focus {
    border-color: #888888;
}

.input-container button {
    padding: 0 22px;
    background-color: #222222;
    color: #ffffff;
    border: none;
    border-radius: 20px;
    cursor: pointer;
    font-size: 0.95rem;
    transition: background-color 0.2s;
}

.input-container button:hover {
    background-color: #444444;
}
</style>
