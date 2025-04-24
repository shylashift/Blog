<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getPostById, createPost, updatePost } from '@/api/posts'
import { useUserStore } from '@/stores/user'
import axios from 'axios'
import { BASE_URL } from '@/utils/constants'
import RichTextEditor from '@/components/RichTextEditor.vue'
import TurndownService from 'turndown'
import showdown from 'showdown'

interface PostForm {
  title: string
  content: string
  tags: string[]
  summary: string
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const isEdit = ref(false)

const postForm = ref<PostForm>({
  title: '',
  content: '',
  tags: [],
  summary: ''
})

const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()

const editorType = ref('markdown')
const handleEditorTypeChange = () => {
  // 在切换编辑器时保持内容同步
  if (editorType.value === 'markdown') {
    // 如果从富文本切换到Markdown，需要将HTML转换为Markdown
    const turndownService = new TurndownService()
    postForm.value.content = turndownService.turndown(postForm.value.content)
  } else {
    // 如果从Markdown切换到富文本，需要将Markdown转换为HTML
    const showdownConverter = new showdown.Converter()
    postForm.value.content = showdownConverter.makeHtml(postForm.value.content)
  }
}

const handleClose = (tag: string) => {
  postForm.value.tags = postForm.value.tags.filter(t => t !== tag)
}

const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.input?.focus()
  })
}

const handleTagInputConfirm = () => {
  if (tagInputValue.value) {
    if (!postForm.value.tags.includes(tagInputValue.value)) {
      postForm.value.tags.push(tagInputValue.value)
    }
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

const fetchPost = async (id: string) => {
  try {
    loading.value = true
    const data = await getPostById(parseInt(id))
    postForm.value = {
      title: data.title,
      content: data.content,
      tags: typeof data.tags === 'string' ? data.tags.split(/[,，\s]+/).filter(Boolean) : (data.tags || []),
      summary: data.summary || ''
    }
  } catch (error) {
    console.error('获取文章错误:', error)
    ElMessage.error('获取文章失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!postForm.value.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!postForm.value.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  try {
    loading.value = true
    await userStore.initialize()
    
    if (isEdit.value) {
      const postId = parseInt(route.params.id as string)
      await updatePost(postId, postForm.value)
      ElMessage.success('更新成功')
    } else {
      await createPost(postForm.value)
      ElMessage.success('发布成功')
    }
    router.push('/posts')
  } catch (error) {
    console.error('保存文章错误:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleUploadImage = async (files: File[], callback: (urls: string[]) => void) => {
  try {
    const uploadPromises = files.map(async (file) => {
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        ElMessage.error('只能上传图片文件')
        return ''
      }

      // 检查文件大小（最大5MB）
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过5MB')
        return ''
      }

      const formData = new FormData()
      formData.append('image', file)

      // 获取token
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.error('请先登录')
        return ''
      }

      const response = await axios.post(`${BASE_URL}/posts/upload-image`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `${token}`
        }
      })

      if (response.data.code === 200) {
        return `${BASE_URL}${response.data.data}`
      } else {
        ElMessage.error('图片上传失败')
        return ''
      }
    })

    const urls = await Promise.all(uploadPromises)
    const validUrls = urls.filter(url => url !== '')
    callback(validUrls)
  } catch (error) {
    console.error('图片上传错误:', error)
    ElMessage.error('图片上传失败')
  }
}

onMounted(async () => {
  await userStore.initialize()
  
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  const id = route.params.id
  if (id) {
    isEdit.value = true
    await fetchPost(id as string)
  }
})
</script>

<template>
  <div class="post-editor">
    <div class="editor-header">
      <h2>{{ isEdit ? '编辑文章' : '发布文章' }}</h2>
    </div>

    <div class="editor-form">
      <el-form label-position="top">
        <el-form-item label="文章标题">
          <el-input
            v-model="postForm.title"
            placeholder="请输入文章标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="文章摘要">
          <el-input
            v-model="postForm.summary"
            type="textarea"
            placeholder="请输入文章摘要"
            maxlength="200"
            show-word-limit
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="文章标签">
          <div class="tags-container">
            <el-tag
              v-for="tag in postForm.tags"
              :key="tag"
              closable
              :disable-transitions="false"
              @close="handleClose(tag)"
            >
              {{ tag }}
            </el-tag>

            <el-input
              v-if="tagInputVisible"
              ref="tagInputRef"
              v-model="tagInputValue"
              class="tag-input"
              size="small"
              @keyup.enter="handleTagInputConfirm"
              @blur="handleTagInputConfirm"
            />

            <el-button
              v-else
              class="button-new-tag"
              size="small"
              @click="showTagInput"
            >
              + 添加标签
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="编辑器类型">
          <el-radio-group v-model="editorType" @change="handleEditorTypeChange">
            <el-radio label="markdown">Markdown编辑器</el-radio>
            <el-radio label="richtext">富文本编辑器</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="文章内容">
          <div v-if="editorType === 'markdown'" class="editor-container">
            <MdEditor
              v-model="postForm.content"
              preview-theme="github"
              style="height: 500px; width: 100%;"
              :onUploadImg="handleUploadImage"
            />
          </div>
          <div v-else class="editor-container">
            <RichTextEditor
              v-model="postForm.content"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isEdit ? '更新' : '发布' }}
          </el-button>
          <el-button @click="router.push('/posts')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.post-editor {
  width: 100%;
  max-width: 100%;
  margin: 0;
  padding: 40px;
  background-color: var(--el-bg-color);
  box-shadow: none;
  border-radius: 0;
  box-sizing: border-box;
}

.editor-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.editor-header h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 24px;
  font-weight: 600;
}

.editor-form {
  width: 100%;
  max-width: 100%;
}

.editor-form :deep(.el-form) {
  width: 100%;
}

.editor-form :deep(.el-form-item) {
  margin-bottom: 24px;
  width: 100%;
}

.editor-form :deep(.el-form-item__label) {
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
}

.editor-form :deep(.el-input__wrapper),
.editor-form :deep(.el-textarea__wrapper) {
  box-shadow: none;
  border: 1px solid var(--el-border-color);
}

.editor-form :deep(.el-input__wrapper:hover),
.editor-form :deep(.el-textarea__wrapper:hover) {
  border-color: var(--el-border-color-hover);
}

.editor-form :deep(.el-input__wrapper:focus-within),
.editor-form :deep(.el-textarea__wrapper:focus-within) {
  border-color: var(--el-color-primary);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  min-height: 36px;
}

.el-tag {
  margin: 0;
  height: 28px;
  line-height: 26px;
  border-radius: 4px;
}

.tag-input {
  width: 120px;
  margin: 0;
  vertical-align: bottom;
}

.button-new-tag {
  margin: 0;
  height: 28px;
  padding: 0 12px;
}

.editor-container {
  width: 100%;
  display: block;
}

.editor-container :deep(.md-editor) {
  width: 100% !important;
  max-width: 100% !important;
  min-height: 600px !important;
  margin-top: 8px;
  border: 1px solid var(--el-border-color) !important;
  border-radius: 4px;
  box-sizing: border-box;
}

.editor-container :deep(.md-editor-area) {
  width: 100% !important;
}

.editor-container :deep(.md-editor-preview) {
  width: 100% !important;
}

.editor-container :deep(.tox.tox-tinymce) {
  width: 100% !important;
  max-width: 100% !important;
  min-height: 600px !important;
  margin-top: 8px;
  border: 1px solid var(--el-border-color) !important;
  border-radius: 4px;
  box-sizing: border-box;
}

:deep(.md-editor-toolbar) {
  width: 100% !important;
  border-bottom: 1px solid var(--el-border-color) !important;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .post-editor {
    padding: 20px;
  }

  .editor-header h2 {
    font-size: 20px;
  }

  .editor-container :deep(.md-editor),
  .editor-container :deep(.tox.tox-tinymce) {
    min-height: 400px !important;
  }
}
</style> 