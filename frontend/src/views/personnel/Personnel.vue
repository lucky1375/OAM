<template>
  <div class="personnel-management">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>人员管理</span>
        <div>
          <el-button type="primary" icon="el-icon-plus" v-if="hasPerm('department:create') || hasPerm('position:create')" @click="openCreate">新增人员</el-button>
        </div>
      </div>

      <div class="query-bar">
        <el-input v-model.trim="query.keyword" placeholder="姓名/工号/手机号" clearable style="width: 220px" @keyup.enter.native="handleQuery" />
        <el-select v-model="query.departmentId" placeholder="全部部门" clearable style="width: 220px" filterable>
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.pathNames || d.name" :value="d.id" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="employees" border stripe>
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="employeeNo" label="工号" min-width="100">
          <template slot-scope="{ row }">{{ row.employeeNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="性别" width="70" align="center">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="row.gender === 'M' ? 'primary' : 'danger'">{{ row.gender === 'M' ? '男' : '女' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="部门" min-width="160">
          <template slot-scope="{ row }">{{ row.departmentName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="positionName" label="岗位" min-width="140">
          <template slot-scope="{ row }">{{ row.positionName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120">
          <template slot-scope="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="username" label="账号" min-width="140">
          <template slot-scope="{ row }">
            <span v-if="row.username">{{ row.username }}
              <el-tag size="mini" :type="row.userRole === 'ADMIN' ? 'danger' : 'success'" style="margin-left:4px">
                {{ row.userRole === 'ADMIN' ? '管理员' : '普通' }}
              </el-tag>
            </span>
            <span v-else style="color:#9ca3af">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '在职' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="入职时间" min-width="160" />
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button
              type="text"
              class="danger"
              :disabled="row.userRole === 'ADMIN'"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" @closed="resetForm">
      <el-form ref="empForm" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model.trim="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号">
              <el-input v-model.trim="form.employeeNo" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" style="width:100%" placeholder="请选择" clearable>
                <el-option label="男" value="M" />
                <el-option label="女" value="F" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model.trim="form.phone" placeholder="用于生成初始密码后6位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model.trim="form.email" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门">
              <el-cascader
                v-model="form.departmentPath"
                :options="deptTree"
                :props="{ value: 'id', label: 'name', children: 'children', emitPath: true, checkStrictly: true }"
                clearable filterable style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位">
              <el-select v-model="form.positionId" clearable filterable style="width:100%" placeholder="请选择">
                <el-option v-for="p in positionOptions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.enabled" :active-text="form.enabled ? '在职' : '停用'" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 新增时账号自动生成 -->
        <template v-if="!isEdit">
          <el-divider content-position="left">系统账号（默认普通用户角色）</el-divider>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="账号" prop="username">
                <el-input v-model.trim="form.username" placeholder="留空自动生成：emp_工号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="初始密码">
                <el-input v-model="form.initialPassword" placeholder="留空=手机号后6位或123456" show-password />
              </el-form-item>
            </el-col>
          </el-row>
          <el-alert type="info" :closable="false" show-icon>
            新增员工后将自动创建登录账号，默认授予「普通用户」角色。
          </el-alert>
        </template>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listEmployees, createEmployee, updateEmployee, deleteEmployee } from '@/api/modules/employee'
import { getDepartmentFlat, getDepartmentTree } from '@/api/modules/department'
import { listPositions } from '@/api/modules/position'

export default {
  name: 'Personnel',
  data() {
    return {
      loading: false,
      saving: false,
      employees: [],
      deptOptions: [],
      deptTree: [],
      positionOptions: [],
      query: { keyword: '', departmentId: null },
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增人员',
      editId: null,
      form: {
        name: '', employeeNo: '', gender: '', phone: '', email: '',
        departmentPath: [], positionId: null, enabled: true,
        username: '', initialPassword: ''
      },
      formRules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchDeps()
    this.fetchList()
  },
  methods: {
    hasPerm(c) { return this.$store.getters.hasPermission(c) },
    async fetchDeps() {
      try {
        const [flatRes, treeRes, posRes] = await Promise.all([
          getDepartmentFlat(), getDepartmentTree(), listPositions()
        ])
        this.deptOptions = flatRes.data
        this.deptTree = treeRes.data
        this.positionOptions = posRes.data
      } catch (e) { /* ignore */ }
    },
    async fetchList() {
      this.loading = true
      try {
        const params = {}
        if (this.query.keyword) params.keyword = this.query.keyword
        if (this.query.departmentId) params.departmentId = this.query.departmentId
        const res = await listEmployees(params)
        this.employees = res.data
      } finally { this.loading = false }
    },
    handleQuery() { this.fetchList() },
    handleReset() {
      this.query = { keyword: '', departmentId: null }
      this.fetchList()
    },

    openCreate() {
      this.isEdit = false
      this.editId = null
      this.dialogTitle = '新增人员'
      this.dialogVisible = true
    },

    openEdit(row) {
      this.isEdit = true
      this.editId = row.id
      // 回填部门路径
      const path = row.departmentId ? this.findDeptPathIds(row.departmentId) : []
      this.form = {
        name: row.name,
        employeeNo: row.employeeNo || '',
        gender: row.gender || '',
        phone: row.phone || '',
        email: row.email || '',
        departmentPath: path,
        positionId: row.positionId,
        enabled: row.enabled,
        username: '',
        initialPassword: ''
      }
      this.dialogVisible = true
    },
    findDeptPathIds(targetId) {
      const found = this.deptOptions.find(d => d.id === targetId)
      if (!found) return []
      const segments = found.pathNames.split(' / ')
      const result = []
      let curChildren = this.deptTree
      for (const seg of segments) {
        const match = curChildren.find(n => n.name === seg)
        if (!match) break
        result.push(match.id)
        curChildren = match.children || []
      }
      return result
    },

    resetForm() {
      this.form = {
        name: '', employeeNo: '', gender: '', phone: '', email: '',
        departmentPath: [], positionId: null, enabled: true,
        username: '', initialPassword: ''
      }
      this.editId = null
      this.$refs.empForm && this.$refs.empForm.clearValidate()
    },

    async handleSave() {
      this.$refs.empForm.validate(async valid => {
        if (!valid) return
        const path = this.form.departmentPath || []
        const departmentId = path.length > 0 ? path[path.length - 1] : null
        const payload = {
          name: this.form.name,
          employeeNo: this.form.employeeNo,
          gender: this.form.gender,
          phone: this.form.phone,
          email: this.form.email,
          departmentId,
          positionId: this.form.positionId,
          enabled: this.form.enabled
        }
        if (!this.isEdit) {
          payload.username = this.form.username || null
          payload.initialPassword = this.form.initialPassword || null
        }
        this.saving = true
        try {
          if (this.isEdit) {
            await updateEmployee(this.editId, payload)
            this.$message.success('更新成功')
          } else {
            const r = await createEmployee(payload)
            this.$message.success('人员创建成功：账号 ' + r.data.username + '（默认普通用户角色）')
          }
          this.dialogVisible = false
          await this.fetchList()
          await this.fetchDeps()
        } catch (e) { /* 拦截器已提示 */ }
        finally { this.saving = false }
      })
    },

    handleDelete(row) {
      this.$confirm(`确定删除员工「${row.name}」？关联账号将一并删除。`, '提示', {
        confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        await deleteEmployee(row.id)
        this.$message.success('删除成功')
        this.fetchList()
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
