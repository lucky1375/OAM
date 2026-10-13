<template>
  <div class="position-management">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>岗位管理</span>
        <div>
          <el-button type="primary" icon="el-icon-plus" v-if="hasPerm('position:create')" @click="openCreate">新增岗位</el-button>
        </div>
      </div>

      <div class="query-bar">
        <el-input v-model.trim="query.keyword" placeholder="按岗位名称搜索" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
        <el-select v-model="query.departmentId" placeholder="全部部门" clearable style="width: 220px" filterable>
          <el-option
            v-for="d in flatDepts"
            :key="d.id"
            :label="d.pathNames"
            :value="d.id"
          />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="positions" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="岗位名称" min-width="140" />
        <el-table-column prop="departmentName" label="归属部门" min-width="180">
          <template slot-scope="{ row }">{{ row.departmentName || '未分配' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="岗位描述" min-width="220">
          <template slot-scope="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" v-if="hasPerm('position:update')" @click="openEdit(row)">编辑</el-button>
            <el-button
              type="text"
              class="danger"
              v-if="hasPerm('position:delete')"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="480px" @closed="resetForm">
      <el-form ref="posForm" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="岗位名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="归属部门">
          <el-cascader
            v-model="form.departmentPath"
            :options="deptCascader"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: true, checkStrictly: true }"
            placeholder="留空表示未分配"
            clearable
            filterable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-text="form.enabled ? '启用' : '停用'" />
        </el-form-item>
        <el-form-item label="岗位描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填" maxlength="200" show-word-limit />
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
import { listPositions, createPosition, updatePosition, deletePosition } from '@/api/modules/position'
import { getDepartmentFlat, getDepartmentTree } from '@/api/modules/department'

export default {
  name: 'Position',
  data() {
    return {
      loading: false,
      saving: false,
      positions: [],
      flatDepts: [],
      deptCascader: [],
      query: { keyword: '', departmentId: null },
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增岗位',
      editNodeId: null,
      form: {
        name: '',
        departmentPath: [],
        enabled: true,
        description: ''
      },
      formRules: {
        name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchDepartments()
    this.fetchPositions()
  },
  methods: {
    hasPerm(code) { return this.$store.getters.hasPermission(code) },

    async fetchDepartments() {
      try {
        const [flatRes, treeRes] = await Promise.all([getDepartmentFlat(), getDepartmentTree()])
        this.flatDepts = flatRes.data
        this.deptCascader = treeRes.data
      } catch (e) { /* 部门为空时忽略 */ }
    },

    async fetchPositions() {
      this.loading = true
      try {
        const params = {}
        if (this.query.keyword) params.keyword = this.query.keyword
        if (this.query.departmentId) params.departmentId = this.query.departmentId
        const res = await listPositions(params)
        this.positions = res.data
      } finally {
        this.loading = false
      }
    },

    handleQuery() { this.fetchPositions() },
    handleReset() {
      this.query = { keyword: '', departmentId: null }
      this.fetchPositions()
    },

    openCreate() {
      this.isEdit = false
      this.dialogTitle = '新增岗位'
      this.form.departmentPath = []
      this.dialogVisible = true
    },

    openEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑岗位'
      this.editNodeId = row.id
      // 计算级联路径：找到该部门的 pathNames 并映射为 id 链
      const pathIds = this.findDeptPathIds(row.departmentId)
      this.form = {
        name: row.name,
        departmentPath: pathIds,
        enabled: row.enabled,
        description: row.description || ''
      }
      this.dialogVisible = true
    },

    findDeptPathIds(targetId) {
      if (!targetId) return []
      return this.flatDepts
        .filter(d => d.id === targetId)
        .map(d => {
          // pathNames 形如 "集团 / 研发中心"，反推 id 链：从 flatDepts 里找每一级
          const segments = d.pathNames.split(' / ')
          const path = []
          let currentChildren = this.deptCascader
          for (const seg of segments) {
            const match = currentChildren.find(n => n.name === seg)
            if (!match) break
            path.push(match.id)
            currentChildren = match.children || []
          }
          return path
        })[0] || []
    },

    resetForm() {
      this.form = { name: '', departmentPath: [], enabled: true, description: '' }
      this.editNodeId = null
      this.$refs.posForm && this.$refs.posForm.clearValidate()
    },

    async handleSave() {
      this.$refs.posForm.validate(async valid => {
        if (!valid) return
        const path = this.form.departmentPath || []
        const departmentId = path.length > 0 ? path[path.length - 1] : null
        const payload = {
          name: this.form.name,
          departmentId,
          enabled: this.form.enabled,
          description: this.form.description
        }
        this.saving = true
        try {
          if (this.isEdit) {
            await updatePosition(this.editNodeId, payload)
            this.$message.success('更新成功')
          } else {
            await createPosition(payload)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          this.fetchPositions()
        } catch (e) { /* 拦截器已提示 */ }
        finally {
          this.saving = false
        }
      })
    },

    handleDelete(row) {
      this.$confirm(`确定删除岗位「${row.name}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await deletePosition(row.id)
        this.$message.success('删除成功')
        this.fetchPositions()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.query-bar { display: flex; align-items: center; margin-bottom: 12px; gap: 8px; }
.danger { color: #f56c6c; }
</style>
