<template>
  <button @click="visible = true">删除</button>
  <!-- 
  visible：必传bool，用来判断弹窗开关
  title：弹窗左上角的标题
  content：弹窗主要的内容
  -->
  <Dialog
    v-model="visible"
    title="确认删除"  
    content="删除后数据无法恢复，是否继续？"
    @confirm="handleDelete"
  />
</template>

<script setup>
import { ref } from 'vue'
import Dialog from './Dialog.vue'

const visible = ref(false);

const handleDelete = () => {
  // 这里执行你需要的逻辑
  console.log('执行删除');
}
</script>