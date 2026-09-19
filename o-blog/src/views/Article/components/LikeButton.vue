<script setup lang="ts">
import { ref, watch } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import Toast from '@/utils/toast/Toast.vue';

interface Props {
  articleId: string | number;
  count?: number;
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
});

const emit = defineEmits<{
  (e: 'update:count', value: number): void;
}>();

const isLiked = ref(false);
const likeCount = ref(props.count);
const loading = ref(false);

const toastRef = ref<InstanceType<typeof Toast> | null>(null);
const toastMsg = ref('');
const toastType = ref<'success' | 'error'>('success');

const showToast = (type: 'success' | 'error', message: string) => {
  toastType.value = type;
  toastMsg.value = message;
  toastRef.value?.show();
};

const showLoginToast = () => {
  showToast('error', '请先登录后再点赞哟~');
};

// 响应体 code 为 401：未登录
const handleUnauthorized = (res?: Response | null): boolean => {
  if (res?.code === 401) {
    showLoginToast();
    return true;
  }
  return false;
};

// HTTP 状态码 401：未登录
const isAuthError = (error: any): boolean =>
  error?.response?.status === 401 || error?.code === 401;

watch(
  () => props.count,
  (value) => {
    likeCount.value = value;
  }
);

// 获取点赞记录：判断当前用户是否点过赞
const getLikeRecord = async () => {
  if (!props.articleId) return;
  try {
    const res = await httpInstance.get<any, Response>(`/article/like/${props.articleId}`);
    if (handleUnauthorized(res)) return;
    if (res.code === 200) {
      isLiked.value = res.data === true;
    }
  } catch (error) {
    if (isAuthError(error)) {
      showLoginToast();
    } else {
      console.error('Failed to fetch like record', error);
    }
  }
};

// 点赞
const like = async (): Promise<boolean> => {
  try {
    const res = await httpInstance.post<any, Response>(`/article/like/save/${props.articleId}`);
    if (handleUnauthorized(res)) return false;
    return res.code === 200 && res.data === true;
  } catch (error) {
    if (isAuthError(error)) {
      showLoginToast();
    } else {
      console.error('Failed to like article', error);
    }
    return false;
  }
};

// 取消点赞
const unlike = async (): Promise<boolean> => {
  try {
    const res = await httpInstance.delete<any, Response>(`/article/like/save/${props.articleId}`);
    if (handleUnauthorized(res)) return false;
    return res.code === 200 && res.data === true;
  } catch (error) {
    if (isAuthError(error)) {
      showLoginToast();
    } else {
      console.error('Failed to unlike article', error);
    }
    return false;
  }
};

const toggleLike = async () => {
  if (loading.value || !props.articleId) return;
  loading.value = true;
  try {
    if (isLiked.value) {
      if (await unlike()) {
        isLiked.value = false;
        likeCount.value = Math.max(0, likeCount.value - 1);
      }
    } else {
      if (await like()) {
        isLiked.value = true;
        likeCount.value += 1;
      }
    }
    emit('update:count', likeCount.value);
  } finally {
    loading.value = false;
  }
};

watch(
  () => props.articleId,
  () => {
    isLiked.value = false;
    getLikeRecord();
  },
  { immediate: true }
);
</script>

<template>
  <div class="like-wrap">
    <button
      class="like-btn"
      :class="{ liked: isLiked }"
      :disabled="loading"
      aria-label="Like this post"
      @click="toggleLike"
    >
      <svg class="hand-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path d="M2 9.5V21h4.5V9.5H2zm13.5-7c-1.2 0-2.3.5-3 1.4L8.7 8.3c-.4.5-.7 1.2-.7 1.8v8.4c0 1.4 1.1 2.5 2.5 2.5h7.2c1.2 0 2.2-.8 2.4-1.9l1.4-7c.2-1 .1-2.1-.5-2.9-.5-.6-1.3-1-2.1-1h-4.3l.5-2.3c.1-.3.1-.7 0-1-.2-.6-.7-1-1.3-1z"/>
      </svg>
      <span class="like-count">{{ likeCount }}</span>
    </button>
    <Toast ref="toastRef" :message="toastMsg" :type="toastType" />
  </div>
</template>

<style scoped>
.like-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 16px;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid var(--vp-c-border, #e2e8f0);
  border-radius: 999px;
  background: #fcfdfe;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, transform 0.15s, border-color 0.15s;
  user-select: none;
}

.like-btn:hover {
  background: #eef2f6;
  color: var(--vp-c-brand, #3b82f6);
}

.like-btn:active {
  transform: scale(0.95);
}

.like-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.like-btn.liked {
  background: var(--vp-c-brand, #3b82f6);
  color: #ffffff;
  border-color: var(--vp-c-brand, #3b82f6);
}

.like-btn.liked:hover {
  background: #1d4ed8;
}

.hand-icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.like-count {
  min-width: 20px;
  text-align: center;
}
</style>
