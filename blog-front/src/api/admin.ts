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
  content: string
  summary: string
  tags: string[]
  createdAt: string
  updatedAt: string
  authorName: string
  authorAvatar: string
  commentCount: number
  isHidden: boolean
  isDeleted: boolean
  status: string
}

export interface AdminComment {
  commentId: number
  content: string
  userId: number
  postId: number
  createdAt: string
  authorName: string
  authorAvatar: string
  postTitle: string
  isHidden: boolean
  isDeleted: boolean
  status: string
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
}

export interface DashboardStats {
  totalUsers: number
  totalPosts: number
  totalComments: number
  todayVisits: number
}

// 获取仪表盘统计数据
export const getDashboardStats = () => {
  return request<any, DashboardStats>({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
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
  console.log('调用获取文章列表API，参数：', { page, size, keyword })
  try {
    const response = await request.get<any, PaginatedResponse<AdminPost>>(
      `/admin/posts?page=${page}&size=${size}&keyword=${keyword}`
    )
    console.log('API返回数据：', response)
    
    // 确保每篇文章都有正确的状态
    if (response.items) {
      response.items = response.items.map(post => ({
        ...post,
        status: post.isDeleted ? '已删除' : 
                post.isHidden ? '已隐藏' : '正常'
      }))
    }
    
    return response
  } catch (error) {
    console.error('获取文章列表失败：', error)
    throw error
  }
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

// 获取所有评论列表
export const getAllComments = async (page = 1, size = 10, keyword = '') => {
  console.log('调用获取评论列表API，参数：', { page, size, keyword })
  try {
    const response = await request.get<any, PaginatedResponse<AdminComment>>(
      `/admin/comments?page=${page}&size=${size}&keyword=${keyword}`
    )
    console.log('API返回数据：', response)
    
    // 确保每条评论都有正确的状态
    if (response.items) {
      response.items = response.items.map(comment => ({
        ...comment,
        status: comment.isDeleted ? '已删除' : 
                comment.isHidden ? '已隐藏' : '正常'
      }))
    }
    
    return response
  } catch (error) {
    console.error('获取评论列表失败：', error)
    throw error
  }
}

// 删除评论
export const deleteComment = async (commentId: number) => {
  return await request.delete(`/admin/comments/${commentId}`)
}

// 隐藏评论
export const hideComment = async (commentId: number) => {
  return await request.post(`/admin/comments/${commentId}/hide`)
}

// 显示评论
export const showComment = async (commentId: number) => {
  return await request.post(`/admin/comments/${commentId}/show`)
}

interface UserListParams {
  page: number;
  pageSize: number;
  search?: string;
}

interface UserData {
  id: number;
  username: string;
  email: string;
  role: string;
}

// 获取用户列表
export const getUserList = (params: UserListParams) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  });
};

// 更新用户信息
export const updateUser = (data: UserData) => {
  return request({
    url: `/admin/users/${data.id}`,
    method: 'put',
    data
  });
};

// 删除用户
export const deleteUser = (id: number) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  });
}; 