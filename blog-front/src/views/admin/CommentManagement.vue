<template>
  <div class="comment-management">
    <div class="search-toolbar">
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
    </div>

    <!-- 评论列表 -->
    <el-table
      v-loading="loading"
      :data="comments"
      style="width: 100%"
      border
    >
      <el-table-column prop="authorName" label="评论者" width="120">
        <template #default="{ row }">
          <div class="flex items-center">
            {{ row.authorName }}
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="content" label="评论内容" min-width="200">
        <template #default="{ row }">
          <div class="whitespace-pre-wrap">{{ row.content }}</div>
        </template>
      </el-table-column>
      
      <el-table-column prop="postTitle" label="所属文章" width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="viewPost(row.postId)">
            {{ row.postTitle }}
          </el-link>
        </template>
      </el-table-column>
      
      <el-table-column prop="createdAt" label="评论时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button
              v-if="!row.isDeleted"
              :type="row.isHidden ? 'success' : 'warning'"
              size="small"
              @click="handleVisibilityToggle(row)"
            >
              {{ row.isHidden ? '显示' : '隐藏' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :disabled="row.isDeleted"
            >
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="mt-4 flex justify-end">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAllComments, deleteComment, hideComment, showComment } from '@/api/admin'
import type { AdminComment } from '@/api/admin'
import { formatDate } from '@/utils/format'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const comments = ref<AdminComment[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 默认头像URL
const defaultAvatarUrl = ref('/default-avatar.png')

// 处理头像加载错误
const handleAvatarError = (row: AdminComment) => {
  row.authorAvatar = defaultAvatarUrl.value
}

// 获取评论列表
const fetchComments = async () => {
  loading.value = true
  try {
    const response = await getAllComments(
      currentPage.value,
      pageSize.value,
      searchKeyword.value
    )
    comments.value = response.items
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchComments()
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchComments()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchComments()
}

// 处理删除评论
const handleDelete = async (row: AdminComment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？此操作不可恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await deleteComment(row.commentId)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 处理显示/隐藏评论
const handleVisibilityToggle = async (row: AdminComment) => {
  try {
    if (row.isHidden) {
      await showComment(row.commentId)
      ElMessage.success('评论已显示')
    } else {
      await hideComment(row.commentId)
      ElMessage.success('评论已隐藏')
    }
    fetchComments()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看文章
const viewPost = (postId: number) => {
  router.push(`/post/${postId}`)
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case '正常':
      return 'success'
    case '已隐藏':
      return 'warning'
    case '已删除':
      return 'danger'
    default:
      return 'info'
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-management {
  padding: 20px;
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
</style> 