import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type Article } from '@/views/public/Article'
import { httpInstance, type Response } from '@/utils/http.ts'

interface ArticleTopVO {
  newList: Article[]
  tecList: Article[]
  dailyList: Article[]     
}

export const usePinnedStore = defineStore('pinned', () => {
  // ---------- state ----------
  const articleTopList = ref<ArticleTopVO | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  let loadPromise: Promise<void> | null = null

  const news  = computed(() => articleTopList.value?.newList  ?? [])
  const tech  = computed(() => articleTopList.value?.tecList  ?? [])
  const daily = computed(() => articleTopList.value?.dailyList ?? [])

  function load() {
    if (loadPromise) return loadPromise

    loading.value = true
    error.value = null

    loadPromise = (async () => {
      try {
        const res = await httpInstance.get<any, Response>('/article/top')
        if (res.code !== 200) {
          throw new Error(res.message || `HTTP ${res.code}`)
        }
        articleTopList.value = res.data
      } catch (e) {
        error.value = (e as Error).message
        // 失败时给一个空结构，避免组件里到处判空
        articleTopList.value = { newList: [], tecList: [], dailyList: [] }
        loadPromise = null          // 失败后允许重试
      } finally {
        loading.value = false
      }
    })()

    return loadPromise
  }

  function reset() {
    articleTopList.value = null
    error.value = null
    loading.value = false
    loadPromise = null
  }

  return { articleTopList, loading, error, news, tech, daily, load, reset }
})