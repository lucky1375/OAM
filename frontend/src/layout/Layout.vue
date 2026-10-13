<template>
  <el-container class="layout">
    <!-- 左侧边栏 -->
    <el-aside :width="collapse ? '64px' : '220px'" class="sidebar">
      <div class="brand">
        <span class="brand-icon">🗂</span>
        <span v-if="!collapse" class="brand-text">OAM</span>
      </div>
      <el-menu
        class="sidebar-menu"
        mode="vertical"
        :collapse="collapse"
        :collapse-on-click="false"
        :unique-opened="true"
        background-color="#1f2d3d"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#ffffff"
        router
      >
        <template v-for="group in visibleMenus">
          <!-- 无子项的叶子分组：直接菜单项 -->
          <el-menu-item
            v-if="!group.children"
            :key="group.path"
            :index="group.path"
          >
            <i :class="group.icon || 'el-icon-menu'"></i>
            <span slot="title">{{ group.label }}</span>
          </el-menu-item>
          <!-- 有子项的分组：el-submenu -->
          <el-submenu v-else :key="group.label" :index="group.label">
            <template slot="title">
              <i :class="group.icon || 'el-icon-folder'"></i>
              <span>{{ group.label }}</span>
            </template>
            <el-menu-item
              v-for="child in group.children"
              :key="child.path"
              :index="child.path"
            >
              <i :class="child.icon || 'el-icon-document'"></i>
              <span slot="title">{{ child.label }}</span>
            </el-menu-item>
          </el-submenu>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <i
            class="collapse-btn"
            :class="collapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
            @click="collapse = !collapse"
          ></i>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              <i class="el-icon-user-solid"></i>
              {{ nickname }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  data() {
    return {
      collapse: false
    }
  },
  computed: {
    ...mapGetters(['nickname', 'hasPermission']),
    currentTitle() {
      return this.$route.meta.title || ''
    },
    // 菜单按功能域分组，permission 为空表示无需权限
    allMenus() {
      return [
        { path: '/home', label: '工作台', icon: 'el-icon-s-home', permission: '' },
        {
          label: '工作台', icon: 'el-icon-office-building',
          permission: ['attendance:view', 'suggestion:view'],
          children: [
            { path: '/attendance', label: '考勤打卡', icon: 'el-icon-time', permission: 'attendance:view' },
            { path: '/suggestion', label: '建议反馈', icon: 'el-icon-chat-dot-round', permission: 'suggestion:view' }
          ]
        },
        {
          label: '审批管理', icon: 'el-icon-s-check',
          permission: ['workflow:view', 'expense:view'],
          children: [
            { path: '/workflow', label: '流程审批', icon: 'el-icon-s-platform', permission: 'workflow:view' },
            { path: '/expense', label: '报销管理', icon: 'el-icon-wallet', permission: 'expense:view' }
          ]
        },
        {
          label: '协作交流', icon: 'el-icon-chat-line-square',
          permission: ['forum:view', 'announcement:view'],
          children: [
            { path: '/forum', label: '企业论坛', icon: 'el-icon-s-comment', permission: 'forum:view' },
            { path: '/announcement', label: '公告通知', icon: 'el-icon-bell', permission: 'announcement:view' }
          ]
        },
        {
          label: '日程会议', icon: 'el-icon-date',
          permission: ['calendar:view', 'meeting:view'],
          children: [
            { path: '/calendar', label: '日程安排', icon: 'el-icon-calendar', permission: 'calendar:view' },
            { path: '/meeting', label: '会议管理', icon: 'el-icon-video-camera', permission: 'meeting:view' }
          ]
        },
        { path: '/task', label: '任务协作', icon: 'el-icon-s-order', permission: 'task:view' },
        {
          label: '组织架构', icon: 'el-icon-s-custom',
          permission: ['organization:view', 'department:view', 'position:view', 'personnel:view'],
          children: [
            { path: '/org-overview', label: '组织架构总览', icon: 'el-icon-share', permission: 'department:view' },
            { path: '/department', label: '部门管理', icon: 'el-icon-office-building', permission: 'department:view' },
            { path: '/position', label: '岗位管理', icon: 'el-icon-postcard', permission: 'position:view' },
            { path: '/personnel', label: '人员管理', icon: 'el-icon-user-solid', permission: 'personnel:view' }
          ]
        },
        {
          label: '系统管理', icon: 'el-icon-s-tools',
          permission: ['user:view', 'role:view'],
          children: [
            { path: '/users', label: '账号管理', icon: 'el-icon-user', permission: 'user:view' },
            { path: '/role-permission', label: '角色权限', icon: 'el-icon-lock', permission: 'role:view' }
          ]
        }
      ]
    },
    visibleMenus() {
      return this.allMenus
        .map(item => this.filterItem(item))
        .filter(item => item !== null)
    }
  },
  methods: {
    // 过滤菜单项（含 children 的分组逐个过滤子项）
    filterItem(item) {
      if (!item.children) {
        if (!item.permission || this.hasPermission(item.permission)) {
          return item
        }
        return null
      }
      // 父项 permission 为数组，任一命中才可能显示；子项逐个过滤
      const visibleChildren = item.children
        .map(child => this.filterItem(child))
        .filter(child => child !== null)
      if (visibleChildren.length === 0) {
        return null
      }
      return { ...item, children: visibleChildren }
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定退出登录吗？', '提示', {
          confirmButtonText: '退出',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
        }).catch(() => {})
      }
    }
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
/* ========== 侧边栏 ========== */
.sidebar {
  background-color: #1f2d3d;
  transition: width 0.2s;
  overflow: hidden;
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.brand-icon {
  font-size: 24px;
}
.sidebar-menu {
  border-right: none;
}
.sidebar-menu >>> .el-menu-item,
.sidebar-menu >>> .el-submenu__title {
  height: 44px;
  line-height: 44px;
  font-size: 14px;
}
.sidebar-menu >>> .el-menu-item:hover,
.sidebar-menu >>> .el-submenu__title:hover {
  background-color: #2d3e53 !important;
}
.sidebar-menu >>> .el-menu-item.is-active {
  background-color: #2e5bff !important;
  color: #fff !important;
}
.sidebar-menu >>> .el-menu-item i,
.sidebar-menu >>> .el-submenu__title i {
  margin-right: 8px;
  font-size: 16px;
}
.sidebar-menu >>> .el-submenu .el-menu {
  background-color: #1a2634 !important;
}
.sidebar-menu >>> .el-submenu .el-menu-item {
  min-width: 0;
}

/* ========== 顶部 Header ========== */
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  padding: 0 24px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  padding: 4px;
  border-radius: 4px;
}
.collapse-btn:hover {
  background: #f0f2f5;
  color: #2e5bff;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-name {
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.user-name:hover {
  color: #2e5bff;
}

/* ========== 主内容区 ========== */
.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
