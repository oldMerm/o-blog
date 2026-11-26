import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { httpInstance, type Response } from '@/utils/http'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const TIMEOUT_KEY = 'timeout'

interface LoginForm {
  username: string
  password: string
  code: string
  uuid: string
}

interface AuthPayload {
  token: string
  refreshToken?: string
  timeout?: string | number
}

export const useAuthStore = defineStore('auth', () => {
  // ========== State ==========
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const refreshToken = ref(localStorage.getItem(REFRESH_TOKEN_KEY) || '')
  const timeout = ref(localStorage.getItem(TIMEOUT_KEY) || '')
  // 是否已经确认拥有管理员权限（仅保存在内存中，刷新后重新校验）
  const isAdmin = ref(false)

  // ========== Getters ==========
  const isLoggedIn = computed(() => Boolean(token.value))

  // ========== Actions ==========
  /** 把 token 写入 store 并同步到 localStorage */
  const setAuth = (payload: AuthPayload) => {
    token.value = payload.token
    refreshToken.value = payload.refreshToken ?? ''
    timeout.value = String(payload.timeout ?? '')
    isAdmin.value = false

    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken.value)
    localStorage.setItem(TIMEOUT_KEY, timeout.value)
  }

  const clearAuth = () => {
    token.value = ''
    refreshToken.value = ''
    timeout.value = ''
    isAdmin.value = false

    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(TIMEOUT_KEY)
  }

  /**
   * 同步其它组件直接写入 localStorage 的登录信息。
   * 例如首页 / 移动端用户面板完成登录后，guard 里也能拿到正确的 token。
   */
  const syncFromStorage = () => {
    const storedToken = localStorage.getItem(TOKEN_KEY) || ''
    if (storedToken !== token.value) {
      // token 变了，之前的管理员校验结果作废
      isAdmin.value = false
    }
    token.value = storedToken
    refreshToken.value = localStorage.getItem(REFRESH_TOKEN_KEY) || ''
    timeout.value = localStorage.getItem(TIMEOUT_KEY) || ''
  }

  /** 走正常的登录接口，成功后把 token 写入 store */
  const login = async (form: LoginForm) => {
    const res = await httpInstance.post<any, Response>('/auth/login', form)
    if (res.code === 200) {
      const data = res.data ?? {}
      setAuth({
        token: data.token,
        refreshToken: data.refreshToken,
        timeout: data.timeout,
      })
    }
    return res
  }

  /** 校验当前用户是否拥有管理员权限 */
  const verifyAdmin = async () => {
    if (!token.value) {
      isAdmin.value = false
      return false
    }

    try {
      const res = await httpInstance.get<any, Response>('/admin/usr/isAdmin')
      // 接口对已登录用户都会返回 200，真正的管理员标识是 data === 'admin'
      isAdmin.value = res.code === 200 && res.data === 'admin'
    } catch {
      isAdmin.value = false
    }

    return isAdmin.value
  }

  return {
    token,
    refreshToken,
    timeout,
    isAdmin,
    isLoggedIn,
    setAuth,
    clearAuth,
    syncFromStorage,
    login,
    verifyAdmin,
  }
})
