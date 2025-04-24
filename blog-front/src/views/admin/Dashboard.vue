<template>
  <div class="dashboard-content">
    <h2 class="text-2xl font-bold mb-6">仪表盘概览</h2>
    <div class="stats-grid">
      <div
        v-for="card in statsCards"
        :key="card.key"
        class="stat-card"
        :style="{ borderColor: card.color }"
      >
        <component :is="card.icon" class="stat-icon" :style="{ color: card.color }" />
        <div class="stat-info">
          <div class="stat-value">{{ stats[card.key] }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats, type DashboardStats } from '@/api/admin'
import { 
  UserOutlined, 
  FileTextOutlined, 
  CommentOutlined, 
  EyeOutlined
} from '@ant-design/icons-vue'

const stats = ref<DashboardStats>({
  totalUsers: 0,
  totalPosts: 0,
  totalComments: 0,
  todayVisits: 0
})

// 统计卡片数据
const statsCards = [
  {
    key: 'totalUsers' as keyof DashboardStats,
    label: '总用户数',
    icon: UserOutlined,
    color: '#1890ff'
  },
  {
    key: 'totalPosts' as keyof DashboardStats,
    label: '文章总数',
    icon: FileTextOutlined,
    color: '#52c41a'
  },
  {
    key: 'totalComments' as keyof DashboardStats,
    label: '评论总数',
    icon: CommentOutlined,
    color: '#faad14'
  },
  {
    key: 'todayVisits' as keyof DashboardStats,
    label: '今日访问',
    icon: EyeOutlined,
    color: '#722ed1'
  }
]

const fetchDashboardStats = async () => {
  try {
    const data = await getDashboardStats()
    stats.value = data
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

onMounted(() => {
  fetchDashboardStats()
})
</script>

<style scoped>
.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
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
</style> 