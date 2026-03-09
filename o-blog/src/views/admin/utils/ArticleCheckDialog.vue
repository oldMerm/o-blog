<template>
  <!-- 使用 Transition 包裹，实现渐变动画 -->
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click.self="close">
      <div class="modal-content">
        <h3 class="modal-title">请选择操作</h3>
        
        <!-- 横向排列的单选组 -->
        <div class="radio-group">
          <label class="radio-item">
            <input type="radio" value="publish" v-model="selectedValue" />
            <span class="circle"></span> 发布
          </label>
          <label class="radio-item">
            <input type="radio" value="unpublish" v-model="selectedValue" />
            <span class="circle"></span> 下架
          </label>
        </div>

        <button class="confirm-btn" @click="handleConfirm">确定</button>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Props<T = any> {
  visible: boolean;
  onConfirm?: (value: 'publish' | 'unpublish', extraParam?: T) => void;
  extraParam?: T;
}
const props = defineProps<Props>();
const emit = defineEmits(['update:visible']);

const selectedValue = ref<'publish' | 'unpublish'>('publish');

const handleConfirm = () => {
  props.onConfirm?.(selectedValue.value, props.extraParam);
  emit('update:visible', false);
};

const close = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
/* 1. 动画过渡样式 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-active .modal-content,
.modal-fade-leave-active .modal-content {
  transition: transform 0.3s ease;
}
.modal-fade-enter-from .modal-content,
.modal-fade-leave-to .modal-content {
  transform: scale(0.95); /* 进入时稍微缩小一点点，产生缩放感 */
}

/* 2. 布局样式 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 24px;
  border-radius: 12px;
  width: 300px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}
.modal-title { margin: 0 0 20px 0; font-size: 18px; text-align: left; }

/* 3. 横向排列的关键代码 */
.radio-group { 
  display: flex; 
  margin-bottom: 25px; 
}
.radio-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: 500;
  margin-right: 20px;
}

/* 自定义单选框样式 */
input[type="radio"] { display: none; }
.circle {
  width: 18px;
  height: 18px;
  border: 2px solid #ccc;
  border-radius: 50%;
  margin-right: 8px;
  transition: border-color 0.2s;
}
input[type="radio"]:checked + .circle {
  border-color: #007bff;
  background-color: #007bff;
  box-shadow: inset 0 0 0 4px white;
}

.confirm-btn {
  width: 100%;
  padding: 10px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.2s;
}
.confirm-btn:hover { background: #0056b3; }
</style>
