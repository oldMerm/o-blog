import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home/index.vue'
import Article from '@/views/Article/components/MarkdownViewer.vue'
import Login from '@/views/Login/index.vue'
import Manage from '@/views/Manage/index.vue'

import Admin from '@/views/admin/index.vue'
import AdminDashboard from '@/views/admin/components/AdminDashboard.vue'
import AdminUser from '@/views/admin/components/AdminUser.vue'
import AdminFeedback from '@/views/admin/components/AdminFeedback.vue'
import AdminArticle from '@/views/admin/components/AdminArticle.vue'
import ContentDialogDemo from '@/text/dia/ContentDialogDemo.vue'

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
        }
      ]
    },
    {
      name: 'text',
      path: '/text',
      component: ContentDialogDemo
    }
  ],
})

export default router
