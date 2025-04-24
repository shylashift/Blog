import request from '@/api/axios'

export interface Notification {
  messageId: number
  userId: number
  postId: number
  commentId?: number
  type: string
  content: string
  isRead: boolean
  createdAt: string
  senderName?: string
  senderEmail?: string
}

// 获取通知列表
export const getNotifications = () => {
  return request.get<Notification[]>('/messages')
}

// 标记所有通知为已读
export const markAllAsRead = () => {
  return request.put('/messages/read-all')
}

// 标记通知为已读
export const markAsRead = (messageId: number) => {
  return request.put(`/messages/${messageId}/read`)
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return request.get<number>('/messages/unread/count')
}

// 删除指定消息通知
export const deleteMessage = (messageId: number) => {
  return request.delete(`/messages/${messageId}`)
}