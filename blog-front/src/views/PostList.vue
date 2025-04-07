<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Collection, User } from '@element-plus/icons-vue'
import { getPosts } from '@/api/posts'
import type { Post, PostQuery } from '@/api/posts'

const router = useRouter()
const loading = ref(false)
const posts = ref<Post[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const selectedTag = ref('')

const fetchPosts = async () => {
  try {
    loading.value = true
    const query: PostQuery = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (searchQuery.value) {
      query.keyword = searchQuery.value
    }
    
    if (selectedTag.value) {
      query.tag = selectedTag.value
    }

    const response = await getPosts(query)
    posts.value = response.items
    total.value = response.total
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchPosts()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
}

const handleTagSelect = (tag: string) => {
  selectedTag.value = tag
  currentPage.value = 1
  fetchPosts()
}

const handleViewPost = (id: number) => {
  router.push(`/posts/${id}`)
}

onMounted(() => {
  fetchPosts()
})
</script>

<template>
  <div class="post-list">
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文章..."
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-row :gutter="20">
      <el-col 
        v-for="post in posts" 
        :key="post.postId" 
        :xs="24" 
        :sm="12" 
        :md="8"
        class="post-col"
      >
        <el-card 
          class="post-card" 
          shadow="hover" 
          @click="handleViewPost(post.postId)"
        >
          <template #header>
            <div class="post-header">
              <h3 class="post-title">{{ post.title }}</h3>
            </div>
          </template>
          <div class="post-content">
            <p class="post-summary">{{ post.summary || '暂无摘要' }}</p>
            <div class="post-info">
              <div class="post-meta">
                <span class="author">
                  <el-avatar 
                    :size="24" 
                    :src="post.authorAvatar || post.author?.avatar || ''" 
                    v-if="post.authorAvatar || post.author?.avatar"
                  >
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  {{ post.authorName || post.author?.username || '未知作者' }}
                </span>
                <span class="time">
                  <el-icon><Calendar /></el-icon>
                  {{ new Date(post.createdAt).toLocaleDateString() }}
                </span>
              </div>
              <div class="post-tags" v-if="post.tags?.length">
                <el-icon><Collection /></el-icon>
                <el-tag
                  v-for="tag in post.tags"
                  :key="tag"
                  size="small"
                  class="post-tag"
                  @click.stop="handleTagSelect(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-if="!loading && posts.length === 0" class="empty-container">
      <el-empty description="暂无文章" />
    </div>

    <div class="pagination-container" v-if="total > 0">
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
  </div>
</template>

<style scoped>
.post-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.search-bar {
  margin-bottom: 2rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.post-col {
  margin-bottom: 1.5rem;
}

.post-card {
  height: 100%;
  cursor: pointer;
  transition: transform 0.2s;
}

.post-card:hover {
  transform: translateY(-4px);
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-title {
  margin: 0;
  font-size: 1.2rem;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-summary {
  color: var(--el-text-color-regular);
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex-wrap: wrap;
}

.post-meta .author {
  font-weight: 500;
}

.post-meta .author .el-avatar {
  margin-right: 5px;
}

.post-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.post-tag {
  margin: 0.25rem;
  cursor: pointer;
}

.post-tag:hover {
  opacity: 0.8;
}

.loading-container,
.empty-container {
  padding: 2rem 0;
  text-align: center;
}

.pagination-container {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .post-list {
    padding: 1rem;
  }

  .search-bar {
    margin-bottom: 1.5rem;
  }

  .post-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style> 