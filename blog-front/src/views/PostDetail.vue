<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUserStore } from '@/stores/user'
import { Star, StarFilled, Pointer, UserFilled } from '@element-plus/icons-vue'
import { getPostById, likePost, addComment, checkPostFavorite, favoritePost, unfavoritePost } from '@/api/posts'
import type { Post } from '@/api/posts'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)  // 初始化时设置为 true

const post = ref<Post | null>(null)
const commentContent = ref('')
const commentLoading = ref(false)

// 收藏状态
const isFavorited = ref(false)
const favoriteLoading = ref(false)

const fetchPost = async () => {
  try {
    const postId = parseInt(route.params.id as string)
    if (isNaN(postId)) {
      throw new Error('无效的文章ID')
    }
    post.value = await getPostById(postId)
    console.log('文章详情:', {
      postId: post.value?.postId,
      title: post.value?.title,
      commentsCount: post.value?.comments?.length || 0,
      comments: post.value?.comments
    })
    await nextTick()  // 等待 DOM 更新
  } catch (error) {
    console.error('获取文章错误:', error)
    ElMessage.error('获取文章失败')
    router.push('/posts')
  }
}

// 检查是否已收藏
const checkFavoriteStatus = async () => {
  try {
    if (!post.value || !post.value.postId) return
    
    console.log('检查文章收藏状态:', {
      postId: post.value.postId,
      isLoggedIn: userStore.isLoggedIn,
      hasToken: !!userStore.token
    })
    
    // 确保已登录
    if (!userStore.isLoggedIn) {
      console.log('用户未登录，收藏状态默认为false')
      isFavorited.value = false
      return
    }
    
    // 确保用户状态已初始化
    if (!userStore.initialized) {
      await userStore.initialize()
    }
    
    const result = await checkPostFavorite(post.value.postId)
    console.log('收藏状态结果:', result)
    isFavorited.value = result
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorited.value = false
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage({
      message: '登录后即可收藏文章',
      type: 'info',
      showClose: true,
      duration: 5000,
      center: true
    })
    router.push('/login')
    return
  }

  if (!post.value) return

  try {
    favoriteLoading.value = true
    // 确保用户状态已初始化
    if (!userStore.initialized) {
      await userStore.initialize()
    }

    // 重新检查登录状态
    if (!userStore.isLoggedIn || !userStore.token) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return
    }
    
    console.log('收藏操作前状态:', {
      isLoggedIn: userStore.isLoggedIn,
      hasToken: !!userStore.token,
      token: userStore.token ? userStore.token.substring(0, 20) + '...' : 'none'
    })
    
    if (isFavorited.value) {
      await unfavoritePost(post.value.postId)
      ElMessage.success('已取消收藏')
    } else {
      await favoritePost(post.value.postId)
      ElMessage.success('收藏成功')
    }
    isFavorited.value = !isFavorited.value
  } catch (error: any) {
    console.error('收藏操作失败:', error)
    if (error.response?.status === 401) {
      ElMessage({
        message: '登录已过期，请重新登录',
        type: 'warning',
        showClose: true,
        duration: 5000
      })
      // 清除用户状态
      userStore.clearUser()
      // 延迟跳转，让用户看到消息
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    favoriteLoading.value = false
  }
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    commentLoading.value = true
    if (!post.value?.postId) return
    
    const newComment = await addComment(post.value.postId, commentContent.value)
    
    // 确保评论对象结构与后端返回一致，在后端返回的评论中添加用户信息
    // 后端应该直接返回带有user对象的评论，但我们这里做一个兼容处理
    if (!newComment.user && userStore.userInfo) {
      // 如果后端返回的评论没有user对象，但用户已登录，添加user信息
      newComment.user = {
        userId: userStore.userInfo.userId,
        username: userStore.userInfo.username,
        avatar: userStore.userInfo.avatar || undefined
      }
    }
    
    if (post.value) {
      post.value.comments = post.value.comments || []
      post.value.comments.unshift(newComment)
    }
    commentContent.value = ''
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('评论错误:', error)
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 初始化数据
const initializeData = async () => {
  try {
    loading.value = true
    // 确保用户状态已初始化
    await userStore.initialize()
    console.log('用户状态初始化完成:', {
      isLoggedIn: userStore.isLoggedIn,
      hasToken: !!userStore.token,
      initialized: userStore.initialized
    })
    // 获取文章数据
    await fetchPost()
    // 检查收藏状态
    if (post.value && userStore.initialized) {
      await checkFavoriteStatus()
    }
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('加载失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initializeData()
})

onUnmounted(() => {
  // 清理组件状态
  post.value = null
  commentContent.value = ''
  isFavorited.value = false
  loading.value = true  // 重置为初始状态
  favoriteLoading.value = false
  commentLoading.value = false
})
</script>

<template>
  <div class="post-detail">
    <!-- 加载状态 -->
    <template v-if="loading">
      <div class="skeleton-content">
        <el-skeleton>
          <template #template>
            <div style="padding: 20px">
              <el-skeleton-item variant="h1" style="width: 50%" />
              <div style="margin: 20px 0">
                <el-skeleton-item variant="text" style="width: 80%" />
              </div>
              <el-skeleton-item variant="text" style="width: 100%" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </template>

    <!-- 文章内容 -->
    <template v-else-if="post">
      <!-- 文章标题和元信息 -->
      <div class="post-header">
        <h1>{{ post.title }}</h1>
        <div class="post-meta">
          <div class="author-info">
            <el-avatar 
              :size="40" 
              :src="post.authorAvatar || post.author?.avatar || ''" 
              v-if="post.authorAvatar || post.author?.avatar"
            >
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="author">{{ post.authorName || post.author?.username || '未知作者' }}</span>
          </div>
          <span class="time">发布时间：{{ new Date(post.createdAt).toLocaleDateString() }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="post-actions">
        <el-tooltip
          :content="userStore.isLoggedIn ? '' : '登录后即可收藏文章'"
          placement="top"
          effect="light"
        >
          <el-button
            :type="isFavorited ? 'warning' : 'default'"
            :icon="isFavorited ? StarFilled : Star"
            :loading="favoriteLoading"
            @click="toggleFavorite"
          >
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </el-tooltip>
      </div>

      <!-- 文章标签 -->
      <div v-if="post.tags?.length" class="post-tags">
        <el-tag
          v-for="tag in post.tags"
          :key="tag"
          size="small"
          class="tag"
        >
          {{ tag }}
        </el-tag>
      </div>

      <!-- 文章内容 -->
      <div class="post-content">
        <MdPreview :modelValue="post.content || ''" />
      </div>

      <!-- 评论区 -->
      <div class="post-comments">
        <h2>评论 ({{ post.comments?.length || 0 }})</h2>
        
        <!-- 评论输入框 -->
        <div class="comment-form">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            :disabled="!userStore.isLoggedIn"
          />
          <div class="comment-form-actions">
            <el-button
              type="primary"
              :loading="commentLoading"
              @click="submitComment"
              :disabled="!userStore.isLoggedIn"
            >
              发表评论
            </el-button>
            <p v-if="!userStore.isLoggedIn" class="login-tip">
              请 <el-link type="primary" @click="router.push('/login')">登录</el-link> 后发表评论
            </p>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comments-list">
          <template v-if="post.comments?.length">
            <div
              v-for="comment in post.comments"
              :key="comment.commentId"
              class="comment-item"
            >
              <div class="comment-header">
                <div class="comment-author-info">
                  <el-avatar 
                    :size="32" 
                    :src="comment.user?.avatar" 
                    v-if="comment.user?.avatar"
                  >
                    <el-icon><UserFilled /></el-icon>
                  </el-avatar>
                  <span class="comment-author-name">{{ comment.user?.username || '匿名用户' }}</span>
                </div>
                <span class="comment-time">
                  {{ new Date(comment.createdAt).toLocaleString() }}
                </span>
              </div>
              <div class="comment-content">
                {{ comment.content }}
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无评论" />
        </div>
      </div>
    </template>

    <!-- 文章不存在 -->
    <template v-else>
      <el-result
        icon="warning"
        title="文章不存在或已被删除"
        sub-title="请检查文章链接是否正确"
      >
        <template #extra>
          <el-button type="primary" @click="router.push('/posts')">
            返回文章列表
          </el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<style scoped>
.post-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 400px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.skeleton-content {
  padding: 20px;
  min-height: 400px;
}

.post-header {
  margin-bottom: 24px;
}

.post-header h1 {
  margin: 0 0 16px;
  font-size: 2rem;
  color: var(--el-text-color-primary);
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-info .author {
  font-weight: 500;
  margin-left: 5px;
}

.post-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.post-tags {
  margin: 16px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-content {
  margin: 24px 0;
  padding: 20px;
  background-color: var(--el-bg-color-page);
  border-radius: 4px;
}

.post-comments {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-light);
}

.comment-form {
  margin: 24px 0;
}

.comment-form-actions {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.login-tip {
  margin: 0;
  color: var(--el-text-color-secondary);
}

.comments-list {
  margin-top: 24px;
}

.comment-item {
  padding: 16px;
  margin-bottom: 16px;
  background-color: var(--el-bg-color-page);
  border-radius: 4px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-author-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.comment-time {
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.comment-content {
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .post-detail {
    padding: 16px;
  }

  .post-header h1 {
    font-size: 1.5rem;
  }

  .post-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-actions {
    flex-wrap: wrap;
  }
}
</style> 