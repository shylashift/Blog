<template>
  <div class="ai-chat-page">
    <div class="page-background"></div>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="20" :md="18" :lg="16" :xl="14" class="main-column">
        <div class="page-header">
          <h2 class="page-title">
            <el-icon class="title-icon"><ChatRound /></el-icon>
            AI智能助手
          </h2>
          <div class="feature-badges">
            <span class="feature-badge">deepseek-V3</span>
            <span class="feature-badge">智能对话</span>
            <span class="feature-badge">写作建议</span>
          </div>
          <p class="page-description">
            AI助手可以帮助你解答问题，提供写作建议，优化文章内容，或者讨论任何你感兴趣的话题。
          </p>
        </div>
        <div class="chat-box-container">
          <AIChatBox />
        </div>
        <div class="usage-tips">
          <h4>使用小贴士</h4>
          <ul>
            <li>使用简洁明了的语言描述你的问题，以获得更准确的回答</li>
            <li>尝试询问与写作、文章优化相关的问题，获得专业建议</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import AIChatBox from '@/components/AIChatBox.vue'
import { ChatRound } from '@element-plus/icons-vue'
</script>

<style scoped>
.ai-chat-page {
  padding: 20px;
  min-height: calc(100vh - 80px);
  position: relative;
  overflow: hidden;
}

.page-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(240, 249, 255, 0.8), rgba(245, 245, 255, 0.8));
  z-index: -1;
}

.main-column {
  margin: 0 auto;
  max-width: 1200px;
}

.page-header {
  padding: 20px 30px 30px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, var(--el-color-primary), var(--el-color-success));
}

.page-title {
  margin-bottom: 16px;
  color: var(--el-color-primary);
  font-size: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 32px;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  color: white;
  padding: 8px;
  border-radius: 50%;
}

.feature-badges {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.feature-badge {
  padding: 4px 12px;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.page-description {
  color: var(--el-text-color-secondary);
  margin-bottom: 10px;
  line-height: 1.6;
  font-size: 15px;
}

.chat-box-container {
  margin-bottom: 24px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.usage-tips {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 20px 30px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.usage-tips h4 {
  color: var(--el-color-primary);
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 18px;
}

.usage-tips ul {
  margin: 0;
  padding-left: 20px;
}

.usage-tips li {
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

@media (max-width: 768px) {
  .ai-chat-page {
    padding: 12px;
  }
  
  .page-header {
    padding: 16px 20px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .title-icon {
    font-size: 24px;
    padding: 6px;
  }
}
</style> 