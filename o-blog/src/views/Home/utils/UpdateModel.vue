<template>
  <!-- 使用 Teleport 将弹窗挂载到 body 上，避免受父组件 overflow: hidden 等样式影响 -->
  <Teleport to="body">
    <!-- 遮罩层动画 -->
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay"></div>
    </Transition>

    <!-- 弹窗主体动画 -->
    <Transition name="pop">
      <div v-if="modelValue" class="modal-wrapper">
        <div class="modal-container">
          
          <!-- 头部：蓝白点缀与标题 -->
          <div class="modal-header">
            <div class="title-indicator"></div>
            <h3 class="modal-title">{{ title }}</h3>
          </div>

          <!-- 正文内容 -->
          <div class="modal-body">
            <!-- pre-wrap 可以完美保留传入字符串中的换行符 -->
            <p class="modal-content">{{ content }}</p>
          </div>

          <!-- 底部：靠右的按钮 -->
          <div class="modal-footer">
            <button class="confirm-btn" @click="handleClose">
              已知晓
            </button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
// 定义传入的 Props
interface Props {
  modelValue: boolean; // 控制弹窗显隐
  title?: string;      // 标题
  content?: string;    // 正文
}

withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: '系统更新通知',
  content: '这里是更新内容...'
});

// 定义向外抛出的事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'close'): void;
}>();

// 点击关闭按钮的处理逻辑
const handleClose = () => {
  emit('update:modelValue', false);
  emit('close');
};
</script>

<style scoped>
/* ================= 基础布局 ================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px); /* 优雅的背景模糊 */
  z-index: 9998;
}

.modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  pointer-events: none; /* 让 wrapper 不阻挡点击，只有 container 阻挡 */
}

/* ================= 弹窗样式 ================= */
.modal-container {
  width: 90%;
  max-width: 480px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  pointer-events: auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.modal-header {
  display: flex;
  align-items: center;
  padding: 24px 24px 16px;
}

.title-indicator {
  width: 4px;
  height: 18px;
  background: #3b82f6; /* 主题蓝 */
  border-radius: 4px;
  margin-right: 12px;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

/* 正文 */
.modal-body {
  padding: 0 24px;
  max-height: 35vh; /* 限制最大高度，内容过多时内部滚动 */
  overflow-y: auto;
}

.modal-content {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #4b5563;
  white-space: pre-wrap; /* 识别字符串中的 \n 换行符 */
}

/* 底部 */
.modal-footer {
  padding: 20px 24px 24px;
  display: flex;
  justify-content: flex-end; /* 靠右对齐 */
}

/* 按钮 */
.confirm-btn {
  background: #3b82f6;
  color: #ffffff;
  border: none;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.confirm-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.confirm-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

/* ================= 动画效果 ================= */

/* 遮罩层淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 弹窗主体缩放弹出 */
.pop-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); /* 带有轻微回弹的贝塞尔曲线，弹出感强 */
}
.pop-leave-active {
  transition: all 0.25s ease-in;
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: scale(0.85) translateY(10px);
}
</style>