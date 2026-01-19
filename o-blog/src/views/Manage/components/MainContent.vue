<script setup lang="ts">
import { ref } from 'vue';
import { httpInstance, type Response } from '@/utils/http';

interface Article {
  id: number;
  title: string;
  status: number;
  date: string;
}

// 假数据：生成多一点以展示滚动条效果
const articleList = ref<Article[]>([
  { id: 1, title: 'Vue3 组合式 API 最佳实践指南', status: 1,date: '2023-10-01' },
  { id: 2, title: 'TypeScript 在前端项目中的深度应用', status: 1,date: '2023-10-05' },
  { id: 3, title: 'CSS Grid 与 Flexbox 布局对比', status: 1,date: '2023-10-12' },
  { id: 4, title: '前端性能优化：从入门到精通', status: 1,date: '2023-10-15' },
  { id: 5, title: 'Webpack 与 Vite 构建工具解析', status: 1,date: '2023-10-20' },
  { id: 6, title: 'React 与 Vue 的哲学差异', status: 1,date: '2023-10-22' },
  { id: 7, title: 'Node.js 中间件原理解析', status: 1,date: '2023-10-25' },
  { id: 8, title: 'WebAssembly 的未来展望', status: 1,date: '2023-10-28' },
  { id: 9, title: '微前端架构落地实战', status: 1,date: '2023-11-01' },
  { id: 10, title: 'Serverless 架构在前端的应用', status: 1,date: '2023-11-05' },
]);

interface FeedbackType {
  id: number;
  content: string;
}

const feedbackTypeList = ref<FeedbackType[]>([
  {id: 1, content: "文章内容劣质"},
  {id: 2, content: "文章内容有误"},
  {id: 3, content: "网页体验"},
  {id: 4, content: "侵权投诉"},
  {id: 5, content: "和作者吹水"},
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
  if(feedbackContent.value.length >= 255){
    alert("反馈内容信息过长！");
    return;
  }

  const req:submitTable = {
    selectIds: selectedIds.value.join(','),
    content: feedbackContent.value
  }
  try {
    const res = await httpInstance.post<any, Response>('/feedback',req);
    if(res.code === 200){
      alert("感谢您的反馈，会尽快回复！");
      selectedIds.value = [];
      feedbackContent.value = '';
    }else{
      alert(res.message);
      return;
    }
  } catch (error) {
    alert(error);
  }
}

const mdFile:any = ref(null);
const imgMap:any = ref({});
/* 选择文件主入口*/
const selectMdAndImg = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.md';
  input.onchange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if(!file) return;
    mdFile.value = file;    
    const imgConfirm = confirm("文章内是否有图片(需要放到同一文件夹内)？");
    if(imgConfirm){
      // 这里存在图片，打开图片选择框
      selectImgDir();
    }
  }
  input.click();

}
/* ---------- 选图片文件夹 ---------- */
const selectImgDir = () => {
  const input = document.createElement('input')
  input.type       = 'file';
  input.webkitdirectory = true;   // 关键：允许选文件夹
  input.multiple   = true;
  input.onchange   = (e:Event) => {
    handleDir((e.target as HTMLInputElement).files);
    uploadImgsToOSS();
  }
  input.click();
}

/* 处理文件夹扫描 */
const handleDir = (files:any) => {
  imgMap.value = {};               // 先清空
  for (const f of files) {
    const relPath = f.webkitRelativePath; // "dir/sub/a.png"
    if (/\.(png|jpe?g|gif|svg|webp)$/i.test(f.name)) {
      imgMap.value[relPath] = f;
    }
  }
}

/* 将文件传到后端 */
const uploadImgsToOSS = async () => {
  const list = await extractLocalImgs();
  if(!list.length) return [];

  const fd = new FormData();
  list.forEach((item, idx) => {
    fd.append('paths', item.path);
    fd.append('files', item.file);
  });

  try {
    const res = await httpInstance.post<any, Response>('/article/upload/img',fd);
    if(res.code === 200){
      alert("图片上传成功！");
    }else{
      alert(res.message);
      return;
    }
    console.log(res);
  } catch (error) {
    alert(error);
  } finally {
    imgMap.value = {};
  }
}

// 批量读取md中的图片路径，到map中寻找
const extractLocalImgs = async() => {
  const mdText = await mdFile.value.text()
  const reg = /!\[.*?\]\((.*?)\)/g;
  const needUpload = [];
  let m;
  while ((m = reg.exec(mdText)) !== null) {
    const raw:any = m[1];                      // "./pics/a.png" 或 "pics/a.png"
    const key = Object.keys(imgMap.value).find(k => k.endsWith(raw.replace(/^\.?\//, '')));
    if (key) needUpload.push({ path: raw, file: imgMap.value[key] });
  }
  return needUpload;
}

/* 请求后端获取（该用户）md文件并渲染 */
const getUserMdToRender = () => {
  
}
</script>

<template>
  <!-- 区块3：文章列表 -->
  <div class="main-block article-list-container">
    <h3>我的文章列表</h3>
    <div class="scroll-area">
      <ul class="article-list">
        <li v-for="article in articleList" :key="article.id" class="article-item">
          <span class="article-title">{{ article.title }}</span>
          <span class="article-date">{{ (article.status==1?'审核中':'已发布')+'-'+article.date }}</span>
        </li>
      </ul>
    </div>
  </div>

  <div class="add-feedback-block">
    <div class="main-block" style="margin-right: 30px;">
      <div class="add-content">
        <h3>添加文章</h3>
        <div class="add" @click="selectMdAndImg">
          <img src="../../../static/add.svg" alt="">
        </div>
      </div>
    </div>
    <div class="main-block" style="margin-right: 30px;">
      <div class="o-content">
        <h3>功能待开发</h3>
      </div>
    </div>
    <div class="main-block">
      <div class="feedback-content">
        <h3 style="display: block; width: 100%;">信息反馈</h3>
        <div class="feedback-type">
          反馈类型
          <div class="feedback-type-choice">
            <div 
              v-for="item in feedbackTypeList" 
              :key="item.id" 
              class="type-block" 
              :class="{ 'type-active': isSelected(item.id) }"
              @click="handleFeedbackTypeClick(item.id)"
            >
              {{ item.content }}
            </div>
          </div>
        </div>
        <div class="feedback-main">
          反馈内容<br>
          <textarea placeholder="请输入您的反馈内容(限255字)..."
          v-model="feedbackContent" class="feedback-textarea">

          </textarea>
        </div>
        <button class="submit" @click="submitFeedback">全部提交</button>
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

.add-content {
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
</style>