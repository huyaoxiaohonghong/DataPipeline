<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const formState = reactive({
  username: '',
  password: '',
  remember: true
})

// 表单引用
const formRef = ref()
const loading = ref(false)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 登录
const handleLogin = async () => {
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 调用登录 API
    await userStore.login(formState.username, formState.password)
    
    message.success('登录成功')
    
    // 跳转到之前的页面或首页
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (error: any) {
    if (!error.errorFields) {
      // 非表单验证错误已在拦截器中处理
      console.error('登录失败:', error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo -->
      <div class="login-header">
        <h1 class="login-title">DataPipeline</h1>
        <p class="login-subtitle">数据管道管理平台</p>
      </div>

      <!-- 登录表单 -->
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        class="login-form"
        @finish="handleLogin"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formState.username"
            size="large"
            placeholder="用户名"
            autocomplete="username"
          >
            <template #prefix>
              <UserOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            size="large"
            placeholder="密码"
            autocomplete="current-password"
          >
            <template #prefix>
              <LockOutlined class="input-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <div class="login-options">
            <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox>
            <a class="forgot-link">忘记密码？</a>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            size="large"
            block
            html-type="submit"
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <!-- 底部信息 -->
      <div class="login-footer">
        <p>演示账号：admin / 123456</p>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
  </div>
</template>

<style scoped lang="less">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .login-title {
    font-size: 32px;
    font-weight: 600;
    color: #1890ff;
    margin-bottom: 8px;
  }

  .login-subtitle {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
  }
}

.login-form {
  .input-icon {
    color: rgba(0, 0, 0, 0.25);
  }

  .login-options {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .forgot-link {
      color: #1890ff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;

  .bg-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    background: #fff;
  }

  .shape-1 {
    width: 600px;
    height: 600px;
    top: -200px;
    left: -200px;
  }

  .shape-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    right: -100px;
  }

  .shape-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    right: 20%;
  }
}
</style>
