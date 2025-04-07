<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  getAllPosts, 
  deletePost, 
  hidePost, 
  showPost,
  type AdminPost, 
  type PaginatedResponse
} from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const posts = ref<AdminPost[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const isAdmin = computed(() => userStore.userInfo?.roles?.includes('ROLE_ADMIN'))

// 检查是否为管理员
onMounted(async () => {
  await userStore.initialize()
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  
  if (!isAdmin.value) {
    ElMessage.error('只有管理员可以访问此页面')
    router.push('/profile')
    return
  }
  
  fetchPosts()
})

// 获取所有文章
const fetchPosts = async () => {
  try {
    loading.value = true
    const response = await getAllPosts(currentPage.value, pageSize.value, searchKeyword.value)
    posts.value = response.items || []
    total.value = response.total || 0
  } catch (error) {
    console.error('获取文章列表失败:', error)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索文章
const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchPosts()
}

// 删除文章
const handleDeletePost = async (postId: number) => {
  try {
    await deletePost(postId)
    ElMessage.success('删除文章成功')
    fetchPosts()
  } catch (error) {
    console.error('删除文章失败:', error)
    ElMessage.error('删除文章失败')
  }
}

// 确认删除
const confirmDelete = (postId: number, title: string) => {
  ElMessageBox.confirm(
    `确定要删除文章 "${title}" 吗？此操作不可恢复。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    handleDeletePost(postId)
  }).catch(() => {})
}

// 隐藏/显示文章
const togglePostVisibility = async (postId: number, isHidden: boolean) => {
  try {
    if (isHidden) {
      await showPost(postId)
      ElMessage.success('文章已公开显示')
    } else {
      await hidePost(postId)
      ElMessage.success('文章已隐藏')
    }
    fetchPosts()
  } catch (error) {
    console.error(`${isHidden ? '显示' : '隐藏'}文章失败:`, error)
    ElMessage.error(`${isHidden ? '显示' : '隐藏'}文章失败`)
  }
}

// 确认隐藏/显示
const confirmToggleVisibility = (postId: number, title: string, isHidden: boolean) => {
  ElMessageBox.confirm(
    `确定要${isHidden ? '公开显示' : '隐藏'}文章 "${title}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    togglePostVisibility(postId, isHidden)
  }).catch(() => {})
}
</script>

<template>
  <div class="post-management">
    <div class="page-header">
      <h1>文章管理</h1>
      <div class="header-actions">
        <el-button @click="router.push('/profile')">返回个人中心</el-button>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <el-card class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文章标题或内容"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </el-card>

    <!-- 文章列表 -->
    <el-card class="post-list-card">
      <el-table
        v-loading="loading"
        :data="posts"
        style="width: 100%"
        border
      >
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column label="文章标题" min-width="200">
          <template #default="{ row }">
            <div class="post-title" :class="{ 'is-hidden': row.isHidden }">
              {{ row.title }}
            </div>
            <div class="post-summary">{{ row.summary || '无摘要' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="150">
          <template #default="{ row }">
            <div class="author-info">
              <el-avatar 
                :size="30" 
                :src="row.authorAvatar" 
                v-if="row.authorAvatar"
              />
              <span>{{ row.authorName || '未知作者' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="150">
          <template #default="{ row }">
            <div class="tags-container">
              <el-tag 
                v-for="tag in row.tags" 
                :key="tag" 
                size="small"
                class="tag"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!row.tags || row.tags.length === 0">无标签</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发表时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="评论数" width="100" align="center">
          <template #default="{ row }">
            {{ row.commentCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isHidden ? 'info' : 'success'">
              {{ row.isHidden ? '已隐藏' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                size="small"
                type="primary"
                @click="router.push(`/posts/${row.postId}`)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                :type="row.isHidden ? 'success' : 'warning'"
                @click="confirmToggleVisibility(row.postId, row.title, row.isHidden)"
              >
                {{ row.isHidden ? '显示' : '隐藏' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="confirmDelete(row.postId, row.title)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.post-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: var(--text-color);
}

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  max-width: 400px;
}

.post-list-card {
  margin-bottom: 20px;
}

.post-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.post-title.is-hidden {
  text-decoration: line-through;
  opacity: 0.7;
}

.post-summary {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag {
  margin-right: 0;
}

.table-actions {
  display: flex;
  gap: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .post-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-input {
    max-width: 100%;
  }
  
  .table-actions {
    flex-direction: column;
  }
}
</style> 