<template>
  <div class="permission-management">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>权限管理</span>
        <div class="toolbar">
          <span class="label">选择用户：</span>
          <el-select
            v-model="selectedUserId"
            style="width: 220px"
            filterable
            placeholder="选择要授权的用户"
            @change="loadAssigned"
          >
            <el-option
              v-for="u in users"
              :key="u.id"
              :label="u.nickname + '（' + u.username + '）'"
              :value="u.id"
            />
          </el-select>
          <el-button @click="selectAll" :disabled="!canEdit">全选</el-button>
          <el-button @click="clearAll" :disabled="!canEdit">清空</el-button>
          <el-button type="primary" icon="el-icon-check" :loading="saving" :disabled="!canEdit" @click="handleSave">
            保存授权
          </el-button>
        </div>
      </div>

      <el-alert
        v-if="!canEdit"
        title="管理员默认拥有全部权限，无需分配。"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
      />

      <div ref="treeContainer" :style="canEdit ? '' : 'pointer-events:none;opacity:.6'">
        <el-tree
          ref="tree"
          :data="treeData"
          show-checkbox
          node-key="id"
          :expand-on-click-node="true"
          :default-checked-keys="defaultChecked"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { getPermissionTree, getAssignedPermissions, updatePermissions, listUsers } from '@/api/modules/permission'

export default {
  name: 'PermissionManagement',
  data() {
    return {
      loading: false,
      saving: false,
      users: [],
      selectedUserId: null,
      treeData: [],
      defaultChecked: [],
      editingAdmin: false
    }
  },
  computed: {
    canEdit() {
      return !!this.selectedUserId && !this.editingAdmin
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      this.loading = true
      try {
        const [treeRes, userRes] = await Promise.all([getPermissionTree(), listUsers()])
        this.treeData = this.toTree(treeRes.data)
        this.users = userRes.data
        const target = this.users.find(u => u.role !== 'ADMIN') || this.users[0]
        if (target) {
          this.selectedUserId = target.id
          this.loadAssigned(target.id)
        }
      } finally {
        this.loading = false
      }
    },
    // 后端功能树 → el-tree 数据（叶节点 id 即权限码）
    toTree(modules) {
      return (modules || []).map(m => this.toTreeNode(m))
    },
    // 递归构造树节点：含 children 的模块作为分组节点，叶模块挂操作节点
    toTreeNode(m) {
      if (m.children && m.children.length) {
        return {
          id: 'group:' + m.module,
          label: m.name,
          children: m.children.map(c => this.toTreeNode(c))
        }
      }
      return {
        id: m.module,
        label: m.name,
        children: (m.operations || []).map(op => ({
          id: m.module + ':' + op.code,
          label: op.name
        }))
      }
    },
    allLeafCodes() {
      const codes = []
      const walk = nodes => nodes.forEach(n => {
        if (n.children && n.children.length) walk(n.children)
        else codes.push(n.id)
      })
      walk(this.treeData)
      return codes
    },
    async loadAssigned(userId) {
      if (!userId) return
      this.editingAdmin = false
      this.defaultChecked = []
      try {
        const res = await getAssignedPermissions(userId)
        this.editingAdmin = res.data.role === 'ADMIN'
        this.$nextTick(() => {
          this.$refs.tree && this.$refs.tree.setCheckedKeys(res.data.permissions || [])
        })
      } catch (e) {
        this.$refs.tree && this.$refs.tree.setCheckedKeys([])
      }
    },
    selectAll() {
      this.$refs.tree.setCheckedKeys(this.allLeafCodes())
    },
    clearAll() {
      this.$refs.tree.setCheckedKeys([])
    },
    handleSave() {
      const codes = this.$refs.tree.getCheckedKeys(true)
      this.saving = true
      updatePermissions(this.selectedUserId, codes)
        .then(() => {
          this.$message.success('授权成功')
        })
        .finally(() => {
          this.saving = false
        })
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar .label {
  color: #606266;
}
</style>
