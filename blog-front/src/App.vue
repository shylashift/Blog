<script setup lang="ts">
// App.vue
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)

// 应用初始化时预加载用户信息
onMounted(async () => {
  console.log('App初始化，开始加载用户信息')
  loading.value = true
  try {
    await userStore.initialize()
    console.log('App初始化完成，用户登录状态:', userStore.isLoggedIn)
  } catch (error) {
    console.error('App初始化用户信息失败:', error)
    ElMessage.error('初始化失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <el-config-provider :locale="zhCn">
    <div v-if="loading" class="global-loading">
      <el-skeleton :rows="10" animated />
    </div>
    <router-view v-else />
  </el-config-provider>
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-size: 16px;
}

#app {
  height: 100%;
  width: 100%;
  overflow-x: hidden; /* 防止水平滚动条 */
  overflow-y: hidden; /* 防止垂直滚动条 */
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 确保元素盒模型统一 */
* {
  box-sizing: border-box;
}

/* 消除所有元素的边框，确保布局连续 */
.el-container, .el-main, .el-header, .el-footer, .el-aside {
  border: none !important;
}

/* 设置统一的内容宽度 */
.el-main, .el-header-content, .main-content {
  width: 100%;
  margin: 0 auto;
}

/* 博客前台页面保持原有宽度限制 */
.blog-content .el-main,
.blog-content .el-header-content,
.blog-content .main-content {
  max-width: 1200px;
}

.admin-content .el-main,
.admin-content .el-header-content,
.admin-content .main-content {
  max-width: none;
  padding: 0 16px;
}

/* 确保所有卡片样式统一 */
.el-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
  border: none !important;
  width: 100%;
}

/* 确保内容区域占满容器 */
.el-main > div {
  width: 100%;
}

.global-loading {
  max-width: 800px;
  margin: 100px auto;
  padding: 20px;
}
</style> 