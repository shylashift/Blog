<template>
  <div class="image-cropper">
    <canvas ref="canvasRef" class="cropper-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'

const props = defineProps<{
  imageUrl: string
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const ctx = ref<CanvasRenderingContext2D | null>(null)
const image = ref<HTMLImageElement | null>(null)
const isDragging = ref(false)
const startPos = ref({ x: 0, y: 0 })
const imagePos = ref({ x: 0, y: 0 })
const scale = ref(1)

const CANVAS_SIZE = 400

onMounted(() => {
  if (canvasRef.value) {
    canvasRef.value.width = CANVAS_SIZE
    canvasRef.value.height = CANVAS_SIZE
    ctx.value = canvasRef.value.getContext('2d')
    
    // 添加鼠标事件监听
    canvasRef.value.addEventListener('mousedown', startDrag)
    canvasRef.value.addEventListener('mousemove', onDrag)
    canvasRef.value.addEventListener('mouseup', endDrag)
    canvasRef.value.addEventListener('mouseleave', endDrag)
    canvasRef.value.addEventListener('wheel', onWheel)
    
    loadImage()
  }
})

watch(() => props.imageUrl, () => {
  loadImage()
})

const loadImage = () => {
  image.value = new Image()
  image.value.onload = () => {
    if (!image.value || !canvasRef.value) return
    
    // 计算初始缩放比例和位置
    const aspectRatio = image.value.width / image.value.height
    if (aspectRatio > 1) {
      scale.value = CANVAS_SIZE / image.value.height
      imagePos.value = {
        x: (CANVAS_SIZE - image.value.width * scale.value) / 2,
        y: 0
      }
    } else {
      scale.value = CANVAS_SIZE / image.value.width
      imagePos.value = {
        x: 0,
        y: (CANVAS_SIZE - image.value.height * scale.value) / 2
      }
    }
    
    drawImage()
  }
  image.value.src = props.imageUrl
}

const drawImage = () => {
  if (!ctx.value || !image.value || !canvasRef.value) return
  
  // 清除画布
  ctx.value.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)
  
  // 绘制图片
  ctx.value.save()
  ctx.value.translate(imagePos.value.x, imagePos.value.y)
  ctx.value.scale(scale.value, scale.value)
  ctx.value.drawImage(image.value, 0, 0)
  ctx.value.restore()
  
  // 绘制裁剪框
  ctx.value.strokeStyle = '#fff'
  ctx.value.lineWidth = 2
  ctx.value.strokeRect(
    (CANVAS_SIZE - 200) / 2,
    (CANVAS_SIZE - 200) / 2,
    200,
    200
  )
}

const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  startPos.value = {
    x: e.clientX - imagePos.value.x,
    y: e.clientY - imagePos.value.y
  }
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  
  imagePos.value = {
    x: e.clientX - startPos.value.x,
    y: e.clientY - startPos.value.y
  }
  
  drawImage()
}

const endDrag = () => {
  isDragging.value = false
}

const onWheel = (e: WheelEvent) => {
  e.preventDefault()
  
  const delta = e.deltaY > 0 ? 0.9 : 1.1
  scale.value *= delta
  
  // 限制缩放范围
  scale.value = Math.max(0.1, Math.min(5, scale.value))
  
  drawImage()
}

// 获取裁剪后的Canvas对象
const getCroppedCanvas = () => {
  const cropCanvas = document.createElement('canvas')
  cropCanvas.width = 200
  cropCanvas.height = 200
  
  const cropCtx = cropCanvas.getContext('2d')
  if (!cropCtx || !canvasRef.value) return null
  
  // 从原canvas复制裁剪区域到新canvas
  cropCtx.drawImage(
    canvasRef.value,
    (CANVAS_SIZE - 200) / 2,
    (CANVAS_SIZE - 200) / 2,
    200,
    200,
    0,
    0,
    200,
    200
  )
  
  return cropCanvas
}

// 清理事件监听
onUnmounted(() => {
  if (canvasRef.value) {
    canvasRef.value.removeEventListener('mousedown', startDrag)
    canvasRef.value.removeEventListener('mousemove', onDrag)
    canvasRef.value.removeEventListener('mouseup', endDrag)
    canvasRef.value.removeEventListener('mouseleave', endDrag)
    canvasRef.value.removeEventListener('wheel', onWheel)
  }
})

defineExpose({
  getCroppedCanvas
})
</script>

<style scoped>
.image-cropper {
  width: 400px;
  height: 400px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.cropper-canvas {
  cursor: move;
}
</style> 