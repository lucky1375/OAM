<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">
        <span class="brand-icon">🗂</span>
        办公自动化管理系统
      </div>
      <el-menu
        class="menu"
        mode="horizontal"
        background-color="#2e5bff"
        text-color="rgba(255,255,255,0.85)"
        active-text-color="#ffffff"
        router
      >
        <template v-for="item in visibleMenus">
          <!-- 普通一级菜单项 -->
          <el-menu-item
            v-if="!item.children"
            :key="item.path"
            :index="item.path"
          >{{ item.label }}</el-menu-item>
          <!-- 含 children 的父菜单：hover 展示子项 -->
          <el-submenu v-else :key="item.label" :index="item.label">
            <template slot="title">
              {{ item.label }}
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.path"
              :index="child.path"
            >{{ child.label }}</el-menu-item>
          </el-submenu>
        </template>
      </el-menu>
      <div class="user-area">
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
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapGetters(['nickname', 'hasPermission']),
    allMenus() {
      // 扁平项：permission 为空表示无需权限；
      // 后续新增业务模块（公告、审批、日程等）时在此补充菜单项。
      return [
        { path: '/home', label: '首页', permission: '' },
        { path: '/users', label: '账号管理', permission: 'user:view' },
        { path: '/permissions', label: '权限管理', permission: 'user:view' }
      ]
    },
    visibleMenus() {
      return this.allMenus
        .map(item => this.filterItem(item))
        .filter(item => item !== null)
    }
  },
  methods: {
    // 判断单个菜单项（可能含 children）是否对当前用户可见
    filterItem(item) {
      if (!item.children) {
        // 叶子项：无 permission 直接显示，否则校验
        if (!item.permission || this.hasPermission(item.permission)) {
          return item
        }
        return null
      }
      // 父项：permission 为数组，任一命中才可能显示；子项逐个过滤
      const visibleChildren = item.children
        .map(child => this.filterItem(child))
        .filter(child => child !== null)
      if (visibleChildren.length === 0) {
        return null
      }
      // 父项本身无需单独显示——只要存在可见子项即展示
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
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #2e5bff 0%, #1a3dd4 100%);
  color: #fff;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(46, 91, 255, 0.25);
}
.brand {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-icon {
  font-size: 22px;
}
/* 子菜单下拉面板也用蓝色 */
.menu >>> .el-submenu__title {
  background-color: transparent !important;
}
.menu >>> .el-menu-item,
.menu >>> .el-submenu__title {
  border-bottom: 2px solid transparent;
}
.menu >>> .el-menu-item.is-active {
  border-bottom-color: #ffffff;
  background-color: rgba(255, 255, 255, 0.12) !important;
}
/* 下拉弹层（hover 时出现） */
.menu >>> .el-submenu .el-menu {
  background-color: #ffffff !important;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border: none;
  padding: 6px 0;
}
.menu >>> .el-submenu .el-menu-item {
  background-color: #ffffff !important;
  color: #1f2937 !important;
  border-bottom: none !important;
  padding: 0 20px;
}
.menu >>> .el-submenu .el-menu-item:hover {
  background-color: #eef2ff !important;
  color: #2e5bff !important;
}
.menu >>> .el-submenu .el-menu-item.is-active {
  background-color: #eef2ff !important;
  color: #2e5bff !important;
  font-weight: 500;
}
.user-area {
  cursor: pointer;
}
.user-name {
  color: #fff;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.main {
  background: #f5f7fa;
}
</style>
