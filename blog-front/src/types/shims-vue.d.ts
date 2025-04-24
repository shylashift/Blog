declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'turndown' {
  const TurndownService: any
  export default TurndownService
}

declare module 'showdown' {
  const showdown: any
  export default showdown
} 