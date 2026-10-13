<template>
  <div class="department-management">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>部门管理</span>
        <div>
          <el-button type="primary" icon="el-icon-plus" v-if="hasPerm('department:create')" @click="openCreate(null)">新增根部门</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="treeData"
        border
        stripe
        row-key="id"
        :default-expand-all="true"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="部门名称" min-width="160">
          <template slot-scope="{ row }">
            <span class="dept-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pathNames" label="完整路径" min-width="260">
          <template slot-scope="{ row }">{{ row.pathNames || row.name }}</template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="80" align="center">
          <template slot-scope="{ row }">第 {{ row.level + 1 }} 级</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序号" width="80" align="center" />
        <el-table-column prop="leaderName" label="负责人" min-width="100">
          <template slot-scope="{ row }">{{ row.leaderName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" min-width="120">
          <template slot-scope="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140">
          <template slot-scope="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" v-if="hasPerm('department:create')" @click="openCreate(row)">添加子部门</el-button>
            <el-button type="text" v-if="hasPerm('department:update')" @click="openEdit(row)">编辑</el-button>
            <el-button
              type="text"
              class="danger"
              v-if="hasPerm('department:delete') && !row.children"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px" @closed="resetForm">
      <el-form ref="deptForm" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="上级部门">
          <el-cascader
            v-model="form.parentPath"
            :options="cascaderOptions"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: true, checkStrictly: true }"
            placeholder="留空表示根部门"
            clearable
            filterable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model.trim="form.leaderName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model.trim="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-text="form.enabled ? '启用' : '停用'" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填，最多500字" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDepartmentTree, createDepartment, updateDepartment, deleteDepartment } from '@/api/modules/department'

export default {
  name: 'Department',
  data() {
    return {
      loading: false,
      saving: false,
      treeData: [],
      cascaderOptions: [],
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增根部门',
      editNodeId: null,
      form: {
        parentPath: [],
        name: '',
        sort: 0,
        leaderName: '',
        phone: '',
        enabled: true,
        remark: ''
      },
      formRules: {
        name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchTree()
  },
  methods: {
    hasPerm(code) { return this.$store.getters.hasPermission(code) },

    async fetchTree() {
      this.loading = true
      try {
        const res = await getDepartmentTree()
        this.treeData = res.data
        // 级联选择器专用：过滤掉当前编辑节点及其后代（防循环移动）
        this.cascaderOptions = this.excludeNodeAndDescendants(this.treeData, this.editNodeId)
      } finally {
        this.loading = false
      }
    },

    // 从树中移除 targetId 及其所有后代（编辑时用，防循环）
    excludeNodeAndDescendants(tree, targetId) {
      if (targetId == null) return tree
      return tree
        .filter(n => n.id !== targetId)
        .map(n => ({ ...n, children: this.excludeNodeAndDescendants(n.children || [], targetId) }))
        .filter(n => (n.children && n.children.length > 0) || n.children === undefined)
    },

    openCreate(parentRow) {
      this.isEdit = false
      this.dialogTitle = parentRow ? `新增子部门（${parentRow.name} 下）` : '新增根部门'
      this.editNodeId = null
      // parentPath 为级联路径，直接选 parentRow 自身的 id 就行，因为 emitPath 会让它成为路径末节点
      const initialPath = parentRow ? [parentRow.id] : []
      this.form.parentPath = initialPath
      this.form.sort = (parentRow ? (parentRow.children ? parentRow.children.length : 0) : this.treeData.length)
      this.dialogVisible = true
    },

    openEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑部门'
      this.editNodeId = row.id
      // 计算 row 自己的级联路径（需要重建）
      const path = this.findPath(this.treeData, row.id)
      this.form.parentPath = path
      this.form.name = row.name
      this.form.sort = row.sort
      this.form.leaderName = row.leaderName || ''
      this.form.phone = row.phone || ''
      this.form.enabled = row.enabled
      this.form.remark = row.remark || ''
      this.dialogVisible = true
    },

    // 从树中找节点 id 的完整 path（从根到父级）
    findPath(tree, targetId, acc = []) {
      for (const n of tree) {
        if (n.id === targetId) return acc.slice() // 返回父级路径（不含自身）
        if (n.children && n.children.length) {
          const r = this.findPath(n.children, targetId, [...acc, n.id])
          if (r) return r
        }
      }
      return null
    },

    resetForm() {
      this.form = {
        parentPath: [],
        name: '',
        sort: 0,
        leaderName: '',
        phone: '',
        enabled: true,
        remark: ''
      }
      this.editNodeId = null
      this.$refs.deptForm && this.$refs.deptForm.clearValidate()
    },

    async handleSave() {
      this.$refs.deptForm.validate(async valid => {
        if (!valid) return
        // parentPath 是级联全路径，末节点即 parentId；若空数组则 parentId 为 null
        const parentPath = this.form.parentPath || []
        const parentId = parentPath.length > 0 ? parentPath[parentPath.length - 1] : null
        const payload = {
          name: this.form.name,
          parentId,
          sort: this.form.sort,
          leaderName: this.form.leaderName,
          phone: this.form.phone,
          enabled: this.form.enabled,
          remark: this.form.remark
        }
        this.saving = true
        try {
          if (this.isEdit) {
            await updateDepartment(this.editNodeId, payload)
            this.$message.success('更新成功')
          } else {
            await createDepartment(payload)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          await this.fetchTree()
        } catch (e) {
          // 错误已由拦截器提示
        } finally {
          this.saving = false
        }
      })
    },

    handleDelete(row) {
      this.$confirm(`确定删除部门「${row.name}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await deleteDepartment(row.id)
        this.$message.success('删除成功')
        await this.fetchTree()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex; align-items: center; justify-content: space-between;
}
.danger { color: #f56c6c; }
.dept-name { font-weight: 500; }
</style>
