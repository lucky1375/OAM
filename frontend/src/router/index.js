import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'

Vue.use(VueRouter)

// 避免重复导航到同一路由时抛出 NavigationDuplicated 告警
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(err => err)
}
const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace (location) {
  return originalReplace.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/UserManagement.vue'),
        meta: { title: '账号管理', permission: 'user:view' }
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/permission/PermissionManagement.vue'),
        meta: { title: '权限管理', permission: 'user:view' }
      }
    ]
  },
  {
    path: '*',
    redirect: '/'
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

router.beforeEach((to, _from, next) => {
  const isLoggedIn = store.getters.isLoggedIn

  if (to.meta.public) {
    // 已登录访问登录页 → 跳回首页
    if (to.path === '/login' && isLoggedIn) {
      next('/')
    } else {
      next()
    }
    return
  }

  // 未登录 → 登录页
  if (!isLoggedIn) {
    next('/login')
    return
  }

  // 无对应权限 → 回首页
  if (to.meta.permission && !store.getters.hasPermission(to.meta.permission)) {
    next('/')
    return
  }

  next()
})

export default router
