import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  sub: string;
  exp: number;
  iat: number;
  userId: number;
  username: string;
  roles: string[];
}

/**
 * 解析JWT token
 * @param token JWT token字符串
 * @returns 解析后的payload，如果token无效则返回null
 */
export function parseToken(token: string): JwtPayload | null {
  try {
    return jwtDecode<JwtPayload>(token);
  } catch (error) {
    console.error('Token解析失败:', error);
    return null;
  }
}

/**
 * 检查token是否过期
 * @param token JWT token字符串
 * @returns 如果token过期或无效返回true，否则返回false
 */
export function isTokenExpired(token: string): boolean {
  const payload = parseToken(token);
  if (!payload) return true;
  
  const currentTime = Math.floor(Date.now() / 1000);
  return payload.exp < currentTime;
}

/**
 * 从token中获取用户ID
 * @param token JWT token字符串
 * @returns 用户ID，如果token无效则返回null
 */
export function getUserIdFromToken(token: string): number | null {
  const payload = parseToken(token);
  return payload?.userId ?? null;
}

/**
 * 从token中获取用户角色
 * @param token JWT token字符串
 * @returns 用户角色数组，如果token无效则返回空数组
 */
export function getUserRolesFromToken(token: string): string[] {
  const payload = parseToken(token);
  return payload?.roles ?? [];
}

/**
 * 检查token是否包含特定角色
 * @param token JWT token字符串
 * @param role 要检查的角色
 * @returns 如果token中包含指定角色返回true，否则返回false
 */
export function hasRole(token: string, role: string): boolean {
  const roles = getUserRolesFromToken(token);
  return roles.includes(role);
}
