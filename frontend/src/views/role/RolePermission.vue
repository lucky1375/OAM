<template>
  <div class="role-permission">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>角色权限配置</span>
        <el-alert type="info" :closable="false" show-icon style="margin:0;flex:1;margin-left:16px">
          ADMIN 角色默认拥有全部权限（含未来新增的模块）；USER 角色仅拥有下方勾选的权限。
        </el-alert>
      </div>

      <!-- Tab 切换角色 -->
      <el-tabs v-model="activeRole" @tab-click="handleRoleChange">
        <el-tab-pane
          v-for="r in roles"
          :key="r.role"
          :label="r.label + (r.admin ? '（全部权限）' : '')"
          :name="r.role"
        >
          <div v-if="r.admin" class="admin-tip">
            <el-icon style="vertical-align:-3px;color:#10b981"><i class="el-icon-circle-check"></i></el-icon>
            <span>管理员角色自动拥有所有权限，无需手动配置。</span>
          </div>

          <template v-else>
            <div class="perm-meta">
              <span>当前已选权限数：<b style="color:#2e5bff">{{ selectedCodes.length }}</b> / {{ totalCodes }}</span>
              <div>
                <el-button type="text" @click="selectAll">全选</el-button>
                <el-button type="text" @click="clearAll">清空</el-button>
                <el-button type="text" @click="resetToDefaults">恢复默认</el-button>
              </div>
            </div>

            <div v-for="mod in groupedCatalog" :key="mod.module" class="perm-group">
              <div class="group-header">
                <el-checkbox
                  :value="isModuleAllSelected(mod)"
                  :indeterminate="isModulePartialSelected(mod)"
                  @change="toggleModule(mod, $event)"
                >
                  <b>{{ mod.label }}</b>
                </el-checkbox>
                <span class="group-count">{{ mod.ops.filter(o => selectedCodes.includes(o.code)).length }}/{{ mod.ops.length }}</span>
              </div>
              <div class="op-grid">
                <el-checkbox
                  v-for="op in mod.ops"
                  :key="op.code"
                  :label="op.code"
                  v-model="selectedCodes"
                  :disabled="r.admin"
                >
                  {{ op.label }}
                </el-checkbox>
              </div>
            </div>

            <el-divider></el-divider>
            <div style="text-align:right">
              <el-button type="primary" :loading="saving" :disabled="r.admin" @click="handleSave">保存配置</el-button>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getRoles, updateRolePermissions } from '@/api/modules/role'

export default {
  name: 'RolePermission',
  data() {
    return {
      roles: [],
      activeRole: 'USER',
      selectedCodes: [],
      allCodes: [],
      catalogGroups: [], // 原样返回后端 PermissionCatalog 的树形
      saving: false
    }
  },
  computed: {
    totalCodes() { return this.allCodes.length },
    groupedCatalog() {
      // catalogGroups: [{ module, label, ops:[{code,label}] }]
      return this.catalogGroups
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      try {
        const res = await getRoles()
        this.roles = res.data
        const userRole = this.roles.find(r => r.role === 'USER')
        if (userRole) this.selectedCodes = [...(userRole.permissions || [])]
        const adminRole = this.roles.find(r => r.role === 'ADMIN')
        if (adminRole) this.allCodes = adminRole.permissions || []
        // 初始化 catalog 分组：用 adminRole.permissions 按 catalog 模块切分
        this.catalogGroups = this.buildGroups(this.allCodes)
      } catch (e) { /* 拦截器提示 */ }
    },

    // 用 PermissionCatalog 的结构来分组——后端 roles 接口已返回 catalog 结构
    // 但 roles 接口当前只返回扁平 permissions 列表，这里简单按 module 前缀切分
    buildGroups(codes) {
      const map = {}
      for (const c of codes) {
        const module = c.split(':')[0]
        const op = c.split(':')[1]
        if (!map[module]) map[module] = { module, label: this.moduleLabel(module), ops: [] }
        map[module].ops.push({ code: c, label: op })
      }
      return Object.values(map)
    },
    moduleLabel(m) {
      const map = {
        workbench: '工作台',
        attendance: '考勤打卡',
        suggestion: '建议反馈',
        approval: '审批管理',
        workflow: '流程审批',
        expense: '报销管理',
        collaboration: '协作交流',
        forum: '企业论坛',
        announcement: '公告通知',
        schedule: '日程会议',
        calendar: '日程安排',
        meeting: '会议管理',
        task: '任务协作',
        organization: '组织架构',
        department: '部门管理',
        position: '岗位管理',
        user: '账号管理'
      }
      return map[m] || m
    },

    handleRoleChange(tab) {
      const r = this.roles.find(x => x.role === tab.name)
      if (r && !r.admin) this.selectedCodes = [...(r.permissions || [])]
    },

    isModuleAllSelected(mod) {
      return mod.ops.every(o => this.selectedCodes.includes(o.code))
    },
    isModulePartialSelected(mod) {
      const hit = mod.ops.filter(o => this.selectedCodes.includes(o.code)).length
      return hit > 0 && hit < mod.ops.length
    },
    toggleModule(mod, checked) {
      const s = new Set(this.selectedCodes)
      if (checked) mod.ops.forEach(o => s.add(o.code))
      else mod.ops.forEach(o => s.delete(o.code))
      this.selectedCodes = Array.from(s)
    },

    selectAll() { this.selectedCodes = [...this.allCodes] },
    clearAll() { this.selectedCodes = [] },
    async resetToDefaults() {
      // 恢复到后端 RoleService.USER_DEFAULT_PERMISSIONS 种子
      try {
        const res = await getRoles()
        const user = res.data.find(r => r.role === 'USER')
        this.selectedCodes = [...(user.permissions || [])]
      } catch (e) {}
    },

    async handleSave() {
      this.saving = true
      try {
        await updateRolePermissions(this.activeRole, this.selectedCodes)
        this.$message.success('角色权限已更新')
        await this.fetchData()
      } catch (e) {}
      finally { this.saving = false }
    }
  }
}
</script>

<style scoped>
.card-header { display: flex; align-items: center; }
.admin-tip {
  padding: 20px; color: #10b981; background: #ecfdf5; border-radius: 8px;
  display: flex; gap: 8px; align-items: center;
}
.perm-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; color: #6b7280; }
.perm-meta b { font-size: 15px; }
.perm-group { margin-bottom: 18px; }
.group-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; background: #f8fafc; border-radius: 6px; margin-bottom: 8px;
}
.group-count { font-size: 12px; color: #9ca3af; }
.op-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 6px 16px;
  padding: 0 4px;
}
</style>
