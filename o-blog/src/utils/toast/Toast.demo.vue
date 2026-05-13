<template>
  <button @click="triggerToast('success')">修改成功</button>
  <button @click="triggerToast('error')">修改失败</button>
  
  <Toast 
    ref="toastRef" 
    :message="msg" 
    :type="type" 
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Toast from './Toast.vue';

const toastRef = ref();
const msg = ref('');
const type = ref<'success' | 'error'>('success');

const triggerToast = (status: 'success' | 'error') => {
  type.value = status;
  msg.value = status === 'success' ? '用户信息更新成功！' : '系统繁忙，更新失败。';
  toastRef.value?.show();
};
</script>
