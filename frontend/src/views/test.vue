<template>
  <div class="auth-container">
    <div class="auth-form">
      <h2>{{ isLogin ? '登录' : '注册' }}</h2>

      <!-- 切换按钮 -->
      <button class="switch-btn" @click="toggleForm">
        {{ isLogin ? '切换到注册' : '切换到登录' }}
      </button>

      <!-- 表单区域 -->
      <form @submit.prevent="handleSubmit">
        <!-- 注册时才显示用户名输入 -->
        <div class="form-group" v-if="!isLogin">
          <label>用户名</label>
          <input
            type="text"
            v-model="username"
            placeholder="请输入用户名"
            required
          />
        </div>

        <div class="form-group">
          <label>邮箱</label>
          <input
            type="email"
            v-model="email"
            placeholder="请输入邮箱"
            required
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            type="password"
            v-model="password"
            placeholder="请输入密码"
            required
            minlength="6"
          />
        </div>

        <button type="submit" class="submit-btn">
          {{ isLogin ? '登录' : '注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const isLogin = ref(true);
const username = ref('');
const email = ref('');
const password = ref('');

const toggleForm = () => {
  isLogin.value = !isLogin.value;
  // 切换时清空表单
  username.value = '';
  email.value = '';
  password.value = '';
};

const handleSubmit = () => {
  if (isLogin.value) {
    // 处理登录逻辑
    console.log('登录提交:', {
      email: email.value,
      password: password.value
    });
  } else {
    // 处理注册逻辑
    console.log('注册提交:', {
      username: username.value,
      email: email.value,
      password: password.value
    });
  }

  // 这里可以添加API请求
  // axios.post(url, { ... })
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.auth-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.submit-btn {
  width: 100%;
  padding: 0.8rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  margin-top: 1rem;
}

.submit-btn:hover {
  background: #0056b3;
}

.switch-btn {
  background: none;
  border: none;
  color: #007bff;
  cursor: pointer;
  margin-bottom: 1rem;
  display: block;
  width: 100%;
  text-align: right;
}

.switch-btn:hover {
  text-decoration: underline;
}
</style>
