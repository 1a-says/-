import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由配置
 * 区分管理员和普通用户的访问权限
 */
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin',
    name: 'AdminHome',
    component: () => import('@/views/AdminHome.vue'),
    meta: {
      title: '管理员首页',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/book-management',
    name: 'BookManagement',
    component: () => import('@/views/BookManagement.vue'),
    meta: {
      title: '图书管理',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/user-management',
    name: 'UserManagement',
    component: () => import('@/views/UserManagement.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/borrow-management',
    name: 'BorrowManagement',
    component: () => import('@/views/BorrowManagement.vue'),
    meta: {
      title: '借阅管理',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/borrow-query',
    name: 'AdminBorrowQuery',
    component: () => import('@/views/AdminBorrowQuery.vue'),
    meta: {
      title: '借阅查询',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/borrow-statistics',
    name: 'BorrowStatistics',
    component: () => import('@/views/BorrowStatistics.vue'),
    meta: {
      title: '借阅统计',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/admin/system-config',
    name: 'SystemConfig',
    component: () => import('@/views/SystemConfig.vue'),
    meta: {
      title: '系统配置',
      requiresAuth: true,
      role: 'admin'
    }
  },
  {
    path: '/user',
    name: 'UserHome',
    component: () => import('@/views/UserHome.vue'),
    meta: {
      title: '用户首页',
      requiresAuth: true,
      role: 'user'
    }
  },
  {
    path: '/user/borrow-query',
    name: 'UserBorrowQuery',
    component: () => import('@/views/UserBorrowQuery.vue'),
    meta: {
      title: '我的借阅',
      requiresAuth: true,
      role: 'user'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫 - 权限验证
 */
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 高校图书馆管理系统` : '高校图书馆管理系统'

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next('/login')
      return
    }

    // 验证角色权限
    if (to.meta.role && to.meta.role !== userStore.userRole) {
      // 角色不匹配，跳转到对应角色的首页
      if (userStore.userRole === 'admin') {
        next('/admin')
      } else {
        next('/user')
      }
      return
    }
  }

  // 已登录用户访问登录页，自动跳转到对应首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    if (userStore.userRole === 'admin') {
      next('/admin')
    } else {
      next('/user')
    }
    return
  }

  next()
})

export default router
