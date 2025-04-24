<template>
  <div class="rich-text-editor">
    <Editor
      v-model="content"
      :api-key="apiKey"
      :init="{
        height: 600,
        width: '100%',
        menubar: true,
        plugins: [
          'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
          'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
          'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
        ],
        toolbar: 'undo redo | blocks | ' +
          'bold italic forecolor | alignleft aligncenter ' +
          'alignright alignjustify | bullist numlist outdent indent | ' +
          'removeformat | help',
        content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px; width: 100%; }',
        resize: true,
        autoresize_min_height: 600,
        autoresize_max_height: 1000
      }"
      style="width: 100%"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Editor from '@tinymce/tinymce-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const content = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  if (newValue !== content.value) {
    content.value = newValue
  }
})

watch(content, (newValue) => {
  emit('update:modelValue', newValue)
})

// 你需要在这里替换成你自己的TinyMCE API密钥
const apiKey = 'xsno5z0qkjqrnp36n54ubjjkopfp6ujsvcquhy23s066b5kr'
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
  margin-bottom: 20px;
  box-sizing: border-box;
}

:deep(.tox-tinymce) {
  width: 100% !important;
  min-height: 600px !important;
  box-sizing: border-box;
}

:deep(.tox-editor-container) {
  width: 100% !important;
  box-sizing: border-box;
}

:deep(.tox-edit-area) {
  width: 100% !important;
  box-sizing: border-box;
}

:deep(.tox-edit-area__iframe) {
  width: 100% !important;
  box-sizing: border-box;
}

:deep(.tox-toolbar-overlord),
:deep(.tox-toolbar__primary) {
  width: 100% !important;
  box-sizing: border-box;
}
</style>
