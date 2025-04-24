<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Edit, Share, Comment } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleStartWriting = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再开始写作')
    router.push('/login')
    return
  }
  router.push('/posts/create')
}

const handleViewMessages = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后查看消息')
    router.push('/login')
    return
  }
  router.push('/messages')
}

const handleViewNotifications = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后查看消息')
    router.push('/login')
    return
  }
  router.push('/notifications')
}

</script>

<template>
  <div class="home-container">
    <div class="hero-section">
      <h1>分享你的故事</h1>
      <p class="subtitle">在这里，每个人都是独特的创作者</p>
      <div class="hero-buttons">
        <el-button type="primary" size="large" @click="handleStartWriting">开始写作</el-button>
        <el-button size="large" @click="handleViewMessages">
          <el-icon><Comment /></el-icon>
          互动消息
        </el-button>
      </div>
    </div>

    <div class="features-section">
      <h2>探索更多可能</h2>
      <div class="features-grid">
        <div class="feature-card" @click="handleStartWriting">
          <el-icon class="feature-icon"><Edit /></el-icon>
          <h3>轻松写作</h3>
          <p>支持 Markdown 编辑器，让写作变得简单愉悦</p>
        </div>
        <div class="feature-card" @click="handleViewNotifications">
          <el-icon class="feature-icon"><Share /></el-icon>
          <h3>信息通知</h3>
          <p>让你了解其他学者对你的文章的评论和反馈</p>
        </div>
        <div class="feature-card" @click="handleViewMessages">
          <el-icon class="feature-icon"><Comment /></el-icon>
          <h3>评论中心</h3>
          <p>与志同道合的学者交流讨论，收藏有价值的文章</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.hero-section {
  text-align: center;
  padding: 60px 0;
  width: 100%;
}

.hero-section h1 {
  font-size: 3rem;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
}

.subtitle {
  font-size: 1.5rem;
  color: var(--el-text-color-secondary);
  margin-bottom: 40px;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.features-section {
  padding: 60px 0;
  width: 100%;
}

.features-section h2 {
  text-align: center;
  font-size: 2.5rem;
  color: var(--el-text-color-primary);
  margin-bottom: 40px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  width: 100%;
}

.feature-card {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 40px;
  color: var(--primary-color);
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 1.4rem;
  color: var(--text-color);
  margin-bottom: 16px;
}

.feature-card p {
  color: var(--text-color-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .home-container {
    padding: 20px 15px;
  }

  .hero-section {
    padding: 40px 0;
  }

  .hero-section h1 {
    font-size: 2rem;
  }

  .hero-buttons {
    flex-direction: column;
    gap: 12px;
    padding: 0 20px;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .feature-card {
    padding: 20px;
  }
}
</style> 