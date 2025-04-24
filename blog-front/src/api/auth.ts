import request from '../utils/axios'
import type { UserInfo } from '../stores/user'

export interface LoginData {
  email: string
  password: string
}

interface LoginResponse {
  token: string
  userId: number
  username: string
  email: string
  avatar?: string | null
  bio?: string
  roles?: string[]
}

// 解析 JWT token
function parseJwt(token: string) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('[Auth API] JWT解析失败:', error)
    return null
  }
}

// 从邮箱生成用户ID的函数
function generateUserId(email: string): number {
  let hash = 0
  for (let i = 0; i < email.length; i++) {
    const char = email.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // Convert to 32bit integer
  }
  return Math.abs(hash)
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  try {
    console.log('[Auth API] 开始登录请求:', { email })
    const response = await request.post('/auth/login', {
      email,
      password
    })
    
    console.log('[Auth API] 登录响应:', response)
    
    if (!response || !response.data) {
      console.error('[Auth API] 登录响应数据为空:', response)
      throw new Error('登录响应数据为空')
    }

    const responseData = response.data
    console.log('[Auth API] 解析响应数据:', responseData)
    
    // 尝试从不同的可能的字段中获取用户ID
    let userId = null
    // 首先尝试 UserId（后端字段名）
    if (typeof responseData.UserId === 'number') {
      userId = responseData.UserId
    } else if (typeof responseData.UserId === 'string') {
      userId = parseInt(responseData.UserId, 10)
    } 
    // 然后尝试其他可能的字段名
    else if (typeof responseData.userId === 'number') {
      userId = responseData.userId
    } else if (typeof responseData.userId === 'string') {
      userId = parseInt(responseData.userId, 10)
    } else if (typeof responseData.id === 'number') {
      userId = responseData.id
    } else if (typeof responseData.id === 'string') {
      userId = parseInt(responseData.id, 10)
    }

    if (!userId || isNaN(userId) || userId === 0) {
      console.error('[Auth API] 无法获取有效的用户ID:', responseData)
      throw new Error('登录响应中缺少有效的用户ID')
    }

    // 确保返回的数据符合接口定义
    const loginResponse: LoginResponse = {
      token: responseData.token || responseData.accessToken,
      userId: userId || responseData.userId || responseData.UserId || generateUserId(email),
      username: responseData.username || responseData.name || email.split('@')[0],
      email: responseData.email || email,
      avatar: responseData.avatar || null,
      bio: responseData.bio || responseData.description || '',
      roles: Array.isArray(responseData.roles) ? responseData.roles : 
             (responseData.role ? [responseData.role] : ['ROLE_USER'])
    }

    // 验证必要字段
    if (!loginResponse.token) {
      console.error('[Auth API] 缺少token:', responseData)
      throw new Error('登录响应中缺少访问令牌')
    }
    
    console.log('[Auth API] 登录成功，返回数据:', loginResponse)
    return loginResponse
  } catch (error: any) {
    console.error('[Auth API] 登录失败:', error)
    // 增加更友好的错误提示
    if (error.response?.status === 401) {
      throw new Error('用户名或密码错误')
    } else if (error.response?.status === 403) {
      throw new Error('账号已被禁用')
    } else if (error.message) {
      throw new Error(error.message)
    } else {
      throw new Error('登录失败，请稍后重试')
    }
  }
}

export async function register(username: string, email: string, password: string) {
  try {
    const response = await request.post('/auth/register', {
      username,
      email,
      password
    })
    return response.data
  } catch (error) {
    console.error('[Auth API] 注册失败:', error)
    throw error
  }
}

export async function logout() {
  try {
    const response = await request.post('/auth/logout')
    return response.data
  } catch (error) {
    console.error('[Auth API] 登出失败:', error)
    throw error
  }
}

export async function validateToken() {
  try {
    const response = await request.get('/auth/validate-token')
    return response.data
  } catch (error) {
    console.error('[Auth API] Token 验证失败:', error)
    throw error
  }
} 