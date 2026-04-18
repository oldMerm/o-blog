<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import UserInfo from '@/views/Home/components/userInfo.vue';
import router from '@/router';
import Dialog from '@/utils/dia/Dialog.vue';

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

interface UserManageInfo {
  username: string;
  password: string;
}
let checkedPassword = ref('');
const updateUserInfo = async () => {
  if (usrInfo.password !== checkedPassword.value) {
    alert("请输入相同的新密码");
    checkedPassword.value = usrInfo.password = '';
    return;
  }
  try {
    const res = await httpInstance.post<any, Response>('/usr/manage', usrInfo);
    if (res.code === 200) {
      alert("信息修改成功！");
      renderUsrInfo();
    } else {
      alert(res.message);
    }
  } catch (error) {
    alert(`系统错误:${error}`)
  }

}

/* 上方管理栏目的行为 */
const checkTheWeekReport = () => {
  alert("功能尚未开发!");
}

const visible1 = ref(false);
const logout = async () => {
  try {
    const res = await httpInstance.post<any, Response>('/auth/logout');
    if (res.code === 200) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('timeout');
      setTimeout(() => {router.push({name:'home'})},1000);
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
      setTimeout(() => {router.push({name:'home'})},1000);
    } else {
      alert(res.message);
    }
  } catch (error) {
    alert(`系统错误:${error}`)
  }
}
</script>

<template>
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



  <!-- 区块3：预留模块 -->
  <div class="sidebar-block future-module">
    <h3>待开发模块</h3>
    <div class="placeholder-content">
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
  align-items: center;
  justify-content: center;
  background-color: #e4e3e3;
  /* 稍微深一点的浅蓝背景区分 */
}

.placeholder-content {
  text-align: center;
  color: #000000;
  font-weight: bold;
}
</style>
