<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue';
import { type Article } from '@/views/public/Article';
import { httpInstance, type Response } from '@/utils/http';
import UserInfo from '@/views/Home/components/userInfo.vue';
import router from '@/router';
import Dialog from '@/utils/dia/Dialog.vue';
import Toast from '@/utils/toast/Toast.vue';

const usrInfo: UserManageInfo = reactive({
  username: '',
  password: ''
})
interface UserInfo {
  username: string;
  article: number;
  like: number;
  attrURL: string;
}
const renderUsrInfo = async () => {
  try {
    const res = await httpInstance.get<any, Response>('/usr/info');
    if (res.code !== 200) {
      return;
    }
    const data: UserInfo = res.data;
    usrInfo.username = data.username;
  } catch (error) {
    const data = await httpInstance.get<any, Response>('/auth/refresh');
    if (data.code !== 200) {
      return;
    }
    const { token, refreshToken, timeout } = <any>data.data;
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('timeout', timeout);
    location.reload();
  }
}
onMounted(renderUsrInfo);

// 弹框设置
const toastRef = ref();
const msg = ref('');
const type = ref<'success' | 'error'>('success');
// 弹窗函数
const triggerToast = (message: string, status: 'success' | 'error') => {
  type.value = status;
  msg.value = message;
  toastRef.value?.show();
}

// 用户管理相关接口
interface UserManageInfo {
  username: string;
  password: string;
}
let checkedPassword = ref('');
const updateUserInfo = async () => {
  if (usrInfo.password !== checkedPassword.value) {
    triggerToast("两次输入的密码应一致！", "error");
    checkedPassword.value = usrInfo.password = '';
    return;
  }
  try {
    const res = await httpInstance.post<any, Response>('/usr/manage', usrInfo);
    if (res.code === 200) {
      triggerToast("信息修改成功", 'success');
      renderUsrInfo();
    } else {
      if (res.message) triggerToast(res.message, 'error');
    }
  } catch (error) {
    alert(`系统错误:${error}`)
  }

}

/* 上方管理栏目的行为 */
const checkTheWeekReport = () => {
  triggerToast("功能尚未开发", 'error');
}

const visible1 = ref(false);
const logout = async () => {
  try {
    const res = await httpInstance.post<any, Response>('/auth/logout');
    if (res.code === 200) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('timeout');
      setTimeout(() => { router.push({ name: 'home' }) }, 1000);
    } else {
      alert(res.message);
    }
  } catch (error) {
    alert(`系统错误:${error}`)
  }
}

const visible2 = ref(false);
const handleDeleteUserAccount = async () => {
  try {
    const res = await httpInstance.delete<any, Response>('/auth/logoff');
    if (res.code === 200) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('timeout');
      setTimeout(() => { router.push({ name: 'home' }) }, 1000);
    } else {
      alert(res.message);
    }
  } catch (error) {
    alert(`系统错误:${error}`)
  }
}

/* 文章分组 */
/* 分组渲染对象 */
interface GroupRenderParam {
  id: number,
  groupName: string,
  createdAt: string,
  number: number,
}
/* 创建分组对象 */
interface GroupCreateParam {
  groupName: string;
  groupDesc: string;
}
const articleGroupList = ref<GroupRenderParam[]>([]);
const getRenderArticleGroupList = async () => {
  const res = await httpInstance.get<any, Response>('/article/group');
  articleGroupList.value = res.data;
}
onMounted(getRenderArticleGroupList)

// 添加集合弹框
const showAddGroupModal = ref(false);
const groupForm = reactive<GroupCreateParam>({
  groupName: '',
  groupDesc: '',
});

const openAddGroup = () => {
  groupForm.groupName = '';
  groupForm.groupDesc = '';
  showAddGroupModal.value = true;
};

const closeAddGroup = () => {
  showAddGroupModal.value = false;
};

const handleSubmitAddGroup = async () => {
  const formData = new FormData();
  formData.append('groupName', groupForm.groupName);
  formData.append('groupDesc', groupForm.groupDesc);

  const res = await httpInstance.post<any, Response>('/article/group', formData);
  if (res.code !== 200) {
    triggerToast(`创建失败:${res.message}`, "error");
  } else {
    triggerToast('集合添加成功', 'success');
  }
  getRenderArticleGroupList()
  closeAddGroup();
};

// 集合管理弹框
const showManageGroupModal = ref(false);
const selectedGroup = ref<GroupRenderParam | null>(null);

// 模拟各集合内的文章
const groupArticlesMock = ref<Record<number, Article[]>>({
  1: [
    { id: '101', articleName: '文章一', articleStatus: 2, like: 10, createdAt: '2026.1.1' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
    { id: '102', articleName: '文章二', articleStatus: 1, like: 5, createdAt: '2026.1.2' },
  ],
});

const openManageGroup = (group: GroupRenderParam) => {
  selectedGroup.value = group;
  showManageGroupModal.value = true;
};

const closeManageGroup = () => {
  showManageGroupModal.value = false;
  selectedGroup.value = null;
};

const addArticleToGroup = () => {
  // TODO: 请求逻辑
};

const removeArticleFromGroup = (article: Article) => {
  // TODO: 请求逻辑
};

const visible3 = ref(false);
const handleDeleteGroup = async () => {
  // 这里执行你需要的逻辑
  const res = await httpInstance.delete<any, Response>(`/article/group/${selectedGroup.value?.id}`);
  if(res.code !== 200) {
    triggerToast(`集合删除失败: ${res.message}`, 'error');
  }else{
    triggerToast('集合删除成功', 'success');
    getRenderArticleGroupList();
  }
  closeManageGroup();
}

</script>

<template>
  <!-- info弹框 -->
  <Toast ref="toastRef" :message="msg" :type="type" />

  <!-- 添加集合弹框 -->
  <Transition name="modal-fade">
    <div v-if="showAddGroupModal" class="modal-overlay" @click.self="closeAddGroup">
      <div class="modal-panel">
        <div class="modal-title">
          添加集合
          <span class="modal-close" @click="closeAddGroup">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>集合名称</label>
            <input type="text" v-model="groupForm.groupName" placeholder="请输入集合名称" />
          </div>
          <div class="form-group">
            <label>集合描述</label>
            <textarea v-model="groupForm.groupDesc" placeholder="请输入集合描述" class="modal-textarea"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="closeAddGroup">取消</button>
          <button class="modal-btn confirm" @click="handleSubmitAddGroup">添加集合</button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- 集合管理弹框 -->
  <Transition name="modal-fade">
    <div v-if="showManageGroupModal" class="modal-overlay" @click.self="closeManageGroup">
      <div class="modal-panel manage-panel">
        <div class="modal-title">
          集合管理
          <span class="modal-close" @click="closeManageGroup">&times;</span>
        </div>
        <div class="modal-body manage-body">
          <div class="manage-left">
            <ul class="manage-article-list">
              <li v-for="article in groupArticlesMock[selectedGroup?.id ?? -1] || []" :key="article.id"
                class="manage-article-item">
                <span class="manage-article-name">{{ article.articleName }}</span>
                <span class="manage-article-remove" @click="removeArticleFromGroup(article)">删除</span>
              </li>
              <li v-if="!groupArticlesMock[selectedGroup?.id ?? -1]?.length" class="manage-empty">暂无文章</li>
            </ul>
          </div>
          <div class="manage-footer">
            <button class="modal-btn manage-action" @click="addArticleToGroup">添加文章</button>
            <button class="modal-btn manage-action" @click="visible3 = true">删除集合</button>
            <Dialog v-model="visible3" title="确认删除" content="集合删除后将无法恢复，是否继续？" @confirm="handleDeleteGroup" />
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- 区块2：一些管理项 -->
  <div class="manage-info">
    <div class="manage-box" @click="router.push({ name: 'home' })">返回主页</div>
    <div class="manage-box" @click="visible1 = true">
      退出登录
      <Dialog v-model="visible1" title="确认退出" content="退出后部分功能将无法使用，是否继续？" @confirm="logout" />
    </div>
    <div class="manage-box" @click="visible2 = true">
      注销账号
      <Dialog v-model="visible2" title="确认删除" content="删除后将无法恢复，是否继续？" @confirm="handleDeleteUserAccount" />
    </div>
    <div class="manage-box" @click="checkTheWeekReport">查看周报</div>
  </div>

  <!-- 区块1：用户信息 -->
  <div class="sidebar-block user-info-card" style="padding-left: 3.6rem;">
    <h3>用户信息设置</h3>
    <div class="form-group">
      <label>用户名</label>
      <input type="text" v-model="usrInfo.username" placeholder="请输入用户名" />
    </div>
    <div class="form-group">
      <label>新密码</label>
      <input type="password" v-model="usrInfo.password" placeholder="请输入新密码" />
    </div>
    <div class="form-group">
      <label>确认密码</label>
      <input type="password" v-model="checkedPassword" placeholder="请再次输入密码" />
    </div>
    <button class="save-btn" @click="updateUserInfo">保存修改</button>
  </div>

  <!-- 区块3：文章分组 -->
  <div class="sidebar-block future-module">
    <h3>集合管理</h3>
    <div class="scroll-area">
      <ul class="article-list">
        <li class="article-item" @click="openAddGroup">
          <span class="article-title" style="font-size: 15px; font-style: italic;">点此创建分类集合</span>
        </li>
        <li v-for="group in articleGroupList" class="article-item" @click="openManageGroup(group)">
          <span class="article-title">{{ group.groupName }}</span>
          <span class="article-date">{{ group.createdAt.substring(0, 10) }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.sidebar-block {
  height: 46vh;
  /* 需求：占视口高度 40% */
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(173, 216, 230, 0.3);
  /* 浅蓝色阴影 */
  padding: 15px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  margin-left: 20px;
}

h3 {
  margin-bottom: 10px;
  color: #303133;
  font-size: 1.1rem;
  font-weight: 300;
}

.form-group {
  margin-bottom: 10px;
}

label {
  display: block;
  margin-bottom: 5px;
  color: #546e7a;
  font-size: 0.9rem;
}

input {
  width: 80%;
  padding: 10px;
  border: 1px solid #b3e5fc;
  border-radius: 6px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #0e9ce3;
}

.save-btn {
  width: 80%;
  background-color: #49b4ea;
  color: black;
  border: none;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 300;
  transition: background-color 0.3s;

}

.save-btn:hover {
  background-color: #51a7fe;
}

.manage-info {
  margin-left: 11px;
  display: flex;
  align-items: center;
}

.manage-box {
  background-color: #b3e5fc;
  margin: 10px 10px;
  padding: 5px 8px;
  border-radius: 4px;
  font-weight: 300;
  cursor: pointer;
  transition: all 0.3s ease;
}

.manage-box:hover {
  background-color: #51a7fe;
}

.future-module {
  height: 40vh;
  margin-top: 10px;
  display: flex;
  /* 稍微深一点的浅蓝背景区分 */
}

.placeholder-content {
  text-align: center;
  color: #000000;
  font-weight: bold;
}

/* 集合管理 */
.article-link-manage {
  font-weight: 300;
  font-size: 18px;
  margin-bottom: 20px;
  /* border-bottom: 2px solid #e4e7ed; */
  max-height: 100px;
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

/* ===== 弹框样式 ===== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-panel {
  width: 380px;
  background-color: #ffffff;
  border: 1px solid #d9ecff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;
}

.manage-panel {
  width: 480px;
}

.modal-title {
  padding: 8px 12px;
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: bold;
  border-bottom: 1px solid #d9ecff;
  position: relative;
}

.modal-close {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}

.modal-close:hover {
  color: #409eff;
}

.modal-body {
  padding: 16px;
}

.modal-body .form-group {
  margin-bottom: 14px;
}

.modal-body .form-group label {
  display: block;
  margin-bottom: 5px;
  color: #546e7a;
  font-size: 0.9rem;
}

.modal-body .form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  outline: none;
  box-sizing: border-box;
  font-size: 0.9rem;
  background-color: #fafcff;
  transition: border-color 0.3s, background-color 0.3s;
}

.modal-body .form-group input:focus {
  border-color: #409eff;
  background-color: #fff;
}

.modal-body .form-group input::placeholder {
  color: #c0c4cc;
}

.modal-textarea {
  width: 100%;
  height: 80px;
  padding: 10px 12px;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  outline: none;
  box-sizing: border-box;
  resize: vertical;
  font-family: inherit;
  font-size: 0.9rem;
  background-color: #fafcff;
  transition: border-color 0.3s, background-color 0.3s;
}

.modal-textarea:focus {
  border-color: #409eff;
  background-color: #fff;
}

.modal-textarea::placeholder {
  color: #c0c4cc;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 16px 16px;
}

.modal-btn {
  padding: 6px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.modal-btn.confirm {
  background-color: #49b4ea;
  color: #000;
}

.modal-btn.confirm:hover {
  background-color: #51a7fe;
}

.modal-btn.cancel {
  background-color: #e4e7ed;
  color: #606266;
}

.modal-btn.cancel:hover {
  background-color: #d2d5da;
}

/* 集合管理弹框布局 */
.manage-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.manage-left {
  flex: 1;
  max-height: 260px;
  overflow-y: auto;
  padding-right: 8px;
}

.manage-left::-webkit-scrollbar {
  width: 4px;
}

.manage-left::-webkit-scrollbar-thumb {
  background-color: #b3e5fc;
  border-radius: 6px;
}

.manage-left::-webkit-scrollbar-track {
  background-color: #f0f8ff;
}

.manage-article-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.manage-article-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 1rem;
  color: #455a64;
}

.manage-article-item:last-child {
  border-bottom: none;
}

.manage-article-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.manage-article-remove {
  color: #e86868;
  cursor: pointer;
  font-size: 0.85rem;
  margin-left: 8px;
  flex-shrink: 0;
}

.manage-article-remove:hover {
  color: #cf4444;
}

.manage-empty {
  color: #999;
  font-size: 0.85rem;
  text-align: center;
  padding: 20px 0;
}

.manage-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #e4e7ed;
}

.manage-action {
  padding: 7px 18px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
}

.manage-action:hover {
  background-color: #409EFF;
  color: #fff;
}

/* 过渡动画 */
.modal-fade-enter-active {
  transition: all 0.3s ease-out;
}

.modal-fade-leave-active {
  transition: opacity 0.2s ease-in;
}

.modal-fade-enter-from {
  opacity: 0;
}

.modal-fade-enter-from .modal-panel {
  transform: scale(0.85);
}

.modal-fade-leave-to {
  opacity: 0;
}
</style>
