<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats, type DashboardStats } from '@/api/admin'
import { useRouter } from 'vue-router'
import {
  Monitor,
  User,
  Document,
  ChatDotRound
} from '@element-plus/icons-vue'

const activeTab = ref('')
const stats = ref<DashboardStats>({
  totalUsers: 0,
  totalPosts: 0,
  totalComments: 0,
  todayVisits: 0
})

const router = useRouter()

const handleSelect = (key: string) => {
  activeTab.value = key
  router.push(`/admin/${key}`)
}

const fetchDashboardStats = async () => {
  try {
    const data = await getDashboardStats()
    stats.value = data
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

onMounted(() => {
  // 根据当前路由路径设置激活的标签
  const path = router.currentRoute.value.path
  const key = path.replace('/admin/', '')
  activeTab.value = key
  
  fetchDashboardStats()
})
</script>

<template>
  <div class="admin-dashboard">
    <!-- 左侧导航栏 -->
    <div class="nav-sidebar">
      <div class="logo">
        <h1 class="text-xl font-bold">博客管理系统</h1>
      </div>
      <el-menu
        :default-active="activeTab"
        class="nav-menu"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#ffffff"
        @select="handleSelect"
      >
        <el-menu-item index="">
          <el-icon><Monitor /></el-icon>
          <span>仪表盘概览</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="posts">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  display: flex;
  min-height: 100vh;
}

.nav-sidebar {
  width: 240px;
  background: #001529;
  color: white;
  padding: 20px 0;
  flex-shrink: 0;
}

.logo {
  padding: 16px 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-menu {
  border-right: none;
}

.main-content {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
  overflow-y: auto;
}

.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 4px solid;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 32px;
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
  color: #262626;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 4px;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
}

:deep(.el-menu-item:hover) {
  background-color: #1890ff40 !important;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 10px;
  font-size: 18px;
}
</style> 