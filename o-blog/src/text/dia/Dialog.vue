<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="modelValue" class="dialog-overlay" @click.self="close">
        <div class="dialog-box" :style="{ width }">
          <!-- 标题 -->
          <div class="dialog-header">
            <span class="dialog-title">{{ title }}</span>
            <button class="dialog-close" @click="close">×</button>
          </div>
          
          <!-- 内容 -->
          <div class="dialog-body">
            <slot>
              <p>{{ content }}</p>
            </slot>
          </div>
          
          <!-- 底部按钮 -->
          <div class="dialog-footer">
            <button class="btn btn-cancel" @click="cancel">
              {{ cancelText }}
            </button>
            <button class="btn btn-confirm" @click="confirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script lang="ts" setup>
const props = defineProps({
  modelValue: Boolean,      // 控制显示
  title: { type: String, default: '提示' },
  content: { type: String, default: '' },  // 传入的文本内容
  width: { type: String, default: '420px' },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const close = () => emit('update:modelValue', false)

const confirm = () => {
  emit('confirm')
  close()
}

const cancel = () => {
  emit('cancel')
  close()
}
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.dialog-box {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #e4e7ed;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  line-height: 1;
}

.dialog-close:hover {
  color: #409eff;
}

.dialog-body {
  padding: 15px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 10px 20px 20px;
}

.btn {
  padding: 9px 15px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.btn-cancel {
  background: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.btn-cancel:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background: #ecf5ff;
}

.btn-confirm {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.btn-confirm:hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

/* 动画 */
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.3s;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.dialog-enter-active .dialog-box,
.dialog-leave-active .dialog-box {
  transition: transform 0.3s;
}

.dialog-enter-from .dialog-box,
.dialog-leave-to .dialog-box {
  transform: translateY(-20px);
}
</style>