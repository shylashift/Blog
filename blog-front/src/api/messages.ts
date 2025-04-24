import request from './axios'

// 消息类型
export type MessageType = 'comment' | 'favorite' | 'admin_message'

// 消息接口
export interface Message {
  messageId: number
  userId: number
  postId: number | null
  commentId: number | null
  type: MessageType
  content: string
  isRead: boolean
  createdAt: string
  senderId: number | null
  senderName: string | null
  senderEmail: string | null
  postTitle?: string
}

// 获取用户的所有消息
export const getUserMessages = () => {
  return request.get<any, Message[]>('/messages')
}

// 获取未读消息数量
export const getUnreadCount = () => {
  return request.get<any, number>('/messages/unread/count')
}

// 标记消息为已读
export const markMessageAsRead = (messageId: number) => {
  return request.put(`/messages/${messageId}/read`)
}

// 标记所有消息为已读
export const markAllMessagesAsRead = () => {
  return request.put('/messages/read-all')
}

// 删除消息
export const deleteMessage = (messageId: number) => {
  return request.delete(`/messages/${messageId}`)
}

interface MessageSenderInfo {
  username: string
  email: string
}

interface SendMessageData {
  recipientId: number
  content: string
  senderInfo: MessageSenderInfo
}

// 发送消息给管理员
export const sendMessageToAdmin = (data: SendMessageData) => {
  return request.post<any, Message>('/messages/admin', data)
} 