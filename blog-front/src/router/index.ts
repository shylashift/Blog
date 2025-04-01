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

router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title} - 我的博客`

  // 检查是否需要登录
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
