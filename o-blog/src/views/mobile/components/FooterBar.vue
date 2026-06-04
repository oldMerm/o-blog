<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { httpInstance, type Response } from '@/utils/http'
import UpdateModel from '@/views/Home/utils/UpdateModel.vue';

const versionData = ref<{ versionId: string; versionContent: string } | null>(null)

onMounted(() => {
  fetchVersion()
})

const isModalVisible = ref(false); // 单独控制弹窗显隐

const fetchVersion = async () => {
  const res = await httpInstance.get<any, Response>("/version");

  if (res.code !== 200) {
    return;
  }

  versionData.value = res.data;

  if (versionData.value?.versionId) {
    const currentVersionId = versionData.value.versionId;
    const localVersion = localStorage.getItem("version_news");

    if (localVersion !== currentVersionId) {
      isModalVisible.value = true;
      localStorage.setItem("version_news", currentVersionId);
    }
  }
}
</script>

<template>
  <UpdateModel v-model="isModalVisible" :title="versionData?.versionId" :content="versionData?.versionContent" />
  <footer class="footer-bar">
    <div class="footer-inner">
      <span class="version-line" @click="isModalVisible = true">
        © 版本号: {{ versionData?.versionId || '获取中...' }}
      </span>
      <a href="http://beian.miit.gov.cn" class="beian-link" target="_blank" rel="noopener noreferrer">
        粤ICP备2026023638号-1
      </a>
      <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44196202000120" class="beian-link"
        target="_blank" rel="noopener noreferrer">
        <img src="https://beian.mps.gov.cn/img/logo01.dd7ff50e.png" alt="" class="beian-icon" />
        粤公网安备44196202000120号
      </a>
    </div>
  </footer>
</template>

<style scoped>
.footer-bar {
  background: rgb(28, 32, 38);
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.7rem;
  margin-top: 8px;
}

.footer-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 5px 20px calc(8px + env(safe-area-inset-bottom, 0px));
}

.version-line {
  font-size: 0.72rem;
  color: rgba(255, 255, 255, 0.5);
}

.beian-link {
  color: rgba(255, 255, 255, 0.45);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
  font-size: 0.7rem;
}

.beian-link:active {
  color: rgba(255, 255, 255, 0.8);
}

.beian-icon {
  width: 13px;
  height: 14px;
  vertical-align: middle;
}
</style>
