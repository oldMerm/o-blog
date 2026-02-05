<template>
  <div class="auth-container">
    <div class="auth-box">
      <!-- 标题部分 -->
      <h2 class="auth-title">{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>

      <!-- 登录表单 -->
      <div v-if="isLogin" class="form-wrapper">
        <div class="input-group">
          <label>账号</label>
          <input 
            v-model="loginForm.username" 
            type="text" 
            placeholder="请输入邮箱或用户名"
          />
        </div>
        <div class="input-group">
          <label>密码</label>
          <input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码"
          />
        </div>
        <div class="input-group">
          <label>图形码</label>
          <div class="captcha-row">
            <input
              v-model="loginForm.code"
              type="text"
              placeholder="请输入图形验证码"
            />
            <img
              :src="captchaImage"
              alt="验证码"
              class="captcha-img"
              title="点击刷新验证码"
              @click="fetchCaptcha"
            />
          </div>
        </div>
        <button class="btn-primary" @click="handleLogin">立即登录</button>
        <div class="footer-tip">
          <span>还没有账号？</span>
          <a href="javascript:void(0)" @click="toggleMode">立即注册</a>
        </div>
      </div>

      <!-- 注册表单 -->
      <div v-else class="form-wrapper">
        <div class="input-group">
          <label>邮箱地址</label>
          <div class="email-row">
            <input 
              v-model="registerForm.email" 
              type="email" 
              placeholder="example@mail.com"
            />
            <button 
              class="btn-code" 
              :disabled="countdown > 0" 
              @click="sendVerifyCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </div>
        </div>
        <div class="input-group">
          <label>验证码</label>
          <input 
            v-model="registerForm.code" 
            type="text" 
            placeholder="6位验证码"
          />
        </div>
        <div class="input-group">
          <label>设置密码</label>
          <input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码"
          />
        </div>
        <div class="input-group">
          <label>确认密码</label>
          <input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码"
          />
        </div>
        <button class="btn-primary" @click="handleRegister">完成注册</button>
        <div class="footer-tip">
          <a href="javascript:void(0)" @click="toggleMode">返回登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import {httpInstance, type Response} from '@/utils/http';
import router from '@/router';

// 状态控制
const isLogin = ref(true);
const countdown = ref(0);

// 数据模型
const loginForm = reactive({
  username: '',
  password: '',
  code: '',
  uuid: ''
});

const registerForm = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
});

// 🔄 切换模式
const toggleMode = () => {
  isLogin.value = !isLogin.value;
  if (isLogin.value) fetchCaptcha();
};

// 图片验证码状态（模拟）
const captchaImage = ref('');
interface CaptchaVO {
  captcha: string,
  uuid: string
}

// 获取图形验证码（示例：生成一个简单的 SVG DataURL）
const fetchCaptcha = async () => {
  const capVo = httpInstance.get<CaptchaVO>('/auth/captcha');
  const {captcha, uuid} = (await capVo).data
  captchaImage.value = captcha
  localStorage.setItem("cuid",uuid)
};
// 进入到登录页面，自动调用一次渲染验证码
onMounted(() => {
  if (isLogin.value) fetchCaptcha();
});

// 发送验证码逻辑
const sendVerifyCode = async () => {
  if (!registerForm.email.includes('@')) {
    alert('请输入有效的邮箱地址');
    return;
  }
  
  try {
    await httpInstance.get('/auth/email',{
      params: {
        email: registerForm.email
      }
    });
  } catch (error) {
    alert(error)
  }

  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
};

// 🔑 登录提交
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password || !loginForm.code) {
    alert('请填写完整信息');
    return;
  }

  loginForm.uuid = <string>localStorage.getItem("cuid");
  try {
    console.log(loginForm.password);
    const data = await httpInstance.post<any, Response>('/auth/login', loginForm);
    if(data.code !== 200){
      alert(data.message);
      return;
    }
    const {token, refreshToken, timeout} = <any>data.data;
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('timeout', timeout);
    alert('登录成功!');
  } catch (error) {
    alert(error)
    return;
  }
  
  setInterval(() => {
    router.push({name:'home'});
  }, 1000)
};

// 📝 注册提交
const handleRegister = async () => {
  // 逻辑校验
  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次输入的密码不一致');
    return;
  }

  try {
    const data = await httpInstance.post<any, Response>('/auth/register',registerForm);
    console.log(data);
    if(data.code !== 200){
      alert(data.message)
      return;
    }
  } catch (error) {
    alert(error)
    return;
  }

  setTimeout(() => {
    alert('注册成功，正在跳转登录...');
    isLogin.value = true; // 注册成功回到登录
  }, 500);
};
</script>

<style scoped>
/* 🎨 样式设计：蓝白风格 */
.auth-container {
  --primary-blue: #2563eb;
  --light-blue: #eff6ff;
  --border-color: #d1d5db;
  
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  height: 100vh;
  background-color: var(--light-blue);
  font-family: 'PingFang SC', sans-serif;
}

.auth-box {
  width: 400px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.auth-title {
  text-align: center;
  color: #1f2937;
  margin-bottom: 30px;
  font-size: 24px;
}

.form-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 14px;
  color: #4b5563;
}

.input-group input {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s;
}

.input-group input:focus {
  border-color: var(--primary-blue);
}

.email-row {
  display: flex;
  gap: 10px;
}

.email-row input {
  flex: 1;
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-row input {
  flex: 1;
}

.captcha-img {
  width: 100px;
  height: 40px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  cursor: pointer;
  object-fit: cover;
}

.btn-code {
  padding: 0 15px;
  background: white;
  border: 1px solid var(--primary-blue);
  color: var(--primary-blue);
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  font-size: 13px;
  width: 6rem;
}

.btn-code:disabled {
  color: #9ca3af;
  border-color: #d1d5db;
  cursor: not-allowed;
}

.btn-primary {
  margin-top: 10px;
  padding: 12px;
  background-color: var(--primary-blue);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.footer-tip {
  text-align: right;
  font-size: 13px;
  color: #6b7280;
}

.footer-tip a {
  color: var(--primary-blue);
  text-decoration: none;
  font-weight: 500;
}

.footer-tip a:hover {
  text-decoration: underline;
}
</style>
