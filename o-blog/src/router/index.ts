import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home/index.vue'
import Article from '@/views/Article/index.vue'
import Login from '@/views/Login/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      name: 'home',
      path: '/',
      component: Home
    },
    {
      path: '/md',
      component: Article
    },
    {
      name: 'login',
      path: '/login',
      component: Login
    }
  ],
})

export default router
