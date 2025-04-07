import request from './axios'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: {
    id: number
    username: string
    role: string
  }
}

export const login = (data: LoginData) => {
  return request.post<any, LoginResponse>('/auth/login', data)
} 