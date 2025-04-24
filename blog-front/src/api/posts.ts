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
  tags: string | string[]  // 可以是字符串或字符串数组
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
  isHidden?: boolean
  isDeleted?: boolean
}

export interface PostQuery {
  page: number
  size: number
  keyword?: string
  tag?: string
}

export interface CreatePostData {
  title: string
  content: string
  tags: string[]
  summary: string
}

export interface PostListResponse {
  // 新格式字段
  content?: Post[]  
  totalElements?: number  
  totalPages?: number
  number?: number
  size?: number
  
  // 旧格式字段
  items?: Post[]
  total?: number
}

// 获取帖子列表
export const getPosts = (params: PostQuery) => {
  console.log('请求帖子列表参数:', params)
  return request.get<any, PostListResponse | Post[]>('/posts', { 
    params: {
      ...params,
      page: params.page - 1  // 后端分页从0开始
    }
  })
    .then(response => {
      console.log('帖子列表原始响应:', response);
      
      // 处理文章列表中的头像URL
      if (response) {
        if (!Array.isArray(response) && 'content' in response && Array.isArray(response.content)) {
          response.content.forEach(post => {
            console.log(`帖子ID ${post.postId} 的头像信息:`, {
              authorAvatar: post.authorAvatar,
              authorAvatarType: post.authorAvatar ? typeof post.authorAvatar : 'null/undefined',
              authorObject: post.author ? {
                avatar: post.author.avatar,
                avatarType: post.author.avatar ? typeof post.author.avatar : 'null/undefined',
                username: post.author.username
              } : 'null/undefined'
            });
          });
        } else if (!Array.isArray(response) && 'items' in response && Array.isArray(response.items)) {
          response.items.forEach(post => {
            console.log(`帖子ID ${post.postId} 的头像信息:`, {
              authorAvatar: post.authorAvatar,
              authorAvatarType: post.authorAvatar ? typeof post.authorAvatar : 'null/undefined',
              authorObject: post.author ? {
                avatar: post.author.avatar, 
                avatarType: post.author.avatar ? typeof post.author.avatar : 'null/undefined',
                username: post.author.username
              } : 'null/undefined'
            });
          });
        }
      }
      
      return response;
    });
}

// 获取用户的文章列表
export const getUserPosts = (userId: number) => {
  return request.get<any, any[]>(`/posts/user/${userId}`)  // 修改为正确的API路径
}

// 获取帖子详情
export const getPostById = (postId: number) => {
  return request.get<any, Post>(`/posts/${postId}`)
}

// 添加评论
export const addComment = (postId: number, content: string) => {
  const trimmedContent = content.trim()
  console.log('开始提交评论', { postId, content: trimmedContent })
  
  // 构造请求数据
  const requestData = {
    content: trimmedContent
  }
  
  return request.post<any, Comment>(`/posts/${postId}/comments`, requestData)
    .then(response => {
      console.log('评论提交成功，返回数据:', response)
      return response
    })
    .catch(error => {
      console.error('评论提交失败:', error)
      if (error.response?.status === 401) {
        throw new Error('请先登录后再评论')
      } else if (error.response?.status === 400) {
        throw new Error('该用户被禁言，暂时无法发表评论')
      } else if (error.response?.data?.message) {
        // 使用后端返回的具体错误信息
        throw new Error(error.response.data.message)
      } else {
        throw new Error('评论失败，请稍后重试')
      }
    })
}

// 创建帖子
export const createPost = (data: CreatePostData) => {
  // 确保 tags 是字符串
  const postData = {
    ...data,
    tags: data.tags.join(',')  // 将数组转换为逗号分隔的字符串
  }
  return request.post<any, Post>('/posts', postData)
    .then(response => {
      console.log('创建帖子成功，返回数据:', response)
      return response
    })
    .catch(error => {
      console.error('创建帖子失败:', error)
      if (error.response?.status === 401) {
        throw new Error('请先登录后再发帖')
      } else if (error.response?.data?.message) {
        // 使用后端返回的具体错误信息
        throw new Error(error.response.data.message)
      } else {
        throw new Error('创建帖子失败，请稍后重试')
      }
    })
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