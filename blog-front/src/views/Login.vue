<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref({
  email: '',
  password: ''
})

const loading = ref(false)
const showPassword = ref(false)
const rememberMe = ref(false)

const handleLogin = async () => {
  if (!loginForm.value.email || !loginForm.value.password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }

  if (!loginForm.value.email.includes('@')) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  loading.value = true
  try {
    const success = await userStore.login(
      loginForm.value.email,
      loginForm.value.password
    )

    if (success) {
      if (rememberMe.value) {
        localStorage.setItem('rememberedEmail', loginForm.value.email)
      } else {
        localStorage.removeItem('rememberedEmail')
      }
      if (userStore.isLoggedIn) {
        router.push('/')
      } else {
        throw new Error('登录状态验证失败')
      }
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('登录失败，请检查邮箱和密码')
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/auth/register')
}

const rememberedEmail = localStorage.getItem('rememberedEmail')
if (rememberedEmail) {
  loginForm.value.email = rememberedEmail
  rememberMe.value = true
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="../assets/logo.svg" alt="Logo" class="login-logo" />
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">登录您的账号以继续</p>
      </div>
      
      <el-form class="login-form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱地址"
            type="email"
            :prefix-icon="User"
            autocomplete="email"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          >
            <template #suffix>
              <el-icon 
                class="cursor-pointer" 
                @click="showPassword = !showPassword"
              >
                <component :is="showPassword ? View : Hide" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="rememberMe" size="large">记住我</el-checkbox>
        </div>

        <el-button
          type="primary"
          :loading="loading"
          class="login-button"
          size="large"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>

        <div class="register-link">
          还没有账号？
          <el-button link type="primary" @click="handleRegister">立即注册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 2.5rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(8px);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-logo {
  width: 64px;
  height: 64px;
  margin-bottom: 1rem;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 0.5rem;
}

.login-subtitle {
  color: #666;
  font-size: 16px;
}

.login-form {
  margin-top: 1.5rem;
}

.login-form :deep(.el-input__wrapper) {
  padding: 0 12px;
  height: 48px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.login-form :deep(.el-input__inner) {
  height: 48px;
  font-size: 16px;
}

.login-form :deep(.el-input__prefix-inner) {
  font-size: 20px;
  margin-right: 12px;
}

.login-form :deep(.el-input__suffix) {
  cursor: pointer;
  font-size: 20px;
  margin-left: 8px;
  color: #666;
  transition: color 0.2s;
}

.login-form :deep(.el-input__suffix:hover) {
  color: #409eff;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 1.5rem 0;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 1rem;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  transition: transform 0.2s;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.login-button:active {
  transform: translateY(0);
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
  font-size: 15px;
}

:deep(.el-checkbox__label) {
  font-size: 15px;
}

:deep(.el-link) {
  font-size: 15px;
}

.cursor-pointer {
  cursor: pointer;
}

@media (max-width: 768px) {
  .login-box {
    margin: 1rem;
    padding: 2rem;
  }
}
</style> 