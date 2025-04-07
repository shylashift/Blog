<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  getAllUsers, 
  promoteToAdmin, 
  demoteFromAdmin, 
  disableUser, 
  enableUser,
  checkAdminRole,
  type AdminUser, 
  type PaginatedResponse
} from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const users = ref<AdminUser[]>([])
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
  
  // 检查用户角色并打印日志
  console.log('当前用户信息:', {
    isLoggedIn: userStore.isLoggedIn,
    roles: userStore.userInfo?.roles,
    isAdmin: isAdmin.value
  })
  
  // 尝试检查和修复管理员权限
  try {
    const adminCheck = await checkAdminRole()
    console.log('管理员权限检查结果:', adminCheck)
    
    // 如果修复了权限，更新本地用户信息
    if (adminCheck && adminCheck.isAdmin && !isAdmin.value) {
      userStore.setUserInfo({
        ...userStore.userInfo!,
        roles: adminCheck.roles as string[]
      })
      console.log('已更新用户角色:', adminCheck.roles)
    }
  } catch (error) {
    console.error('管理员权限检查失败:', error)
  }
  
  if (!isAdmin.value) {
    ElMessage.error('只有管理员可以访问此页面')
    router.push('/profile')
    return
  }
  
  // 尝试获取用户列表，如果失败则可能是权限问题
  try {
    fetchUsers()
  } catch (error) {
    console.error('获取用户列表失败，可能是权限问题:', error)
    ElMessage.error('无法访问管理员功能，请确认您有管理员权限')
    router.push('/profile')
  }
})

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
</script>

<template>
  <div class="user-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <div class="header-actions">
        <el-button @click="router.push('/profile')">返回个人中心</el-button>
      </div>
    </div>

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
                :src="row.avatar" 
                v-if="row.avatar"
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
              :type="role === 'ROLE_ADMIN' ? 'danger' : 'info'"
              class="role-tag"
            >
              {{ role === 'ROLE_ADMIN' ? '管理员' : '用户' }}
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
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="table-actions">
              <el-tooltip :content="row.isAdmin ? '移除管理员权限' : '设为管理员'" placement="top">
                <el-button
                  :type="row.isAdmin ? 'danger' : 'success'"
                  :icon="row.isAdmin ? 'RemoveCircle' : 'Star'"
                  circle
                  @click="confirmAction(row.userId, row.username, row.isAdmin)"
                />
              </el-tooltip>
              <el-tooltip :content="row.disabled ? '启用用户' : '禁用用户'" placement="top">
                <el-button
                  :type="row.disabled ? 'success' : 'warning'"
                  :icon="row.disabled ? 'Check' : 'Close'"
                  circle
                  @click="confirmToggleStatus(row.userId, row.username, row.disabled)"
                />
              </el-tooltip>
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
.user-management {
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

.user-list-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
}

.email {
  font-size: 0.9rem;
  color: var(--text-color-secondary);
}

.role-tag {
  margin-right: 5px;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .user-management {
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
    flex-wrap: wrap;
  }
}
</style> 