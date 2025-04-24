<script setup lang="ts">
import { ref, onMounted, computed, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Upload } from '@element-plus/icons-vue'
import type { TabsPaneContext } from 'element-plus'
import { getCurrentUser, updateCurrentUser, uploadUserAvatar } from '@/api/user'
import { getUserPosts } from '@/api/posts'
import { getUserFavorites } from '@/api/favorites'
import { DEFAULT_AVATAR } from '@/utils/constants'
import ImageCropper from '@/components/ImageCropper.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('posts')
const fileInput = ref<HTMLInputElement | null>(null)
const imageUrl = ref<string>('')
const cropperVisible = ref(false)

// 添加基础URL
const BASE_URL = 'http://localhost:8080'  // 修改为后端服务器地址

interface UserProfile {
  id: number
  username: string
  email: string
  avatar: string
  bio: string
  createdAt: string
}

interface Post {
  postId: number
  title: string
  summary: string
  tags: string[]
  createdAt: string
}

const profile = ref<UserProfile>({
  id: 0,
  username: '',
  email: '',
  avatar: '',
  bio: '',
  createdAt: ''
})

const posts = ref<Post[]>([])
const favorites = ref<Post[]>([])
const isEditing = ref(false)
const editForm = ref({
  username: '',
  email: '',
  bio: '',
  oldPassword: '',
  password: '',
  confirmPassword: ''
})

// 获取用户详细信息
const fetchUserProfile = async () => {
  try {
    loading.value = true
    // 先检查token是否有效
    const tokenValid = await userStore.validateToken()
    if (!tokenValid) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return
    }
    
    const userInfo = await getCurrentUser()
    profile.value = {
      id: userInfo.userId,
      username: userInfo.username,
      email: userInfo.email,
      avatar: userInfo.avatar || '',
      bio: userInfo.bio || '',
      createdAt: new Date().toISOString()
    }
    editForm.value = {
      username: profile.value.username,
      email: profile.value.email,
      bio: profile.value.bio,
      oldPassword: '',
      password: '',
      confirmPassword: ''
    }
  } catch (error: any) {
    console.error('获取用户信息错误:', error)
    ElMessage.error('获取用户信息失败')
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.clearUserInfo()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 获取用户的文章
const fetchUserPosts = async () => {
  try {
    console.log('[Profile] 开始获取用户文章', new Date().toISOString())
    loading.value = true
    
    // 检查用户登录状态和用户信息
    if (!userStore.isLoggedIn || !userStore.userInfo) {
      console.log('[Profile] 获取文章失败：用户未登录或用户信息不存在')
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    // 检查用户ID
    const userId = userStore.userInfo.userId
    if (!userId) {
      console.error('[Profile] 用户ID不存在:', userStore.userInfo)
      ElMessage.error('获取用户信息失败')
      return
    }
    console.log('[Profile] 准备获取用户文章，用户ID:', userId)
    
    posts.value = [] // 先清空数据，避免显示旧数据
    
    const userPosts = await getUserPosts(userId)
    console.log('[Profile] 获取到用户文章数据:', userPosts)
    
    if (!userPosts || userPosts.length === 0) {
      console.log('[Profile] 用户没有文章')
      return
    }
    
    posts.value = userPosts.map(post => ({
      postId: post.postId,
      title: post.title,
      summary: post.summary || '',
      tags: Array.isArray(post.tags) ? post.tags : 
            (typeof post.tags === 'string' ? post.tags.split(/[,，\s]+/).filter(Boolean) : []),
      createdAt: post.createdAt
    }))
    console.log('[Profile] 用户文章数据处理完成, 文章数量:', posts.value.length)
  } catch (error: any) {
    console.error('[Profile] 获取文章列表错误:', error)
    ElMessage.error('获取文章列表失败')
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.clearUserInfo()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 获取用户的收藏
const fetchUserFavorites = async () => {
  try {
    loading.value = true
    // 检查token是否有效
    if (!userStore.isLoggedIn) {
      return
    }
    
    const userFavorites = await getUserFavorites()
    favorites.value = userFavorites.map(fav => ({
      postId: fav.postId,
      title: fav.title,
      summary: '',
      tags: [],
      createdAt: fav.createdAt,
    }))
  } catch (error: any) {
    console.error('获取收藏列表错误:', error)
    ElMessage.error('获取收藏列表失败')
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.clearUserInfo()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 更新用户信息
const updateProfile = async () => {
  try {
    // 验证密码
    if (editForm.value.password && editForm.value.password !== editForm.value.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    
    // 如果要修改密码，必须提供旧密码
    if (editForm.value.password && !editForm.value.oldPassword) {
      ElMessage.error('请输入旧密码')
      return
    }
    
    loading.value = true
    
    // 准备提交的数据
    const updateData: any = {
      username: editForm.value.username,
      email: editForm.value.email,
      bio: editForm.value.bio
    }
    
    // 如果修改密码，添加密码信息
    if (editForm.value.password) {
      updateData.oldPassword = editForm.value.oldPassword
      updateData.password = editForm.value.password
    }
    
    const updatedProfile = await updateCurrentUser(updateData)
    
    profile.value = {
      ...profile.value,
      username: updatedProfile.username,
      email: updatedProfile.email,
      bio: updatedProfile.bio || ''
    }
    
    isEditing.value = false
    ElMessage.success('更新个人信息成功')
    
    // 更新store中的用户信息
    userStore.updateUserInfo({
      userId: userStore.userInfo!.userId,
      username: updatedProfile.username,
      email: updatedProfile.email,
      bio: updatedProfile.bio || '',
      roles: userStore.userInfo!.roles,
      avatar: userStore.userInfo!.avatar
    })
    
    // 如果修改了密码，清空密码字段
    editForm.value.oldPassword = ''
    editForm.value.password = ''
    editForm.value.confirmPassword = ''
  } catch (error) {
    console.error('更新用户信息错误:', error)
    ElMessage.error('更新个人信息失败')
  } finally {
    loading.value = false
  }
}

// 处理标签页切换
const handleTabChange = (pane: TabsPaneContext) => {
  const tab = pane.props.name as string
  activeTab.value = tab
  if (tab === 'posts') {
    fetchUserPosts()
  } else if (tab === 'favorites') {
    fetchUserFavorites()
  }
}

// 注册时间
const registrationDate = computed(() => {
  return new Date(profile.value.createdAt).toLocaleDateString()
})

// 处理头像URL的计算属性
const avatarUrl = computed(() => {
  if (!profile.value.avatar) {
    return DEFAULT_AVATAR
  }
  // 如果已经是完整URL就直接使用
  if (profile.value.avatar.startsWith('http')) {
    return profile.value.avatar
  }
  // 确保路径以 / 开头
  const path = profile.value.avatar.startsWith('/') ? profile.value.avatar : '/' + profile.value.avatar
  return `${BASE_URL}${path}?t=${Date.now()}`
})

// 在组件挂载或激活时调用的初始化函数
const initPageData = async () => {
  console.log('[Profile] 初始化页面数据', new Date().toISOString())
  console.log('[Profile] 当前用户状态:', {
    isLoggedIn: userStore.isLoggedIn,
    userInfo: userStore.userInfo,
    initialized: userStore.initialized
  })

  if (!userStore.isLoggedIn || !userStore.userInfo) {
    console.log('[Profile] 用户未登录或用户信息不存在，跳转到登录页')
    router.push('/login')
    return
  }

  // 重置状态
  posts.value = []
  favorites.value = []
  loading.value = true
  
  // 加载数据
  await fetchUserProfile()
  
  // 根据激活的标签加载对应数据
  if (activeTab.value === 'posts') {
    await fetchUserPosts()
  } else if (activeTab.value === 'favorites') {
    await fetchUserFavorites()
  }
}

onMounted(() => {
  console.log('[Profile] 组件挂载', new Date().toISOString())
  initPageData()
})

// 添加onActivated钩子确保每次从详情页返回时也重新加载数据
onActivated(() => {
  console.log('[Profile] 页面被激活', new Date().toISOString())
  initPageData()
})

// 使用defineOptions定义组件名称，这样keep-alive能正确识别
defineOptions({
  name: 'Profile',
  components: {
    ImageCropper
  }
})

// 修改文件选择处理函数
const handleFileChange = (e: Event) => {
  console.log('文件选择事件触发')
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) {
    console.log('没有选择文件')
    return
  }

  console.log('选择的文件:', {
    name: file.name,
    type: file.type,
    size: file.size
  })

  if (!file.type.includes('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    if (reader.result) {
      console.log('图片读取成功，准备设置 imageUrl')
      imageUrl.value = reader.result as string
      cropperVisible.value = true
      console.log('imageUrl 已设置，对话框已打开')
    }
  }
  reader.onerror = (error) => {
    console.error('图片读取失败:', error)
    ElMessage.error('图片读取失败，请重试')
  }
  reader.readAsDataURL(file)
}

const cropperRef = ref<InstanceType<typeof ImageCropper>>()
const uploading = ref(false)

// 处理头像上传
const handleAvatarUpload = async () => {
  if (!cropperRef.value) {
    console.error('cropperRef is null')
    return
  }
  
  const canvas = cropperRef.value.getCroppedCanvas()
  if (!canvas) {
    console.error('No crop data available')
    ElMessage.error('请先选择并裁剪图片')
    return
  }

  uploading.value = true
  try {
    // 将Canvas转换为Blob
    const blob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((blob) => {
        if (blob) resolve(blob)
      }, 'image/jpeg', 0.9)
    })

    const formData = new FormData()
    formData.append('avatar', blob, 'avatar.jpg')

    console.log('准备发送上传请求')
    const result = await uploadUserAvatar(formData)
    console.log('上传请求返回结果:', result)

    // 检查返回的数据结构
    if (result && result.data) {
      console.log('上传成功，更新头像URL:', result.data)
      // 更新头像显示
      profile.value.avatar = result.data
      // 更新用户信息
      userStore.updateUserInfo({
        ...userStore.userInfo!,
        avatar: result.data
      })
      ElMessage.success('头像更新成功')
      cropperVisible.value = false
      imageUrl.value = ''
    } else {
      console.error('上传返回数据格式不正确:', result)
      throw new Error('上传失败：返回数据格式不正确')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '头像上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const resetCropper = () => {
  imageUrl.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>

<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div class="profile-header">
              <span>个人信息</span>
              <el-button
                v-if="!isEditing"
                type="primary"
                text
                @click="isEditing = true"
              >
                编辑
              </el-button>
            </div>
          </template>

          <el-skeleton :loading="loading" animated>
            <template #template>
              <div class="skeleton-content">
                <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 0 auto" />
                <el-skeleton-item variant="h3" style="width: 50%; margin: 16px auto" />
                <el-skeleton-item variant="text" style="width: 80%" />
                <el-skeleton-item variant="text" style="width: 60%" />
              </div>
            </template>

            <template #default>
              <div v-if="!isEditing" class="profile-info">
                <div class="avatar-container">
                  <el-avatar
                    :size="100"
                    :src="avatarUrl"
                    @error="() => {
                      console.error('头像加载失败:', avatarUrl)
                      ElMessage.error('头像加载失败')
                    }"
                  />
                  <input
                    type="file"
                    ref="fileInput"
                    style="display: none"
                    accept="image/jpeg,image/png"
                    @change="handleFileChange"
                  />
                  <el-button
                    class="change-avatar-btn"
                    type="primary"
                    :icon="Upload"
                    circle
                    @click="fileInput?.click()"
                  />
                </div>
                <h2>{{ profile.username }}</h2>
                <!-- 显示用户角色 -->
                <div class="user-roles" v-if="userStore.userInfo?.roles?.length">
                  <el-tag 
                    v-for="role in userStore.userInfo.roles" 
                    :key="role"
                    :type="role === 'ROLE_ADMIN' || role === '管理员' ? 'danger' : 'info'"
                    size="small"
                    style="white-space: nowrap"
                  >
                    {{ role === 'ROLE_ADMIN' ? '管理员' : (role === 'ROLE_USER' ? '普通用户' : role) }}
                  </el-tag>
                </div>
                <p class="bio">{{ profile.bio || '这个人很懒，什么都没写~' }}</p>
                <div class="info-item">
                  <span class="label">邮箱：</span>
                  <span>{{ profile.email }}</span>
                </div>
                <div class="info-item">
                  <span class="label">注册时间：</span>
                  <span>{{ registrationDate }}</span>
                </div>
              </div>

              <div v-else class="profile-edit">
                <el-form>
                  <el-form-item label="用户名">
                    <el-input v-model="editForm.username" />
                  </el-form-item>
                  <el-form-item label="邮箱">
                    <el-input v-model="editForm.email" />
                  </el-form-item>
                  <el-form-item label="个人简介">
                    <el-input
                      v-model="editForm.bio"
                      type="textarea"
                      :rows="3"
                      placeholder="写点什么介绍一下自己吧~"
                    />
                  </el-form-item>
                  
                  <el-divider content-position="center">密码修改（选填）</el-divider>
                  
                  <el-form-item label="当前密码">
                    <el-input
                      v-model="editForm.oldPassword"
                      type="password"
                      placeholder="填写当前密码才能修改密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="新密码">
                    <el-input
                      v-model="editForm.password"
                      type="password"
                      placeholder="请输入新密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="确认密码">
                    <el-input
                      v-model="editForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button
                      type="primary"
                      :loading="loading"
                      @click="updateProfile"
                    >
                      保存
                    </el-button>
                    <el-button @click="isEditing = false">取消</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </template>
          </el-skeleton>
        </el-card>
      </el-col>

      <!-- 右侧内容区域 -->
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab" @tab-click="handleTabChange">
            <el-tab-pane label="我的文章" name="posts">
              <el-skeleton :loading="loading" animated :count="3">
                <template #template>
                  <div class="skeleton-item" style="margin-bottom: 20px">
                    <el-skeleton-item variant="h3" style="width: 50%" />
                    <el-skeleton-item variant="text" style="width: 80%" />
                    <el-skeleton-item variant="text" style="width: 60%" />
                  </div>
                </template>

                <template #default>
                  <div class="posts-list">
                    <el-empty
                      v-if="posts.length === 0"
                      description="暂无文章"
                    />
                    <el-card
                      v-for="post in posts"
                      :key="post.postId"
                      class="post-item"
                      shadow="hover"
                      @click="router.push(`/posts/${post.postId}`)"
                    >
                      <h3>{{ post.title }}</h3>
                      <p class="summary">{{ post.summary }}</p>
                      <div class="post-meta">
                        <div class="tags">
                          <el-tag
                            v-for="tag in post.tags"
                            :key="tag"
                            size="small"
                            class="tag"
                          >
                            {{ tag }}
                          </el-tag>
                        </div>
                        <div class="stats">
                          <span>{{ new Date(post.createdAt).toLocaleDateString() }}</span>
                        </div>
                      </div>
                    </el-card>
                  </div>
                </template>
              </el-skeleton>
            </el-tab-pane>

            <el-tab-pane label="我的收藏" name="favorites">
              <el-skeleton :loading="loading" animated :count="3">
                <template #template>
                  <div class="skeleton-item" style="margin-bottom: 20px">
                    <el-skeleton-item variant="h3" style="width: 50%" />
                    <el-skeleton-item variant="text" style="width: 80%" />
                    <el-skeleton-item variant="text" style="width: 60%" />
                  </div>
                </template>

                <template #default>
                  <div class="posts-list">
                    <template v-if="favorites.length === 0">
                      <el-empty description="暂无收藏" />
                    </template>
                    <template v-else>
                    <el-card
                      v-for="post in favorites"
                        :key="post.postId"
                      class="post-item"
                      shadow="hover"
                        @click="router.push(`/posts/${post.postId}`)"
                    >
                      <h3>{{ post.title }}</h3>
                      <p class="summary">{{ post.summary }}</p>
                      <div class="post-meta">
                        <div class="tags">
                          <el-tag
                            v-for="tag in post.tags"
                            :key="tag"
                            size="small"
                            class="tag"
                          >
                            {{ tag }}
                          </el-tag>
                        </div>
                        <div class="stats">
                          <span>{{ new Date(post.createdAt).toLocaleDateString() }}</span>
                        </div>
                      </div>
                    </el-card>
                    </template>
                  </div>
                </template>
              </el-skeleton>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 头像裁剪对话框 -->
  <el-dialog
    v-model="cropperVisible"
    title="裁剪头像"
    width="500px"
    :close-on-click-modal="false"
  >
    <div class="cropper-container">
      <div v-if="!imageUrl" class="upload-area" @click="fileInput?.click()">
        <el-icon><Upload /></el-icon>
        <div class="upload-text">
          <p>点击或拖拽图片到此处上传</p>
          <p>支持 JPG、PNG 格式，最大 2MB</p>
        </div>
      </div>
      <ImageCropper
        v-else
        ref="cropperRef"
        :imageUrl="imageUrl"
      />
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetCropper">重新选择</el-button>
        <el-button @click="cropperVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAvatarUpload" :loading="uploading">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.profile-info {
  text-align: center;
  padding: 24px;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.el-avatar {
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.el-avatar:hover {
  transform: scale(1.05);
}

.change-avatar-btn {
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.8;
  transition: all 0.3s ease;
  z-index: 1;
}

.change-avatar-btn:hover {
  opacity: 1;
  transform: scale(1.1);
}

.profile-info h2 {
  margin: 16px 0;
  font-size: 24px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.user-roles {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin: 16px 0;
}

.el-tag {
  padding: 6px 12px;
  border-radius: 16px;
  font-weight: 500;
}

.bio {
  margin: 20px 0;
  color: var(--el-text-color-secondary);
  line-height: 1.8;
  font-size: 15px;
  padding: 0 20px;
}

.info-item {
  text-align: left;
  margin: 16px 0;
  padding: 12px 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.info-item:hover {
  background: var(--el-fill-color);
}

.info-item .label {
  color: var(--el-text-color-secondary);
  font-weight: 500;
  margin-right: 8px;
}

.posts-list {
  margin-top: 24px;
}

.post-item {
  margin-bottom: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid var(--el-border-color-light);
}

.post-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--el-border-color);
}

.post-item h3 {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.summary {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.8;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid var(--el-border-color-light);
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  border-radius: 12px;
  padding: 4px 12px;
  font-size: 12px;
}

.stats {
  display: flex;
  gap: 20px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stats .el-icon {
  font-size: 16px;
}

.el-tabs {
  margin-top: 20px;
}

.el-tabs__nav {
  border-radius: 8px;
}

.el-tabs__item {
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
}

.el-tabs__item.is-active {
  font-weight: 600;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }

  .profile-info {
    padding: 16px;
  }

  .bio {
    padding: 0 10px;
  }

  .post-meta {
    flex-direction: column;
    gap: 12px;
  }

  .stats {
    width: 100%;
    justify-content: space-around;
  }
}

.cropper-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .upload-area {
    width: 400px;
    height: 400px;
    border: 2px dashed var(--el-border-color);
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
    
    .el-icon {
      font-size: 48px;
      color: var(--el-text-color-secondary);
      margin-bottom: 16px;
    }
    
    .upload-text {
      text-align: center;
      color: var(--el-text-color-secondary);
      
      p {
        margin: 4px 0;
      }
    }
  }
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 管理员面板样式 */
.admin-panel {
  padding: 20px 0;
}

.admin-panel h3 {
  font-size: 1.5rem;
  color: var(--text-color);
  margin-bottom: 10px;
}

.admin-card {
  margin-bottom: 20px;
}

.admin-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.admin-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-dialog__body) {
  padding: 0;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-upload__tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}
</style> 