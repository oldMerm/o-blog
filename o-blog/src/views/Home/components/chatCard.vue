<script setup lang="ts">
import { ref } from 'vue'
import AgentChatDialog from '@/components/agent-chat/AgentChatDialog.vue'
import Toast from '@/utils/toast/Toast.vue'

const showDialog = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const toastMsg = ref('')

const openChat = () => {
  if (!localStorage.getItem('token')) {
    toastMsg.value = '请先登录后再使用 AI 助手'
    toastRef.value?.show()
    return
  }
  showDialog.value = true
}
</script>

<template>
  <div class="cc">
    <div class="ai-title">鱼人博客对话助手</div>
    <p class="ai-desc">基于博客系统，回答你的问题</p>
    <div class="ai-btn" @click="openChat">开始对话</div>

    <AgentChatDialog v-model="showDialog" />
    <Toast ref="toastRef" :message="toastMsg" type="error" />
  </div>
</template>

<style scoped>
.cc {
    width: 100%;
    margin-top: 10px;
    height: 16rem;
    border: 1px solid #dadee5;
    border-radius: 12px;
    font-size: 1rem;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    gap: 8px;
    padding: 16px 20px;
    line-height: normal;
    overflow: hidden;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.ai-title {
    color: #303133;
    font-size: 1.2rem;
    font-weight: 600;
    letter-spacing: 1px;
}

.ai-desc {
    margin: 0;
    color: #909399;
    font-size: 0.75rem;
    line-height: 1.5;
    text-align: center;
}

.ai-btn {
    margin-top: 4px;
    padding: 7px 18px;
    border: none;
    color: #000;
    font-size: 1.1rem;
    font-weight: 500;
    white-space: nowrap;
    cursor: pointer;
    transition: background 0.2s ease, transform 0.1s ease;
    text-decoration: underline;
}

.ai-btn:hover {
    color: rgb(64, 158, 255);
}

</style>
