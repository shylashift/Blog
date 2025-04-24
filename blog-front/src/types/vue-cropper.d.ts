declare module 'vue-cropper/dist/vue-cropper.es.js' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'vue-cropper' {
  import VueCropper from 'vue-cropper/dist/vue-cropper.es.js'
  export default VueCropper
} 