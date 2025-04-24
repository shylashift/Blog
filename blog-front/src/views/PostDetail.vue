<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUserStore } from '@/stores/user'
import { Star, StarFilled, Calendar, Collection, Edit } from '@element-plus/icons-vue'
import { getPostById, addComment, checkPostFavorite, favoritePost, unfavoritePost } from '@/api/posts'
import type { Post } from '@/api/posts'
import { getAvatarUrl } from '@/utils/avatarUtils'

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

interface User {
  userId: number
  username: string
  avatar?: string
  bio?: string
  createdAt?: string
}

const selectedUser = ref<User | null>(null)
const showUserInfo = ref(false)

const fetchPost = async () => {
  try {
    const postId = parseInt(route.params.id as string)
    if (isNaN(postId)) {
      throw new Error('无效的文章ID')
    }
    post.value = await getPostById(postId)
    
    // 添加详细的调试日志
    console.log('文章详情数据:', {
      postId: post.value?.postId,
      title: post.value?.title,
      createdAt: post.value?.createdAt,
      authorName: post.value?.authorName,
      authorUsername: post.value?.author?.username,
      authorAvatar: post.value?.authorAvatar,
      authorObject: post.value?.author,
      comments: post.value?.comments?.map(c => ({
        commentId: c.commentId,
        content: c.content?.substring(0, 30) + '...',
        postId: c.postId,
        user: c.user ? {
          userId: c.user.userId,
          username: c.user.username,
          avatar: c.user.avatar
        } : '无用户信息'
      }))
    })
    
    // 检查评论数据结构
    if (post.value?.comments) {
      for (const comment of post.value.comments) {
        if (!comment.user || !comment.user.username) {
          console.warn('发现缺少用户信息的评论:', {
            commentId: comment.commentId,
            content: comment.content?.substring(0, 30) + '...',
            postId: comment.postId,
            user: comment.user
          })
        }
      }
    }
    
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
      userStore.clearUserInfo()
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
    router.push('/login')
    return
  }

  // 检查用户是否被禁用
  if (userStore.userInfo?.disabled) {
    ElMessage.error('您的账号已被禁用，暂时无法发表评论')
    return
  }

  const trimmedContent = commentContent.value.trim()
  if (!trimmedContent) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    commentLoading.value = true
    if (!post.value?.postId) {
      ElMessage.error('文章不存在')
      return
    }
    
    // 添加日志输出用户信息
    console.log('提交评论前的信息:', {
      postId: post.value.postId,
      content: trimmedContent,
      contentLength: trimmedContent.length,
      userInfo: userStore.userInfo,
      isLoggedIn: userStore.isLoggedIn,
      token: userStore.token ? '已设置' : '未设置'
    })
    
    const newComment = await addComment(post.value.postId, trimmedContent)
    
    // 详细输出后端返回的评论对象
    console.log('后端返回的新评论对象:', newComment)
    
    // 确保评论对象结构与后端返回一致
    if (!newComment.user && userStore.userInfo) {
      // 如果后端返回的评论没有user对象，但用户已登录，添加user信息
      newComment.user = {
        userId: userStore.userInfo.userId,
        username: userStore.userInfo.username,
        avatar: userStore.userInfo.avatar || undefined
      }
      console.log('手动添加评论用户信息:', newComment.user)
    }
    
    if (post.value) {
      post.value.comments = post.value.comments || []
      post.value.comments.unshift(newComment)
      post.value.commentCount = (post.value.commentCount || 0) + 1
    }
    commentContent.value = ''
    ElMessage.success('评论成功')
  } catch (error: any) {
    console.error('评论提交失败:', error)
    ElMessage.error(error.message || '评论失败，请稍后重试')
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
    
    // 输出作者信息，用于调试
    console.log('文章作者信息:', {
      postId: post.value?.postId,
      author: post.value?.author,
      authorName: post.value?.authorName,
      currentUser: userStore.userInfo,
      isAuthor: isAuthor.value
    })
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

// 格式化日期
const formatDate = (date: string | number | Date) => {
  if (!date) return '暂无发布时间'
  try {
    const d = new Date(date)
    if (isNaN(d.getTime())) {
      console.error('无效的日期格式:', date)
      return '时间格式错误'
    }
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('时间格式化错误:', error, date)
    return '时间格式错误'
  }
}

// 获取作者名称的辅助函数
const getAuthorName = (post: Post | null): string => {
  if (!post) return '未知作者'
  // 优先使用authorName字段
  if (post.authorName && post.authorName.trim() !== '') {
    return post.authorName
  }
  // 其次使用author.username
  if (post.author && post.author.username && post.author.username.trim() !== '') {
    return post.author.username
  }
  // 最后返回默认值
  return '未知作者'
}

// 获取评论用户名称的辅助函数
const getCommentUserName = (comment: any): string => {
  if (!comment) return '匿名用户'
  
  // 优先使用comment.user.username
  if (comment.user && comment.user.username && comment.user.username.trim() !== '') {
    return comment.user.username
  }
  
  // 如果comment中有userName字段
  if (comment.userName && comment.userName.trim() !== '') {
    return comment.userName
  }
  
  // 最后返回默认值
  return '匿名用户'
}

// 获取用户显示名称
const getUserDisplayName = () => {
  if (!userStore.isLoggedIn || !userStore.userInfo) return '游客';
  return userStore.userInfo.username || '未知用户';
}

// 检查当前用户是否是文章作者
const isAuthor = computed(() => {
  if (!userStore.isLoggedIn || !userStore.userInfo || !post.value) {
    return false;
  }
  
  // 检查用户ID是否与文章作者ID匹配
  if (post.value.author && post.value.author.userId && userStore.userInfo.userId) {
    return post.value.author.userId === userStore.userInfo.userId;
  }
  
  // 如果没有userId，则通过用户名比较（不太可靠，但作为备选）
  if (post.value.author && post.value.author.username && userStore.userInfo.username) {
    return post.value.author.username === userStore.userInfo.username;
  }
  
  // 如果只有authorName，则与当前用户名比较
  if (post.value.authorName && userStore.userInfo.username) {
    return post.value.authorName === userStore.userInfo.username;
  }
  
  return false;
})

// 跳转到编辑页面
const handleEditPost = () => {
  if (!post.value || !post.value.postId) return;
  router.push(`/posts/${post.value.postId}/edit`);
}

// 显示当前用户信息，用于调试
console.log('当前用户信息:', {
  isLoggedIn: userStore.isLoggedIn,
  userInfo: userStore.userInfo,
  displayName: getUserDisplayName()
});

// 处理点击用户头像
const handleUserClick = (user: User) => {
  selectedUser.value = user
  showUserInfo.value = true
}
</script>

<template>
  <div class="post-detail" v-loading="loading">
    <!-- 加载状态 -->
    <template v-if="loading">
      <div class="skeleton-content">
        <el-skeleton :rows="6" animated />
      </div>
    </template>

    <!-- 文章内容 -->
    <template v-else-if="post">
      <!-- 文章标题和元信息 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <div class="author-info">
            <el-avatar 
              :size="40" 
              :src="getAvatarUrl(post.authorAvatar || (post.author && post.author.avatar))"
            />
            <span class="author">{{ getAuthorName(post) }}</span>
          </div>
          <div class="meta-info">
            <span class="time">
              <el-icon><Calendar /></el-icon>
              {{ post.createdAt ? formatDate(post.createdAt) : '暂无发布时间' }}
            </span>
          </div>
        </div>
      </div>

      <!-- 文章标签 -->
      <div v-if="post.tags" class="post-tags">
        <el-icon><Collection /></el-icon>
        <el-tag
          v-for="tag in (Array.isArray(post.tags) ? post.tags : post.tags.split(','))"
          :key="tag"
          size="small"
          class="tag"
          effect="light"
        >
          {{ tag.trim() }}
        </el-tag>
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
            class="action-button"
          >
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </el-tooltip>
        
        <!-- 编辑按钮，仅作者可见 -->
        <el-button
          v-if="isAuthor"
          type="primary"
          :icon="Edit"
          class="action-button"
          @click="handleEditPost"
        >
          编辑文章
        </el-button>
      </div>

      <!-- 文章内容 -->
      <div class="post-content">
        <MdPreview :modelValue="post.content || ''" />
      </div>

      <!-- 评论区 -->
      <div class="post-comments">
        <h2>评论区</h2>
        <!-- 评论输入框 -->
        <div class="comment-form">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="4"
            placeholder="写下你的评论..."
            :disabled="!userStore.isLoggedIn"
          />
          <div class="comment-form-actions">
            <div class="comment-author-info" v-if="userStore.isLoggedIn">
              <el-avatar 
                :size="32" 
                :src="userStore.userInfo?.avatar ? 
                  `http://localhost:8080/uploads/${userStore.userInfo.avatar.replace(/^.*\/uploads\//, '')}`
                  :'https://api.dicebear.com/7.x/bottts/png?seed=1234'"
              />
              <span class="comment-author-name">{{ userStore.userInfo?.username }}</span>
            </div>
            <el-button 
              type="primary" 
              @click="submitComment" 
              :loading="commentLoading"
              :disabled="!userStore.isLoggedIn"
            >
              {{ userStore.isLoggedIn ? '发表评论' : '请先登录' }}
            </el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div v-if="post?.comments?.length" class="comment-list">
          <div v-for="comment in post.comments" :key="comment.commentId" class="comment-item">
            <div class="comment-header">
              <div class="comment-author-info">
                <el-avatar 
                  :size="40" 
                  :src="getAvatarUrl(comment.user?.avatar)" 
                  @click="handleUserClick(comment.user)"
                  class="clickable"
                />
                <div class="comment-author-meta">
                  <div class="comment-author-name">{{ getCommentUserName(comment) }}</div>
                  <div class="comment-time">{{ formatDate(comment.createdAt) }}</div>
                </div>
              </div>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评论" />
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
    <UserInfoPopup
      v-if="selectedUser"
      v-model:visible="showUserInfo"
      :user="selectedUser"
    />
  </div>
</template>

<style scoped>
.post-detail {
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
  padding: 32px;
  min-height: 400px;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.5s ease-in-out;
  box-sizing: border-box;
  overflow: hidden; /* 防止内容溢出 */
  border: none; /* 完全移除边框 */
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.post-title {
  margin: 0 0 24px;
  font-size: 2.5rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.3;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--el-text-color-secondary);
  font-size: 0.95rem;
}

.meta-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.post-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 24px 0;
  padding: 16px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.tag {
  margin: 0;
  border-radius: 12px;
  padding: 0 12px;
  height: 24px;
  line-height: 22px;
  background-color: var(--el-bg-color);
  border-color: var(--el-border-color);
  transition: all 0.3s ease;
}

.tag:hover {
  transform: scale(1.05);
  background-color: var(--el-color-primary-light-9);
}

.post-actions {
  display: flex;
  gap: 16px;
  margin: 24px 0;
}

.action-button {
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
}

.post-content {
  margin: 32px 0;
  padding: 24px;
  background-color: var(--el-bg-color-page);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 评论区样式优化 */
.post-comments {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 2px solid var(--el-border-color-lighter);
}

.post-comments h2 {
  font-size: 1.5rem;
  margin-bottom: 24px;
  color: var(--el-text-color-primary);
}

.comment-form {
  margin: 24px 0;
  padding: 20px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.comment-form-actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}

.comment-form :deep(.el-textarea__inner) {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s;
}

.comment-form :deep(.el-textarea__inner:focus) {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.comment-item {
  padding: 20px;
  margin-bottom: 20px;
  background-color: var(--el-bg-color-page);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.comment-item:hover {
  transform: translateY(-2px);
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
  margin-top: 12px;
  color: var(--el-text-color-primary);
  line-height: 1.6;
  white-space: pre-wrap;
}

.comment-author-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-tip {
  margin: 0;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .post-detail {
    padding: 20px;
    border-radius: 0;
  }

  .post-title {
    font-size: 1.8rem;
  }

  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .meta-info {
    flex-wrap: wrap;
  }
}
</style> 