import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  exp: number
  [key: string]: any
}

const TOKEN_KEY = 'blog_token'

/**
 * 从 localStorage 获取 token
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 token 到 localStorage
 */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 从 localStorage 移除 token
 */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查 token 是否过期
 * @param token JWT token
 * @returns boolean 是否过期
 */
export function isTokenExpired(token: string): boolean {
  try {
    const decoded = jwtDecode<JwtPayload>(token)
    const currentTime = Date.now() / 1000

    // 提前 5 分钟认为 token 过期，以避免边界情况
    return decoded.exp < currentTime + 300
  } catch (error) {
    console.error('Token decode error:', error)
    return true
  }
} 