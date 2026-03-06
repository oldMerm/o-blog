<template>
  <div style="padding: 50px;">
    <button @click="openModal">发布新文章</button>
    
    <!-- 引入弹窗组件 -->
    <UploadModal ref="uploadModalRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import UploadModal from '../../views/Manage/utils/ContentDialog.vue';

const uploadModalRef = ref<InstanceType<typeof UploadModal> | null>(null);

const openModal = () => {
  uploadModalRef.value?.openModal();
};
</script>
