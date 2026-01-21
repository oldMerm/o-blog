import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home/index.vue'
import Article from '@/views/Article/components/MarkdownViewer.vue'
import Login from '@/views/Login/index.vue'
import Manage from '@/views/Manage/index.vue'

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
      path: '/markdown',
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
    }
  ],
})

export default router
