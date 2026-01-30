<script setup lang="ts">
import { ref, computed } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import router from '@/router/index.ts';
import type { Article } from '@/views/public/Article';

// 假数据：生成多一点以展示滚动条效果
const statusMap = new Map([
  [1, "未审核"],
  [2, "已过审"],
  [3, "已发布"],
  [4, "未过审"]
]);
const articleList = ref<Article[]>([]);

/* 请求后端获取（该用户）md文件并渲染 */
const getUserMdToRender = async () => {
  const res = await httpInstance.get<any, Response>('/article/info');
  articleList.value = res.data;
}
getUserMdToRender();

// 访问文章功能，根据文章id渲染并跳转
const goToArticle = async (articleId: string) => {
  router.push({
    name: 'markdown',
    params: { id: articleId }
  })
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

// --- 状态定义 ---
let mdFile: any = ref(null);
let imgMap: any = ref({});
let newMd: any = null; // 修复：这里存储的是替换后的 Markdown 字符串内容
let imgList: any = [];

// --- 辅助接口 ---
interface ArticleCreateDTO {
  articleName: string;
  articleDecr: string;
  articleType: number;
  attrs: string[];
}

/**
 * 1. 【入口】选择 MD 文件
 */
const selectMdAndImg = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.md';
  input.onchange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;

    // 重置状态
    mdFile.value = file;
    newMd = null;
    imgList = [];
    imgMap.value = {};

    const imgConfirm = confirm("文章内是否有图片(需要放到同一文件夹内)？");
    if (imgConfirm) {
      // 1.1 有图片，去选文件夹
      selectImgDir();
    } else {
      // 1.2 无图片，直接上传原始 MD
      uploadMd();
    }
  }
  input.click();
}

/**
 * 2. 选择图片文件夹
 */
const selectImgDir = () => {
  const input = document.createElement('input');
  input.type = 'file';
  // @ts-ignore: webkitdirectory 是非标准属性，TS 需要忽略检查
  input.webkitdirectory = true;
  input.multiple = true;

  input.onchange = async (e: Event) => {
    const files = (e.target as HTMLInputElement).files;
    if (!files) return;

    handleDir(files);

    // 等待图片上传并替换链接完成
    const replacedText = await uploadImgsToOSS();

    if (replacedText) {
      newMd = replacedText; // 将处理好的文本赋值给 newMd
      // 图片处理完毕，执行最终上传
      await uploadMd();
    }

    // 清理 input
    input.remove();
  }
  input.click();
}

/**
 * 3. 扫描文件夹到 Map 中
 */
const handleDir = (files: FileList) => {
  imgMap.value = {};
  for (let i = 0; i < files.length; i++) {
    const f: any = files[i];
    // webkitRelativePath 包含文件夹路径，如 "imgs/demo.png"
    const relPath = f.webkitRelativePath;
    if (/\.(png|jpe?g|gif|webp)$/i.test(f.name)) {
      imgMap.value[relPath] = f;
    }
  }
}

/**
 * 4. 上传图片到 OSS 并替换 Markdown 内容
 */
const uploadImgsToOSS = async () => {
  // 4.1 提取需要上传的图片
  const list = await extractLocalImgs();

  // 如果没有匹配到图片，直接返回 null 或 原始内容
  if (!list.length) {
    alert("未在文件夹中匹配到 MD 文档引用的图片，将直接上传。");
    return await mdFile.value.text();
  };

  const fd = new FormData();
  list.forEach((item: any) => {
    // 注意：这里后端可能需要 path 来区分，或者只需要 file
    fd.append('paths', item.path);
    fd.append('files', item.file);
  });

  try {
    const res = await httpInstance.post<any, any>('/article/upload/img', fd);

    if (res.code === 200) {
      // alert("图片上传成功！(后台处理中...)");
      imgList = res.data; // 假设返回的是新的图片 URL 数组

      // 4.2 获取 MD 文本内容 (必须 await)
      const rawText = await mdFile.value.text();

      // 4.3 替换链接
      return replaceMdImgUrlsByIndex(rawText, res.data);
    } else {
      alert(res.message || "图片上传失败");
      return null;
    }
  } catch (error) {
    console.error(error);
    alert("图片上传异常");
    return null;
  }
}

/**
 * 5. 解析 MD 中的图片语法，并从 Map 中找到对应的 File
 */
const extractLocalImgs = async () => {
  const mdText = await mdFile.value.text(); // 修复：必须 await text()
  const reg = /!\[.*?\]\((.*?)\)/g;
  const needUpload = [];
  let m;

  while ((m = reg.exec(mdText)) !== null) {
    const rawUrl: string | undefined = m[1]; // 例如 "./pics/a.png"

    // 核心逻辑：尝试匹配文件名
    // 去掉路径前的 ./ 或 /，只保留文件名或相对路径片段进行模糊匹配
    if (!rawUrl) continue;
    const cleanRawUrl = rawUrl.replace(/^\.?\//, '');

    const key = Object.keys(imgMap.value).find(k => k.endsWith(cleanRawUrl));

    if (key) {
      needUpload.push({ path: rawUrl, file: imgMap.value[key] });
    }
  }
  return needUpload;
}

/**
 * 6. 字符串替换：旧链接 -> 新链接
 */
const replaceMdImgUrlsByIndex = (
  mdText: string,
  newUrls: string[]
): string => {
  let idx = 0;
  return mdText.replace(/!\[.*?\]\((.*?)\)/g, (matched, oldUrl) => {
    const cleanOldUrl = oldUrl.replace(/^\.?\//, '');
    // 再次确认这个链接是否是我们上传列表里的
    const key = Object.keys(imgMap.value).find(k => k.endsWith(cleanOldUrl));

    // 如果找不到 key，说明这张图没在本地文件夹里，不替换
    // 如果 idx 越界，也不替换
    if (!key || idx >= newUrls.length) return matched;

    return matched.replace(oldUrl, newUrls[idx++] ?? '');
  });
};

/**
 * 7. 辅助：字符串转 File 对象
 */
const stringToFile = (text: string, fileName: string): File => {
  return new File([text], fileName, { type: 'text/markdown' });
};

/**
 * 8. 最终上传 Markdown 文件
 */
const uploadMd = async () => {
  const formData = new FormData();

  // 公共 DTO 参数
  const dto: ArticleCreateDTO = {
    articleName: mdFile.value.name.replace(/\.[^/.]+$/, ""), // 始终使用原始文件的文件名(去掉拓展名)
    articleDecr: "",
    articleType: 3,
    attrs: imgList || [] // 图片列表
  };

  // 分支逻辑
  if (newMd === null) {
    // A. 没处理过图片（或没图片），直接传原始文件对象
    formData.append('md', mdFile.value);
  } else {
    // B. 处理过图片，newMd 是字符串，需要转回 File 对象
    // 修复：newMd 是 string，没有 .name 属性，使用 mdFile.value.name
    const finalFile = stringToFile(newMd, mdFile.value.name);
    formData.append('md', finalFile);
  }

  // 追加其他参数
  formData.append('articleName', dto.articleName);
  formData.append('articleDecr', dto.articleDecr);
  formData.append('articleType', dto.articleType.toString());
  // 如果 attrs 是数组，需要遍历 append
  if (dto.attrs && dto.attrs.length) {
    dto.attrs.forEach(t => formData.append('attrs', t));
  }

  try {
    const res = await httpInstance.post<any, any>('/article/upload', formData);
    if (res.code === 200) {
      alert("文章上传成功，请等待审核！");
      // 清理
      mdFile.value = null;
      newMd = null;
      imgMap.value = {};
      imgList = [];
    } else {
      alert(res.message);
    }
  } catch (error) {
    alert("文章上传失败: " + error);
  }
}

// --- TS 逻辑部分 (你可以根据需要修改) ---
const replyContent = ref('');
const showFeedbackList = ref(false);

// 假数据模型
interface ReplyItem {
  id: number;
  replier: string;
  feedbackContent: string; // 反馈内容
  replyContent: string;    // 回复内容
  replyTime: string;
}

// 假数据
const mockData = ref<ReplyItem[]>([
  {
    id: 1,
    replier: '管理员01',
    feedbackContent: '系统有时候加载图片会变得非常慢，希望能优化一下CDN配置。',
    replyContent: '收到，技术部已经排查完毕，预计今晚更新修复。',
    replyTime: '2023-10-27 10:00'
  },
  {
    id: 2,
    replier: '客服小美',
    feedbackContent: '导出Excel报表的时候格式乱了。',
    replyContent: '您好，请尝试更新浏览器版本，如果还有问题请联系IT支持。',
    replyTime: '2023-10-26 15:30'
  },
  {
    id: 3,
    replier: '系统自动',
    feedbackContent: '账号无法登陆。',
    replyContent: '密码错误次数过多，账号已锁定，请24小时后重试。',
    replyTime: '2023-10-25 09:00'
  },
  {
    id: 3,
    replier: '系统自动',
    feedbackContent: '账号无法登陆。',
    replyContent: '密码错误次数过多，账号已锁定，请24小时后重试。',
    replyTime: '2023-10-25 09:00'
  },
  {
    id: 3,
    replier: '系统自动',
    feedbackContent: '账号无法登陆。',
    replyContent: '密码错误次数过多，账号已锁定，请24小时后重试。',
    replyTime: '2023-10-25 09:00'
  },
  {
    id: 3,
    replier: '系统自动',
    feedbackContent: '账号无法登陆。',
    replyContent: '密码错误次数过多，账号已锁定，请24小时后重试。',
    replyTime: '2023-10-25 09:00'
  },
  {
    id: 3,
    replier: '系统自动',
    feedbackContent: '账号无法登陆。',
    replyContent: '密码错误次数过多，账号已锁定，请24小时后重试。',
    replyTime: '2023-10-25 09:00'
  },
]);

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
        <li v-for="article in articleList" :key="article.id" class="article-item" @click="goToArticle(article.id)">
          <span class="article-title">{{ article.articleName }}</span>
          <span class="article-date">{{ statusMap.get(article.articleStatus) + '-' + article.createdAt }}</span>
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
                  <span class="time">于{{ item.replyTime }}</span>
                </div>
                <!-- 第二行：反馈内容 (原问题) -->
                <div class="item-row" :title="item.feedbackContent">
                  <span class="label">问:</span> {{ item.feedbackContent }}
                </div>
                <!-- 第三行：回复内容 -->
                <div class="item-row reply-text" :title="item.replyContent">
                  <span class="label">答:</span> {{ item.replyContent }}
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