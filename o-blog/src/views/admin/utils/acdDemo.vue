<template>
  <button @click="showModal = true">打开操作框</button>

  <Modal 
    v-model:visible="showModal" 
    :onConfirm="handleAction" 
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Modal from './ArticleCheckDialog.vue';

const showModal = ref(false);

// 处理回调的逻辑
const handleAction = (type: 'publish' | 'unpublish') => {
  if (type === 'publish') {
    console.log("执行发布逻辑...");
    // 这里可以调用 API
  } else {
    console.log("执行下架逻辑...");
  }
};
</script>
