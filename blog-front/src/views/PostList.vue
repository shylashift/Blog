<script setup lang="ts">
import { ref, onMounted, watch, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Calendar, Collection } from '@element-plus/icons-vue'
import { getPosts } from '@/api/posts'
import type { Post, PostQuery } from '@/api/posts'
import { DEFAULT_AVATAR, BASE_URL } from '@/utils/constants'
import {  getBackupAvatar } from '@/utils/avatarUtils'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const posts = ref<Post[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const selectedTag = ref('')

// 处理头像URL
const getPostAvatarUrl = (post: Post): string => {
  console.log('开始处理帖子头像:', post.title);
  
  // 获取服务器基础URL
  // 正确解析服务器URL，避免重复的/api路径
  const serverBaseUrl = BASE_URL.includes('/api') 
    ? BASE_URL.substring(0, BASE_URL.indexOf('/api'))
    : BASE_URL;
  
  console.log('服务器基础URL:', serverBaseUrl);
  
  // 首先检查是否有authorAvatar字段
  if (post.authorAvatar) {
    console.log('使用authorAvatar:', post.authorAvatar);
    
    // 如果已经是完整URL就直接使用
    if (post.authorAvatar.startsWith('http')) {
      return post.authorAvatar;
    }
    
    // 拼接路径，确保不会有重复的/api
    // 如果是以/uploads开头，直接拼接到服务器基础URL
    if (post.authorAvatar.startsWith('/uploads/')) {
      const url = `${serverBaseUrl}${post.authorAvatar}`;
      console.log('拼接uploads路径:', url);
      return url;
    }
    
    // 确保路径以 / 开头
    const path = post.authorAvatar.startsWith('/') ? post.authorAvatar : '/' + post.authorAvatar;
    const url = `${serverBaseUrl}${path}`;
    console.log('拼接基础URL和authorAvatar:', url);
    return url;
  }
  
  // 其次检查author对象中的avatar字段
  if (post.author) {
    // 小写avatar（前端接口定义）
    if (post.author.avatar) {
      console.log('使用author.avatar:', post.author.avatar);
      
      // 如果已经是完整URL就直接使用
      if (post.author.avatar.startsWith('http')) {
        return post.author.avatar;
      }
      
      // 拼接路径，确保不会有重复的/api
      // 如果是以/uploads开头，直接拼接到服务器基础URL
      if (post.author.avatar.startsWith('/uploads/')) {
        const url = `${serverBaseUrl}${post.author.avatar}`;
        console.log('拼接uploads路径:', url);
        return url;
      }
      
      // 确保路径以 / 开头
      const path = post.author.avatar.startsWith('/') ? post.author.avatar : '/' + post.author.avatar;
      const url = `${serverBaseUrl}${path}`;
      console.log('拼接基础URL和author.avatar:', url);
      return url;
    }
    
    // 大写Avatar（数据库字段）
    // @ts-ignore - 忽略TypeScript错误，因为我们知道可能存在Avatar属性
    if (post.author.Avatar) {
      // @ts-ignore
      console.log('使用author.Avatar(大写):', post.author.Avatar);
      
      // @ts-ignore
      // 如果已经是完整URL就直接使用
      if (post.author.Avatar.startsWith('http')) {
        // @ts-ignore
        return post.author.Avatar;
      }
      
      // @ts-ignore
      // 拼接路径，确保不会有重复的/api
      // 如果是以/uploads开头，直接拼接到服务器基础URL
      if (post.author.Avatar.startsWith('/uploads/')) {
        // @ts-ignore
        const url = `${serverBaseUrl}${post.author.Avatar}`;
        console.log('拼接uploads路径(大写Avatar):', url);
        return url;
      }
      
      // 确保路径以 / 开头
      // @ts-ignore
      const path = post.author.Avatar.startsWith('/') ? post.author.Avatar : '/' + post.author.Avatar;
      const url = `${serverBaseUrl}${path}`;
      console.log('拼接基础URL和author.Avatar(大写):', url);
      return url;
    }
  }
  
  // 最后返回默认头像
  console.log('使用默认头像');
  return DEFAULT_AVATAR;
}

// 获取作者名称的辅助函数
const getAuthorName = (post: Post): string => {
  if (!post) return '未知作者'
  
  // 优先使用authorName字段（大小写不敏感）
  if (post.authorName && post.authorName.trim() !== '') {
    return post.authorName
  }
  
  // 支持数据库中字段名为Username的情况
  if (post.author) {
    // 首先检查小写的username（前端接口定义）
    if (post.author.username && post.author.username.trim() !== '') {
      return post.author.username
    }
    
    // 再检查大写的Username（数据库字段）
    // @ts-ignore - 忽略TypeScript错误，因为我们知道可能存在Username属性
    if (post.author.Username && post.author.Username.trim() !== '') {
      // @ts-ignore
      return post.author.Username
    }
  }
  
  // 最后返回默认值
  return '未知作者'
}

const fetchPosts = async () => {
  loading.value = true
  try {
    console.log('开始获取帖子列表，页码:', currentPage.value)
    const query: PostQuery = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      tag: selectedTag.value
    }
    
    // 从路由获取查询参数
    if (route.query.search) {
      searchQuery.value = route.query.search as string
      query.keyword = searchQuery.value
    }
    
    if (route.query.tag) {
      selectedTag.value = route.query.tag as string
      query.tag = selectedTag.value
    }
    
    // 请求数据
    console.log('发送请求参数:', query)
    const response = await getPosts(query)
    console.log('获取到响应:', response)
    
    // 数据安全处理 - 兼容两种可能的返回格式
    if (response) {
      // 处理新格式 {content, totalElements}
      if (!Array.isArray(response) && 'content' in response && Array.isArray(response.content)) {
        posts.value = response.content
        total.value = response.totalElements || 0
        console.log(`获取到${posts.value.length}篇帖子，总数:${total.value}`)
      } 
      // 处理旧格式 {items, total}
      else if (!Array.isArray(response) && 'items' in response && Array.isArray(response.items)) {
        posts.value = response.items
        total.value = response.total || 0
        console.log(`获取到${posts.value.length}篇帖子，总数:${total.value}`)
      }
      // 如果返回的就是数组
      else if (Array.isArray(response)) {
        posts.value = response
        total.value = response.length
        console.log(`获取到${posts.value.length}篇帖子`)
      }
      else {
        console.error('响应数据格式异常:', response)
        posts.value = []
        total.value = 0
      }
    } else {
      console.error('响应为空')
      posts.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    posts.value = []
    total.value = 0
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

// 监听路由变化
watch(
  () => route.fullPath,
  () => {
    console.log('PostList检测到路由变化，重新加载数据')
    fetchPosts()
  }
)

// 使用路由参数
onMounted(() => {
  console.log('PostList组件挂载，加载帖子数据')
  fetchPosts()
})

// 添加onActivated钩子确保从详情页返回时正确处理
onActivated(() => {
  console.log('PostList页面被激活')
})

// 添加组件名称以匹配keep-alive
defineOptions({
  name: 'PostList'
})

// 处理头像加载错误
const handleAvatarError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  target.src = getBackupAvatar();
}
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
        v-for="(post, index) in posts" 
        :key="post.postId || index" 
        :xs="24" 
        :sm="12" 
        :md="8"
        class="post-col"
      >
        <el-card 
          class="post-card" 
          shadow="hover" 
          @click="post.postId ? handleViewPost(post.postId) : null"
        >
          <template #header>
            <div class="post-header">
              <h3 class="post-title">{{ post.title || '无标题' }}</h3>
            </div>
          </template>
          <div class="post-content">
            <p class="post-summary">{{ post.summary || '暂无摘要' }}</p>
            <div class="post-info">
              <div class="post-meta">
                <span class="author">
                  <el-avatar 
                    :size="24" 
                    :src="getPostAvatarUrl(post)"
                    @error="handleAvatarError"
                  />
                  {{ getAuthorName(post) }}
                </span>
                <span class="time" v-if="post.createdAt">
                  <el-icon><Calendar /></el-icon>
                  {{ new Date(post.createdAt).toLocaleDateString() }}
                </span>
              </div>
              <div class="post-tags" v-if="post.tags && (Array.isArray(post.tags) ? post.tags.length > 0 : post.tags)">
                <el-icon><Collection /></el-icon>
                <el-tag
                  v-for="tag in (Array.isArray(post.tags) ? post.tags : post.tags.split(','))"
                  :key="tag"
                  size="small"
                  class="post-tag"
                  effect="light"
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
  gap: 4px;
}

.post-tag {
  margin: 0;
  cursor: pointer;
  border-radius: 12px;
  padding: 0 12px;
  height: 24px;
  line-height: 22px;
  background-color: var(--el-fill-color-light);
  border-color: var(--el-border-color-lighter);
  color: var(--el-text-color-regular);
  transition: all 0.2s;
}

.post-tag:hover {
  opacity: 1;
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-7);
  color: var(--el-color-primary);
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