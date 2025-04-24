<script setup lang="ts">
import { ref, onMounted} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAllUsers, 
  promoteToAdmin, 
  demoteFromAdmin, 
  disableUser, 
  enableUser,
  type AdminUser
} from '@/api/admin'
import { getAvatarUrl } from '@/utils/avatarUtils'
const loading = ref(false)
const users = ref<AdminUser[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jumpPage = ref(1)

// 获取所有用户
const fetchUsers = async () => {
  try {
    loading.value = true
    const response = await getAllUsers(currentPage.value, pageSize.value, searchKeyword.value)
    users.value = response.items || []
    total.value = response.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchUsers()
}

// 设置管理员
const setAdminRole = async (userId: number, isAdmin: boolean) => {
  try {
    if (isAdmin) {
      await demoteFromAdmin(userId)
      ElMessage.success('移除管理员权限成功')
    } else {
      await promoteToAdmin(userId)
      ElMessage.success('设置管理员权限成功')
    }
    fetchUsers()
  } catch (error) {
    console.error(`${isAdmin ? '移除' : '设置'}管理员权限失败:`, error)
    ElMessage.error(`${isAdmin ? '移除' : '设置'}管理员权限失败`)
  }
}

// 确认操作
const confirmAction = (userId: number, username: string, isAdmin: boolean) => {
  ElMessageBox.confirm(
    `确定要${isAdmin ? '移除' : '授予'} ${username} 的管理员权限吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    setAdminRole(userId, isAdmin)
  }).catch(() => {})
}

// 禁用或启用用户
const toggleUserStatus = async (userId: number, isDisabled: boolean) => {
  try {
    if (isDisabled) {
      await enableUser(userId)
      ElMessage.success('启用用户成功')
    } else {
      await disableUser(userId)
      ElMessage.success('禁用用户成功')
    }
    fetchUsers()
  } catch (error) {
    console.error(`${isDisabled ? '启用' : '禁用'}用户失败:`, error)
    ElMessage.error(`${isDisabled ? '启用' : '禁用'}用户失败`)
  }
}

// 确认禁用或启用
const confirmToggleStatus = (userId: number, username: string, isDisabled: boolean) => {
  ElMessageBox.confirm(
    `确定要${isDisabled ? '启用' : '禁用'}用户 ${username} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    toggleUserStatus(userId, isDisabled)
  }).catch(() => {})
}

onMounted(async () => {
  fetchUsers()
})
</script>

<template>
  <div class="user-management">
    <!-- 搜索工具栏 -->
    <el-card class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名或邮箱"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="user-list-card">
      <el-table
        v-loading="loading"
        :data="users"
        style="width: 100%"
        border
      >
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar 
                :size="40" 
                :src="getAvatarUrl(row.avatar)" 
              />
              <div class="user-details">
                <div class="username">{{ row.username }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bio" label="简介" show-overflow-tooltip />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag 
              v-for="role in row.roles" 
              :key="role" 
              :type="role === '管理员' ? 'danger' : 'info'"
              style="white-space: nowrap"
            >
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.disabled ? 'danger' : 'success'">
              {{ row.disabled ? '已禁用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <div class="table-actions">
              <!-- 管理员权限按钮 -->
              <el-button
                size="small"
                :type="row.roles.includes('管理员') ? 'warning' : 'success'"
                @click="confirmAction(row.userId, row.username, row.roles.includes('管理员'))"
              >
                {{ row.roles.includes('管理员') ? '降级为普通用户' : '升级为管理员' }}
              </el-button>
              <!-- 禁用/启用按钮 -->
              <el-button
                size="small"
                :type="row.disabled ? 'success' : 'danger'"
                @click="confirmToggleStatus(row.userId, row.username, row.disabled)"
              >
                {{ row.disabled ? '启用' : '禁用' }}
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
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
        <div class="jump-container">
          <span>前往</span>
          <el-input-number v-model="jumpPage" :min="1" :max="Math.max(1, Math.ceil(total / pageSize))" :controls="false" />
          <span>页</span>
          <el-button size="small" @click="handlePageChange(jumpPage)">确定</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.user-management {
  width: 100%;
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

.user-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.el-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.el-avatar:hover {
  transform: scale(1.1);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 15px;
}

.email {
  font-size: 13px;
  color: var(--el-text-color-secondary);
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
  gap: 12px;
  justify-content: flex-start;
}

.table-actions .el-button {
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
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

.jump-container .el-input {
  width: 60px;
}

.jump-container :deep(.el-input__wrapper) {
  padding: 0 8px;
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
  padding: 16px 0;
}

@media (max-width: 768px) {
  .search-input {
    max-width: 100%;
  }
  
  .table-actions {
    flex-wrap: wrap;
  }

  .table-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
}
</style> 