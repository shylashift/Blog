<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserPosts } from '@/api/posts'
import { getAvatarUrl } from '@/utils/avatarUtils'
import { useRouter } from 'vue-router'
import { Message, Calendar, Document, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { sendMessageToAdmin } from '@/api/messages'
import { getUserFullInfo, type UserInfo } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

interface User extends Partial<UserInfo> {
  userId: number
  username: string
}

interface Post {
  postId: number
  title: string
  content: string
  createdAt: string
}

const props = defineProps<{
  visible: boolean
  user: User | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const dialogVisible = ref(props.visible)
const recentPosts = ref<Post[]>([])
const loading = ref(false)
const messageContent = ref('')
const userFullInfo = ref<UserInfo | null>(null)

watch(() => props.visible, async (newVal) => {
  dialogVisible.value = newVal
  if (newVal && props.user) {
    loading.value = true
    try {
      // 获取用户完整信息
      const fullInfo = await getUserFullInfo(props.user.userId)
      userFullInfo.value = fullInfo
      // 获取用户文章
      await fetchUserPosts()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    } finally {
      loading.value = false
    }
  }
})

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 获取用户最近的帖子
const fetchUserPosts = async () => {
  if (!props.user?.userId) return
  
  try {
    loading.value = true
    const posts = await getUserPosts(props.user.userId)
    recentPosts.value = posts.slice(0, 5) // 只显示最近5篇
  } catch (error) {
    console.error('获取用户帖子失败:', error)
    ElMessage.error('获取用户帖子失败')
  } finally {
    loading.value = false
  }
}

// 跳转到帖子详情
const goToPost = (postId: number) => {
  dialogVisible.value = false
  router.push(`/posts/${postId}`)
}

// 判断是否显示消息输入框
const showMessageInput = computed(() => {
  // 如果没有用户信息，不显示
  if (!userFullInfo.value) {
    console.log('[UserInfoPopup] 没有用户信息，不显示消息框')
    return false
  }
  
  console.log('[UserInfoPopup] 被点击的用户信息:', {
    userId: userFullInfo.value.userId,
    roles: userFullInfo.value.roles
  })
  
  console.log('[UserInfoPopup] 当前用户信息:', {
    userId: userStore.userInfo?.userId,
    roles: userStore.userInfo?.roles
  })
  
  // 检查被点击的用户是否是管理员
  const isTargetAdmin = userFullInfo.value.roles?.includes('ROLE_ADMIN') || userFullInfo.value.roles?.includes('管理员')
  console.log('[UserInfoPopup] 目标用户是否是管理员:', isTargetAdmin)
  
  // 检查当前用户是否是管理员
  const isCurrentUserAdmin = userStore.userInfo?.roles?.includes('ROLE_ADMIN') || userStore.userInfo?.roles?.includes('管理员')
  console.log('[UserInfoPopup] 当前用户是否是管理员:', isCurrentUserAdmin)
  
  // 不能给自己发消息
  if (userStore.userInfo?.userId === userFullInfo.value.userId) {
    console.log('[UserInfoPopup] 不能给自己发消息')
    return false
  }
  
  // 如果目标用户是管理员，则显示消息框
  const shouldShow = isTargetAdmin
  console.log('[UserInfoPopup] 是否显示消息框:', shouldShow)
  
  return shouldShow
})

// 发送消息给管理员
const sendMessage = async () => {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  try {
    await sendMessageToAdmin({
      recipientId: props.user!.userId,
      content: messageContent.value,
      senderInfo: {
        username: userStore.userInfo?.username || '',
        email: userStore.userInfo?.email || ''
      }
    })
    
    ElMessage.success('消息发送成功')
    messageContent.value = '' // 清空输入框
    dialogVisible.value = false // 关闭弹窗
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

onMounted(async () => {
  if (props.visible && props.user) {
    loading.value = true
    try {
      // 获取用户完整信息
      const fullInfo = await getUserFullInfo(props.user.userId)
      userFullInfo.value = fullInfo
      // 获取用户文章
      await fetchUserPosts()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="userFullInfo?.username || user?.username || '用户信息'"
    width="500px"
    destroy-on-close
    class="user-info-dialog"
  >
    <div v-loading="loading">
      <div v-if="userFullInfo || user" class="user-info-content">
        <!-- 基本信息部分 -->
        <div class="user-basic-info">
          <div class="user-avatar">
            <el-avatar
              :size="100"
              :src="(userFullInfo?.avatar || user?.avatar) ? getAvatarUrl(userFullInfo?.avatar || user?.avatar) : undefined"
            />
          </div>
          <div class="user-details">
            <h3 class="username">{{ userFullInfo?.username || user?.username }}</h3>
            <p class="email" v-if="userFullInfo?.email || user?.email">
              <el-icon><Message /></el-icon>
              {{ userFullInfo?.email || user?.email }}
            </p>
            <p class="bio">{{ userFullInfo?.bio || user?.bio || '这个用户很懒，还没有写简介' }}</p>
            <p class="join-date">
              <el-icon><Calendar /></el-icon>
              加入时间：{{ formatDate(userFullInfo?.createdAt || user?.createdAt) }}
            </p>
          </div>
        </div>

        <!-- 最近的帖子部分 -->
        <div class="recent-posts">
          <h4 class="section-title">
            <el-icon><Document /></el-icon>
            最近发布的帖子
          </h4>
          <div v-if="recentPosts.length > 0" class="posts-list">
            <div
              v-for="post in recentPosts"
              :key="post.postId"
              class="post-item"
              @click="goToPost(post.postId)"
            >
              <span class="post-title">{{ post.title }}</span>
              <span class="post-date">{{ formatDate(post.createdAt) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无发布的帖子" />
        </div>

        <!-- 消息输入框部分 -->
        <div v-if="showMessageInput" class="message-section">
          <h4 class="section-title">
            <el-icon><ChatDotRound /></el-icon>
            发送消息给管理员
          </h4>
          <div class="message-input">
            <el-input
              v-model="messageContent"
              type="textarea"
              :rows="3"
              placeholder="请输入您要发送给管理员的消息..."
              maxlength="500"
              show-word-limit
            />
            <el-button 
              type="primary" 
              @click="sendMessage"
              :disabled="!messageContent.trim()"
              style="margin-top: 10px; width: 100%;"
            >
              发送消息
            </el-button>
          </div>
        </div>
      </div>
      <div v-else class="no-user-info">
        未找到用户信息
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.user-info-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.user-basic-info {
  display: flex;
  gap: 24px;
  padding: 20px;
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-details {
  flex: 1;
}

.username {
  margin: 0 0 12px;
  font-size: 1.5em;
  color: var(--el-text-color-primary);
}

.email {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.bio {
  margin: 12px 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.5;
}

.join-date {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.recent-posts {
  padding: 20px;
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: var(--el-bg-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-item:hover {
  transform: translateX(4px);
  background-color: var(--el-color-primary-light-9);
}

.post-title {
  flex: 1;
  margin-right: 16px;
  font-size: 14px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.no-user-info {
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 20px;
}

:deep(.el-dialog__body) {
  padding: 0 20px 20px;
}

.message-section {
  margin-top: 20px;
  padding: 20px;
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
}

.message-input {
  margin-top: 16px;
}
</style> 