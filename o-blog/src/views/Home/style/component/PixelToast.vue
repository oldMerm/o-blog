<script setup lang="ts">
import { ref } from 'vue';

const visible = ref(false);
const msg = ref('');
const icon = ref('💡');
let timer: ReturnType<typeof setTimeout> | null = null;

const show = (text: string, ic = '💡') => {
    msg.value = text;
    icon.value = ic;
    visible.value = true;
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
        visible.value = false;
    }, 3200);
};

defineExpose({ show });
</script>

<template>
    <div class="px-toast" :class="{ show: visible }">
        <div class="px-toast-inner">
            <span class="px-toast-icon">{{ icon }}</span>
            <span class="px-toast-msg">{{ msg }}</span>
        </div>
    </div>
</template>
