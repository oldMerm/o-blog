<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { httpInstance, type Response } from '@/utils/http'
import MobileSheet from './MobileSheet.vue'

interface UserInfo {
  username: string
  article: number
  like: number
  attrURL: string | null
}

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const router = useRouter()

const loading = ref(false)
const logged = ref(false)
const username = ref('')
const article = ref(0)
const like = ref(0)
const avatar = ref('')
const fileRef = ref<HTMLInputElement | null>(null)

const hasSession = () =>
  Boolean(localStorage.getItem('token') || localStorage.getItem('refreshToken'))

const clearSession = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('timeout')
}

const refreshSession = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/auth/refresh')
    if (res.code !== 200 || !res.data?.token) return false
    const { token, refreshToken, timeout } = res.data
    localStorage.setItem('token', token)
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
    if (timeout) localStorage.setItem('timeout', timeout)
    return true
  } catch {
    return false
  }
}

const loadInfo = async (retried = false) => {
  if (!hasSession()) {
    logged.value = false
    loading.value = false
    return
  }

  loading.value = true
  try {
    const res = await httpInstance.get<any, Response>('/usr/info')
    if (res.code === 401) {
      if (!retried && (await refreshSession())) {
        await loadInfo(true)
        return
      }
      clearSession()
      logged.value = false
      return
    }
    if (res.code !== 200) {
      logged.value = false
      return
    }

    const data = res.data as UserInfo
    username.value = data.username
    article.value = data.article
    like.value = data.like
    avatar.value = data.attrURL || ''
    logged.value = true
  } catch {
    logged.value = false
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return
    if (!hasSession()) {
      logged.value = false
      loading.value = false
      return
    }
    void loadInfo()
  }
)

const close = () => emit('update:modelValue', false)

const goLogin = () => {
  close()
  router.push({ name: 'login', query: { from: 'mobile' } })
}

const openFile = () => {
  fileRef.value?.click()
}

const handleSelect = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  avatar.value = URL.createObjectURL(file)
  const formData = new FormData()
  formData.append('img', file)
  try {
    await httpInstance.put<any, Response>('/oss/upload', formData)
  } catch {
    // 上传失败时保留本地预览，不打断当前操作
  } finally {
    input.value = ''
  }
}
</script>

<template>
  <MobileSheet
    :model-value="modelValue"
    title="用户中心"
    centered
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="loading" class="state-box">
      <span class="loading-dot" />
      <span>正在加载用户信息...</span>
    </div>

    <div v-else-if="!logged" class="login-state">
      <div class="login-avatar">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path
            d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4Zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4Z"
          />
        </svg>
      </div>
      <p class="login-title">还没有登录</p>
      <p class="login-desc">登录后可以查看账号信息和浏览记录</p>
      <button class="primary-btn" type="button" @click="goLogin">去登录</button>
    </div>

    <div v-else class="profile">
      <div class="profile-top">
        <button class="avatar-wrap" type="button" title="点击更换头像" @click="openFile">
          <img v-if="avatar" :src="avatar" alt="用户头像" class="avatar" />
          <span v-else class="avatar-fallback">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4Zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4Z"
              />
            </svg>
          </span>
          <span class="avatar-edit">更换</span>
          <input
            ref="fileRef"
            type="file"
            accept="image/*"
            class="file-input"
            @change="handleSelect"
          />
        </button>

        <div class="profile-info">
          <p class="username">{{ username }}</p>
          <p class="welcome">欢迎回来，今天也要好好记录生活。</p>
        </div>
      </div>

      <div class="stats">
        <div class="stat-item">
          <strong>{{ article }}</strong>
          <span>文章</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <strong>{{ like }}</strong>
          <span>获赞</span>
        </div>
      </div>
    </div>
  </MobileSheet>
</template>

<style scoped>
.state-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 0;
  color: #8a9bab;
  font-size: 0.86rem;
}

.loading-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgb(75, 155, 210);
  animation: user-pulse 1s ease-in-out infinite;
}

@keyframes user-pulse {
  0%,
  100% {
    transform: scale(0.7);
    opacity: 0.4;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}

.login-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 8px 10px;
  text-align: center;
}

.login-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: rgba(75, 155, 210, 0.12);
  color: rgb(75, 155, 210);
}

.login-avatar svg {
  width: 40px;
  height: 40px;
  fill: currentColor;
}

.login-title {
  margin: 14px 0 6px;
  color: #2b3a4a;
  font-size: 1.05rem;
  font-weight: 600;
}

.login-desc {
  margin: 0 0 20px;
  max-width: 240px;
  color: #8a9bab;
  font-size: 0.82rem;
  line-height: 1.6;
}

.primary-btn {
  width: 100%;
  max-width: 260px;
  padding: 12px 0;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(96, 178, 232), rgb(48, 118, 184));
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
}

.primary-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.profile {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.avatar,
.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  overflow: hidden;
  background: rgba(75, 155, 210, 0.12);
  color: rgb(75, 155, 210);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 10px rgba(60, 120, 180, 0.15);
}

.avatar-fallback svg {
  width: 38px;
  height: 38px;
  fill: currentColor;
}

.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  padding: 2px 7px;
  border-radius: 9px;
  background: rgb(75, 155, 210);
  color: #fff;
  font-size: 0.6rem;
  line-height: 1.5;
  white-space: nowrap;
}

.file-input {
  display: none;
}

.profile-info {
  min-width: 0;
}

.username {
  margin: 0 0 4px;
  overflow: hidden;
  color: #2b3a4a;
  font-size: 1.1rem;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.welcome {
  margin: 0;
  color: #8a9bab;
  font-size: 0.76rem;
  line-height: 1.5;
}

.stats {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(225, 237, 248, 0.7), rgba(210, 226, 242, 0.5));
  border: 1px solid rgba(180, 210, 240, 0.35);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.stat-item strong {
  color: rgb(40, 65, 90);
  font-size: 1.15rem;
  line-height: 1.2;
}

.stat-item span {
  color: #7b93a8;
  font-size: 0.72rem;
}

.stat-divider {
  width: 1px;
  height: 26px;
  background: rgba(140, 175, 205, 0.35);
}
</style>
