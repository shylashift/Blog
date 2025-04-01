<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUserStore } from '@/stores/user'
import { Star, StarFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

interface Post {
  id: number
  title: string
  content: string
  summary: string
  tags: string[]
  author: string
  createdAt: string
  likes: number
  comments: Comment[]
}

interface Comment {
  id: number
  content: string
  author: string
  createdAt: string
}

const post = ref<Post | null>(null)
const commentContent = ref('')
const commentLoading = ref(false)

// 收藏状态
const isFavorited = ref(false)
const favoriteLoading = ref(false)

const fetchPost = async () => {
  try {
    loading.value = true
    const response = await fetch(`/api/posts/${route.params.id}`)
    if (!response.ok) throw new Error('获取文章失败')
    post.value = await response.json()
  } catch (error) {
    console.error('获取文章错误:', error)
    ElMessage.error('获取文章失败')
    router.push('/posts')
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const response = await fetch(`/api/posts/${post.value?.id}/like`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (!response.ok) throw new Error('点赞失败')
    if (post.value) {
      post.value.likes++
    }
    ElMessage.success('点赞成功')
  } catch (error) {
    console.error('点赞错误:', error)
    ElMessage.error('点赞失败')
  }
}

const submitComment = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    commentLoading.value = true
    const response = await fetch(`/api/posts/${post.value?.id}/comments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ content: commentContent.value })
    })

    if (!response.ok) throw new Error('发表评论失败')
    const newComment = await response.json()
    
    if (post.value) {
      post.value.comments.unshift(newComment)
    }
    commentContent.value = ''
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('评论错误:', error)
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 检查是否已收藏
const checkFavoriteStatus = async () => {
  if (!userStore.isAuthenticated) return
  try {
    // TODO: 调用后端API检查收藏状态
    // const response = await api.checkFavorite(postId.value)
    // isFavorited.value = response.data.isFavorited
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }

  favoriteLoading.value = true
  try {
    // TODO: 调用后端API切换收藏状态
    // await api.toggleFavorite(postId.value)
    isFavorited.value = !isFavorited.value
    ElMessage.success(isFavorited.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

onMounted(async () => {
  await fetchPost()
  await checkFavoriteStatus()
})
</script>

<template>
  <div class="post-detail">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-content">
          <el-skeleton-item variant="h1" style="width: 50%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 80%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 100%" />
        </div>
      </template>
      
      <template #default>
        <template v-if="post">
          <div class="post-header">
            <h1>{{ post.title }}</h1>
            <div class="post-meta">
              <span class="author">作者：{{ post.author }}</span>
              <span class="time">发布时间：{{ new Date(post.createdAt).toLocaleDateString() }}</span>
              <el-button
                :type="isFavorited ? 'warning' : 'default'"
                :icon="isFavorited ? StarFilled : Star"
                circle
                :loading="favoriteLoading"
                @click="toggleFavorite"
              />
              <span>
                <el-button type="primary" text @click="handleLike">
                  <el-icon><ThumbsUp /></el-icon>
                  {{ post.likes }} 赞
                </el-button>
              </span>
            </div>
            <div class="post-tags">
              <el-tag
                v-for="tag in post.tags"
                :key="tag"
                size="small"
                class="tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="post-content">
            <MdPreview :modelValue="post.content" />
          </div>

          <div class="post-comments">
            <h2>评论</h2>
            <div class="comment-form">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="写下你的评论..."
                :disabled="!userStore.isAuthenticated"
              />
              <el-button
                type="primary"
                :loading="commentLoading"
                @click="submitComment"
                :disabled="!userStore.isAuthenticated"
              >
                发表评论
              </el-button>
            </div>

            <div class="comments-list">
              <template v-if="post.comments && post.comments.length > 0">
                <div
                  v-for="comment in post.comments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.author }}</span>
                    <span class="comment-time">
                      {{ new Date(comment.createdAt).toLocaleString() }}
                    </span>
                  </div>
                  <div class="comment-content">
                    {{ comment.content }}
                  </div>
                </div>
              </template>
              <el-empty
                v-else
                description="暂无评论"
              />
            </div>
          </div>
        </template>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.post-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.skeleton-content {
  padding: 20px;
}

.post-header {
  margin-bottom: 30px;
}

.post-header h1 {
  margin: 0 0 16px;
  font-size: 2rem;
  color: var(--text-color);
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
  margin-bottom: 20px;
}

.post-tags {
  margin-top: 16px;
}

.tag {
  margin-right: 8px;
}

.post-content {
  margin: 30px 0;
  line-height: 1.8;
}

.post-comments {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.post-comments h2 {
  margin: 0 0 20px;
  font-size: 1.5rem;
  color: var(--text-color);
}

.comment-form {
  margin-bottom: 30px;
}

.comment-form .el-button {
  margin-top: 16px;
}

.comments-list {
  margin-top: 20px;
}

.comment-item {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: bold;
  color: var(--text-color);
}

.comment-time {
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}

.comment-content {
  color: var(--text-color);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .post-detail {
    padding: 15px;
  }

  .post-header h1 {
    font-size: 1.5rem;
  }

  .post-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 