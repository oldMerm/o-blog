<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{
    (e: 'toast', msg: string, icon?: string): void;
}>();

const steveQuotes = [
    '不要垂直向下挖！ ⛏️',
    '欢迎来到oldmerman的世界！ 🌲',
    '来创造新的世界吧... ✨',
    '钻石！！！ 💎',
    '看那发生了爆炸事故！ 💥'
];

const text = ref('Hi, Traveler! 👋');
const scaling = ref(false);
let quoteIndex = 0;

const interact = () => {
    quoteIndex = (quoteIndex + 1) % steveQuotes.length;
    text.value = steveQuotes[quoteIndex]!;
    scaling.value = true;
    setTimeout(() => {
        scaling.value = false;
    }, 180);
    emit('toast', `Steve: "${text.value}"`, '⛏️');
};
</script>

<template>
    <div class="px-steve" @click="interact">
        <div class="px-steve-bubble" :class="{ scale: scaling }">
            <span>{{ text }}</span>
            <div class="px-steve-arrow"></div>
        </div>
        <div class="px-steve-body">
            <div class="px-steve-box">
                <div class="css-steve-pixel"></div>
            </div>
        </div>
    </div>
</template>
