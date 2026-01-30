<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import Dialog from '@/text/dia/Dialog.vue';

// --- 1. TS 接口定义 (Interface) ---
interface User {
  id: number;
  username: string;
  articleCount: number;
  status: number; // 限制状态类型
  updatedAt: string;
  attr: string;
  isVisable: boolean;
}

// --- 2. 假数据 (Mock Data) ---
const userList = ref<User[]>([]);

// --- 3. 分页逻辑 ---
const currentPage = ref(1);
const pageSize = ref(7); // 每页显示5条

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return userList.value.slice(start, end);
});

// --- 4. 业务逻辑 (Action Handlers) ---

// 切换用户状态
const toggleStatus = (user: User) => {
  // 实际项目中这里会调用 API
  user.status = -user.status;
  console.log(`用户 ${user.id} 状态已更新为: ${user.status}`);
};

// 删除用户
const handleDelete = (id: number) => {
  if (confirm('确定要彻底删除该账户吗？此操作不可恢复。')) {
    // 过滤掉被删除的ID，更新列表
    userList.value = userList.value.filter(u => u.id !== id);

    // 如果当前页删空了，且不是第一页，自动回到上一页
    if (paginatedUsers.value.length === 0 && currentPage.value > 1) {
      currentPage.value--;
    }

    console.log(`用户 ID: ${id} 已被删除`);
  }
};

const page = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/usr/page', {
      params: {
        current: currentPage.value,
        size: pageSize.value
      }
    });
    if (res.code !== 200) {
      alert(`系统发生错误:${res.message}`);
      return;
    } else {
      userList.value = res.data.records;
    }
  } catch (error) {
    alert(error);
    console.log(error);
  }
}

watch(currentPage, (newPage, oldPage) => {
  console.log(`页码从${oldPage}变为${newPage}，将请求数据...`);
  page();
}, { immediate: true });

</script>

<template>
  <!-- 遵循之前定义的规范：最外层容器撑满，白色背景，圆角 -->
  <div class="page-container">

    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <h3 class="page-title">用户管理</h3>
      <div class="actions">
        <!-- 只是装饰用的搜索框 -->
        <input type="text" placeholder="搜索用户名..." class="search-input" />
        <button class="btn-primary">搜索</button>
      </div>
    </div>

    <!-- 数据表格区域 -->
    <div class="table-wrapper">
      <table class="custom-table">
        <thead>
          <tr>
            <th style="width: 80px;">ID</th>
            <th>用户名</th>
            <th>文章数量</th>
            <th>账号状态</th>
            <th>更新时间</th>
            <th style="text-align: right;">操作</th>
          </tr>
        </thead>
        <tbody>
          <!-- 循环渲染分页后的数据 -->
          <tr v-for="user in paginatedUsers" :key="user.id">
            <td class="text-light">#{{ String(user.id).slice(0, -5) + '***' }}</td>

            <td class="font-medium">
              <div class="user-info">
                <!-- 模拟头像 -->
                <div class="avatar-circle">
                  <img :src="user.attr" alt="用户头像" class="avatar">
                </div>
                {{ user.username }}
              </div>
            </td>

            <td>{{ user.articleCount }} 篇</td>

            <td>
              <!-- 点击切换状态 -->
              <span class="status-badge" :class="user.status === 1 ? 'status-active' : 'status-frozen'"
                @click="user.isVisable = true" title="点击切换状态">
                {{ user.status === 1 ? '正常' : '冻结' }}
              </span>
              <Dialog 
                v-model="user.isVisable" 
                title="确认修改用户状态" 
                content="该操作会导致用户被停用或解除停用，是否继续？" 
                @confirm="toggleStatus(user)" />
            </td>

            <td class="text-light">{{ user.updatedAt }}</td>

            <td style="text-align: right;">
              <button class="btn-text btn-delete" @click="handleDelete(user.id)">
                彻底删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 📄 底部底部分页栏 -->
    <div class="pagination-footer">
      <span class="total-info">共 {{ userList.length }} 条数据</span>
      <div class="pagination-controls">
        <button :disabled="currentPage === 1" @click="currentPage--" class="page-btn">
          上一页
        </button>
        <span class="page-number">第 {{ currentPage }} 页</span>
        <button :disabled="currentPage * pageSize >= userList.length" @click="currentPage++" class="page-btn">
          下一页
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* --- 🎨 颜色变量 (与主框架保持一致) --- */
.page-container {
  --primary-blue: #3b82f6;
  /* 主蓝 */
  --light-blue-bg: #eff6ff;
  /* 极浅蓝背景 (表头/Hover) */
  --border-color: #e5e7eb;
  /* 边框灰 */
  --text-main: #1f2937;
  --text-sub: #6b7280;
  --danger: #ef4444;
  /* 删除红 */
  --success: #10b981;
  /* 状态绿 */

  background-color: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-height: calc(100vh - 110px);
  /* 保证高度合适 */
  display: flex;
  flex-direction: column;
}

/* 顶部栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
  border-left: 4px solid var(--primary-blue);
  padding-left: 12px;
}

.actions {
  display: flex;
  gap: 12px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  color: var(--text-main);
}

.search-input:focus {
  border-color: var(--primary-blue);
}

.btn-primary {
  background-color: var(--primary-blue);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.btn-primary:hover {
  background-color: #2563eb;
}

/* 表格样式 - 蓝白风格核心 */
.table-wrapper {
  flex: 1;
  /* 撑开剩余空间 */
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.custom-table th {
  text-align: left;
  padding: 14px 16px;
  background-color: var(--light-blue-bg);
  /* 浅蓝表头 */
  color: var(--text-main);
  font-weight: 600;
  border-bottom: 1px solid var(--primary-blue);
  /* 稍微深一点的蓝底线 */
}

.custom-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f3f4f6;
  color: var(--text-main);
  transition: background 0.2s;
}

.custom-table tr:hover td {
  background-color: #f9fafb;
  /* Hover 极淡灰 */
}

/* 用户信息列特殊样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-circle {
  width: 30px;
  height: 30px;
  background-color: var(--light-blue-bg);
  color: var(--primary-blue);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  /* 圆形 */
  object-fit: cover;
  border: 2px solid #fff;
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  user-select: none;
}

.status-active {
  background-color: #ecfdf5;
  color: var(--success);
  border: 1px solid #d1fae5;
}

.status-frozen {
  background-color: #fef2f2;
  color: var(--text-sub);
  border: 1px solid #fee2e2;
}

/* 辅助文字 */
.text-light {
  color: var(--text-sub);
  font-size: 13px;
}

.font-medium {
  font-weight: 500;
}

/* 按钮样式 */
.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
}

.btn-delete {
  color: var(--danger);
}

.btn-delete:hover {
  background-color: #fef2f2;
}

/* 分页栏 */
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.total-info {
  font-size: 13px;
  color: var(--text-sub);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-btn {
  background: white;
  border: 1px solid var(--border-color);
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-main);
  font-size: 13px;
}

.page-btn:disabled {
  background-color: #f3f4f6;
  color: #d1d5db;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  border-color: var(--primary-blue);
  color: var(--primary-blue);
}

.page-number {
  font-size: 13px;
  font-weight: 500;
}
</style>
