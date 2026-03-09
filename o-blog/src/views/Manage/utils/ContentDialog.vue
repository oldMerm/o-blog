<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { httpInstance, type Response } from '@/utils/http';

// 渲染差别
const isAdmin = ref(false);

onMounted(async() => {
  try {
    const res = await httpInstance.get<any, Response>('/admin/usr/isAdmin');
    if(res.code === 200){
      isAdmin.value = res.data === 'admin' ? !isAdmin.value : isAdmin.value; 
    }else{
      alert(`无访问权限${res.data}`)
    }
  } catch (error) {
    alert(`系统出现错误${error}`)
  }
})

// --- 状态控制 ---
const visible = ref(false);
const isSubmitting = ref(false);
const isUploadingImages = ref(false); // 修复报错：模板用到的上传状态

// DOM 引用
const markdownInput = ref<HTMLInputElement | null>(null);
const imageInput = ref<HTMLInputElement | null>(null);

// --- 核心数据 ---
const markdownFile = ref<File | null>(null);
const imgMap = ref<Record<string, File>>({}); // 本地图片映射表
let imgList: string[] = []; // 后端返回的图片 URL 列表

// 表单数据
const articleForm = reactive({
  articleName: '',
  articleDecr: '',
  articleType: '1', 
  attrs: [] as string[] 
});

// --- 弹窗与触发逻辑 ---
const openModal = () => {
  visible.value = true;
  resetForm();
};

const closeModal = () => {
  if (isSubmitting.value) return;
  visible.value = false;
};

defineExpose({ openModal, closeModal });

const triggerMarkdownUpload = () => markdownInput.value?.click();
const triggerImageUpload = () => imageInput.value?.click();

// --- 文件选择处理 ---
const handleMarkdownChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (file && file.name.endsWith('.md')) {
    markdownFile.value = file;
    if (!articleForm.articleName) {
      articleForm.articleName = file.name.replace(/\.[^/.]+$/, ""); // 默认去掉拓展名作为标题
    }
  } else if (file) {
    alert('请上传 .md 格式的文件！');
  }
};

const handleImageChange = (e: Event) => {
  const files = (e.target as HTMLInputElement).files;
  if (!files) return;
  
  imgMap.value = {};
  articleForm.attrs = []; // 清空重置
  
  Array.from(files).forEach((f: any) => {
    if (/\.(png|jpe?g|gif|webp)$/i.test(f.name)) {
      imgMap.value[f.webkitRelativePath] = f;
      // 仅用于模板展示“已选 X 张”，最终提交时会被真实 URL 替换
      articleForm.attrs.push(f.name); 
    }
  });
};

// --- 核心逻辑：合并处理图片上传与最终提交 ---
const submitArticle = async () => {
  if (!markdownFile.value) return alert('主文件 (Markdown) 为必填项！');
  
  isSubmitting.value = true;
  isUploadingImages.value = true; // 复用状态给模板显示“处理中”

  try {
    let mdText = await markdownFile.value.text();
    const imgKeys = Object.keys(imgMap.value);

    // 1. 如果选择了图片文件夹，先处理图片提取、上传与替换
    if (imgKeys.length > 0) {
      const fd = new FormData();
      // 明确声明 file 为 File 类型
      const needUpload: { path: string, file: File }[] = []; 
      
            // 1.1 扫描 MD 提取引用的图片
      const reg = /!\[.*?\]\((.*?)\)/g;
      let m;
      while ((m = reg.exec(mdText)) !== null) {
        const rawUrl = m[1];
        if (!rawUrl) continue;
        
        // 1. 去除 Markdown 语法中可能的 title，例如: ./pic.png "图片标题" -> ./pic.png
        let cleanUrl:any = rawUrl.split(/\s+/)[0];
        // 2. 去除可能携带的 URL 参数或哈希，例如: pic.png?v=1 -> pic.png
        cleanUrl = cleanUrl.split('?')[0].split('#')[0];
        // 3. URL 解码，处理中文或空格被转义的情况，例如: %E6%B5%8B.png -> 测.png
        try { cleanUrl = decodeURIComponent(cleanUrl); } catch (e) {}
        // 4. 提取纯文件名，兼容 Windows(\) 和 Mac/Linux(/) 路径
        const fileName = cleanUrl.split(/[\/\\]/).pop();

        if (!fileName) continue;

        // 5. 直接通过纯文件名去本地 Map 中匹配 (最稳妥的方式)
        const key = imgKeys.find(k => imgMap.value[k]?.name === fileName);
        const matchedFile = key ? imgMap.value[key] : null;

        if (key && matchedFile) {
          // 注意：传给后端的 path 依然保持原样，以便后端如果需要做特殊处理
          needUpload.push({ path: rawUrl, file: matchedFile });
        }
      }

      // 1.2 如果有匹配的图片，执行 OSS 上传
      if (needUpload.length > 0) {
        needUpload.forEach(item => {
          fd.append('paths', item.path);
          fd.append('files', item.file); 
        });

        // 统一使用要求的 axios 格式
        const res = await httpInstance.post<any, Response>('/article/upload/img', fd);
        
        if (res.code === 200 && res.data) {
          imgList = res.data;
          
          // 1.3 顺序替换 MD 文本中的图片 URL
          let idx = 0;
          mdText = mdText.replace(/!\[.*?\]\((.*?)\)/g, (matched, oldUrl) => {
            let cleanOld = oldUrl.split(/\s+/)[0];
            cleanOld = cleanOld.split('?')[0].split('#')[0];
            try { cleanOld = decodeURIComponent(cleanOld); } catch (e) {}
            const oldFileName = cleanOld.split(/[\/\\]/).pop();

            const key = imgKeys.find(k => imgMap.value[k]?.name === oldFileName);
            
            // 如果没匹配上本地文件，或者超出了后端返回的 URL 数量，则跳过替换
            if (!key || idx >= imgList.length) return matched;
            
            // 仅替换链接部分，这样能保留原来的 Markdown 语法(如 title 等)
            return matched.replace(oldUrl, imgList[idx++] ?? '');
          });
        } else {
          throw new Error(res.message || "图片上传失败");
        }
      }
    }

    // 2. 组装并上传最终的 Markdown 文件
    articleForm.attrs = imgList; // 确保存库关联的是真实的 URL
    const finalFd = new FormData();
    const finalFile = new File([mdText], markdownFile.value.name, { type: 'text/markdown' });

    finalFd.append('md', finalFile);
    finalFd.append('articleName', articleForm.articleName || markdownFile.value.name.replace(/\.[^/.]+$/, ""));
    finalFd.append('articleDecr', articleForm.articleDecr);
    finalFd.append('articleType', articleForm.articleType);
    imgList.forEach(url => finalFd.append('attrs', url));

    // 提交最终文章
    const res = await httpInstance.post<any, Response>('/article/upload', finalFd);
    
    if (res.code === 200) {
      alert("文章发布成功，请等待审核！");
      visible.value = false;
    } else {
      alert(res.message || "文章发布失败");
    }

  } catch (error: any) {
    alert(error.message || "处理发布流程时发生异常！");
  } finally {
    isSubmitting.value = false;
    isUploadingImages.value = false;
  }
};

// --- 重置表单 ---
const resetForm = () => {
  markdownFile.value = null;
  imgList = [];
  imgMap.value = {};
  articleForm.articleName = '';
  articleForm.articleDecr = '';
  articleForm.articleType = '1';
  articleForm.attrs = [];
  if (markdownInput.value) markdownInput.value.value = '';
  if (imageInput.value) imageInput.value.value = '';
};
</script>

<template>
  <transition name="modal-fade">
    <div class="modal-overlay" v-if="visible" @click.self="closeModal">
      <div class="modal-content">
        <!-- 弹窗标题 -->
        <h2 class="modal-title">发布新文章</h2>

        <div class="modal-body">
          <!-- 左侧：主按钮 (上传 Markdown) -->
          <div class="left-panel">
            <div class="main-upload-btn" :class="{ 'has-file': markdownFile }" @click="triggerMarkdownUpload">
              <input type="file" ref="markdownInput" accept=".md" @change="handleMarkdownChange" hidden />
              <div class="btn-content">
                <span class="icon">📄</span>
                <span class="text">{{ markdownFile ? markdownFile.name : '点击上传 Markdown 文件' }}</span>
                <span class="sub-text" v-if="!markdownFile">(必填)</span>
              </div>
            </div>
          </div>

          <!-- 右侧：各项功能设置 -->
          <div class="right-panel">

            <!-- 项1：上传图片文件夹 -->
            <div class="form-row">
              <span class="status-dot" :class="{ active: isUploadingImages || articleForm.attrs.length > 0 }"></span>
              <div class="input-wrapper">
                <button class="sub-upload-btn" @click="triggerImageUpload" :disabled="isUploadingImages">
                  {{ isUploadingImages ? '图片上传中...' : '上传图片文件夹 (JPG/PNG)' }}
                </button>
                <span class="file-count" v-if="articleForm.attrs.length > 0">已传 {{ articleForm.attrs.length }} 张</span>
                <input type="file" ref="imageInput" webkitdirectory directory multiple @change="handleImageChange"
                  hidden />
              </div>
            </div>

            <!-- 项2：文章介绍 -->
            <div class="form-row">
              <span class="status-dot" :class="{ active: articleForm.articleDecr.trim() !== '' }"></span>
              <div class="input-wrapper">
                <input type="text" class="text-input" v-model="articleForm.articleDecr" placeholder="为文章写一个简短的介绍..." />
              </div>
            </div>

            <!-- 项3：文章名 -->
            <div class="form-row">
              <span class="status-dot" :class="{ active: articleForm.articleName.trim() !== '' }"></span>
              <div class="input-wrapper">
                <input type="text" class="text-input" v-model="articleForm.articleName" placeholder="填写文章名称..." />
              </div>
            </div>

            <!-- 项4：文章类型 (Check框/Radio) -->
            <div class="form-row type-row">
              <div class="input-wrapper radio-group">
                <label class="radio-label">
                  <input type="radio" value="1" v-model="articleForm.articleType" />
                  <span class="radio-custom"></span>
                  技术
                </label>
                <label class="radio-label">
                  <input type="radio" value="2" v-model="articleForm.articleType" />
                  <span class="radio-custom"></span>
                  日常
                </label>
                <label class="radio-label" v-show="isAdmin">
                  <input type="radio" value="0" v-model="articleForm.articleType"/>
                  <span class="radio-custom"></span>
                  公告
                </label>
              </div>
            </div>

          </div>
        </div>

        <!-- 底部：操作按钮 -->
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal" :disabled="isSubmitting">取消</button>
          <button class="btn-submit" @click="submitArticle"
            :disabled="!markdownFile || isUploadingImages || isSubmitting">
            {{ isSubmitting ? '处理并提交中...' : '确认提交' }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
/* 样式与之前保持一致，未做修改 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(234, 244, 252, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background: #ffffff;
  width: 760px;
  max-width: 90%;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 85, 153, 0.1);
  padding: 30px;
  display: flex;
  flex-direction: column;
  border: 1px solid #e1f0fa;
}

.modal-title {
  margin: 0 0 24px 0;
  color: #1a4a75;
  font-size: 20px;
  font-weight: 600;
}

.modal-body {
  display: flex;
  gap: 30px;
  align-items: stretch;
}

.left-panel {
  flex: 1;
  display: flex;
}

.main-upload-btn {
  flex: 1;
  border: 2px dashed #90c2e7;
  border-radius: 12px;
  background-color: #f4f9fd;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  min-height: 240px;
}

.main-upload-btn:hover {
  background-color: #e6f2fb;
  border-color: #5ba4e5;
}

.main-upload-btn.has-file {
  border-style: solid;
  border-color: #81c784;
  background-color: #f0f9f1;
}

.btn-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-content .icon {
  font-size: 48px;
}

.btn-content .text {
  color: #2c5c8f;
  font-weight: 500;
  font-size: 16px;
  padding: 0 10px;
  word-break: break-all;
}

.btn-content .sub-text {
  color: #88a6c1;
  font-size: 13px;
}

.right-panel {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 10px 0;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.form-row:last-child {
  margin-bottom: 0;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #d1e4f3;
  flex-shrink: 0;
  transition: background-color 0.4s ease, transform 0.3s ease;
}

.status-dot.active {
  background-color: #81c784;
  transform: scale(1.2);
  box-shadow: 0 0 8px rgba(129, 199, 132, 0.4);
}

.input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #cce3f5;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
  color: #333;
  transition: border-color 0.3s;
  background-color: #fafcfd;
}

.text-input:focus {
  border-color: #5ba4e5;
  background-color: #fff;
}

.sub-upload-btn {
  padding: 9px 16px;
  background-color: #eef6fc;
  border: 1px solid #b3d6f2;
  color: #2c5c8f;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.sub-upload-btn:hover:not(:disabled) {
  background-color: #ddebfa;
}

.sub-upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-count {
  font-size: 12px;
  color: #81c784;
  font-weight: bold;
}

.type-row {
  padding-left: 26px;
}

.radio-group {
  display: flex;
  gap: 24px;
}

.radio-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #4a6c8c;
}

.radio-label input[type="radio"] {
  display: none;
}

.radio-custom {
  width: 16px;
  height: 16px;
  border: 2px solid #b3d6f2;
  border-radius: 50%;
  margin-right: 8px;
  position: relative;
  transition: all 0.3s;
}

.radio-label input[type="radio"]:checked+.radio-custom {
  border-color: #5ba4e5;
}

.radio-label input[type="radio"]:checked+.radio-custom::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background-color: #5ba4e5;
  border-radius: 50%;
}

.modal-footer {
  margin-top: 36px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.btn-cancel,
.btn-submit {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background-color: #f0f4f8;
  border: 1px solid #d1e0ec;
  color: #668299;
}

.btn-cancel:hover:not(:disabled) {
  background-color: #e2eaf1;
}

.btn-submit {
  background-color: #4ca3eb;
  border: none;
  color: #fff;
  font-weight: 500;
}

.btn-submit:hover:not(:disabled) {
  background-color: #3b8fd1;
  box-shadow: 0 4px 12px rgba(76, 163, 235, 0.3);
}

.btn-submit:disabled {
  background-color: #b3d6f2;
  cursor: not-allowed;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.4s ease, transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-content,
.modal-fade-leave-to .modal-content {
  transform: translateY(25px);
  opacity: 0;
}

.modal-fade-enter-to .modal-content,
.modal-fade-leave-from .modal-content {
  transform: translateY(0);
  opacity: 1;
}
</style>
