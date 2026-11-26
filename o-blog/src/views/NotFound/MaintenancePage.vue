<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { httpInstance } from '@/utils/http'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// --- 管理员登录入口 ---
const showLogin = ref(false)
const loading = ref(false)
const captchaImage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  code: '',
})

interface CaptchaVO {
  captcha: string
  uuid: string
}

const fetchCaptcha = async () => {
  try {
    const res = await httpInstance.get<CaptchaVO>('/auth/captcha')
    const { captcha, uuid } = res.data
    captchaImage.value = captcha
    localStorage.setItem('cuid', uuid)
  } catch (error) {
    console.error('获取图形验证码失败:', error)
  }
}

const openLogin = () => {
  showLogin.value = true
  fetchCaptcha()
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password || !loginForm.code) {
    alert('请填写完整信息')
    return
  }

  loading.value = true
  try {
    // 1. 走正常登录接口，成功后 token 会自动写入 store
    const res = await auth.login({
      ...loginForm,
      uuid: localStorage.getItem('cuid') ?? '',
    })

    if (res.code !== 200) {
      alert(res.message || '登录失败')
      return
    }

    // 2. 再校验管理员权限，两个接口都 200 才放行
    const isAdmin = await auth.verifyAdmin()
    if (!isAdmin) {
      auth.clearAuth()
      alert('该账号没有管理员权限')
      return
    }

    // 3. 登录成功，正常访问
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    alert(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="maintenance-main">
    <!-- 右上角管理员登录入口 -->
    <div class="admin-entry">
      <button v-if="!showLogin" class="admin-entry-btn" @click="openLogin">管理员登录</button>

      <form v-else class="login-card" @submit.prevent="handleLogin">
        <input v-model="loginForm.username" type="text" placeholder="账号" />
        <input v-model="loginForm.password" type="password" placeholder="密码" />
        <div class="captcha-row">
          <input v-model="loginForm.code" type="text" placeholder="图形验证码" />
          <img
            v-if="captchaImage"
            :src="captchaImage"
            alt="验证码"
            title="点击刷新验证码"
            @click="fetchCaptcha"
          />
        </div>
        <div class="login-actions">
          <button type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <button type="button" class="ghost-btn" @click="showLogin = false">取消</button>
        </div>
      </form>
    </div>

    <h1>系统维护中</h1>
    <p>博客系统正在维护中，敬请期待</p>

    <footer class="page-footer">
      <span>© 2026 鱼人博客 · All Rights Reserved</span>
    </footer>
  </div>
</template>

<style scoped>
.maintenance-main {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  min-height: 100vh;
  padding: 2rem 2rem 5rem;
  text-align: center;
  color: #111;
  background: #fff;
  font-family: var(--font-sans);
}

.maintenance-main h1 {
  font-family: var(--font-mono);
  font-size: clamp(2rem, 4vw, 3.25rem);
  font-weight: 500;
  line-height: 1.1;
  letter-spacing: 0.04em;
}

.maintenance-main p {
  font-size: 1rem;
  letter-spacing: 0.08em;
  color: #555;
}

.page-footer {
  position: fixed;
  right: 0;
  bottom: 1.5rem;
  left: 0;
  text-align: center;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  color: #aaa;
  pointer-events: none;
}

/* --- 右上角管理员入口 --- */
.admin-entry {
  position: fixed;
  top: 1.5rem;
  right: 1.5rem;
  z-index: 10;
  text-align: right;
}

.admin-entry-btn {
  padding: 0.45rem 0.9rem;
  font-size: 0.85rem;
  color: #111;
  background: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.admin-entry-btn:hover {
  opacity: 0.6;
  color: rgb(56, 142, 255);
}

.login-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 240px;
  padding: 0.9rem;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
}

.login-card input {
  padding: 0.5rem 0.6rem;
  font-size: 0.85rem;
  color: #111;
  background: #fafafa;
  border: 1px solid #ddd;
  border-radius: 4px;
  outline: none;
}

.login-card input:focus {
  border-color: #111;
}

.captcha-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.captcha-row input {
  flex: 1;
  min-width: 0;
}

.captcha-row img {
  width: 72px;
  height: 32px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  object-fit: cover;
}

.login-actions {
  display: flex;
  gap: 0.5rem;
}

.login-actions button {
  flex: 1;
  padding: 0.45rem 0.6rem;
  font-size: 0.85rem;
  border-radius: 4px;
  cursor: pointer;
}

.login-actions button[type='submit'] {
  color: #fff;
  background: #111;
  border: 1px solid #111;
}

.login-actions button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.ghost-btn {
  color: #555;
  background: #fff;
  border: 1px solid #ddd;
}
</style>
