<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star, Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 消息类型
type MessageType = 'comment' | 'favorite'

// 消息数据接口
interface Message {
  id: number
  type: MessageType
  title: string
  content: string
  author: string
  postId: number
  createdAt: string
  isRead: boolean
}

// 收藏数据接口
interface Favorite {
  id: number
  postId: number
  title: string
  author: string
  createdAt: string
}

const activeTab = ref<MessageType>('comment')
const messages = ref<Message[]>([])
const favorites = ref<Favorite[]>([])
const loading = ref(false)

// 获取消息列表
const fetchMessages = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取消息列表
    // const response = await api.getMessages()
    // messages.value = response.data
  } catch (error) {
    ElMessage.error('获取消息失败')
  } finally {
    loading.value = false
  }
}

// 获取收藏列表
const fetchFavorites = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取收藏列表
    // const response = await api.getFavorites()
    // favorites.value = response.data
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 标记消息为已读
const markAsRead = async (messageId: number) => {
  try {
    // TODO: 调用后端API标记消息为已读
    // await api.markMessageAsRead(messageId)
    const message = messages.value.find(m => m.id === messageId)
    if (message) {
      message.isRead = true
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 取消收藏
const removeFavorite = async (favoriteId: number) => {
  try {
    // TODO: 调用后端API取消收藏
    // await api.removeFavorite(favoriteId)
    favorites.value = favorites.value.filter(f => f.id !== favoriteId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 跳转到文章详情
const goToPost = (postId: number) => {
  router.push(`/posts/${postId}`)
}

onMounted(() => {
  if (!userStore.isAuthenticated) {
    router.push('/login')
    return
  }
  fetchMessages()
  fetchFavorites()
})
</script>

<template>
  <div class="messages-container">
    <div class="messages-header">
      <h1>学术交流中心</h1>
      <p class="subtitle">在这里查看您的学术讨论和收藏的文章</p>
    </div>

    <el-tabs v-model="activeTab" class="messages-tabs">
      <el-tab-pane label="学术讨论" name="comment">
        <div class="messages-list" v-loading="loading">
          <template v-if="messages.length > 0">
            <div v-for="message in messages" :key="message.id" 
                 class="message-item" :class="{ 'unread': !message.isRead }"
                 @click="goToPost(message.postId)">
              <div class="message-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="message-content">
                <h3>{{ message.title }}</h3>
                <p class="message-text">{{ message.content }}</p>
                <div class="message-meta">
                  <span class="author">{{ message.author }}</span>
                  <span class="time">{{ message.createdAt }}</span>
                </div>
              </div>
              <div class="message-actions">
                <el-button 
                  v-if="!message.isRead"
                  type="primary" 
                  link
                  @click.stop="markAsRead(message.id)">
                  标记已读
                </el-button>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无学术讨论消息" />
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
                  @click.stop="removeFavorite(favorite.id)">
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
}

.message-actions,
.favorite-actions {
  display: flex;
  align-items: center;
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