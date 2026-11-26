<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    fullscreen?: boolean
    centered?: boolean
    height?: string
    scrollable?: boolean
    closeOnOverlay?: boolean
  }>(),
  {
    title: '',
    fullscreen: false,
    centered: false,
    height: '',
    scrollable: true,
    closeOnOverlay: true
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const close = () => {
  emit('update:modelValue', false)
}

const onOverlay = () => {
  if (props.closeOnOverlay) close()
}

const onEsc = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && props.modelValue) close()
}

let previousBodyOverflow = ''
let bodyLocked = false

const lockBody = () => {
  if (bodyLocked) return
  previousBodyOverflow = document.body.style.overflow
  document.body.style.overflow = 'hidden'
  bodyLocked = true
}

const unlockBody = () => {
  if (!bodyLocked) return
  document.body.style.overflow = previousBodyOverflow
  bodyLocked = false
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) lockBody()
    else unlockBody()
  },
  { immediate: true }
)

onMounted(() => {
  window.addEventListener('keydown', onEsc)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onEsc)
  unlockBody()
})
</script>

<template>
  <Teleport to="body">
    <Transition name="msheet">
      <div
        v-if="modelValue"
        class="msheet-overlay"
        :class="{ 'is-fullscreen': fullscreen, 'is-centered': centered }"
        @click.self="onOverlay"
      >
        <div
          class="msheet"
          :class="{ 'is-fullscreen': fullscreen, 'is-centered': centered, 'no-scroll': !scrollable }"
          :style="!fullscreen && height ? { height, maxHeight: height } : undefined"
          role="dialog"
          aria-modal="true"
          :aria-label="title || '面板'"
        >
          <div class="msheet-header">
            <div class="msheet-title">
              <slot name="title">{{ title }}</slot>
            </div>
            <button class="msheet-close" type="button" aria-label="关闭" @click="close">
              &times;
            </button>
          </div>
          <div class="msheet-body">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.msheet-overlay {
  position: fixed;
  inset: 0;
  z-index: 9990;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(2px);
}

.msheet {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  max-height: 86vh;
  max-height: 86dvh;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
  background: #fff;
  box-shadow: 0 -8px 40px rgba(15, 40, 70, 0.18);
}

.msheet.is-fullscreen {
  max-width: none;
  height: 100vh;
  height: 100dvh;
  max-height: 100vh;
  max-height: 100dvh;
  border-radius: 0;
}

.msheet-overlay.is-centered {
  align-items: center;
  padding: 20px;
}

.msheet.is-centered {
  width: min(360px, 100%);
  max-width: 100%;
  max-height: calc(100vh - 40px);
  max-height: calc(100dvh - 40px);
  border-radius: 14px;
  box-shadow: 0 18px 50px rgba(15, 40, 70, 0.22);
}

.msheet-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f6;
}

.msheet.is-fullscreen .msheet-header {
  padding-top: calc(14px + env(safe-area-inset-top, 0px));
}

.msheet-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: #2b3a4a;
  font-size: 1rem;
  font-weight: 600;
}

.msheet-close {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #8a9bab;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.msheet-close:active {
  background: rgba(75, 155, 210, 0.12);
  color: rgb(50, 120, 185);
}

.msheet-body {
  flex: 1;
  min-height: 0;
  padding: 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 0px));
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-width: none;
}

.msheet-body::-webkit-scrollbar {
  display: none;
}

.msheet.no-scroll .msheet-body {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.msheet-enter-active,
.msheet-leave-active {
  transition: opacity 0.22s ease;
}

.msheet-enter-active .msheet,
.msheet-leave-active .msheet {
  transition: transform 0.28s cubic-bezier(0.22, 0.61, 0.36, 1);
}

.msheet-enter-from,
.msheet-leave-to {
  opacity: 0;
}

.msheet-enter-from .msheet,
.msheet-leave-to .msheet {
  transform: translateY(100%);
}

.msheet-enter-from .msheet.is-centered,
.msheet-leave-to .msheet.is-centered {
  transform: translateY(10px) scale(0.96);
}
</style>
