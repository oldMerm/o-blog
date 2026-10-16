<script setup lang="ts">
import { ref } from 'vue';
import Toast from '@/utils/toast/Toast.vue';

interface Props {
  /** 文章名，用于拼接转发文案 */
  articleName?: string;
  /** 转发链接，默认取当前页面地址 */
  url?: string;
}

const props = withDefaults(defineProps<Props>(), {
  articleName: '',
  url: '',
});

const toastRef = ref<InstanceType<typeof Toast> | null>(null);
const toastMsg = ref('');
const toastType = ref<'success' | 'error'>('success');

const showToast = (type: 'success' | 'error', message: string) => {
  toastType.value = type;
  toastMsg.value = message;
  toastRef.value?.show();
};

// 写入剪贴板：优先使用 Clipboard API，非安全上下文回退到 execCommand
const writeToClipboard = async (text: string): Promise<boolean> => {
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch (error) {
      // 忽略，继续尝试回退方案
    }
  }
  try {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'fixed';
    textarea.style.top = '-9999px';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    textarea.setSelectionRange(0, text.length);
    const ok = document.execCommand('copy');
    document.body.removeChild(textarea);
    return ok;
  } catch (error) {
    return false;
  }
};

// 转发：复制【文章名】url-来自o-blog
const share = async () => {
  const name = props.articleName.trim() || document.title || '文章';
  const link = props.url.trim() || window.location.href;
  const copied = await writeToClipboard(`【${name}-来自o-blog】${link}`);
  if (copied) {
    showToast('success', '链接已复制到剪贴板');
  } else {
    showToast('error', '复制失败，请手动复制链接');
  }
};
</script>

<template>
  <div class="share-wrap">
    <button class="share-btn" type="button" aria-label="转发这篇文章" @click="share">
      <svg class="share-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
        <path
          d="M565.934 817.574a34.816 34.816 0 0 0 9.818 29.394l0.302 0.241a35.539 35.539 0 0 0 25.6 10.963c11.143 0 20.66-5.42 27.226-13.312l354.545-387.072a35.117 35.117 0 0 0 10.24-27.106 35.057 35.057 0 0 0-10.24-27.106L626.892 14.336a36.503 36.503 0 0 0-51.2 0 34.936 34.936 0 0 0-9.758 29.395V253.35c-295.996 0-535.974 238.893-535.974 533.684a529.468 529.468 0 0 0 44.454 212.51C116.7 777.276 329.812 608.437 565.332 608.437l0.602 209.137z"
        />
      </svg>
      <span class="share-label">转发</span>
    </button>
    <Toast ref="toastRef" :message="toastMsg" :type="toastType" />
  </div>
</template>

<style scoped>
.share-wrap {
  display: flex;
  justify-content: center;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 13px;
  border: 1px solid var(--vp-c-border, #e2e8f0);
  border-radius: 999px;
  background: #fcfdfe;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, transform 0.15s, border-color 0.15s;
  user-select: none;
}

.share-btn:hover {
  background: #eef2f6;
  color: var(--vp-c-brand, #3b82f6);
}

.share-btn:active {
  transform: scale(0.95);
}

.share-icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.share-label {
  line-height: 1;
}
</style>
