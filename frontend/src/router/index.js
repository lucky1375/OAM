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
    // 移动端 H5 打卡页，独立全屏布局，不套用后台 Layout
    path: '/h5/attendance',
    name: 'AttendanceH5',
    component: () => import('@/views/attendance/AttendanceH5.vue'),
    meta: { title: '移动考勤', permission: 'attendance:view' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/home',
    children: [
      // 工作台
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'attendance',
        name: 'AttendanceClock',
        component: () => import('@/views/attendance/AttendanceClock.vue'),
        meta: { title: '考勤打卡', permission: 'attendance:view' }
      },
      {
        path: 'suggestion',
        name: 'Suggestion',
        component: () => import('@/views/suggestion/Suggestion.vue'),
        meta: { title: '建议反馈', permission: 'suggestion:view' }
      },
      // 审批管理
      {
        path: 'workflow',
        name: 'Workflow',
        component: () => import('@/views/workflow/Workflow.vue'),
        meta: { title: '流程审批', permission: 'workflow:view' }
      },
      {
        path: 'expense',
        name: 'Expense',
        component: () => import('@/views/expense/Expense.vue'),
        meta: { title: '报销管理', permission: 'expense:view' }
      },
      // 协作交流
      {
        path: 'forum',
        name: 'Forum',
        component: () => import('@/views/forum/Forum.vue'),
        meta: { title: '企业论坛', permission: 'forum:view' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/announcement/Announcement.vue'),
        meta: { title: '公告通知', permission: 'announcement:view' }
      },
      // 日程会议
      {
        path: 'calendar',
        name: 'Calendar',
        component: () => import('@/views/calendar/Calendar.vue'),
        meta: { title: '日程安排', permission: 'calendar:view' }
      },
      {
        path: 'meeting',
        name: 'Meeting',
        component: () => import('@/views/meeting/Meeting.vue'),
        meta: { title: '会议管理', permission: 'meeting:view' }
      },
      // 任务协作
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/Task.vue'),
        meta: { title: '任务协作', permission: 'task:view' }
      },
      // 组织架构
      {
        path: 'org-overview',
        name: 'OrgOverview',
        component: () => import('@/views/org/OrgOverview.vue'),
        meta: { title: '组织架构总览', permission: ['organization:view', 'department:view'] }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/department/Department.vue'),
        meta: { title: '部门管理', permission: 'department:view' }
      },
      {
        path: 'position',
        name: 'Position',
        component: () => import('@/views/position/Position.vue'),
        meta: { title: '岗位管理', permission: 'position:view' }
      },
      {
        path: 'personnel',
        name: 'Personnel',
        component: () => import('@/views/personnel/Personnel.vue'),
        meta: { title: '人员管理', permission: 'personnel:view' }
      },
      // 系统管理
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/UserManagement.vue'),
        meta: { title: '账号管理', permission: 'user:view' }
      },
      {
        path: 'role-permission',
        name: 'RolePermission',
        component: () => import('@/views/role/RolePermission.vue'),
        meta: { title: '角色权限', permission: 'role:view' }
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
