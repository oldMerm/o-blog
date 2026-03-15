<template>
  <Teleport to="body">
    <!-- 遮罩层动画 -->
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay"></div>
    </Transition>

    <!-- 弹窗主体动画 -->
    <Transition name="pop">
      <div v-if="modelValue" class="modal-wrapper">
        <div class="modal-container">
          
          <!-- 头部 -->
          <div class="modal-header">
            <div class="title-indicator"></div>
            <h3 class="modal-title">发布新版本</h3>
          </div>

          <!-- 表单正文 -->
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">
                版本号 <span class="form-hint">（格式建议: Vx.y.z）</span>
              </label>
              <input 
                v-model="formData.versionId" 
                type="text" 
                class="form-input" 
                placeholder="例如: V1.2.0"
              />
            </div>

            <div class="form-group">
              <label class="form-label">版本内容</label>
              <textarea 
                v-model="formData.versionContent" 
                class="form-textarea" 
                placeholder="请输入本次更新的详细内容..."
              ></textarea>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="modal-footer">
            <button class="cancel-btn" @click="handleClose">取消</button>
            <button class="submit-btn" @click="handleSubmit">确认发布</button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

// 定义传入的 Props
interface Props {
  modelValue: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false
});

// 定义向外抛出的事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  // 提交时，将管理员填写的两个核心字段传给父组件
  (e: 'submit', payload: { versionId: string; versionContent: string }): void;
}>();

// 表单数据状态
const formData = ref({
  versionId: '',
  versionContent: ''
});

// 监听弹窗打开，每次打开时清空上次填写的表单
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    formData.value.versionId = '';
    formData.value.versionContent = '';
  }
});

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false);
};

// 提交表单
const handleSubmit = () => {
  // 简单的必填校验（你可以根据需要替换为更严格的UI提示库，比如 ElMessage）
  if (!formData.value.versionId.trim() || !formData.value.versionContent.trim()) {
    alert('请填写完整的版本号和版本内容');
    return;
  }
  
  // 触发 submit 事件，由父组件去组装完整的 Version 对象并调用后端接口
  emit('submit', { ...formData.value });
};
</script>

<style scoped>
/* ================= 基础布局（与前置组件一致） ================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
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
  pointer-events: none;
}

.modal-container {
  width: 90%;
  max-width: 520px; /* 表单稍微宽一点，视觉更舒展 */
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  pointer-events: auto;
  display: flex;
  flex-direction: column;
}

/* ================= 头部 ================= */
.modal-header {
  display: flex;
  align-items: center;
  padding: 24px 24px 16px;
}

.title-indicator {
  width: 4px;
  height: 18px;
  background: #3b82f6;
  border-radius: 4px;
  margin-right: 12px;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

/* ================= 表单正文 ================= */
.modal-body {
  padding: 10px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px; /* 表单项之间的间距 */
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-hint {
  font-weight: 400;
  color: #9ca3af;
  font-size: 12px;
}

/* 输入框和文本域的通用样式 */
.form-input,
.form-textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2937;
  background-color: #f9fafb;
  transition: all 0.2s ease;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); /* 蓝色的焦点光晕 */
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #9ca3af;
}

.form-textarea {
  min-height: 120px;
  resize: vertical; /* 允许垂直拖拽调整高度 */
  line-height: 1.6;
}

/* ================= 底部按钮 ================= */
.modal-footer {
  padding: 20px 24px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px; /* 两个按钮的间距 */
}

/* 取消按钮 */
.cancel-btn {
  background: #f3f4f6;
  color: #4b5563;
  border: none;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  background: #e5e7eb;
  color: #1f2937;
}

/* 确认按钮 */
.submit-btn {
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

.submit-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.submit-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

/* ================= 动画效果 ================= */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.pop-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
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
