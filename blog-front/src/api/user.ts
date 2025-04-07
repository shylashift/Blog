import request from './axios'

export interface UserInfo {
  userId: number
  username: string
  email: string
  avatar: string | null
  bio: string | null
  roles: string[]
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
export const getCurrentUser = () => {
  return request.get<any, UserInfo>('/users/me')
}

// 注册
export const register = (data: { username: string; email: string; password: string }) => {
  return request.post<any, LoginResponse>('/auth/register', data)
}

export const logout = async () => {
  return request.post('/auth/logout')
} 