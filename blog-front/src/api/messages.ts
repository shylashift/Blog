import request from './axios'

// 消息类型
export type MessageType = 'comment' | 'favorite'

// 消息接口
export interface Message {
  id: number
  type: MessageType
  title: string
  content: string
  author: string
  postId: number
  createdAt: string
  isRead: boolean
}

// 收藏接口
export interface Favorite {
  id: number
  postId: number
  title: string
  author: string
  createdAt: string
}

// 获取未读消息数量
export const getUnreadMessageCount = () => {
  return request.get<any, number>('/messages/count')
}

// 获取学术讨论消息
export const getDiscussionMessages = () => {
  return request.get<any, Message[]>('/messages/discussions')
}

// 标记消息为已读
export const markMessageAsRead = (messageId: number) => {
  return request.put<any, void>(`/messages/${messageId}/read`)
}

// 标记所有消息为已读
export const markAllMessagesAsRead = () => {
  return request.put<any, void>('/messages/read-all')
}

// 获取用户收藏列表
export const getUserFavorites = () => {
  return request.get<any, Favorite[]>('/users/favorites')
}

// 获取收藏列表
export const getFavorites = () => {
  return request.get<any, Favorite[]>('/favorites')
}

// 添加收藏
export const addFavorite = (postId: number) => {
  return request.post<any, Favorite>(`/favorites/add/${postId}`)
}

// 取消收藏
export const removeFavorite = (postId: number) => {
  return request.delete<any, void>(`/posts/${postId}/favorite`)
}

// 检查是否已收藏
export const checkFavorite = (postId: number) => {
  return request.get<any, boolean>(`/favorites/check/${postId}`)
} 