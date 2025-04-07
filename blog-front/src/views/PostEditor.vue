<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getPostById, createPost, updatePost } from '@/api/posts'
import type { Post } from '@/api/posts'
import { useUserStore } from '@/stores/user'

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
const inputRef = ref<HTMLInputElement>()

const postForm = ref<PostForm>({
  title: '',
  content: '',
  tags: [],
  summary: ''
})

const formRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { min: 10, message: '内容至少需要10个字符', trigger: 'blur' }
  ]
}

const formRef = ref<FormInstance>()
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()

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
      tags: data.tags || [],
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

        <el-form-item label="文章内容">
          <MdEditor
            v-model="postForm.content"
            preview-theme="github"
            style="height: 500px"
          />
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.editor-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.editor-header h2 {
  margin: 0;
  color: var(--text-color);
  font-size: 1.8rem;
}

.editor-form {
  margin-top: 20px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-input {
  width: 100px;
  margin-right: 8px;
  vertical-align: bottom;
}

.button-new-tag {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .post-editor {
    padding: 15px;
  }

  .editor-header h2 {
    font-size: 1.5rem;
  }
}
</style> 