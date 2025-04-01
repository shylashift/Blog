<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Plus, Upload } from '@element-plus/icons-vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import type { TabsPaneContext } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('posts')
const uploadRef = ref()
const cropperRef = ref()
const cropperVisible = ref(false)
const cropperKey = ref(0)
const imageUrl = ref('')

interface UserProfile {
  id: number
  username: string
  email: string
  avatar: string
  bio: string
  createdAt: string
}

interface Post {
  id: number
  title: string
  summary: string
  tags: string[]
  createdAt: string
  likes: number
  commentCount: number
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
  email: '',
  bio: ''
})

// 获取用户详细信息
const fetchUserProfile = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/users/profile', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    if (!response.ok) throw new Error('获取用户信息失败')
    profile.value = await response.json()
    editForm.value = {
      email: profile.value.email,
      bio: profile.value.bio
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 获取用户的文章
const fetchUserPosts = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/users/posts', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    if (!response.ok) throw new Error('获取文章列表失败')
    posts.value = await response.json()
  } catch (error) {
    console.error('获取文章列表错误:', error)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

// 获取用户的收藏
const fetchUserFavorites = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/users/favorites', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    if (!response.ok) throw new Error('获取收藏列表失败')
    favorites.value = await response.json()
  } catch (error) {
    console.error('获取收藏列表错误:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 更新用户信息
const updateProfile = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/users/profile', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`
      },
      body: JSON.stringify(editForm.value)
    })
    
    if (!response.ok) throw new Error('更新用户信息失败')
    const updatedProfile = await response.json()
    profile.value = {
      ...profile.value,
      ...updatedProfile
    }
    isEditing.value = false
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新用户信息错误:', error)
    ElMessage.error('更新失败')
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

// 头像上传相关
const beforeAvatarUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }

  // 读取文件并显示裁剪对话框
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target?.result as string
    cropperVisible.value = true
    cropperKey.value++ // 强制重新创建裁剪组件
  }
  reader.readAsDataURL(file)
  return false // 阻止自动上传
}

// 上传头像
const uploadAvatar = async (blob: Blob) => {
  try {
    loading.value = true
    const formData = new FormData()
    formData.append('avatar', blob, 'avatar.png')

    const response = await fetch('/api/users/avatar', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      },
      body: formData
    })

    if (!response.ok) throw new Error('上传头像失败')
    const data = await response.json()
    profile.value.avatar = data.avatarUrl
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('上传头像错误:', error)
    ElMessage.error('上传头像失败')
  } finally {
    loading.value = false
    cropperVisible.value = false
  }
}

// 处理裁剪完成
const handleCropFinish = async (blob: Blob) => {
  await uploadAvatar(blob)
}

// 取消裁剪
const handleCropCancel = () => {
  cropperVisible.value = false
  imageUrl.value = ''
}

onMounted(() => {
  if (!userStore.isAuthenticated) {
    router.push('/login')
    return
  }
  fetchUserProfile()
  fetchUserPosts()
})

defineOptions({
  components: {
    VueCropper
  }
})
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
                    :src="profile.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                  />
                  <el-upload
                    class="avatar-uploader"
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    ref="uploadRef"
                  >
                    <el-button
                      class="change-avatar-btn"
                      type="primary"
                      :icon="Upload"
                      circle
                    />
                  </el-upload>
                </div>
                <h2>{{ profile.username }}</h2>
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
                      :key="post.id"
                      class="post-item"
                      shadow="hover"
                      @click="router.push(`/posts/${post.id}`)"
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
                          <span>
                            <el-icon><ThumbsUp /></el-icon>
                            {{ post.likes }}
                          </span>
                          <span>
                            <el-icon><ChatRound /></el-icon>
                            {{ post.commentCount }}
                          </span>
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
                    <el-empty
                      v-if="favorites.length === 0"
                      description="暂无收藏"
                    />
                    <el-card
                      v-for="post in favorites"
                      :key="post.id"
                      class="post-item"
                      shadow="hover"
                      @click="router.push(`/posts/${post.id}`)"
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
                          <span>
                            <el-icon><ThumbsUp /></el-icon>
                            {{ post.likes }}
                          </span>
                          <span>
                            <el-icon><ChatRound /></el-icon>
                            {{ post.commentCount }}
                          </span>
                          <span>{{ new Date(post.createdAt).toLocaleDateString() }}</span>
                        </div>
                      </div>
                    </el-card>
                  </div>
                </template>
              </el-skeleton>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 头像裁剪对话框 -->
    <el-dialog
      v-model="cropperVisible"
      title="裁剪头像"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div v-if="cropperVisible" class="cropper-container">
        <vue-cropper
          :key="cropperKey"
          ref="cropperRef"
          :src="imageUrl"
          :aspect-ratio="1"
          :view-mode="1"
          :guides="true"
          :center="true"
          :highlight="true"
          :background="true"
          :auto-crop-area="0.8"
          :rotatable="false"
          :zoomable="true"
          :movable="true"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCropCancel">取消</el-button>
          <el-button
            type="primary"
            :loading="loading"
            @click="() => cropperRef?.getCropBlob?.(handleCropFinish)"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  text-align: center;
}

.profile-info h2 {
  margin: 16px 0;
  color: var(--text-color);
}

.bio {
  margin: 16px 0;
  color: var(--text-color-secondary);
  line-height: 1.6;
}

.info-item {
  text-align: left;
  margin: 12px 0;
  color: var(--text-color);
}

.info-item .label {
  color: var(--text-color-secondary);
}

.profile-edit {
  margin-top: 20px;
}

.posts-list {
  margin-top: 20px;
}

.post-item {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}

.post-item:hover {
  transform: translateY(-2px);
}

.post-item h3 {
  margin: 0 0 12px;
  color: var(--text-color);
}

.summary {
  margin: 0 0 12px;
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
}

.tags {
  display: flex;
  gap: 8px;
}

.tag {
  margin-right: 8px;
}

.stats {
  display: flex;
  gap: 16px;
  color: var(--text-color-secondary);
  font-size: 0.9rem;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 15px;
  }

  .el-row {
    margin: 0 !important;
  }

  .el-col {
    padding: 0 !important;
  }

  .post-meta {
    flex-direction: column;
    gap: 12px;
  }

  .stats {
    width: 100%;
    justify-content: space-between;
  }
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.change-avatar-btn {
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.change-avatar-btn:hover {
  opacity: 1;
}

.cropper-container {
  height: 400px;
  background-color: #000;
}

/* 确保裁剪对话框中的按钮样式正确 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog__body) {
  padding: 0;
}
</style> 