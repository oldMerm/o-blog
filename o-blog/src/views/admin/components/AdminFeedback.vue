<script setup lang="ts">
import { httpInstance, type Response } from '@/utils/http';
import { ref, watch } from 'vue';

// 实际开发中可能从后端字典接口获取
const REASON_MAP: Record<number, string> = {
  1: '文章内容劣质',
  2: '文章内容有误',
  3: '网页体验',
  4: '侵权投诉',
  5: '和作者吹水'
};

const getReasonText = (id: number) => REASON_MAP[id] || '未知类型';

// 分页逻辑
interface FeedbackVO {
  id: string;
  replier: string;
  feedbackType: number[];
  feedback: string;
  reply: string | null;
  repliedAt: string | null;
  username: string;
  status: 'replied' | 'unreplied' | null; 
}
const FeedbackVOS = ref<FeedbackVO[]>([]);
const currentPage = ref(1);
const pageSize = ref(7);
const pageCount = ref();
watch(
  currentPage,
  async (newVal) => {
    try {
      const res = await httpInstance.get<any, Response>('/feedback/page', {
        params: {
          current: newVal,
          size: pageSize.value
        }
      });
      if (res.code !== 200) {
        alert(`出现错误:${res.message}`);
      } else {
        FeedbackVOS.value = transformFeedbackList(res.data.records);
        pageCount.value = res.data.pages;
        console.log(FeedbackVOS.value);
        
      }
    } catch (error) {
      alert(error);
    }
  },
  { immediate: true }  // 立即执行
)

// 转换函数
const transformFeedbackList = (records: any[]): FeedbackVO[] => {
  return records.map((item): FeedbackVO => ({
    ...item,
    // 处理 status：reply 为 null 时是 'unreplied'，否则 'replied'
    // 注意：你说反了，reply 为 null 应该是未回复 'unreplied'，有回复才是 'replied'
    status: item.reply === null || item.reply === undefined || item.reply === '' 
      ? 'unreplied' 
      : 'replied',
    
    // 处理 feedbackType：字符串 "1,2,3" 转数字数组 [1, 2, 3]
    feedbackType: item.feedbackType 
      ? item.feedbackType.split(',').map((num: string) => parseInt(num.trim(), 10))
      : []
  }));
};

// --- 弹窗逻辑 ---
const showModal = ref(false);
const currentItem = ref<FeedbackVO | null>(null);
const replyInput = ref(''); // 绑定输入框内容

const openDetailModal = (item: FeedbackVO) => {
  currentItem.value = item;
  // 如果已回复，不需要填充输入框；如果未回复，清空输入框
  replyInput.value = '';
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  currentItem.value = null;
};

const handleSaveReply = async (id: string) => {
  if (!replyInput.value.trim()) {
    alert('请输入回复内容');
    return;
  }

  if (currentItem.value) {
    try {
      const param = {id: id, cont: replyInput.value}
      const res = await httpInstance.post<any, Response>('/feedback/save', param);

      if(res.code !== 200){
        alert(`程序发生错误:${res.message}`);
        closeModal();
      }else{
        currentItem.value.status = 'replied';
        currentItem.value.reply = replyInput.value;
        alert('回复成功！');
        closeModal();
      }
    } catch (error) {
      alert(error);
    }
  }
};
</script>

<template>
  <div class="page-container">
    <!-- 1. 顶部标题 (无搜索框) -->
    <div class="header-row">
      <h3>用户反馈管理</h3>
      <span class="subtitle">按时间倒序排列</span>
    </div>

    <!-- 2. 反馈列表表格 -->
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th width="80">ID</th>
            <th width="150">反馈人</th>
            <th>反馈类型 (Tag)</th>
            <th width="120">状态</th>
            <th width="180">提交时间</th>
            <th width="100">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in FeedbackVOS" :key="item.id">
            <td>{{ item.id }}</td>
            <td class="username">{{ item.username }}</td>

            <!-- 核心功能：ID 集合展示 + 悬停提示 -->
            <td>
              <div class="tags-wrapper">
                <div v-for="rId in item.feedbackType" :key="rId" class="reason-tag">
                  {{ rId }}
                  <!-- 纯 CSS 实现的 Tooltip -->
                  <span class="tooltip-text">{{ getReasonText(rId) }}</span>
                </div>
              </div>
            </td>

            <!-- 状态 -->
            <td>
              <span class="status-badge" :class="item.status">
                {{ item.status === 'replied' ? '已回复' : '待处理' }}
              </span>
            </td>

            <td class="time">{{ item.repliedAt }}</td>

            <!-- 操作 -->
            <td>
              <button class="btn-text" @click="openDetailModal(item)">
                查看详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 3. 分页控件 (UI 演示) -->
    <div class="pagination">
      <button :disabled="currentPage === 1" @click="currentPage--">上一页</button>
      <span class="page-info">{{ currentPage }}/{{ pageCount }}</span>
      <button :disabled="currentPage * pageSize >= pageCount" @click="currentPage++">下一页</button>
    </div>

    <!-- 4. 详情与回复弹窗 (Modal) -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h4>反馈详情 #{{ currentItem?.id }}</h4>
          <span class="close-btn" @click="closeModal">×</span>
        </div>

        <div class="modal-body" v-if="currentItem">
          <!-- 上部分：用户反馈内容 -->
          <div class="section user-section">
            <label>用户 {{ currentItem.username }} 的反馈：</label>
            <div class="info-box">
              <div class="reason-text-list">
                <span class="label-tip">反馈类型：</span>
                {{currentItem.feedbackType.map(id => getReasonText(id)).join('，')}}
              </div>
              <p class="content-text">{{ currentItem.feedback }}</p>
            </div>
          </div>

          <!-- 下部分：管理员回复区域 -->
          <div class="section admin-section">
            <label>管理员回复：</label>

            <!-- 场景 A: 已回复 (只读展示) -->
            <div v-if="currentItem.status === 'replied'" class="reply-box readonly">
              <p>{{ currentItem.reply }}</p>
              <div class="reply-time">回复于：{{ currentItem.repliedAt }}</div>
            </div>

            <!-- 场景 B: 未回复 (输入框 + 按钮) -->
            <div v-else class="reply-box editable">
              <textarea v-model="replyInput" placeholder="请输入您的回复内容..." rows="4"></textarea>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">关闭</button>
          <!-- 仅在未回复状态显示保存按钮 -->
          <button v-if="currentItem?.status === 'unreplied'" class="btn-save" @click="handleSaveReply(currentItem.id)">
            保存回复
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 延续之前的蓝白风格 */
.page-container {
  background-color: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.header-row {
  margin-bottom: 24px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 16px;
}

.header-row h3 {
  margin: 0;
  color: #1f2937;
  display: inline-block;
  margin-right: 12px;
}

.subtitle {
  font-size: 12px;
  color: #9ca3af;
}

/* --- 表格样式 --- */
.table-container {
  flex: 1;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 12px 16px;
  background-color: #f9fafb;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

td {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  color: #374151;
  font-size: 14px;
}

.username {
  font-weight: 500;
  color: #1e3a8a;
  /* 深蓝强调 */
}

.time {
  color: #9ca3af;
  font-size: 13px;
}

/* --- 核心：标签与Tooltip样式 --- */
.tags-wrapper {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.reason-tag {
  background-color: #eff6ff;
  /* 浅蓝背景 */
  color: #3b82f6;
  /* 亮蓝文字 */
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: help;
  /* 鼠标样式：问号 */
  position: relative;
  /* 为绝对定位的tooltip做参照 */
}

/* Tooltip 文本默认隐藏 */
.tooltip-text {
  visibility: hidden;
  width: 120px;
  background-color: #1e3a8a;
  /* 深蓝背景 */
  color: #fff;
  text-align: center;
  border-radius: 6px;
  padding: 5px;

  /* 定位 */
  position: absolute;
  z-index: 10;
  bottom: 125%;
  /* 位于标签上方 */
  left: 50%;
  margin-left: -60px;
  /* 居中 */

  /* 动画 */
  opacity: 0;
  transition: opacity 0.3s;
  font-weight: normal;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 小三角箭头 */
.tooltip-text::after {
  content: "";
  position: absolute;
  top: 100%;
  left: 50%;
  margin-left: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: #1e3a8a transparent transparent transparent;
}

/* 悬停时显示 */
.reason-tag:hover .tooltip-text {
  visibility: visible;
  opacity: 1;
}

/* --- 状态 Badge --- */
.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.replied {
  background-color: #d1fae5;
  color: #059669;
  /* 绿色 */
}

.status-badge.unreplied {
  background-color: #f3f4f6;
  color: #6b7280;
  /* 灰色 */
}

.btn-text {
  background: none;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  font-size: 13px;
}

.btn-text:hover {
  text-decoration: underline;
}

/* --- 弹窗 Modal 样式 --- */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  background: white;
  width: 600px;
  max-width: 90%;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f9fafb;
}

.modal-header h4 {
  margin: 0;
  color: #1f2937;
}

.close-btn {
  font-size: 24px;
  cursor: pointer;
  color: #9ca3af;
}

.close-btn:hover {
  color: #4b5563;
}

.modal-body {
  padding: 24px;
}

.section {
  margin-bottom: 24px;
}

.section label {
  display: block;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.info-box,
.reply-box {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
}

.reason-text-list {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #e5e7eb;
}

.content-text {
  color: #1f2937;
  line-height: 1.6;
}

/* 回复区域样式 */
.reply-box.readonly {
  background-color: #eff6ff;
  /* 浅蓝背景表示已回复 */
  border-color: #bfdbfe;
  color: #1e40af;
}

.reply-time {
  margin-top: 10px;
  font-size: 12px;
  color: #60a5fa;
  text-align: right;
}

.reply-box textarea {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  padding: 8px;
  font-family: inherit;
  resize: vertical;
}

.reply-box textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  text-align: right;
  background-color: #f9fafb;
}

.btn-cancel {
  background: white;
  border: 1px solid #d1d5db;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  color: #374151;
}

.btn-save {
  background: #3b82f6;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  color: white;
}

.btn-save:hover {
  background: #2563eb;
}

/* 分页样式 (简单版) */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.pagination button {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

.pagination button:disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
}
</style>
