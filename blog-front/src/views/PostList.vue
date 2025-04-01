<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

interface Post {
  id: number
  title: string
  summary: string
  tags: string[]
  author: string
  createdAt: string
  likes: number
  commentCount: number
}

const posts = ref<Post[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedTags = ref<string[]>([])
const allTags = ref<string[]>([])

const fetchTags = async () => {
  try {
    const response = await fetch('/api/posts/tags')
    if (!response.ok) throw new Error('获取标签失败')
    allTags.value = await response.json()
  } catch (error) {
    console.error('获取标签错误:', error)
    ElMessage.error('获取标签失败')
  }
}

const fetchPosts = async () => {
  try {
    loading.value = true
    const queryParams = new URLSearchParams({
      page: currentPage.value.toString(),
      pageSize: pageSize.value.toString(),
      tags: selectedTags.value.join(',')
    })

    const response = await fetch(`/api/posts?${queryParams}`)
    if (!response.ok) throw new Error('获取文章列表失败')
    const data = await response.json()
    posts.value = data.items
    total.value = data.total
  } catch (error) {
    console.error('获取文章列表错误:', error)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchPosts()
}

const handleTagChange = () => {
  currentPage.value = 1
  fetchPosts()
}

const createPost = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  router.push('/posts/create')
}

onMounted(() => {
  fetchTags()
  fetchPosts()
})
</script>

<template>
  <div class="post-list">
    <div class="list-header">
      <h1>文章列表</h1>
      <el-button type="primary" @click="createPost">
        <el-icon><Plus /></el-icon>
        发布文章
      </el-button>
    </div>

    <div class="filter-section">
      <el-select
        v-model="selectedTags"
        multiple
        clearable
        placeholder="选择标签筛选"
        @change="handleTagChange"
      >
        <el-option
          v-for="tag in allTags"
          :key="tag"
          :label="tag"
          :value="tag"
        />
      </el-select>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div v-for="i in 3" :key="i" class="skeleton-item">
          <el-skeleton-item variant="h3" style="width: 50%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 80%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 60%" />
        </div>
      </template>

      <template #default>
        <div class="posts-container">
          <el-card
            v-for="post in posts"
            :key="post.id"
            class="post-card"
            shadow="hover"
            @click="router.push(`/posts/${post.id}`)"
          >
            <div class="post-title">
              <h2>{{ post.title }}</h2>
            </div>
            
            <div class="post-summary">
              {{ post.summary }}
            </div>

            <div class="post-meta">
              <div class="post-info">
                <span>作者：{{ post.author }}</span>
                <span>发布时间：{{ new Date(post.createdAt).toLocaleDateString() }}</span>
              </div>
              
              <div class="post-stats">
                <span>
                  <el-icon><ThumbsUp /></el-icon>
                  {{ post.likes }}
                </span>
                <span>
                  <el-icon><ChatRound /></el-icon>
                  {{ post.commentCount }}
                </span>
              </div>
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
          </el-card>

          <el-empty
            v-if="posts.length === 0"
            description="暂无文章"
          />
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchPosts"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.post-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.list-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: var(--text-color);
}

.filter-section {
  margin-bottom: 24px;
}

.filter-section .el-select {
  width: 100%;
  max-width: 400px;
}

.skeleton-item {
  padding: 20px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 8px;
}

.posts-container {
  display: grid;
  gap: 20px;
  margin-bottom: 24px;
}

.post-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.post-card:hover {
  transform: translateY(-2px);
}

.post-title h2 {
  margin: 0 0 16px;
  font-size: 1.4rem;
  color: var(--text-color);
}

.post-summary {
  margin-bottom: 16px;
  color: var(--text-color-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 0.9rem;
  color: var(--text-color-secondary);
}

.post-info {
  display: flex;
  gap: 16px;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  margin-right: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .post-list {
    padding: 15px;
  }

  .list-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .list-header h1 {
    font-size: 1.5rem;
  }

  .post-meta {
    flex-direction: column;
    gap: 8px;
  }

  .post-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 