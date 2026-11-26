import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home/index.vue'
import Article from '@/views/Article/components/MarkdownViewer.vue'
import MobileArticle from '@/views/Article/components/MobileMarkdownViewer.vue'
import Login from '@/views/Login/index.vue'
import Manage from '@/views/Manage/index.vue'
import MobileHome  from '@/views/mobile/index.vue'
import NotFoundPage from '@/views/NotFound/NotFoundPage.vue'
import MaintenancePage from '@/views/NotFound/MaintenancePage.vue'

import Admin from '@/views/admin/index.vue'
import AdminDashboard from '@/views/admin/components/AdminDashboard.vue'
import AdminUser from '@/views/admin/components/AdminUser.vue'
import AdminFeedback from '@/views/admin/components/AdminFeedback.vue'
import AdminArticle from '@/views/admin/components/AdminArticle.vue'
import AdminVersion from '@/views/admin/components/AdminVersion.vue'
import { httpInstance, type Response } from '@/utils/http'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      name: 'home',
      path: '/',
      component: Home
    },
    {
      name: 'markdown',
      path: '/markdown/:id',
      component: Article
    },
    {
      name: 'login',
      path: '/login',
      component: Login
    },
    {
      name: 'manage',
      path: '/manage',
      component: Manage
    },
    {
      name: 'admin',
      path: '/admin',
      component: Admin,
      children: [
        {
          name: 'adminDashboard',
          path: '/admin/dashboard',
          component: AdminDashboard
        },
        {
          path: '/admin/users',
          component: AdminUser
        },
        {
          path: '/admin/feedback',
          component: AdminFeedback
        },
        {
          path: '/admin/article',
          component: AdminArticle
        },
        {
          path: '/admin/version',
          component: AdminVersion
        }
      ]
    },
    {
      name: 'moblie_home',
      path: '/mobile-home',
      component: MobileHome
    },
    {
      name: 'mobile_markdown',
      path: '/mobile/markdown/:id',
      component: MobileArticle
    },
    {
      // 系统维护时展示的页面
      name: 'maintenance',
      path: '/maintenance',
      component: MaintenancePage
    },
    {
      // 兜底路由：访问不存在的地址时展示 404 页面
      name: 'not-found',
      path: '/:pathMatch(.*)*',
      component: NotFoundPage
    },
  ],
})

// 请求系统状态：1-正常运行，2-维护中
const fetchSystemStatus = async (): Promise<number | null> => {
  try {
    const res = await httpInstance.get<any, Response>('/counter/system/status')
    return res.code === 200 ? Number(res.data) : null
  } catch (error) {
    // 状态接口异常时按正常运行处理，避免整个站点无法访问
    console.warn('[router] 获取系统状态失败:', error)
    return null
  }
}

// 每次进入路由（即挂载新的 Vue 组件）前先检查系统状态
router.beforeEach(async (to) => {
  const status = await fetchSystemStatus()

  if (status === 2) {
    // 已经在维护页，直接放行
    if (to.name === 'maintenance') return true

    const auth = useAuthStore()
    auth.syncFromStorage()

    // 已确认管理员身份，或 token 校验通过（登录 + isAdmin 都 200），正常放行
    if (auth.isAdmin) return true
    if (auth.token && (await auth.verifyAdmin())) return true

    // 普通用户 / 未登录用户跳到维护页，并记录原地址
    return {
      name: 'maintenance',
      query: to.fullPath === '/' ? {} : { redirect: to.fullPath },
      replace: true,
    }
  }

  // 系统已恢复正常，维护页自动回到首页
  if (status === 1 && to.name === 'maintenance') {
    return { name: 'home', replace: true }
  }

  return true
})

export default router
