<template>
  <div class="home-page">
    <button @click="showUpdate = true">检查更新</button>

    <!-- 引入弹窗组件 -->
    <UpdateModal 
      v-model="showUpdate"
      title="V2.0 版本更新速递"
      :content="updateText"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import UpdateModal from './UpdateModel.vue'; // 替换为你的路径

const showUpdate = ref(false);

// 你可以自由传入带有 \n 换行的字符串，组件内会自动渲染换行
const updateText = ref(
  "亲爱的用户，本次更新为您带来以下新特性：\n\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "1. 全新设计的蓝白主题界面。\n" +
  "2. 优化了系统的加载速度。\n" +
  "3. 修复了若干已知 Bug。\n\n" +
  "感谢您的支持！"
);
</script>
