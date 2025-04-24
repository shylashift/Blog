<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star } from '@element-plus/icons-vue'
import { getUserFavorites, removeFavorite, type Favorite } from '@/api/favorites'
import { getUserComments, deleteComment } from '@/api/comments'
import type { MessageType } from '@/api/messages'
import type { Comment } from '@/api/posts'
import { getAvatarUrl, getBackupAvatar } from '@/utils/avatarUtils'

const router = useRouter()
const userStore = useUserStore()

// 初始化状态
const activeTab = ref<MessageType>('comment')
const comments = ref<Comment[]>([])
const favorites = ref<Favorite[]>([])
const loading = ref(false)

// 获取评论列表
const fetchComments = async () => {
  loading.value = true
  try {
    const response = await getUserComments()
    comments.value = response
    console.log('获取到的评论列表:', comments.value)
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('获取评论失败')
  } finally {
    loading.value = false
  }
}

// 获取收藏列表
const fetchFavorites = async () => {
  loading.value = true
  try {
    const response = await getUserFavorites()
    console.log('收藏文章原始数据:', JSON.stringify(response));
    favorites.value = response
    console.log('获取到的收藏列表:', favorites.value)
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
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

// 删除评论
const handleDeleteComment = async (commentId: number) => {
  try {
    await deleteComment(commentId)
    comments.value = comments.value.filter(c => c.commentId !== commentId)
    ElMessage.success('评论已删除')
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('操作失败')
  }
}

// 取消收藏
const handleRemoveFavorite = async (favoriteId: number, postId: number) => {
  try {
    await removeFavorite(postId)
    favorites.value = favorites.value.filter(f => f.id !== favoriteId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('操作失败')
  }
}

// 跳转到文章详情
const goToPost = (postId: number) => {
  router.push(`/posts/${postId}`)
}

// 处理头像加载错误
const handleAvatarError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  target.src = getBackupAvatar();
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  fetchComments()
  fetchFavorites()
})

// 添加onActivated钩子确保每次从详情页返回时也重新加载数据
onActivated(() => {
  console.log('Messages页面被激活，重新加载数据')
  if (userStore.isLoggedIn) {
    if (activeTab.value === 'comment') {
      fetchComments()
    } else if (activeTab.value === 'favorite') {
      fetchFavorites()
    }
  }
})

// 添加组件名称以匹配keep-alive
defineOptions({
  name: 'Messages'
})
</script>

<template>
  <div class="messages-container">
    <div class="messages-header">
      <h1>评论中心</h1>
      <p class="subtitle">在这里查看您的评论和收藏的文章</p>
    </div>

    <el-tabs v-model="activeTab" class="messages-tabs">
      <el-tab-pane label="我的评论" name="comment">
        <div class="messages-list" v-loading="loading">
          <template v-if="comments.length > 0">
            <div v-for="comment in comments" :key="comment.commentId" 
                 class="message-item"
                 @click="goToPost(comment.postId)">
              <div class="message-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="message-content">
                <h3>文章评论</h3>
                <p class="message-text">{{ comment.content }}</p>
                <div class="message-meta">
                  <div class="author-info">
                    <el-avatar 
                      :size="24" 
                      :src="getAvatarUrl(comment.user?.avatar)"
                      @error="handleAvatarError"
                    />
                    <span class="author">{{ getCommentUserName(comment) }}</span>
                  </div>
                  <span class="time">{{ new Date(comment.createdAt).toLocaleString() }}</span>
                </div>
              </div>
              <div class="message-actions">
                <el-button 
                  type="danger" 
                  link
                  @click.stop="handleDeleteComment(comment.commentId)">
                  删除评论
                </el-button>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无评论" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="收藏文章" name="favorite">
        <div class="favorites-list" v-loading="loading">
          <template v-if="favorites.length > 0">
            <div v-for="favorite in favorites" :key="favorite.id" 
                 class="favorite-item"
                 @click="goToPost(favorite.postId)">
              <div class="favorite-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="favorite-content">
                <h3>{{ favorite.title }}</h3>
                <div class="favorite-meta">
                  <span class="author">{{ favorite.author }}</span>
                  <span class="time">{{ favorite.createdAt }}</span>
                </div>
              </div>
              <div class="favorite-actions">
                <el-button 
                  type="danger" 
                  link
                  @click.stop="handleRemoveFavorite(favorite.id, favorite.postId)">
                  取消收藏
                </el-button>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无收藏文章" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.messages-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.messages-header {
  text-align: center;
  margin-bottom: 40px;
}

.messages-header h1 {
  font-size: 2rem;
  color: var(--text-color);
  margin-bottom: 10px;
}

.subtitle {
  color: var(--text-color-secondary);
  font-size: 1.1rem;
}

.messages-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.messages-list,
.favorites-list {
  min-height: 200px;
}

.message-item,
.favorite-item {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color 0.3s;
}

.message-item:last-child,
.favorite-item:last-child {
  border-bottom: none;
}

.message-item:hover,
.favorite-item:hover {
  background-color: var(--hover-color);
}

.message-item.unread {
  background-color: var(--hover-color);
}

.message-icon,
.favorite-icon {
  margin-right: 20px;
  color: var(--primary-color);
  font-size: 24px;
}

.message-content,
.favorite-content {
  flex: 1;
}

.message-content h3,
.favorite-content h3 {
  margin: 0 0 10px 0;
  font-size: 1.1rem;
  color: var(--text-color);
}

.message-text {
  color: var(--text-color-secondary);
  margin: 0 0 10px 0;
  line-height: 1.5;
}

.message-meta,
.favorite-meta {
  display: flex;
  gap: 20px;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
  align-items: center;
}

.favorite-meta .author {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.favorite-meta .time {
  color: var(--el-text-color-secondary);
  font-size: 0.85rem;
}

.message-actions,
.favorite-actions {
  display: flex;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-meta .author,
.favorite-meta .author {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .messages-container {
    padding: 15px;
  }

  .message-item,
  .favorite-item {
    flex-direction: column;
    padding: 15px;
  }

  .message-icon,
  .favorite-icon {
    margin-bottom: 10px;
  }

  .message-actions,
  .favorite-actions {
    margin-top: 10px;
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 