<script setup lang="ts">
const emit = defineEmits<{
    (e: 'switch-style'): void;
    (e: 'scroll-user'): void;
}>();
</script>

<template>
    <header class="px-header">
        <a href="#top" class="px-logo">
            <div class="px-logo-badge">O</div>
            <span class="px-logo-text">oldmerman</span>
        </a>

        <div class="px-actions">
            <!-- Style：切回经典风格 -->
            <button class="px-btn" title="切换为旧风格" @click="emit('switch-style')">
                <svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="1" width="10" height="13" fill="#d49b6a" />
                    <rect x="1" y="3" width="14" height="9" fill="#d49b6a" />
                    <path d="M4 1H12V3H14V5H15V11H13V13H11V14H5V13H3V11H1V5H2V3H4V1Z" fill="#12263a" />
                    <rect x="3" y="3" width="10" height="9" fill="#e8bb8a" />
                    <rect x="9" y="8" width="3" height="3" fill="#12263a" />
                    <rect x="10" y="9" width="1" height="1" fill="#badcff" />
                    <rect x="4" y="4" width="2" height="2" fill="#ef4444" />
                    <rect x="8" y="3" width="2" height="2" fill="#00aaaa" />
                    <rect x="4" y="8" width="2" height="2" fill="#22c55e" />
                    <rect x="7" y="6" width="2" height="2" fill="#eab308" />
                </svg>
                <span>Style</span>
            </button>

            <!-- User -->
            <button class="px-btn" title="用户信息" @click="emit('scroll-user')">
                <svg viewBox="0 0 24 24" fill="currentColor">
                    <path
                        d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                </svg>
                <span>User</span>
            </button>

            <!-- GitHub -->
            <a href="https://github.com/oldMerm" target="_blank" rel="noopener noreferrer" class="px-btn" title="GitHub">
                <svg viewBox="0 0 24 24" fill="currentColor">
                    <path fill-rule="evenodd" clip-rule="evenodd"
                        d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
                </svg>
                <span>GitHub</span>
            </a>
        </div>
    </header>
</template>
