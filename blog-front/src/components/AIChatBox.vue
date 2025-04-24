<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <div class="header-left">
        <el-icon class="ai-icon"><ChatRound /></el-icon>
        <h3>智能对话</h3>
      </div>
      <div class="header-right">
        <el-button type="primary" plain size="small" @click="clearHistory" :disabled="messages.length === 0">
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <template v-if="messages.length === 0">
        <div class="welcome-message">
          <div class="welcome-avatar">
            <el-icon class="welcome-icon"><ChatRound /></el-icon>
          </div>
          <h4>你好！我是AI助手</h4>
          <p>我可以帮你解答问题，提供建议，或者陪你聊天。</p>
          <div class="suggested-questions">
            <p>👇 你可以问我：</p>
            <div class="questions-grid">
              <el-button 
                v-for="question in suggestedQuestions" 
                :key="question"
                type="info"
                plain
                size="small"
                class="question-btn"
                @click="handleSuggestedQuestion(question)"
              >
                {{ question }}
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <div 
        v-for="(message, index) in messages" 
        :key="message.id" 
        class="message"
        :class="[message.role, { 'consecutive': isConsecutive(message, index) }]"
      >
        <div v-if="!isConsecutive(message, index)" class="message-avatar">
          <template v-if="message.role === 'assistant'">
            <el-icon class="assistant-avatar"><ChatRound /></el-icon>
          </template>
          <template v-else>
            <div class="user-avatar">你</div>
          </template>
        </div>
        <div class="message-wrapper" :class="{ 'with-avatar': !isConsecutive(message, index) }">
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
          </div>
          <div class="message-time">{{ formatTime(message.createdAt) }}</div>
        </div>
      </div>

      <div v-if="loading" class="message assistant">
        <div class="message-avatar">
          <el-icon class="assistant-avatar"><ChatRound /></el-icon>
        </div>
        <div class="message-wrapper with-avatar">
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入你的问题..."
        :disabled="loading"
        @keydown.enter.exact.prevent="handleSend"
        class="message-textarea"
      />
      <div class="input-actions">
        <el-tooltip content="长风破浪" placement="top">
          <span class="markdown-hint">
            <el-icon><Edit /></el-icon>
            加油
          </span>
        </el-tooltip>
        <el-button 
          type="primary" 
          :disabled="!inputMessage.trim() || loading"
          @click="handleSend"
          class="send-button"
        >
          <el-icon><Position /></el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatRound, Delete, Edit, Position } from '@element-plus/icons-vue'
import { sendMessage, getChatHistory, clearChatHistory } from '@/api/ai'
import type { ChatMessage} from '@/api/ai'
import { formatTime } from '@/utils/time'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

// 设置 marked 为同步模式
marked.setOptions({ async: false })

const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref<HTMLElement>()
const currentChatId = ref<number | null>(null)

// 建议的问题列表
const suggestedQuestions = [
  '如何写一篇好的博客文章？',
  '怎样提高文章的阅读量？',
  '有哪些写作技巧可以分享？',
  '如何优化文章SEO?',
  '写作时如何克服拖延症？',
  '如何撰写有吸引力的标题？'
]

// 判断消息是否是连续的（同一角色连续发送）
const isConsecutive = (message: ChatMessage, index: number): boolean => {
  if (index === 0) return false
  return messages.value[index - 1].role === message.role
}

// 格式化消息内容（支持Markdown）
const formatMessage = (content: string) => {
  const htmlContent = marked(content) as string
  return DOMPurify.sanitize(htmlContent)
}

// 滚动到最新消息
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 发送聊天消息
const sendChatMessage = async (message: ChatMessage) => {
  try {
    messages.value.push(message)
    loading.value = true
    await scrollToBottom()

    const response = await sendMessage(message)
    console.log('AI响应:', response)

    if (!response) {
      throw new Error('未收到AI响应')
    }

    // 创建助手消息，确保ID格式正确
    messages.value.push({
      id: response.messageId || Date.now().toString(),
      chatId: response.chatId || (currentChatId.value as number | undefined),
      role: 'assistant',
      content: response.content || '抱歉，我现在无法回答。',
      createdAt: response.createdAt || new Date().toISOString()
    })
    
    // 更新当前聊天ID
    if (response.chatId) {
      currentChatId.value = response.chatId
    }

    await scrollToBottom()
  } catch (error: any) {
    console.error('发送消息失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '发送消息失败')
  } finally {
    loading.value = false
  }
}

const handleSend = async (_event: Event) => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  const message: ChatMessage = {
    id: Date.now().toString(),
    chatId: currentChatId.value as number | undefined,
    role: 'user',
    content: inputMessage.value,
    createdAt: new Date().toISOString()
  }

  inputMessage.value = ''
  await sendChatMessage(message)
}

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有对话记录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await clearChatHistory()
    messages.value = []
    currentChatId.value = null
    ElMessage.success('对话记录已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空历史记录失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 加载历史记录
const loadHistory = async () => {
  loading.value = true
  try {
    console.log('开始加载历史记录...')
    const history = await getChatHistory()
    console.log('历史记录获取成功:', history)
    
    if (!Array.isArray(history)) {
      console.error('历史记录不是数组格式:', history)
      messages.value = []
      loading.value = false
      return
    }
    
    messages.value = history
    console.log('消息列表已更新:', messages.value)
    
    if (history.length > 0) {
      const lastMessage = history[history.length - 1]
      console.log('最后一条消息:', lastMessage)
      currentChatId.value = lastMessage && typeof lastMessage.chatId === 'number' ? lastMessage.chatId : null
      console.log('设置当前会话ID:', currentChatId.value)
    }
    
    await scrollToBottom()
  } catch (error) {
    console.error('获取历史记录失败:', error)
    messages.value = []
  }
  loading.value = false
}

const handleSuggestedQuestion = (question: string) => {
  const message: ChatMessage = {
    id: Date.now().toString(),
    chatId: currentChatId.value as number | undefined,
    role: 'user',
    content: question,
    createdAt: new Date().toISOString()
  }
  sendChatMessage(message)
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 620px;
  border-radius: 12px;
  background-color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: white;
  border-bottom: 1px solid var(--el-border-color-light);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right {
  display: flex;
  align-items: center;
}

.ai-icon {
  font-size: 24px;
  color: var(--el-color-primary);
}

.chat-header h3 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-weight: 600;
  font-size: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: var(--el-bg-color-page);
  scroll-behavior: smooth;
  
  /* 隐藏默认滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

/* 对Webkit浏览器隐藏滚动条 */
.chat-messages::-webkit-scrollbar {
  display: none;
}

.welcome-message {
  text-align: center;
  padding: 30px 20px;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  margin: 10px;
}

.welcome-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #f0f9ff;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 12px rgba(62, 158, 255, 0.15);
}

.welcome-icon {
  font-size: 38px;
  color: var(--el-color-primary);
}

.welcome-message h4 {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.welcome-message p {
  color: var(--el-text-color-secondary);
  margin-bottom: 20px;
  font-size: 15px;
  line-height: 1.6;
}

.suggested-questions {
  margin-top: 20px;
}

.suggested-questions p {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.question-btn {
  text-align: left;
  white-space: normal;
  height: auto;
  padding: 8px 12px;
  line-height: 1.5;
  font-size: 13px;
  transition: all 0.2s ease;
}

.question-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  align-items: flex-start;
  width: 100%;
}

.message.consecutive {
  margin-top: -5px;
  margin-bottom: 8px;
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.assistant-avatar {
  font-size: 24px;
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 40px);
}

.message.user .message-wrapper {
  align-items: flex-end;
  max-width: 80%;
}

.message-wrapper.with-avatar {
  margin-top: 0;
}

.message-content {
  padding: 0 12px;
  border-radius: 12px;
  position: relative;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-content {
  background-color: #3e9eff;
  color: white;
  border-top-right-radius: 4px;
  max-width: fit-content;
}

.message.user.consecutive .message-content {
  border-top-right-radius: 12px;
}

.message.assistant {
  display: flex;
  align-items: flex-start;
}

.message.assistant .message-wrapper {
  max-width: 80%;
  align-items: flex-start;
}

.message.assistant .message-content {
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  border-top-left-radius: 4px;
  max-width: fit-content;
}

.message.assistant.consecutive .message-content {
  border-top-left-radius: 12px;
}

.message-text {
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

.message.user .message-text :deep(code),
.message.user .message-text :deep(pre) {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
  padding: 0 4px;
  opacity: 0.8;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.chat-input {
  padding: 16px;
  border-top: 1px solid var(--el-border-color-light);
  background-color: white;
}

.message-textarea {
  margin-bottom: 8px;
}

.message-textarea :deep(.el-textarea__inner) {
  resize: none;
  border-radius: 8px;
  padding: 10px 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
  font-size: 14px;
  line-height: 1.5;
}

.message-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.markdown-hint {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  cursor: help;
}

.send-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-left: 16px;
  padding-right: 16px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--el-color-primary);
  animation: typing 1s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-4px); opacity: 1; }
}

/* Markdown样式 */
:deep(pre) {
  background-color: var(--el-bg-color-page);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

:deep(code) {
  font-family: monospace;
  background-color: var(--el-bg-color-page);
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 0.9em;
}

:deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

:deep(a:hover) {
  text-decoration: underline;
}

:deep(p) {
  margin: 8px 0;
}

:deep(ul), :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

:deep(blockquote) {
  border-left: 4px solid var(--el-border-color);
  padding-left: 12px;
  color: var(--el-text-color-secondary);
  margin: 12px 0;
}

:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
  margin: 16px 0 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .questions-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-message {
    padding: 30px 16px;
  }
  
  .welcome-avatar {
    width: 60px;
    height: 60px;
  }
  
  .welcome-icon {
    font-size: 32px;
  }
}
</style> 