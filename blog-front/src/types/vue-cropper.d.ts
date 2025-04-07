declare module 'vue-cropper' {
  import { DefineComponent } from 'vue'

  export const VueCropper: DefineComponent<{
    src: string
    aspectRatio?: number
    viewMode?: number
    guides?: boolean
    center?: boolean
    highlight?: boolean
    background?: boolean
    autoCropArea?: number
    rotatable?: boolean
    zoomable?: boolean
    movable?: boolean
  }>
} 