<template>
  <div class="login-container">
    <div class="operations">
<!--      <icon-button @click="() => openUrl('https://github.com/linyu-im')" icon="icon-github" />-->
      <icon-button
        v-if="themeStore.theme === 'light'"
        @click="(e) => toggleDark(e, 'dark')"
        icon="icon-taiyang"
      />
      <icon-button
        v-if="themeStore.theme === 'dark'"
        @click="(e) => toggleDark(e, 'light')"
        icon="icon-yueliang"
      />
    </div>
    <div class="login-bg">
      <img alt="" :src="`/poster-${themeStore.theme}.png`" class="poster-img" draggable="false" />
<!--      <img class="logo" alt="" :src="`/title-${themeStore.theme}.png`" draggable="false" />-->
      <div class="login-content">
        <div v-if="isLogin" class="login-box">
          <div class="title" style="position: relative">
            <div class="text-[28px] text-[rgb(var(--text-color))] font-[600] leading-[28px]">
              登录
            </div>
            <p class="login-switch" @click="loginSwitch" style="position: absolute; right: 0; cursor: pointer;">还没有账号?去注册-></p>
          </div>
          <div class="info">
            <linyu-input
              v-model:value="username"
              class="mb-[10px]"
              placeholder="用户名"
              @keydown.enter="onLogin"
            />
            <linyu-input v-model:value="password" placeholder="密码" @keydown.enter="onLogin" />
          </div>
          <div @click="onLogin" :class="['login-button', { logging: logging }]">
            {{ !logging ? '登 录' : '请 等 待' }}
          </div>
        </div>

        <div v-if="!isLogin" class="register-box">
          <div class="title" style="position: relative">
            <div class="text-[28px] text-[rgb(var(--text-color))] font-[600] leading-[28px]">
              注册
            </div>
            <p class="login-switch" @click="loginSwitch" style="position: absolute; right: 0; cursor: pointer;">已有账号?去登录-></p>
          </div>
          <div class="info">
            <linyu-input v-model:value="username" class="mb-[10px]" placeholder="用户名" @keydown.enter="onRegister"/>
            <linyu-input v-model:value="password" class="mb-[10px]" placeholder="密码" @keydown.enter="onRegister"/>
            <linyu-input v-model:value="email" class="mb-[10px]" placeholder="邮箱" @keydown.enter="onRegister"/>
            <linyu-input v-model:value="code" class="mb-[10px]" style="width: 70%;"
                         placeholder="邮箱验证码" @keydown.enter="onRegister"/>
            <div @click="getCode" :class="['getCode-button', { logging: logging }]">
              {{ !logging ? '获取验证码' : '请 等 待' }}
            </div>

          </div>
          <div @click="onRegister" :class="['register-button', { logging: logging }]">
            {{ !logging ? '注 册' : '请 等 待' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useThemeStore } from '@/stores/useThemeStore.js'
import LinyuInput from '@/components/LinyuInput.vue'
import { toggleDark } from '@/utils/theme.js'
import IconButton from '@/components/LinyuIconButton.vue'
import { useToast } from '@/components/ToastProvider.vue'
import LoginApi from '@/api/login.js'
import { useUserInfoStore } from '@/stores/useUserInfoStore.js'
import router from "@/router/index.js";

const themeStore = useThemeStore()
const userInfoStore = useUserInfoStore()

const logging = ref(false)
const username = ref('')
const password = ref('')
const email = ref('')
const code = ref('')
const showToast = useToast()
const isLogin = ref(true)


const loginSwitch = () => {
  isLogin.value = !isLogin.value
}
const onLogin = () => {
  if (!username.value) {
    showToast('用户名不能为空~', true)
    return
  }
  if (!password.value) {
    showToast('邮箱不能为空~', true)
    return
  }
  logging.value = true
  LoginApi.login({ userName: username.value, password: password.value })
    .then((res) => {
      if (res.code === 0) {
        localStorage.setItem('x-token', res.data.token)
        userInfoStore.setUserInfo({
          userId: res.data.userId.toString(),
          userName: res.data.userName,
          email: res.data.email,
          avatar: res.data.avatar,
        })
        router.push('/')
      } else {
        showToast(res.msg, true)
      }
    })
    .catch((res) => {
      showToast(res.message, true)
    })
    .finally(() => {
      logging.value = false
    })
}

const onRegister = () => {
  if (!username.value) {
    showToast('用户名不能为空~', true)
    return
  }
  if (!password.value) {
    showToast('密码不能为空~', true)
    return
  }
  if (!email.value) {
    showToast('邮箱不能为空~', true)
    return
  }
  if (!code.value) {
    showToast('邮箱验证码不能为空~', true)
    return
  }
  logging.value = true
  LoginApi.register({ userName: username.value, password: password.value,
    email: email.value, emailCode: code.value})
    .then((res) => {
      if (res.code === 0) {
        isLogin.value = true
      } else {
        showToast(res.msg, true)
      }
    })
    .catch((res) => {
      showToast(res.msg, true)
    })
    .finally(() => {
      logging.value = false
    })
}


const getCode = () => {
  if (!email.value) {
    showToast('邮箱不能为空~', true)
    return
  }
  logging.value = true
  LoginApi.getCode({ email: email.value })
    .then((res) => {
      if (res.code === 0) {
        console.log("验证码已发送，请注意查收")
      } else {
        showToast(res.msg, true)
      }
    })
    .catch((res) => {
      showToast(res.message, true)
    })
    .finally(() => {
      logging.value = false
    })
}
</script>

<style lang="less" scoped>
.login-container {
  height: 100%;
  width: 100%;
  position: absolute;
  display: flex;
  background: var(--screen-bg-color);

  .operations {
    position: absolute;
    top: 20px;
    right: 20px;
    display: flex;
    @media screen and (max-height: 500px) {
      display: none;
    }
  }

  .login-bg {
    width: 100%;
    height: 100%;
    display: flex;
    background-image: var(--scrren-grid-bg-color);
    background-size: 50px 50px;
  }

  .login-switch{
    cursor: pointer;
  }
  .login-switch:hover{
    color: #4c9bff;
  }

  .poster-img {
    height: 100vh;
    min-width: 500px;
    @media screen and (max-width: 1000px) {
      display: none;
    }
  }

  .logo {
    margin: 15px;
    position: absolute;
    display: flex;
    height: 60px;
    @media screen and (max-width: 1000px) {
      height: 40px;
    }
    @media screen and (max-height: 500px) {
      display: none;
    }
  }

  .login-content {
    height: 100%;
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background-size: 50px 50px;
    flex-direction: column;

    .web-info {
      margin-top: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #ababab;
      @media screen and (max-height: 500px) {
        display: none;
      }
    }

    .login-box {
      width: 600px;
      height: 360px;
      border-radius: 10px;
      background-image: linear-gradient(
        130deg,
        rgba(var(--background-color), 0.3),
        rgba(var(--background-color), 0.5)
      );
      backdrop-filter: blur(10px);
      border: rgba(var(--background-color), 0.5) 3px solid;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 60px 60px;

      @media screen and (max-width: 1000px) {
        width: 95%;
        padding: 60px 20px;
      }

      .title {
        display: flex;
        flex-direction: column;
      }

      .info {
        margin-top: 20px;
        margin-bottom: 20px;
      }

      .login-button {
        width: 100%;
        height: 50px;
        font-size: 24px;
        font-weight: 600;
        color: #ffffff;
        background-color: rgb(var(--primary-color));
        border-radius: 5px;
        display: flex;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        user-select: none;

        &.logging,
        &:hover {
          background-color: rgba(76, 155, 255, 0.8);
        }
      }
    }


    .register-box {
      width: 600px;
      height: 500px;
      border-radius: 10px;
      background-image: linear-gradient(
        130deg,
        rgba(var(--background-color), 0.3),
        rgba(var(--background-color), 0.5)
      );
      backdrop-filter: blur(10px);
      border: rgba(var(--background-color), 0.5) 3px solid;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 60px 60px;

      @media screen and (max-width: 1000px) {
        width: 95%;
        padding: 60px 20px;
      }

      .title {
        display: flex;
        flex-direction: column;
      }

      .info {
        margin-top: 20px;
        margin-bottom: 20px;
      }

      .register-button {
        width: 100%;
        height: 50px;
        font-size: 24px;
        font-weight: 600;
        color: #ffffff;
        background-color: rgb(var(--primary-color));
        border-radius: 5px;
        display: flex;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        user-select: none;

        &.logging,
        &:hover {
          background-color: rgba(76, 155, 255, 0.8);
        }
      }
      .getCode-button {
        position: absolute;
        right: 10%;
        bottom: 30%;
        width: 22%;
        height: 50px;
        font-size: 20px;
        font-weight: 600;
        color: #ffffff;
        background-color: rgb(var(--primary-color));
        border-radius: 5px;
        display: flex;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        user-select: none;

        &.logging,
        &:hover {
          background-color: rgba(76, 155, 255, 0.8);
        }
      }
    }
  }
}
</style>
