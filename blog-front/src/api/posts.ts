import request from '../utils/request'

export interface Post {
  id: number
  title: string
  content: string
  author: {
    id: number
    username: string
  }
  createdAt: string
  updatedAt: string
}

export interface PostQuery {
  page: number
  pageSize: number
  keyword?: string
}

export interface PostListResponse {
  items: Post[]
  total: number
}

// 获取帖子列表
export const getPosts = (params: PostQuery) => {
  return request.get<any, PostListResponse>('/posts', { params })
}

// 获取帖子详情
export const getPostById = (id: number) => {
  return request.get<any, Post>(`/posts/${id}`)
}

// 创建帖子
export const createPost = (data: { title: string; content: string }) => {
  return request.post<any, Post>('/posts', data)
}

// 更新帖子
export const updatePost = (id: number, data: { title: string; content: string }) => {
  return request.put<any, Post>(`/posts/${id}`, data)
}

// 删除帖子
export const deletePost = (id: number) => {
  return request.delete(`/posts/${id}`)
} 