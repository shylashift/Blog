import request from './axios'

export interface UserInfo {
  userId: number
  username: string
  email: string
  avatar: string | null
  bio: string | null
  roles: string[] | string | any  // 支持多种角色格式
  createdAt: string
  updatedAt?: string
  disabled?: boolean
  muteEndTime?: string | null
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  email: string
  avatar: string | null
  bio: string | null
  roles: string[]
}

// 登录
export const login = (data: LoginRequest) => {
  return request.post<any, LoginResponse>('/auth/login', data)
}

// 获取当前用户信息
export const getCurrentUser = async () => {
  try {
    console.log('开始获取当前用户信息')
    const response = await request.get<any, UserInfo>('/users/me')
    console.log('获取到用户信息:', response)
    if (!response.roles) {
      console.warn('用户信息中没有角色数据')
    }
    return response
  } catch (error) {
    console.error('获取用户信息失败:', error)
    throw error
  }
}

// 注册
export const register = (data: { username: string; email: string; password: string }) => {
  return request.post<any, LoginResponse>('/auth/register', data)
}

// 更新当前用户信息
export const updateCurrentUser = async (data: { username?: string; email?: string; bio?: string; password?: string; oldPassword?: string }) => {
  try {
    console.log('开始更新当前用户信息:', data)
    const response = await request.put<any, UserInfo>('/users/me', data)
    console.log('用户信息更新成功:', response)
    return response
  } catch (error) {
    console.error('更新用户信息失败:', error)
    throw error
  }
}

// 上传用户头像
export const uploadUserAvatar = async (formData: FormData) => {
  try {
    console.log('开始上传头像')
    const response = await request.post<any, { code: number; data: string; message?: string }>(
      '/users/avatar',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )
    console.log('头像上传成功:', response)
    return response
  } catch (error) {
    console.error('头像上传失败:', error)
    throw error
  }
}

export const logout = async () => {
  return request.post('/auth/logout')
}

// 获取用户完整信息（包含角色）
export const getUserFullInfo = async (userId: number) => {
  try {
    console.log('开始获取用户完整信息:', userId)
    const response = await request.get<any, UserInfo>(`/users/${userId}/full`)
    console.log('获取到用户完整信息:', response)
    return response
  } catch (error) {
    console.error('获取用户完整信息失败:', error)
    throw error
  }
} 