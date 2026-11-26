<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { httpInstance, type Response } from '@/utils/http'

// 系统状态：1-正常运行，2-维护中
const status = ref<number>(1)
const loading = ref(false)

const isMaintenance = computed(() => status.value === 2)
const statusText = computed(() => (isMaintenance.value ? '维护中' : '正常运行'))

const fetchStatus = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/counter/system/status')
    if (res.code === 200) {
      status.value = Number(res.data)
    }
  } catch (error) {
    console.error('获取系统状态失败:', error)
  }
}

const toggleStatus = async () => {
  const nextStatus = isMaintenance.value ? 1 : 2

  if (nextStatus === 2 && !window.confirm('确定要进入维护模式吗？普通用户将被跳转到维护页面。')) {
    return
  }

  loading.value = true
  try {
    const res = await httpInstance.post<any, Response>('/admin/usr/system', null, {
      params: { status: nextStatus },
    })

    if (res.code !== 200) {
      alert(res.message || '修改系统状态失败')
      return
    }

    status.value = nextStatus
  } catch (error) {
    alert(error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchStatus)
</script>

<template>
  <div class="system-status">
    <span class="status-dot" :class="{ maintenance: isMaintenance }"></span>
    <span class="status-text">{{ statusText }}</span>
    <button
      class="status-btn"
      :class="{ maintenance: isMaintenance }"
      :disabled="loading"
      @click="toggleStatus"
    >
      {{ loading ? '处理中...' : isMaintenance ? '恢复正常运行' : '进入维护模式' }}
    </button>
  </div>
</template>

<style scoped>
.system-status {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  font-size: 14px;
  color: #374151;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
}

.status-dot.maintenance {
  background: #f59e0b;
}

.status-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: #3b82f6;
  color: #ffffff;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.status-btn.maintenance {
  background: #22c55e;
}

.status-btn:hover:not(:disabled) {
  opacity: 0.85;
}

.status-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
