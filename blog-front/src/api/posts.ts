import request from './axios'

export interface Comment {
  commentId: number
  content: string
  postId: number
  user: {
    userId: number
    username: string
    avatar?: string
  }
  createdAt: string
}

export interface Post {
  postId: number
  title: string
  content: string
  summary?: string
  tags: string[]
  author: {
    userId: number
    username: string
    avatar?: string
  }
  authorName?: string
  authorAvatar?: string
  coverImage?: string
  createdAt: string
  updatedAt: string
  likes: number
  commentCount: number
  comments?: Comment[]
  isFavorited?: boolean
}

export interface PostQuery {
  page: number
  pageSize: number
  keyword?: string
  tag?: string  // 添加标签查询参数
}

export interface CreatePostData {
  title: string
  content: string
  tags: string[]
  summary: string
}

export interface PostListResponse {
  items: Post[]
  total: number
}

// 获取帖子列表
export const getPosts = (params: PostQuery) => {
  return request.get<any, PostListResponse>('/posts', { params })
}

// 获取当前用户的文章
export const getUserPosts = () => {
  return request.get<any, Post[]>('/users/posts')
}

// 获取帖子详情
export const getPostById = (postId: number) => {
  return request.get<any, Post>(`/posts/${postId}`)
}

// 添加评论
export const addComment = (postId: number, content: string) => {
  return request.post<any, Comment>(`/posts/${postId}/comments`, { content })
}

// 点赞文章
export const likePost = (postId: number) => {
  return request.post<any, void>(`/posts/${postId}/like`)
}

// 创建帖子
export const createPost = (data: CreatePostData) => {
  // 确保 tags 是字符串
  const postData = {
    ...data,
    tags: data.tags.join(',')  // 将数组转换为逗号分隔的字符串
  }
  return request.post<any, Post>('/posts', postData)
}

// 更新帖子
export const updatePost = (id: number, data: CreatePostData) => {
  // 确保 tags 是字符串
  const postData = {
    ...data,
    tags: data.tags.join(',')  // 将数组转换为逗号分隔的字符串
  }
  return request.put<any, Post>(`/posts/${id}`, postData)
}

// 删除帖子
export const deletePost = (id: number) => {
  return request.delete(`/posts/${id}`)
}

// 检查文章是否已收藏
export const checkPostFavorite = (postId: number) => {
  return request.get<any, boolean>(`/posts/${postId}/favorite`)
}

// 收藏文章
export const favoritePost = (postId: number) => {
  return request.post<any, void>(`/posts/${postId}/favorite`)
}

// 取消收藏
export const unfavoritePost = (postId: number) => {
  return request.delete<any, void>(`/posts/${postId}/favorite`)
}