<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import Toast from '@/utils/toast/Toast.vue'
import MobileUserPanel from './MobileUserPanel.vue'
import MobileHistoryPanel from './MobileHistoryPanel.vue'
import MobileAssistant from './MobileAssistant.vue'

const githubUrl = 'https://github.com/oldMerm'

const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)

const showUser = ref(false)
const showHistory = ref(false)
const showAssistant = ref(false)

const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const toastMsg = ref('')

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value
}

const openUser = () => {
  menuOpen.value = false
  showUser.value = true
}

const openHistory = () => {
  menuOpen.value = false
  showHistory.value = true
}

const openAssistant = () => {
  menuOpen.value = false
  if (!localStorage.getItem('token')) {
    toastMsg.value = '请先登录后再使用 AI 助手'
    toastRef.value?.show()
    return
  }
  showAssistant.value = true
}

const onDocumentPointerDown = (event: PointerEvent) => {
  if (!menuOpen.value) return
  const target = event.target as Node | null
  if (menuRef.value && target && !menuRef.value.contains(target)) {
    menuOpen.value = false
  }
}

const onKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') menuOpen.value = false
}

onMounted(() => {
  document.addEventListener('pointerdown', onDocumentPointerDown)
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('pointerdown', onDocumentPointerDown)
  window.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <header class="topbar">
    <span class="brand">oldmerman</span>

    <div ref="menuRef" class="menu-wrap">
      <button
        class="menu-btn"
        :class="{ active: menuOpen }"
        type="button"
        aria-label="打开菜单"
        :aria-expanded="menuOpen"
        @click="toggleMenu"
      >
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="M4 7h16M4 12h16M4 17h16"
            stroke="currentColor"
            stroke-width="2.2"
            stroke-linecap="round"
          />
        </svg>
      </button>

      <Transition name="menu">
        <div v-if="menuOpen" class="dropdown" role="menu" aria-label="导航菜单">
          <a
            :href="githubUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="menu-item"
            role="menuitem"
            @click="menuOpen = false"
          >
            <svg class="menu-icon" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
              <path
                d="M8 0c4.42 0 8 3.58 8 8a8.013 8.013 0 0 1-5.45 7.59c-.4.08-.55-.17-.55-.38 0-.27.01-1.13.01-2.2 0-.75-.25-1.23-.54-1.48 1.78-.2 3.65-.88 3.65-3.95 0-.88-.31-1.59-.82-2.15.08-.2.36-1.02-.08-2.12 0 0-.67-.22-2.2.82-.64-.18-1.32-.27-2-.27-.68 0-1.36.09-2 .27-1.53-1.03-2.2-.82-2.2-.82-.44 1.1-.16 1.92-.08 2.12-.51.56-.82 1.28-.82 2.15 0 3.06 1.86 3.75 3.64 3.95-.23.2-.44.55-.51 1.07-.46.21-1.61.55-2.33-.66-.15-.24-.6-.83-1.23-.82-.67.01-.27.38.01.53.34.19.73.9.82 1.13.16.45.68 1.31 2.69.94 0 .67.01 1.3.01 1.49 0 .21-.15.45-.55.38A7.995 7.995 0 0 1 0 8c0-4.42 3.58-8 8-8Z"
              />
            </svg>
            <span>GitHub</span>
          </a>

          <button class="menu-item" type="button" role="menuitem" @click="openUser">
            <svg class="menu-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path
                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4Zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4Z"
              />
            </svg>
            <span>用户</span>
          </button>

          <button class="menu-item" type="button" role="menuitem" @click="openHistory">
            <svg class="menu-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path
                d="M13 3a9 9 0 1 0 8.94 10.06 1 1 0 0 0-1.99-.2A7 7 0 1 1 13 5v2.5a.5.5 0 0 0 .82.38l3.68-3a.5.5 0 0 0 0-.76l-3.68-3A.5.5 0 0 0 13 1.5V3Z"
              />
              <path
                d="M12 8a1 1 0 0 1 1 1v3.38l2.3 1.53a1 1 0 1 1-1.1 1.67l-2.7-1.8A1 1 0 0 1 11 13V9a1 1 0 0 1 1-1Z"
              />
            </svg>
            <span>历史</span>
          </button>

          <button class="menu-item" type="button" role="menuitem" @click="openAssistant">
            <svg
              class="menu-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path
                d="M21 11.5a8.4 8.4 0 0 1-8.5 8.3 8.7 8.7 0 0 1-3.7-.8L4 20l1.1-4a8.2 8.2 0 0 1-1.1-4.1A8.4 8.4 0 0 1 12.5 3 8.4 8.4 0 0 1 21 11.5Z"
              />
              <path d="M8.8 11h.01M12.5 11h.01M16.2 11h.01" />
            </svg>
            <span>网页助手</span>
          </button>
        </div>
      </Transition>
    </div>

    <MobileUserPanel v-model="showUser" />
    <MobileHistoryPanel v-model="showHistory" />
    <MobileAssistant v-model="showAssistant" />
  </header>

  <Toast ref="toastRef" :message="toastMsg" type="error" />
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital@1&display=swap');
</style>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: rgba(235, 235, 235, 0.75);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.brand {
  font-size: 1.25rem;
  color: rgb(46, 46, 46);
  letter-spacing: 0.04em;
}

.menu-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.menu-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: rgb(46, 46, 46);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.menu-btn svg {
  width: 24px;
  height: 24px;
}

.menu-btn:active,
.menu-btn.active {
  background: rgba(75, 155, 210, 0.14);
  color: rgb(48, 118, 184);
}

.dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  z-index: 120;
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 192px;
  padding: 6px;
  border: 1px solid rgba(180, 210, 240, 0.45);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 12px 30px rgba(40, 80, 120, 0.18);
  backdrop-filter: blur(10px);
  transform-origin: top right;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 11px 12px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: rgb(58, 74, 90);
  font-size: 0.88rem;
  font-weight: 500;
  text-align: left;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.18s, color 0.18s;
}

.menu-item:active {
  background: rgba(75, 155, 210, 0.12);
  color: rgb(48, 118, 184);
}

.menu-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  color: rgb(90, 130, 165);
}

.menu-item:active .menu-icon {
  color: rgb(48, 118, 184);
}

.menu-enter-active,
.menu-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.menu-enter-from,
.menu-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.96);
}
</style>
