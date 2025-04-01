import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/posts'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/posts',
    name: 'PostList',
    component: () => import('../views/PostList.vue'),
    meta: {
      title: '文章列表',
      requiresAuth: true
    }
  },
  {
    path: '/post/create',
    name: 'CreatePost',
    component: () => import('../views/PostEditor.vue'),
    meta: {
      title: '发布文章',
      requiresAuth: true
    }
  },
  {
    path: '/post/:id',
    name: 'EditPost',
    component: () => import('../views/PostEditor.vue'),
    meta: {
      title: '编辑文章',
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router 