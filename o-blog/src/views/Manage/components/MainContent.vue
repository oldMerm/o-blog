<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import { goToArticle, type Article } from '@/views/public/Article';
import UploadModal from '../utils/ContentDialog.vue';

onMounted(() => {
  getFeedback();
  getUserMdToRender();
})

// 假数据：生成多一点以展示滚动条效果
const statusMap = new Map([
  [1, "未审核"],
  [2, "已过审"],
  [3, "已发布"],
  [4, "已下架"]
]);
const articleList = ref<Article[]>([]);

/* 请求后端获取（该用户）md文件并渲染 */
const getUserMdToRender = async () => {
  const res = await httpInstance.get<any, Response>('/article/info');
  articleList.value = res.data;
}

interface FeedbackType {
  id: number;
  content: string;
}

// 反馈功能
const feedbackTypeList = ref<FeedbackType[]>([
  { id: 1, content: "文章内容劣质" },
  { id: 2, content: "文章内容有误" },
  { id: 3, content: "网页体验" },
  { id: 4, content: "侵权投诉" },
  { id: 5, content: "和作者吹水" },
]);

// 核心数组
const selectedIds = ref<number[]>([])

// 判断是否选中
const isSelected = (id: number) => selectedIds.value.includes(id);

// 点一次加，再点一次删
function toggle(id: number) {
  const idx = selectedIds.value.indexOf(id);
  if (idx === -1) {
    selectedIds.value.push(id);
  } else {
    selectedIds.value.splice(idx, 1);
  }
}

// 为反馈类型项添加点击处理
const handleFeedbackTypeClick = (id: number) => {
  toggle(id);
};

const feedbackContent = ref();

interface submitTable {
  selectIds: string;
  content: string;
}
const submitFeedback = async () => {
  if (feedbackContent.value.length >= 255) {
    alert("反馈内容信息过长！");
    return;
  }

  const req: submitTable = {
    selectIds: selectedIds.value.join(','),
    content: feedbackContent.value
  }
  try {
    const res = await httpInstance.post<any, Response>('/feedback', req);
    if (res.code === 200) {
      alert("感谢您的反馈，会尽快回复！");
      selectedIds.value = [];
      feedbackContent.value = '';
    } else {
      alert(res.message);
      return;
    }
  } catch (error) {
    alert(error);
  }
}

const deleteArticle = async () => {
  const articleName = prompt("请输入要删除的文章名:", "");
  if(articleName === "" || articleName === null){
    return;
  }
  // 这里执行删除文章逻辑(根据文章名)
  try {
    const res:Response = await httpInstance.delete(`/article/remove/${articleName}`);
    if(res.code !== 200){
      alert(`系统错误:${res.message}`);
    }
    getUserMdToRender();
  } catch (error) {
    alert(`系统错误:${error}`);
  }
}

const uploadModalRef = ref<InstanceType<typeof UploadModal> | null>(null);

const openModal = () => {
  uploadModalRef.value?.openModal();
};


// 反馈功能
const showFeedbackList = ref(false);

interface ReplyItem {
  id: number;
  replier: string;
  feedback: string; // 反馈内容
  reply: string;    // 回复内容
  repliedAt: string;
}

const mockData = ref<ReplyItem[]>([]);
const getFeedback = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/feedback/batch_info');

    if(res.code !== 200){
      alert(`发生错误：${res}`);
      return;
    }
    mockData.value = res.data;
  } catch (error) {
    alert(`系统错误：${error}`);
  }
}

// 计算属性：获取回复数量
const replyCount = computed(() => mockData.value.length);

// 切换弹框显示
const toggleFeedbackList = () => {
  if (replyCount.value > 0) {
    showFeedbackList.value = !showFeedbackList.value;
  }
};
</script>

<template>
  <!-- 区块3：文章列表 -->
  <div class="main-block article-list-container">
    <h3>我的文章列表</h3>
    <div class="scroll-area">
      <ul class="article-list">
        <li v-for="article in articleList" :key="article.id" class="article-item" @click="goToArticle(article.id, true)">
          <span class="article-title">{{ article.articleName }}</span>
          <span class="article-date">{{ statusMap.get(article.articleStatus) + '-' + article.createdAt }}</span>
        </li>
      </ul>
    </div>
  </div>

  <div class="add-feedback-block">
    <div class="main-block" style="margin-right: 30px;">
      <div class="o-content">
        <h3>文章管理与说明</h3>
        <p class="article-delinfo">
          1.格式使用markdown<br>
          2.只能创建和删除<br>
          3.若修改请重新上传<br>
          4.请勿上传非法内容<br>
          5.文章通过审核后发布<br>
          6.<span style="color: red;cursor: pointer;" @click="deleteArticle">点此红字</span>删除文章
        </p>
      </div>
    </div>
    <div class="main-block" style="margin-right: 30px;">
      <div class="add-content">
        <h3>添加文章</h3>
        <div class="add" @click="openModal">
          <img src="../../../static/add.svg" alt="">
        </div>
      </div>
    </div>
    <UploadModal ref="uploadModalRef" />
    <div class="main-block">
      <div class="feedback-content">
        <h3 style="display: block; width: 100%;">
          <span @click.stop="toggleFeedbackList" class="feedback">信息反馈</span>
          <sup v-if="replyCount > 0" class="reply-badge">{{ replyCount }}</sup>

          <div v-if="showFeedbackList" class="feedback-popover" @click.stop>
            <div class="popover-title">
              回复列表
              <span class="popover-close" @click.stop="showFeedbackList = false">&times;</span>
            </div>
            <div class="popover-content">
              <div v-for="item in mockData" :key="item.id" class="reply-item">
                <!-- 第一行：回复人 + 时间 -->
                <div class="item-meta">
                  <span class="replier">👤 {{ item.replier }}</span>
                  <span class="time">于{{ item.repliedAt }}</span>
                </div>
                <!-- 第二行：反馈内容 (原问题) -->
                <div class="item-row" :title="item.feedback">
                  <span class="label">问:</span> {{ item.feedback }}
                </div>
                <!-- 第三行：回复内容 -->
                <div class="item-row reply-text" :title="item.reply">
                  <span class="label">答:</span> {{ item.reply }}
                </div>
              </div>
            </div>
          </div>
        </h3>
        <div class="feedback-type">
          反馈类型
          <div class="feedback-type-choice">
            <div v-for="item in feedbackTypeList" :key="item.id" class="type-block"
              :class="{ 'type-active': isSelected(item.id) }" @click="handleFeedbackTypeClick(item.id)">
              {{ item.content }}
            </div>
          </div>
        </div>
        <div class="feedback-main">
          反馈内容<br>
          <textarea placeholder="请输入您的反馈内容(限255字)..." v-model="feedbackContent" class="feedback-textarea">
          </textarea>
        </div>
        <button class="submit" @click="submitFeedback">提交反馈</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-block {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(173, 216, 230, 0.3);
  padding: 15px;
  box-sizing: border-box;
  border-bottom: 2px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

h3 {
  color: #303133;
  font-size: 1.1rem;
  font-weight: 300;
  margin-bottom: 15px;
  margin-top: 0;
}

/* 文章列表区域：占视口 60% */
.article-list-container {
  height: 60vh;
  margin-bottom: 20px;
  /* 与下方区块的间距 */
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  /* 关键：内容过多显示下拉条 */
  padding-right: 10px;
  border-top: 2px solid #e4e7ed;
}

/* 自定义滚动条样式 (WebKit内核) */
.scroll-area::-webkit-scrollbar {
  width: 4px;
}

.scroll-area::-webkit-scrollbar-thumb {
  background-color: #b3e5fc;
  border-radius: 6px;
}

.scroll-area::-webkit-scrollbar-track {
  background-color: #f0f8ff;
}

.article-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.article-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 15px;
  border-bottom: 1px solid #e1f5fe;
  transition: background-color 0.2s;
  color: #455a64;
  transition: all 0.2s ease;
}

.article-item:hover {
  margin-left: 5px;
  color: #409eff;
  cursor: pointer;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

.article-date {
  color: #90a4ae;
  font-size: 0.85rem;
}

.feedback-content {
  color: #546e7a;
  line-height: 1.6;
  width: 33rem;
  display: flex;
  flex-wrap: wrap;
}

.add {
  width: 11rem;
  height: 10rem;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 3px solid rgb(80, 195, 236);
  border-radius: 8px;
  cursor: pointer;
}

.o-content {
  width: 11rem;
}

.article-delinfo {
  color: #3c3c3c;
}

.add-feedback-block {
  display: flex;
}

.feedback-type {
  width: 40%;
  height: 100%;
}

.feedback-type-choice {
  display: flex;
  flex-wrap: wrap;
}

.type-block {
  height: 2rem;
  padding: 5px 10px;
  border-radius: 4px;
  background-color: rgb(237, 237, 237);
  margin-right: 8px;
  margin-top: 4px;
  font-size: smaller;
  color: #000;
  cursor: pointer;
}

.type-active {
  background-color: #4da5fd;
}

.feedback-textarea {
  width: 15rem;
  height: 7rem;
  padding: 5px;
  resize: none;
  border: 2px solid black;
  border-radius: 4px;
  font-weight: 500;
  font-size: 0.9rem;
}

.submit {
  padding: 5px 10px;
  width: 5rem;
  height: 2rem;
  position: absolute;
  bottom: 3.5%;
  right: 1.5%;
  transition: all 0.3s ease;
  border-radius: 4px;
  background-color: rgb(162, 224, 249);
  border: none;
  cursor: pointer;
}

.submit:hover {
  background-color: #51a7fe;
}

.feedback {
  cursor: pointer;
}

.feedback:hover {
  color: skyblue;
}

/* 数字角标样式 */
.reply-badge {
  font-size: 12px;
  color: #409EFF;
  /* 浅蓝色系字体 */
  font-weight: bold;
  vertical-align: super;
  /* 上标对齐 */
  margin-left: 2px;
}

/* --- 弹框样式 --- */
.feedback-popover {
  position: fixed;
  top: 50%;
  left: 50%;
  /* 初始位置修正，结合动画使用 */
  transform: translate(-50%, -50%);
  width: 300px;
  /* 适中的宽度 */
  max-height: 400px;
  background-color: #ffffff;
  border: 1px solid #d9ecff;
  /* 浅蓝边框 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  text-align: left;
  /* 确保内部文字左对齐 */
  font-size: 13px;
  line-height: 1.5;
}

.popover-title {
  padding: 8px 12px;
  background-color: #ecf5ff;
  /* 极浅的蓝色背景 */
  color: #409EFF;
  font-weight: bold;
  border-bottom: 1px solid #d9ecff;
}

.popover-content {
  overflow-y: auto;
  /* 内容过多可滚动 */
  padding: 0;
}

/* 单条数据样式 */
.reply-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-item:hover {
  background-color: #fafafa;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
  margin-bottom: 4px;
}

/* 文本截断样式：关键逻辑 */
.item-row {
  white-space: nowrap;
  /* 不换行 */
  overflow: hidden;
  /* 超出隐藏 */
  text-overflow: ellipsis;
  /* 显示省略号 */
  color: #333;
  margin-bottom: 2px;
  max-width: 100%;
}

.item-row .label {
  color: #888;
  margin-right: 4px;
}

.reply-text {
  color: #409EFF;
  /* 回复内容用浅蓝色高亮一点 */
}

.popover-close {
  position: absolute;
  right: 12px;
  top: 10px;
  font-size: 16px; /* 叉叉大一点 */
  color: #999;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
  z-index: 10;
}

.popover-close:hover {
  color: #409eff; /* 悬停变为浅蓝色 */
}
</style>