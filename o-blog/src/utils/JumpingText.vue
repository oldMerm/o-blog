<template>
  <div class="jump-text-container">
    <span
      v-for="(item, index) in charList"
      :key="index"
      class="jump-char"
      :style="{
        '--delay': `${index * delay}s`,
        '--duration': duration
      }"
    >
      {{ item.char }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// 1. 定义 Props 的类型
interface Props {
  text: string      // 要显示的文本（必填）
  delay?: number    // 相邻字符跳动的时间差，单位秒（可选）
  duration?: string // 动画完成一次的周期（可选）
}

// 2. 接收 Props 并设置默认值
const props = withDefaults(defineProps<Props>(), {
  delay: 0.1,
  duration: '1.2s'
})

// 3. 将字符串拆分为数组，并通过 computed 保持响应式
const charList = computed(() => {
  return props.text.split('').map((char) => ({
    // 核心细节：将普通空格替换为 Unicode 的不间断空格 (\u00A0)
    // 否则空字符在 span 标签中宽度可能为 0，导致动画看起来挤在一起
    char: char === ' ' ? '\u00A0' : char
  }))
})
</script>

<style scoped>
.jump-text-container {
  /* 容器基本样式 */
  display: inline-block;
  white-space: pre-wrap; /* 配合 \u00A0 完美处理空格与换行 */
  font-size: 2rem;       /* 字体大小可以由外部控制，这里给个基础值 */
  font-weight: bold;
  color: #333;
}

.jump-char {
  /* 必须是 inline-block，transform 才生效 */
  display: inline-block;
  
  /* 使用 CSS 变量接收来自 template 的动态参数 */
  animation: jump var(--duration) infinite ease-in-out;
  animation-delay: var(--delay);
}

/* 跳动关键帧 */
@keyframes jump {
  0%, 100% {
    transform: translateY(0);
  }
  20% {
    transform: translateY(-8px); /* 向上跳动的高度 */
  }
}
</style>
