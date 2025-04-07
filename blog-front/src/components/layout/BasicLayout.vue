<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  House,
  Document,
  EditPen,
  ChatDotRound,
  Bell,
  User,
  Setting,
  SwitchButton,
  Search
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchQuery = ref('')
const unreadCount = ref(0)  // 这个值应该从后端获取

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => userStore.userInfo?.roles?.includes('ROLE_ADMIN'))

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`)
  }
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'admin-users':
      router.push('/admin/users')
      break
    case 'admin-posts':
      router.push('/admin/posts')
      break
    case 'logout':
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}
</script>

<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <div class="left-section">
          <div class="logo">
            <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
            <span class="logo-text">学术交流博客</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            router
            class="nav-menu"
            background-color="transparent"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>首页
            </el-menu-item>
            <el-menu-item index="/posts">
              <el-icon><Document /></el-icon>文章列表
            </el-menu-item>
            <template v-if="userStore.isLoggedIn">
              <el-menu-item index="/posts/create">
                <el-icon><EditPen /></el-icon>发布文章
              </el-menu-item>
              <el-menu-item index="/messages">
                <el-icon><ChatDotRound /></el-icon>学术交流
              </el-menu-item>
              <el-menu-item v-if="isAdmin" index="/admin/users">
                <el-icon><Setting /></el-icon>管理面板
              </el-menu-item>
            </template>
          </el-menu>
        </div>

        <div class="right-section">
          <el-space>
            <template v-if="userStore.isLoggedIn">
              <el-dropdown trigger="click" @command="handleCommand">
                <div class="user-profile">
                  <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">
                    {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <span class="username">{{ userStore.userInfo?.username }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <template v-if="isAdmin">
                      <el-dropdown-item divided command="admin-users">
                        <el-icon><Setting /></el-icon>用户管理
                      </el-dropdown-item>
                      <el-dropdown-item command="admin-posts">
                        <el-icon><Document /></el-icon>文章管理
                      </el-dropdown-item>
                    </template>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" @click="router.push('/login')">登录</el-button>
              <el-button @click="router.push('/register')">注册</el-button>
            </template>
          </el-space>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: var(--el-bg-color);
}

.header {
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: var(--el-bg-color);
  position: fixed;
  width: 100%;
  z-index: 1000;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-img {
  height: 32px;
  width: auto;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--el-color-primary);
  white-space: nowrap;
}

.nav-menu {
  border-bottom: none;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-profile:hover {
  background-color: var(--el-fill-color-light);
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.notification-badge :deep(.el-badge__content) {
  z-index: 1001;
}

.main-content {
  width: 100%;
  margin: 60px auto 0;
  padding: 20px;
  min-height: calc(100vh - 60px - 200px);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-bottom: 20px;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-section h4 {
  color: var(--el-text-color-primary);
  margin: 0;
}

.footer-section .el-link {
  margin: 4px 0;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-secondary);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 