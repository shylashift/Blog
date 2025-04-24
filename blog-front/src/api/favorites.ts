import request from './axios'

// 收藏接口
export interface Favorite {
  id: number
  postId: number
  title: string
  author: string
  createdAt: string
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