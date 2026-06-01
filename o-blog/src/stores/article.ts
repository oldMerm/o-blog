// stores/article.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { httpInstance, type Response } from '@/utils/http'
import { type Article } from '@/views/public/Article'

export const useArticleStore = defineStore('article', () => {
  // ========== State ==========
  const articleList = ref<Article[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // ========== Getters ==========
  const articleCount = computed(() => articleList.value.length)
  
  const getArticleById = computed(() => {
    return (id: string) => articleList.value.find(a => a.id === id)
  })

  // ========== Actions ==========
  const fetchUserArticles = async () => {
    // 已有数据且未出错，可选：直接返回缓存（根据需求决定）
    // if (articleList.value.length > 0) return
    
    loading.value = true
    error.value = null
    try {
      const res = await httpInstance.get<any, Response>('/article/info')
      articleList.value = res.data
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取文章失败'
      console.error('获取文章失败:', e)
    } finally {
      loading.value = false
    }
  }

  const clearArticles = () => {
    articleList.value = []
    error.value = null
  }

  return {
    articleList,
    loading,
    error,
    articleCount,
    getArticleById,
    fetchUserArticles,
    clearArticles
  }
})