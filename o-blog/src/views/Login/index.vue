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
            v-model="loginForm.identifier" 
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
import { ref, reactive } from 'vue';

// 状态控制
const isLogin = ref(true);
const countdown = ref(0);

// 数据模型
const loginForm = reactive({
  identifier: '',
  password: ''
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
};

// 发送验证码逻辑
const sendVerifyCode = () => {
  if (!registerForm.email.includes('@')) {
    alert('请输入有效的邮箱地址');
    return;
  }
  
  console.log('请求路径: /api/v1/send-code', { email: registerForm.email });
  alert('验证码已发送（模拟）');
  
  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
};

// 🔑 登录提交
const handleLogin = async () => {
  if (!loginForm.identifier || !loginForm.password) {
    alert('请填写完整信息');
    return;
  }
  
  console.log('请求路径: /api/v1/login', loginForm);
  // 模拟 API 请求
  setTimeout(() => {
    alert(`登录成功！欢迎 ${loginForm.identifier}`);
  }, 500);
};

// 📝 注册提交
const handleRegister = async () => {
  // 逻辑校验
  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次输入的密码不一致');
    return;
  }
  
  // 数学逻辑校验示例: 
  // 令 $$ P_1 $$ 为密码, $$ P_2 $$ 为确认密码
  // 校验条件: $$ P_1 = P_2 \land \text{length}(P_1) \ge 6 $$

  console.log('请求路径: /api/v1/register', registerForm);
  
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

.btn-code {
  padding: 0 15px;
  background: white;
  border: 1px solid var(--primary-blue);
  color: var(--primary-blue);
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  font-size: 13px;
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
