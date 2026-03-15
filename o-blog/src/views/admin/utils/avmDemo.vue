<template>
  <div class="admin-page">
    <button @click="showAddModal = true">发布新版本</button>

    <!-- 引入添加版本的弹窗组件 -->
    <AddVersionModel 
      v-model="showAddModal"
      @submit="handlePublish"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import AddVersionModel from './AddVersionModel.vue';

const showAddModal = ref(false);

// 接收弹窗传出的管理员填写数据
const handlePublish = async (payload: { versionId: string; versionContent: string }) => {
  try {
    // 1. 组装请求数据（这里 id, createdAt 等通常由后端生成，前端只传核心数据即可）
    const requestData = {
      versionId: payload.versionId,
      versionContent: payload.versionContent,
      objectName: 'sys_app' // 假设前端需要传的静态标识
    };

    console.log(requestData);

    // 2. 发送API请求
    // await api.publishVersion(requestData);

    // 3. 成功后的交互
    alert(`版本 ${payload.versionId} 发布成功！`);
    
    // 4. 关闭弹窗
    showAddModal.value = false;
    
    // 5. 刷新列表
    // loadVersionList();
  } catch (error) {
    console.error('发布失败', error);
  }
};
</script>
