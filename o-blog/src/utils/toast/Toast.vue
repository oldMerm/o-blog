<template>
  <Transition name="toast-fade">
    <div v-if="visible" :class="['toast-container', type]">
      <span class="message">{{ message }}</span>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Props {
  message: string;
  type?: 'success' | 'error'; // 新增类型定义
  duration?: number;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'success',
  duration: 3000
});

const visible = ref(false);

const show = () => {
  visible.value = true;
  setTimeout(() => {
    visible.value = false;
  }, props.duration);
};

defineExpose({ show });
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 8%;
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 16px;
  border-radius: 8px;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: white;
  font-weight: 500;
  pointer-events: none;
}

.message {
  font-size: 15px;
}

/* 成功样式 */
.toast-container.success {
  color: #52c41a;
  border: 1px solid #cbe0bb;
}

/* 失败样式 */
.toast-container.error {
  color: #ff4d4f;
  border: 1px solid #f3c9c7;
}

/* 动画效果 */
.toast-fade-enter-active {
  animation: slide-in 0.4s ease-out;
}

.toast-fade-leave-active {
  animation: fade-out 0.4s ease-in;
}

@keyframes slide-in {
  from { transform: translate(-50%, -100%); opacity: 0; }
  to { transform: translate(-50%, 0); opacity: 1; }
}

@keyframes fade-out {
  from { transform: translate(-50%, 0); opacity: 1; }
  to { transform: translate(-50%, -20px); opacity: 0; }
}
</style>
