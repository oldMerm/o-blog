<script setup lang="ts">
import { ref } from 'vue'
import PixelAgentChatDialog from './PixelAgentChatDialog.vue'

const emit = defineEmits<{
  (e: 'toast', msg: string, icon?: string): void
}>()

const showDialog = ref(false)

const openChat = () => {
  const token = localStorage.getItem('token') || localStorage.getItem('refreshToken')
  if (!token) {
    emit('toast', '请先登录后再使用 AI 助手！', '🔒')
    return
  }
  showDialog.value = true
}
</script>

<template>
  <article class="px-ai-card" title="召唤像素 AI 助手" @click="openChat">
    <div>
      <div class="px-ai-head">
        <span class="px-ai-tag">AI ASSISTANT</span>
        <span class="px-ai-state">● READY</span>
      </div>

      <div class="px-ai-body">
        <div class="px-ai-avatar" aria-hidden="true">
          <div class="px-ai-face">
            <span class="px-ai-antenna"></span>
            <span class="px-ai-ear left"></span>
            <span class="px-ai-ear right"></span>
            <span class="px-ai-eye left"></span>
            <span class="px-ai-eye right"></span>
            <span class="px-ai-mouth"></span>
          </div>
        </div>

        <div class="px-ai-info">
          <div class="px-ai-name">oldmerman AI</div>
          <div class="px-ai-desc">博客档案 · 像素向导</div>
          <div class="px-ai-tags">
            <span>SSE</span>
            <span>RAG</span>
            <span>CHAT</span>
          </div>
        </div>
      </div>
    </div>

    <div class="px-ai-foot">
      <span class="px-ai-model">MODEL: PIXEL_AI_OS</span>
      <button class="px-ai-start" type="button" @click.stop="openChat">START →</button>
    </div>
  </article>

  <PixelAgentChatDialog v-model="showDialog" />
</template>

<style scoped>
.px-ai-card {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 176px;
  padding: 16px;
  overflow: hidden;
  border: 4px solid var(--px-grass, #6aa82f);
  background: linear-gradient(to bottom right, #f2fae8, #ffffff, #e3f4d2);
  box-shadow: 4px 4px 0 0 var(--px-borderDark, #12263a);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.px-ai-card:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0 0 var(--px-borderDark, #12263a);
}

.px-ai-card::after {
  content: '';
  position: absolute;
  right: -12px;
  top: -12px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: rgba(106, 168, 47, 0.22);
  filter: blur(4px);
  pointer-events: none;
}

.px-ai-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.px-ai-tag {
  padding: 2px 8px;
  border: 1px solid var(--px-borderDark, #12263a);
  background-color: var(--px-grass, #6aa82f);
  box-shadow: 2px 2px 0 0 var(--px-borderDark, #12263a);
  color: #11111b;
  font-family: var(--font-pixel, 'Press Start 2P', 'Silkscreen', monospace);
  font-size: 9px;
}

.px-ai-state {
  color: #059669;
  font-family: var(--font-pixel, 'Press Start 2P', 'Silkscreen', monospace);
  font-size: 10px;
  font-weight: 700;
}

.px-ai-body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.px-ai-avatar {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border: 2px solid var(--px-borderDark, #12263a);
  background-color: var(--px-grass, #6aa82f);
  box-shadow: 2px 2px 0 0 var(--px-borderDark, #12263a);
}

.px-ai-face {
  position: relative;
  width: 34px;
  height: 26px;
  border: 2px solid var(--px-borderDark, #12263a);
  background-color: #e3f4d2;
}

.px-ai-antenna {
  position: absolute;
  top: -10px;
  left: 50%;
  width: 4px;
  height: 9px;
  background-color: var(--px-borderDark, #12263a);
  transform: translateX(-50%);
}

.px-ai-antenna::before {
  content: '';
  position: absolute;
  top: -6px;
  left: -3px;
  width: 6px;
  height: 5px;
  border: 2px solid var(--px-borderDark, #12263a);
  background-color: var(--px-yellow, #facc15);
}

.px-ai-ear {
  position: absolute;
  top: 7px;
  width: 4px;
  height: 8px;
  border: 2px solid var(--px-borderDark, #12263a);
  background-color: var(--px-grass, #6aa82f);
}

.px-ai-ear.left {
  left: -6px;
}

.px-ai-ear.right {
  right: -6px;
}

.px-ai-eye {
  position: absolute;
  top: 5px;
  width: 7px;
  height: 6px;
  border: 2px solid var(--px-borderDark, #12263a);
  background-color: #22c55e;
  animation: px-ai-blink 3.2s steps(1) infinite;
}

.px-ai-eye.left {
  left: 4px;
}

.px-ai-eye.right {
  right: 4px;
}

.px-ai-mouth {
  position: absolute;
  bottom: 4px;
  left: 50%;
  width: 12px;
  height: 3px;
  background-color: var(--px-borderDark, #12263a);
  transform: translateX(-50%);
}

.px-ai-info {
  min-width: 0;
  overflow: hidden;
}

.px-ai-name {
  overflow: hidden;
  color: var(--px-borderDark, #12263a);
  font-family: var(--font-pixel, 'Press Start 2P', 'Silkscreen', monospace);
  font-size: 12px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.px-ai-desc {
  overflow: hidden;
  color: #475569;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.px-ai-tags {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.px-ai-tags span {
  padding: 0 4px;
  border: 1px solid #b7d98c;
  background-color: #ffffff;
  color: #3f6212;
  font-family: var(--font-pixel, 'Press Start 2P', 'Silkscreen', monospace);
  font-size: 8px;
}

.px-ai-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(106, 168, 47, 0.65);
  color: #475569;
  font-family: var(--font-pixel, 'Press Start 2P', 'Silkscreen', monospace);
  font-size: 9px;
}

.px-ai-model {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.px-ai-start {
  flex-shrink: 0;
  padding: 5px 8px;
  border: 2px solid var(--px-borderDark, #12263a);
  background-color: var(--px-grass, #6aa82f);
  box-shadow: 2px 2px 0 0 var(--px-borderDark, #12263a);
  color: #11111b;
  font-family: inherit;
  font-size: 9px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.1s ease;
}

.px-ai-start:hover {
  transform: translate(-1px, -1px);
  box-shadow: 3px 3px 0 0 var(--px-borderDark, #12263a);
}

.px-ai-start:active {
  transform: translate(2px, 2px);
  box-shadow: none;
}

@keyframes px-ai-blink {
  0%,
  92%,
  100% {
    height: 6px;
    top: 5px;
  }
  94%,
  98% {
    height: 2px;
    top: 7px;
  }
}

@media (max-width: 900px) {
  .px-ai-card {
    min-height: 160px;
  }
}
</style>
