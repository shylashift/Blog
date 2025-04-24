<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import ImageCropper from '@/components/ImageCropper.vue'
import type { FormInstance, FormRules } from 'element-plus'
import { uploadUserAvatar } from '@/api/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const fileInput = ref<HTMLInputElement>()
const cropperRef = ref<InstanceType<typeof ImageCropper>>()
const showCropper = ref(false)
const selectedImage = ref('')
const avatarKey = ref(0)

const userForm = ref({
  username: userStore.userInfo?.username || '',
  email: userStore.userInfo?.email || '',
  bio: userStore.userInfo?.bio || ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  // 创建图片URL并打开裁剪对话框
  selectedImage.value = URL.createObjectURL(file)
  showCropper.value = true
  
  // 重置文件输入以允许选择相同文件
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleCropComplete = (croppedData: string) => {
  selectedImage.value = croppedData
}

const uploadAvatar = async () => {
  try {
    const cropperCanvas = cropperRef.value?.getCroppedCanvas()
    if (!cropperCanvas) {
      ElMessage.error('获取裁剪数据失败')
      return
    }
    
    // 将Canvas转换为Blob
    const blob = await new Promise<Blob>((resolve) => {
      cropperCanvas.toBlob((blob) => {
        if (blob) resolve(blob)
      }, 'image/jpeg', 0.9)
    })
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('avatar', blob, 'avatar.jpg')
    
    // 上传头像
    const response = await uploadUserAvatar(formData)
    if (response.data) {
      // 更新用户信息
      await userStore.updateUserInfo({
        ...userStore.$state,
        avatar: response.data
      })
      
      // 增加avatarKey强制重新渲染头像
      avatarKey.value++
      
      ElMessage.success('头像上传成功')
      showCropper.value = false
      
      // 清理临时数据
      selectedImage.value = ''
      URL.revokeObjectURL(selectedImage.value)
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传头像失败，请重试')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userStore.updateUserInfo({
          ...userStore.$state,
          ...userForm.value
        })
        ElMessage.success('个人信息更新成功')
      } catch (error) {
        console.error('更新个人信息失败:', error)
        ElMessage.error('更新个人信息失败，请重试')
      }
    }
  })
}

onMounted(() => {
  // 初始化表单数据
  userForm.value = {
    username: userStore.userInfo?.username || '',
    email: userStore.userInfo?.email || '',
    bio: userStore.userInfo?.bio || ''
  }
})
</script>

<template>
  <div class="user-profile">
    <el-card class="profile-card">
      <div class="avatar-section">
        <el-avatar
          :size="120"
          :src="userStore.userInfo?.avatar || ''"
          :key="avatarKey"
          class="user-avatar"
        >
          <img src="@/assets/default-avatar.png" />
        </el-avatar>
        <div class="avatar-overlay" @click="triggerFileInput">
          <el-icon><Camera /></el-icon>
          <span>更换头像</span>
        </div>
        <input
          type="file"
          ref="fileInput"
          style="display: none"
          accept="image/*"
          @change="handleFileChange"
        />
      </div>

      <el-form
        ref="formRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
        class="profile-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="userForm.bio"
            type="textarea"
            :rows="4"
            placeholder="写点什么介绍一下自己吧..."
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog
      v-model="showCropper"
      title="裁剪头像"
      width="500px"
      :close-on-click-modal="false"
    >
      <ImageCropper
        v-if="showCropper"
        ref="cropperRef"
        :imageUrl="selectedImage"
        @cropComplete="handleCropComplete"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCropper = false">取消</el-button>
          <el-button type="primary" @click="uploadAvatar">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-profile {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.profile-card {
  padding: 20px;
}

.avatar-section {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
}

.user-avatar {
  width: 100%;
  height: 100%;
  border: 2px solid #eee;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-overlay:hover {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.profile-form {
  max-width: 500px;
  margin: 0 auto;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style> 