import request from './axios'
import type { Comment } from './posts'

// 获取用户的所有评论
export const getUserComments = () => {
  return request.get<any, Comment[]>('/comments/user')
}

// 删除评论
export const deleteComment = (commentId: number) => {
  return request.delete<any, void>(`/comments/${commentId}`)
} 