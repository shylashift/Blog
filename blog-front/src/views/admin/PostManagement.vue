<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAllPosts, 
  deletePost, 
  hidePost, 
  showPost,
  type AdminPost
} from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const posts = ref<AdminPost[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jumpPage = ref(1)

// 获取所有文章
const fetchPosts = async () => {
  try {
    loading.value = true
    console.log('正在获取文章列表，页码：', currentPage.value, '每页数量：', pageSize.value, '关键词：', searchKeyword.value)
    const response = await getAllPosts(currentPage.value, pageSize.value, searchKeyword.value)
    console.log('获取到的文章列表：', response)
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

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchPosts()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchPosts()
}

onMounted(async () => {
  fetchPosts()
})
</script>

<template>
  <div class="post-management">
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
        <el-table-column prop="postId" label="ID" width="60" />
        <el-table-column label="文章信息" min-width="200">
          <template #default="{ row }">
            <div class="post-info">
              <div class="post-details">
                <div class="post-title">{{ row.title }}</div>
                <div class="post-summary" v-if="row.summary">{{ row.summary }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="120">
          <template #default="{ row }">
            <el-tag 
              v-if="row.tags"
              type="info"
              effect="plain"
              style="white-space: nowrap"
            >
              {{ row.tags }}
            </el-tag>
            <span v-else class="no-tags">无标签</span>
          </template>
        </el-table-column>
        <el-table-column label="发表时间" width="150">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="评论" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">
              {{ row.commentCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === '正常' ? 'success' : row.status === '已隐藏' ? 'warning' : 'danger'"
              class="status-tag"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
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
                v-if="!row.isDeleted"
                size="small"
                :type="row.isHidden ? 'success' : 'warning'"
                @click="confirmToggleVisibility(row.postId, row.title, row.isHidden)"
              >
                {{ row.isHidden ? '显示' : '隐藏' }}
              </el-button>
              <el-button
                v-if="!row.isDeleted"
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
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        <div class="jump-container">
          <span>前往</span>
          <el-input-number v-model="jumpPage" :min="1" :max="Math.max(1, Math.ceil(total / pageSize))" :controls="false" />
          <span>页</span>
          <el-button size="small" @click="handleCurrentChange(jumpPage)">确定</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.post-management {
  margin: 0;
  padding: 0;
}

.search-bar {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.search-bar:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-input {
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 16px;
}

.post-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.post-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
}

.post-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.post-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 15px;
}

.post-summary {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.el-tag {
  border-radius: 12px;
  padding: 4px 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-tag:hover {
  transform: translateY(-2px);
}

.table-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: nowrap;
}

.table-actions .el-button {
  padding: 6px 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 60px;
}

.table-actions .el-button:hover {
  transform: translateY(-2px);
}

.pagination-container {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.jump-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.jump-container .el-input-number {
  width: 60px;
}

.jump-container span {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: var(--el-fill-color-light);
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: var(--el-fill-color-lighter);
  transform: translateY(-2px);
}

:deep(.el-table__row td) {
  padding: 12px 0;
}

@media (max-width: 768px) {
  .search-input {
    max-width: 100%;
  }
  
  .table-actions {
    gap: 4px;
  }

  .table-actions .el-button {
    padding: 4px 8px;
    min-width: 50px;
  }
}

.status-tag {
  min-width: 65px;
  text-align: center;
}
</style> 