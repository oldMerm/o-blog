<template>
  <!-- 使用 Teleport 将弹窗挂载到 body 上，避免受父组件 overflow: hidden 等样式影响 -->
  <Teleport to="body">
    <!-- 遮罩层动画 -->
    <Transition name="px-fade">
      <div v-if="modelValue" class="px-modal-overlay"></div>
    </Transition>

    <!-- 弹窗主体动画 -->
    <Transition name="px-pop">
      <div v-if="modelValue" class="px-modal-wrapper">
        <div class="px-modal-container">
          <!-- 头部：像素方块点缀与标题 -->
          <div class="px-modal-header">
            <div class="px-title-indicator"></div>
            <h3 class="px-modal-title">{{ title }}</h3>
          </div>

          <!-- 正文内容 (pre-wrap 完整保留字符串中的换行符) -->
          <div class="px-modal-body">
            <p class="px-modal-content">{{ content }}</p>
            <!-- 版本信息结束后换行，靠右展示发布人及日期（yyyy-MM-dd） -->
            <p v-if="formattedTime" class="px-modal-time">oldmerman于{{ formattedTime }}</p>
          </div>

          <!-- 底部：靠右的像素确认按钮 -->
          <div class="px-modal-footer">
            <button type="button" class="px-confirm-btn" @click="handleClose">
              已知晓
            </button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue';

// 定义传入的 Props
interface Props {
  modelValue?: boolean; // 控制弹窗显隐
  title?: string;       // 标题（版本号）
  content?: string;     // 正文（版本内容）
  time?: string;        // 发布时间（后端 createdAt，格式 yyyy-MM-dd HH:mm:ss）
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: '系统更新通知',
  content: '这里是更新内容...',
  time: ''
});

// 时间字段只保留日期部分：'2026-10-17 12:30:00' => '2026-10-17'
const formattedTime = computed(() => (props.time || '').slice(0, 10));

// 定义向外抛出的事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'close'): void;
}>();

// 点击确认按钮的处理逻辑
const handleClose = () => {
  emit('update:modelValue', false);
  emit('close');
};
</script>

<style scoped>
/*
 * 弹窗通过 Teleport 挂载到 body，脱离了 .pixel-home 作用域，
 * 因此这里需要重新声明一份像素主题变量，保证组件可独立使用。
 */
.px-modal-overlay,
.px-modal-wrapper {
  --px-deepBlue: #1e3a8a;
  --px-borderDark: #12263a;
  --font-pixel: 'Press Start 2P', 'Silkscreen', 'Noto Sans SC', monospace;
}

/* ================= 遮罩层 ================= */
.px-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(18, 38, 58, 0.65);
  backdrop-filter: blur(2px);
  z-index: 9998;
}

.px-modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  pointer-events: none; /* 只有 container 阻挡点击 */
}

/* ================= 弹窗容器：4px 实体描边 + 6px 实体像素投影 ================= */
.px-modal-container {
  width: 90%;
  max-width: 440px;
  background: #ffffff;
  border: 4px solid var(--px-borderDark);
  border-radius: 0; /* 像素风格直角 */
  box-shadow: 6px 6px 0 0 var(--px-borderDark);
  pointer-events: auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部：浅蓝方块顶栏 */
.px-modal-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: #e3f1ff;
  border-bottom: 3px solid var(--px-borderDark);
}

.px-title-indicator {
  width: 10px;
  height: 16px;
  background: var(--px-deepBlue);
  border: 2px solid var(--px-borderDark);
  box-shadow: 2px 2px 0 0 var(--px-borderDark);
  border-radius: 0;
  margin-right: 12px;
  flex-shrink: 0;
}

.px-modal-title {
  margin: 0;
  font-family: var(--font-pixel);
  font-size: 18px;
  letter-spacing: 1px;
  color: var(--px-borderDark);
  word-break: break-all;
}

/* 正文区 */
.px-modal-body {
  padding: 20px 24px;
  max-height: 45vh;
  overflow-y: auto;
  background: #ffffff;
}

/* 像素方块滚动条 */
.px-modal-body::-webkit-scrollbar {
  width: 8px;
}

.px-modal-body::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-left: 2px solid var(--px-borderDark);
}

.px-modal-body::-webkit-scrollbar-thumb {
  background: var(--px-borderDark);
}

.px-modal-content {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #334155;
  white-space: pre-wrap;
}

/* 发布信息：换行后靠右显示 */
.px-modal-time {
  margin: 12px 0 0;
  text-align: right;
  font-size: 15px; /* 与正文 .px-modal-content 保持一致 */
  color: #64748b;
}

/* 底部按钮栏 */
.px-modal-footer {
  padding: 14px 20px;
  background: #f8fbff;
  border-top: 2px dashed #cbd5e1;
  display: flex;
  justify-content: flex-end;
}

/* 确认按钮：硬朗像素交互 */
.px-confirm-btn {
  background: var(--px-deepBlue);
  color: #ffffff;
  border: 3px solid var(--px-borderDark);
  box-shadow: 3px 3px 0 0 var(--px-borderDark);
  border-radius: 0;
  padding: 8px 18px;
  font-family: var(--font-pixel);
  font-size: 14px;
  cursor: pointer;
  user-select: none;
  transition: all 0.1s ease;
}

.px-confirm-btn:hover {
  background: #2563eb;
  transform: translate(-2px, -2px);
  box-shadow: 5px 5px 0 0 var(--px-borderDark);
}

.px-confirm-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 0 var(--px-borderDark);
}

/* ================= 弹窗动画 ================= */
.px-fade-enter-active,
.px-fade-leave-active {
  transition: opacity 0.2s ease;
}

.px-fade-enter-from,
.px-fade-leave-to {
  opacity: 0;
}

.px-pop-enter-active {
  transition: transform 0.25s cubic-bezier(0.18, 0.89, 0.32, 1.28), opacity 0.2s ease;
}

.px-pop-leave-active {
  transition: transform 0.15s ease-in, opacity 0.15s ease-in;
}

.px-pop-enter-from,
.px-pop-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(8px);
}
</style>
