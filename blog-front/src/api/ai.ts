import request from '@/api/axios'

export interface ChatMessage {
  id: string
  chatId?: number
  userId?: number
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

export interface ChatResponse {
  chatId?: number
  messageId: string
  content: string
  createdAt: string
}

// 发送消息
export const sendMessage = async (message: ChatMessage): Promise<ChatResponse> => {
  // 只发送必要的内容，符合后端ChatRequest格式
  const requestData = {
    content: message.content,
    chatId: message.chatId
  }
  const response = await request.post<ChatResponse>('/ai/chat', requestData)
  return response.data
}

// 获取对话历史
export const getChatHistory = async (): Promise<ChatMessage[]> => {
  try {
    console.log('开始请求聊天历史')
    const response = await request.get('/ai/chat/history')
    console.log('聊天历史请求响应:', response)
    if (!response || !response.data) {
      console.error('响应格式不正确:', response)
      return []
    }
    return response.data
  } catch (error) {
    console.error('获取聊天历史失败:', error)
    return []
  }
}

// 清空对话历史
export const clearChatHistory = () => {
  return request.delete('/ai/chat/history')
} 