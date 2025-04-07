import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import BasicLayout from '@/components/layout/BasicLayout.vue'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import PostList from '@/views/PostList.vue'
import PostDetail from '@/views/PostDetail.vue'
import PostEditor from '@/views/PostEditor.vue'
import Profile from '@/views/Profile.vue'
import Messages from '@/views/Messages.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: BasicLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: Home,
          meta: {
            title: '首页'
          }
        },
        {
          path: 'posts',
          name: 'posts',
          component: PostList,
          meta: {
            title: '文章列表'
          }
        },
        {
          path: 'posts/create',
          name: 'post-create',
          component: PostEditor,
          meta: {
            title: '发布文章',
            requiresAuth: true
          }
        },
        {
          path: 'posts/:id',
          name: 'post-detail',
          component: PostDetail,
          meta: {
            title: '文章详情'
          }
        },
        {
          path: 'posts/:id/edit',
          name: 'post-edit',
          component: PostEditor,
          meta: {
            title: '编辑文章',
            requiresAuth: true
          }
        },
        {
          path: 'profile',
          name: 'profile',
          component: Profile,
          meta: {
            title: '个人中心',
            requiresAuth: true
          }
        },
        {
          path: '/messages',
          name: 'messages',
          component: Messages,
          meta: {
            requiresAuth: true,
            title: '学术交流中心'
          }
        },
        {
          path: '/admin/users',
          name: 'admin-users',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            title: '用户管理'
          }
        },
        {
          path: '/admin/posts',
          name: 'admin-posts',
          component: () => import('@/views/admin/PostManagement.vue'),
          meta: {
            requiresAuth: true,
            requiresAdmin: true,
            title: '文章管理'
          }
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: {
        title: '登录'
      }
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
      meta: {
        title: '注册'
      }
    }
  ]
})

// 保存上一个路由路径
let lastPath = ''

router.beforeEach(async (to, _, next) => {
  // 设置页面标题
  document.title = `${to.meta.title} - 我的博客`

  const userStore = useUserStore()

  // 如果是登录页面且已登录，直接跳转到首页或上一个页面
  if (to.path === '/login' && userStore.isLoggedIn) {
    return next(lastPath || '/')
  }

  // 如果不是登录页面，保存路径
  if (to.path !== '/login') {
    lastPath = to.path
  }

  try {
    // 确保用户状态已初始化
    await userStore.initialize()

    // 检查是否需要登录
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      // 保存当前路径，登录后返回
      next('/login')
    } 
    // 检查是否需要管理员权限
    else if (to.meta.requiresAdmin && !userStore.userInfo?.roles?.includes('ROLE_ADMIN')) {
      next('/profile')
    }
    else {
      next()
    }
  } catch (error) {
    console.error('路由守卫初始化失败:', error)
    if (to.meta.requiresAuth) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
