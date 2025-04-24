<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getNotifications, markAsRead, deleteMessage, markAllAsRead } from '@/api/notifications'
import type { Notification } from '@/api/notifications'
import { formatTime } from '@/utils/time'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const notifications = ref<Notification[]>([])

// 过滤选项
type FilterType = 'all' | 'unread' | 'read'
const currentFilter = ref<FilterType>('all')

// 根据过滤条件显示消息
const filteredNotifications = computed(() => {
  switch (currentFilter.value) {
    case 'unread':
      return notifications.value.filter(n => !n.isRead)
    case 'read':
      return notifications.value.filter(n => n.isRead)
    default:
      return notifications.value
  }
})

// 获取未读消息数量
const unreadCount = computed(() => 
  notifications.value.filter(n => !n.isRead).length
)

// 获取通知列表
const fetchNotifications = async () => {
  try {
    loading.value = true
    const response = await getNotifications()
    // 直接使用response，因为axios拦截器已经处理了.data
    notifications.value = Array.isArray(response) ? response : []
    // 更新用户store中的未读消息数量
    if (notifications.value && notifications.value.length > 0) {
      userStore.unreadNotificationCount = notifications.value.filter(n => !n.isRead).length
    } else {
      userStore.unreadNotificationCount = 0
    }
  } catch (error) {
    console.error('获取通知失败:', error)
    ElMessage.error('获取通知失败')
    notifications.value = [] // 确保在出错时设置为空数组
  } finally {
    loading.value = false
  }
}

// 标记通知为已读
const handleMarkAsRead = async (notification: Notification) => {
  try {
    await markAsRead(notification.messageId)
    notification.isRead = true
    userStore.decrementUnreadCount()
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 批量标记为已读
const handleMarkAllAsRead = async () => {
  try {
    const unreadNotifications = notifications.value.filter(n => !n.isRead)
    if (unreadNotifications.length === 0) {
      ElMessage.info('没有未读消息')
      return
    }

    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    userStore.unreadNotificationCount = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('批量标记已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 跳转到相关文章或显示完整消息
const handleNotificationClick = (notification: Notification) => {
  if (notification.type === 'admin_message') {
    return // 管理员消息不需要跳转
  }
  
  if (notification.postId) {
    router.push(`/posts/${notification.postId}`)
  }
}

// 删除通知
const handleDelete = async (notification: Notification) => {
  try {
    await deleteMessage(notification.messageId)
    notifications.value = notifications.value.filter(n => n.messageId !== notification.messageId)
    if (!notification.isRead) {
      userStore.decrementUnreadCount()
    }
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除通知失败:', error)
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await fetchNotifications()
})
</script>

<template>
  <div class="notifications-container">
    <div class="notifications-header">
      <div class="header-left">
        <h2>我的通知</h2>
        <el-radio-group v-model="currentFilter" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">
            未读
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="filter-badge" />
          </el-radio-button>
          <el-radio-button label="read">已读</el-radio-button>
        </el-radio-group>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          @click="handleMarkAllAsRead" 
          :loading="loading"
          :disabled="unreadCount === 0"
        >
          全部标记为已读
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="notifications-content">
      <template v-if="filteredNotifications.length">
        <el-card v-for="notification in filteredNotifications" 
                 :key="notification.messageId" 
                 class="notification-card"
                 :class="{ 'admin-message': notification.type === 'admin_message' }">
          <div class="notification-content" @click="handleNotificationClick(notification)">
            <div v-if="notification.type === 'admin_message'" class="admin-info">
              <span class="sender-name">{{ notification.senderName || '系统管理员' }}</span>
              <span class="sender-email" v-if="notification.senderEmail">({{ notification.senderEmail }})</span>
            </div>
            <div class="message">{{ notification.content }}</div>
            <div class="notification-footer">
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
              <div class="notification-actions" @click.stop>
                <el-button 
                  v-if="!notification.isRead" 
                  type="primary" 
                  link 
                  @click="handleMarkAsRead(notification)"
                >
                  标记已读
                </el-button>
                <el-popconfirm
                  title="确定要删除这条通知吗？"
                  @confirm="handleDelete(notification)"
                >
                  <template #reference>
                    <el-button type="danger" link>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </el-card>
      </template>
      <el-empty 
        v-else 
        :description="
          currentFilter === 'unread' 
            ? '没有未读消息' 
            : currentFilter === 'read' 
              ? '没有已读消息' 
              : '暂无通知'
        " 
      />
    </div>
  </div>
</template>

<style scoped>
.notifications-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-badge :deep(.el-badge__content) {
  transform: translateY(-8px);
}

.notification-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-card.admin-message {
  border-left: 4px solid #409EFF;
  background-color: #f5f7fa;
}

.notification-content {
  padding: 12px;
}

.admin-info {
  margin-bottom: 8px;
}

.sender-name {
  font-weight: bold;
  color: #409EFF;
}

.sender-email {
  color: #909399;
  margin-left: 8px;
  font-size: 0.9em;
}

.message {
  color: #303133;
  line-height: 1.5;
  margin: 8px 0;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.notification-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.notification-time {
  font-size: 0.9em;
  color: #909399;
}

.no-notifications {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
</style> 