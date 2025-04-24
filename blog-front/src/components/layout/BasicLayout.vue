<script setup lang="ts">
import {computed} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  House,
  Document,
  EditPen,
  User,
  Setting,
  SwitchButton,
  ChatRound
} from '@element-plus/icons-vue'   //导入图表组件
import { getAvatarUrl, getBackupAvatar } from '@/utils/avatarUtils'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => {
  console.log('组件检查管理员权限:', {
    userInfo: userStore.userInfo,
    roles: userStore.userInfo?.roles
  })
  
  const roles = userStore.userInfo?.roles
  // 同时支持'ROLE_ADMIN'和'管理员'角色
  return roles?.some(role => role === 'ROLE_ADMIN' || role === '管理员') || false
})

// 处理头像加载错误
const handleAvatarError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  target.src = getBackupAvatar();
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}

console.log('头像URL:', getAvatarUrl(userStore.userInfo?.avatar));
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
              <el-menu-item index="/ai-chat">
                <el-icon><ChatRound /></el-icon>AI助手
              </el-menu-item>
            </template>
          </el-menu>
        </div>

        <div class="right-section">
          <el-space>
            <template v-if="userStore.isLoggedIn">
              <el-dropdown trigger="click" @command="handleCommand">
                <div class="user-profile">
                  <el-avatar 
                    :size="32" 
                    :src="userStore.userInfo?.avatar ? 
                      `http://localhost:8080/uploads/${userStore.userInfo.avatar.replace(/^.*\/uploads\//, '')}`
                      : 'https://api.dicebear.com/7.x/bottts/png?seed=1234'" 
                    @error="handleAvatarError"
                  >
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
                      <el-dropdown-item divided command="admin">
                        <el-icon><Setting /></el-icon>后台管理
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
        <component 
          :is="Component" 
          :key="Date.now()"
        />
      </router-view>
    </el-main>

    <el-footer class="footer">
      <div class="footer-content">
        <span>© {{ new Date().getFullYear() }} 学术交流博客. 版权所有.</span>
      </div>
    </el-footer>
  </el-container>
</template>

<style>
/* 添加全局样式重置 */
:global(body),
:global(html) {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
}
</style>

<style scoped>
.layout-container {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background-color: var(--el-bg-color);
  width: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 0;
  margin: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: var(--el-bg-color);
  position: fixed;
  width: 100%;
  z-index: 1000;
  left: 0;
}

.header-content {
  max-width: 100%;
  width: 100%;
  margin: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background-color: var(--el-bg-color);
  box-sizing: border-box;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 40px;
  flex: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-width: 180px;
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
  flex: 1;
  display: flex;
  justify-content: flex-start;
  border-bottom: none !important;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  padding: 0 24px;
  font-size: 15px;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 6px;
  font-size: 18px;
}

.right-section {
  display: flex;
  align-items: center;
  min-width: 200px;
  justify-content: flex-end;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-profile:hover {
  background-color: var(--el-fill-color-light);
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.main-content {
  margin-top: 60px;
  padding: 20px 40px;
  width: 100%;
  max-width: 100%;
  min-height: calc(100vh - 60px - 60px);
  box-sizing: border-box;
  margin-left: 0;
  margin-right: 0;
}

.footer {
  padding: 20px 0;
  width: 100%;
  margin: 0;
}

.footer-content {
  max-width: 100%;
  width: 100%;
  margin: 0;
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 0 40px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .main-content {
    padding: 20px 15px;
  }
  
  .header-content {
    padding: 0 15px;
  }

  .footer-content {
    padding: 0 15px;
  }
}
</style> 