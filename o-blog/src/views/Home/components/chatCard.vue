<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from 'vue';
import JumpingText from '@/utils/JumpingText.vue'; // 假设你的工具组件路径
import { API_BASE_URL, httpInstance, type Response } from '@/utils/http';
import { MarkdownRenderer } from '@/utils/mdRender';
import Dialog from '@/utils/dia/Dialog.vue';


// --- 弹窗状态管理 ---
const isChatOpen = ref(false);
const renderer = new MarkdownRenderer();
const defaultContent = ref<string>("你好，我是老鱼人的专属智能体，请问有什么能帮助你吗？注意先看一下使用须知(左上角第三个图标)！！！");

const renderDefaultContent = () => {
    if (sessionHistory.value.length === 0) {
        defaultContent.value = "点击左侧第一个按钮开始会话吧！";
    } else {
        defaultContent.value = "你好，我是老鱼人的专属智能体，请问有什么能帮助你吗？注意先看一下使用须知(左上角第三个图标)！！！";
    }
}

// 2. 右侧对话内容假数据
interface Session {
    id: number;
    userId: string;
    sessionId: string;
    sessionDecr: string;
}
interface Message {
    sessionId: string;
    role: 'human' | 'ai';
    content: string;
}

const sessionHistory = ref<Session[]>([]);

const messages = ref<Message[]>([]);

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
const gethistorySession = async () => {
    try {
        const res = await httpInstance.get<any, Response>("/agent");
        sessionHistory.value = res.data;
        return res.code;
    } catch (error) {
        alert(`系统错误:${error}`);
    }
}

onMounted(async () => {
    const code = await gethistorySession();
    if (code !== 200) return;
    selectedId.value = sessionHistory.value[0]?.sessionId;
    selectedDecr.value = sessionHistory.value[0]?.sessionDecr;
    renderDefaultContent();
})

watch(selectedId, async (newVal) => {
    if (newVal === undefined) return;
    const formData = new FormData();
    formData.append('sessionId', newVal);
    const res = await httpInstance<any, Response>(`/agent/chatInfo?sessionId=${newVal}`);
    if (res.code !== 200) {
        alert(`服务错误：${res.message}`);
    }
    messages.value = res.data;
}, { immediate: true })

// 新建会话
const createSession = async () => {
    try {
        const res = await httpInstance.post<any, Response>("/agent/session");
        if (res.code !== 200) {
            alert(`服务出错:${res.message}`);
        }
        const newSession: Session = res.data;
        sessionHistory.value.unshift(newSession);
        selectSession(newSession.sessionId, newSession.sessionDecr);
        renderDefaultContent();
    } catch (error) {
        alert(`系统错误:${error}`);
    }
}

const flag = ref<boolean>(false);
// 发送消息
const sendMessage = async () => {
    if (!inputText.value.trim() || selectedId.value === undefined || flag.value === true) return;

    const humanMessage: Message = {
        role: 'human',
        sessionId: selectedId.value,
        content: inputText.value
    }
    const aiMessage: Message = {
        role: 'ai',
        sessionId: selectedId.value,
        content: "思考中..."
    };

    // 添加用户消息
    messages.value.push(humanMessage);
    inputText.value = '';
    scrollToBottom();
    messages.value.push(aiMessage);

    try {
        await streamChat(humanMessage);
    } catch (error) {
        alert(`系统错误:${error}`);
    } finally {
        flag.value = false;
    }
};

const simpleChat = async (humanMessage: Message) => {
    const res = await httpInstance.post<any, Response>("/agent/chat", humanMessage, { timeout: 60000 });
    if (res.code !== 200) {
        alert(`服务错误:${res.message}`);
        return;
    }

    const lastIndex = messages.value.length - 1;
    const lastMessage = messages.value[lastIndex];

    if (lastMessage) {
        lastMessage.content = res.data.content;
    }
}

interface StreamChunk {
    type: string;
    content: string;
    node: string;
    done: boolean
}

let eventSource: any = null;
const streamChat = async (humanMessage: Message) => {
    const token = localStorage.getItem('token');
    if (token === null) {
        alert('未认证')
        return;
    }
    const params = new URLSearchParams({
        sessionId: humanMessage.sessionId,
        message: humanMessage.content,
        token: token
    })

    if (eventSource) {
        eventSource.close();
        eventSource = null
    }

    const lastIndex = messages.value.length - 1;
    const lastMessage = messages.value[lastIndex];

    console.log(humanMessage.content);

    eventSource = new EventSource(`${API_BASE_URL}/agent/chat/stream?${params}`);

    if (lastMessage) {
        lastMessage.content = '';
    }

    eventSource.onmessage = (event: any) => {
        const chunk: StreamChunk = JSON.parse(event.data);
        if (chunk.done === true) {
            eventSource.close();
        } else if (chunk.node === 'tools') {
            // 处理tools渲染逻辑
            if (lastMessage) {
                lastMessage.content += `<br><br>正在调用工具....<br><br>`
            }

        } else if (chunk.node === 'model'){
            if (lastMessage) {
                lastMessage.content += chunk.content;
            }
        } 
    }

    eventSource.onerror = (error: any) => {
        alert(`流传输错误:${error}`);
        eventSource.close();
        return;
    }

}

// 自动滚动到最新消息
const scrollToBottom = async () => {
    await nextTick();
    if (chatScrollRef.value) {
        chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight;
    }
};

// 删除所有会话
const confirmDelete = ref<boolean>(false);
const deleteAll = async () => {
    try {
        const res = await httpInstance.delete<any, Response>("/agent/all");
        if (res.code !== 200) {
            alert(`服务错误:${res.message}`);
        }
        messages.value = sessionHistory.value = [];
        selectedId.value = undefined;
        selectedDecr.value = '';
        renderDefaultContent();
    } catch (error) {
        alert(`系统错误:${error}`);
    }
}
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
                            <img class="topbar-item" src="../../../static/添加.svg" @click="createSession">
                            <img class="topbar-item" src="../../../static/删除.svg" @click="confirmDelete = true">
                            <Dialog v-model="confirmDelete" title="确认删除" content="确认删除所有会话？数据将不可恢复！"
                                @confirm="deleteAll" />
                            <img class="topbar-item" src="../../../static/公告.svg" title="使用须知">
                        </div>
                        <div v-for="item in sessionHistory" class="history-item"
                            :class="{ active: selectedId === item.sessionId }"
                            @click="selectSession(item.sessionId, item.sessionDecr)">
                            {{ item.sessionDecr }}
                        </div>
                    </div>

                    <!-- 右侧：对话内容与输入框 -->
                    <div class="chat-main">

                        <!-- 聊天记录展示区 -->
                        <div class="chat-messages" ref="chatScrollRef">
                            <h5 style="text-align: center; text-decoration: underline;">{{ selectedDecr }}</h5>
                            <div class="message-wrapper ai">
                                <div class="message-bubble">{{ defaultContent }}</div>
                            </div>
                            <div v-for="msg in messages" :class="['message-wrapper', msg.role]">
                                <div class="message-bubble" v-html="renderer.render(msg.content.replace(/\\n/g, '\n'))">
                                </div>
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
    scrollbar-gutter: stable;
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

:deep(ul),
:deep(ol) {
    margin: 15px 0px;
    margin-left: 20px;
}

:deep(h1) {
    margin-bottom: 12px;
}

:deep(h2) {
    margin-top: 25px;
    margin-bottom: 8px;
}

:deep(h3) {
    margin-bottom: 6px;
}

:deep(h4) {
    margin-bottom: 6px;
}

:deep(h5) {
    margin-bottom: 6px;
}

:deep(pre) {
    margin: 8px 0px;
}
</style>
