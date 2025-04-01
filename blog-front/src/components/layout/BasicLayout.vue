<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <h1 class="logo" @click="router.push('/')">我的博客</h1>
        <el-menu
          mode="horizontal"
          :router="true"
          class="nav-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/posts">文章</el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <template v-if="userStore.isAuthenticated">
          <el-dropdown>
            <el-button class="user-button">
              {{ userStore.userInfo?.username }}
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/posts/create')">
                  <el-icon><EditPen /></el-icon>
                  发布文章
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">
      <p>&copy; {{ new Date().getFullYear() }} 我的博客. All rights reserved.</p>
    </el-footer>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: var(--bg-color);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid var(--border-color);
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  margin: 0;
  font-size: 1.5rem;
  color: var(--primary-color);
  cursor: pointer;
}

.nav-menu {
  margin-left: 40px;
  border-bottom: none;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.main {
  margin-top: 60px;
  min-height: calc(100vh - 120px);
  background-color: var(--bg-color);
}

.footer {
  text-align: center;
  color: var(--text-color-secondary);
  background-color: #fff;
  border-top: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }

  .logo {
    font-size: 1.2rem;
  }

  .nav-menu {
    margin-left: 20px;
  }

  .el-menu--horizontal > .el-menu-item {
    padding: 0 10px;
  }
}
</style> 