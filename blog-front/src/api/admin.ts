import request from './axios'

export interface AdminUser {
  userId: number
  username: string
  email: string
  avatar: string | null
  bio: string | null
  createdAt: string
  roles: string[]
  isAdmin: boolean
  disabled: boolean
}

export interface AdminPost {
  postId: number
  userId: number
  title: string
  summary: string
  content: string
  tags: string[]
  createdAt: string
  updatedAt: string
  authorName: string
  authorAvatar: string
  commentCount: number
  isHidden: boolean
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
}

// 获取所有用户列表
export const getAllUsers = async (page = 1, size = 10, keyword = '') => {
  const response = await request.get<any, PaginatedResponse<AdminUser>>(
    `/admin/users?page=${page}&size=${size}&keyword=${keyword}`
  )
  return response
}

// 提升为管理员
export const promoteToAdmin = async (userId: number) => {
  return await request.post(`/admin/users/${userId}/promote`)
}

// 降低为普通用户
export const demoteFromAdmin = async (userId: number) => {
  return await request.post(`/admin/users/${userId}/demote`)
}

// 禁用用户
export const disableUser = async (userId: number) => {
  return await request.post(`/admin/users/${userId}/disable`)
}

// 启用用户
export const enableUser = async (userId: number) => {
  return await request.post(`/admin/users/${userId}/enable`)
}

// 获取所有文章列表
export const getAllPosts = async (page = 1, size = 10, keyword = '') => {
  const response = await request.get<any, PaginatedResponse<AdminPost>>(
    `/admin/posts?page=${page}&size=${size}&keyword=${keyword}`
  )
  return response
}

// 删除文章
export const deletePost = async (postId: number) => {
  return await request.delete(`/admin/posts/${postId}`)
}

// 隐藏文章
export const hidePost = async (postId: number) => {
  return await request.post(`/admin/posts/${postId}/hide`)
}

// 显示文章
export const showPost = async (postId: number) => {
  return await request.post(`/admin/posts/${postId}/show`)
}

// 管理员角色检查响应类型
export interface AdminRoleCheckResponse {
  username: string
  email: string
  isAdmin: boolean
  roles: string[]
}

// 检查并修复管理员角色
export const checkAdminRole = async (): Promise<AdminRoleCheckResponse> => {
  return await request.get<any, AdminRoleCheckResponse>('/admin/checkAdminRole')
} 