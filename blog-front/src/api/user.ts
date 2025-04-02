import api from './axios'

export interface RegisterData {
  username: string
  password: string
  email: string
}

export interface LoginData {
  username: string
  password: string
}

export const register = async (data: RegisterData) => {
  const response = await api.post('/users/register', data)
  return response.data
}

export const login = async (data: LoginData) => {
  const response = await api.post('/auth/login', data)
  return response.data
}

export const getCurrentUser = async () => {
  const response = await api.get('/users/current')
  return response.data
}

export const logout = async () => {
  const response = await api.post('/users/logout')
  return response.data
} 