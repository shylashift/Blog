import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import BasicLayout from '@/components/layout/BasicLayout.vue'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import PostList from '@/views/PostList.vue'
import PostDetail from '@/views/PostDetail.vue'
import PostEditor from '@/views/PostEditor.vue'
import Messages from '@/views/Messages.vue'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
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
          component: () => import('@/views/Profile.vue'),
          meta: {
            title: '个人中心',
            requiresAuth: true
          }
        },
        {
          path: '/notifications',
          name: 'notifications',
          component: () => import('@/views/Notifications.vue'),
          meta: { requiresAuth: true }
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
          path: '/admin',
          component: () => import('@/views/admin/AdminDashboard.vue'),
          meta: { requiresAuth: true, requiresAdmin: true },
          children: [
            {
              path: '',
              name: 'admin-dashboard',
              component: () => import('@/views/admin/Dashboard.vue')
            },
            {
              path: 'users',
              name: 'admin-users',
              component: () => import('@/views/admin/UserManagement.vue')
            },
            {
              path: 'posts',
              name: 'admin-posts',
              component: () => import('@/views/admin/PostManagement.vue')
            },
            {
              path: 'comments',
              name: 'admin-comments',
              component: () => import('@/views/admin/CommentManagement.vue')
            }
          ]
        },
        {
          path: '/ai-chat',
          name: 'ai-chat',
          component: () => import('@/views/AIChat.vue'),
          meta: { 
            requiresAuth: true,
            title: 'AI交流'
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
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '学术博客'} - 博客系统`

  const userStore = useUserStore()
  
  try {
    // 确保用户状态已初始化
    if (!userStore.initialized) {
      await userStore.initialize()
    }

    // 检查是否需要登录
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      return next({ path: '/login', query: { redirect: to.fullPath } })
    } 
    // 检查是否需要管理员权限
    else if (to.meta.requiresAdmin) {
      const roles = userStore.userInfo?.roles || []
      const isAdmin = roles.some(role => role === 'ROLE_ADMIN' || role === '管理员')
      
      if (!isAdmin) {
        ElMessage.warning('您没有管理员权限，无法访问此页面')
        return next('/')
      }
    }
    
    next()
  } catch (error) {
    console.error('路由守卫初始化失败:', error)
    if (to.meta.requiresAuth) {
      return next('/login')
    }
    next()
  }
})

export default router
