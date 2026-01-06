<script setup lang="ts">
import { onMounted, ref , reactive} from 'vue';
import { httpInstance, type Response } from '@/utils/http';
import UserInfo from '@/views/Home/components/userInfo.vue';

const usrInfo:UserManageInfo = reactive({
  username: '',
  password: ''
})
interface UserInfo{
    username: string;
    article: number;
    like: number;
    attrURL: string;
}
const renderUsrInfo = async() => {
    try {
        const res = await httpInstance.get<any, Response>('/usr/info');
        if(res.code !== 200){
            return;
        }
        const data:UserInfo = res.data;
        usrInfo.username = data.username;
    } catch (error) {
        const data = await httpInstance.get<any, Response>('/auth/refresh');
        if(data.code !== 200){
            return;
        }
        const {token, refreshToken, timeout} = <any>data.data;
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
  if(usrInfo.password !== checkedPassword.value){
    alert("请输入相同的新密码");
    checkedPassword.value = usrInfo.password = '';
    return;
  }
  try {
      const res = await httpInstance.post<any, Response>('/usr/manage', usrInfo);
      console.log(res);
      if(res.code === 200){
        alert("信息修改成功！");
        renderUsrInfo();
      }else{
        alert(res.message);
      }
  } catch (error) {
      console.log(error);
  }

}
</script>

<template>
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

  <!-- 区块2：预留模块 -->
  <div class="sidebar-block future-module">
    <h3>待开发模块</h3>
    <div class="placeholder-content">
      <p>这里是预留的功能区域...</p>
      <p>Coming Soon</p>
    </div>
  </div>
</template>

<style scoped>
.sidebar-block {
  height: 46vh; /* 需求：占视口高度 40% */
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(173, 216, 230, 0.3); /* 浅蓝色阴影 */
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
  background-color: #0a9ee7;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
  
}

.save-btn:hover {
  background-color: #0277bd;
}

.future-module {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e4e3e3; /* 稍微深一点的浅蓝背景区分 */
}

.placeholder-content {
  text-align: center;
  color: #000000;
  font-weight: bold;
}
</style>
