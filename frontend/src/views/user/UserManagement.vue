<template>
  <div class="user-management">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span>账号管理</span>
        <div>
          <el-button icon="el-icon-download" v-if="hasPerm('user:export')" @click="handleExport">导出</el-button>
          <el-button type="primary" icon="el-icon-plus" v-if="hasPerm('user:create')" @click="openCreate">新增账号</el-button>
        </div>
      </div>

      <div class="query-bar">
        <el-input v-model.trim="query.keyword" placeholder="请输入账号/昵称/手机号" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="users" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="登录账号" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="角色" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="createdBy" label="创建人" min-width="100" />
        <el-table-column prop="createdAt" label="创建时间" min-width="150" />
        <el-table-column prop="updatedBy" label="更新人" min-width="100">
          <template slot-scope="{ row }">{{ row.updatedBy || '-' }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="150">
          <template slot-scope="{ row }">{{ row.updatedAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" v-if="hasPerm('user:update')" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" v-if="hasPerm('user:resetpassword')" @click="openResetPassword(row)">重置密码</el-button>
            <el-button
              type="text"
              class="danger"
              v-if="hasPerm('user:delete')"
              :disabled="row.role === 'ADMIN'"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="480px" @closed="resetForm">
      <el-form ref="userForm" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="登录账号" prop="username" v-if="!isEdit">
          <el-input v-model.trim="form.username" placeholder="3-50位字母、数字或下划线" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model.trim="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="6-50位" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch
            v-model="form.enabled"
            :active-text="form.enabled ? '启用' : '停用'"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model.trim="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model.trim="form.email" placeholder="选填" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </span>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog title="重置密码" :visible.sync="pwdVisible" width="420px">
      <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="账号">{{ currentUser ? currentUser.username : '' }}</el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="6-50位" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleResetPassword">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listUsers, createUser, updateUser, resetPassword, deleteUser, exportUsers } from '@/api/modules/user'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      saving: false,
      users: [],
      query: { keyword: '' },
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增账号',
      form: {
        username: '',
        nickname: '',
        password: '',
        role: 'USER',
        enabled: true,
        phone: '',
        email: ''
      },
      formRules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]{3,50}$/, message: '3-50位字母、数字或下划线', trigger: 'blur' }
        ],
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入初始密码', trigger: 'blur' },
          { min: 6, max: 50, message: '长度需为6-50位', trigger: 'blur' }
        ],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },
      currentUser: null,
      pwdVisible: false,
      pwdForm: { newPassword: '' },
      pwdRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 50, message: '长度需为6-50位', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    hasPerm(code) {
      return this.$store.getters.hasPermission(code)
    },
    async fetchUsers() {
      this.loading = true
      try {
        const params = {}
        if (this.query.keyword) params.keyword = this.query.keyword
        const res = await listUsers(params)
        this.users = res.data
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.fetchUsers()
    },
    handleReset() {
      this.query.keyword = ''
      this.fetchUsers()
    },
    openCreate() {
      this.isEdit = false
      this.dialogTitle = '新增账号'
      this.dialogVisible = true
    },
    openEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑账号'
      this.form = {
        id: row.id,
        username: row.username,
        nickname: row.nickname,
        password: '',
        role: row.role,
        enabled: row.enabled,
        phone: row.phone || '',
        email: row.email || ''
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = {
        username: '',
        nickname: '',
        password: '',
        role: 'USER',
        enabled: true,
        phone: '',
        email: ''
      }
      this.$refs.userForm && this.$refs.userForm.clearValidate()
    },
    handleSave() {
      this.$refs.userForm.validate(async valid => {
        if (!valid) return
        // 编辑时移除新密码字段
        const payload = { ...this.form }
        if (this.isEdit) {
          delete payload.username
          delete payload.password
        }
        this.saving = true
        try {
          if (this.isEdit) {
            await updateUser(this.form.id, payload)
            this.$message.success('更新成功')
          } else {
            await createUser(payload)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          this.fetchUsers()
        } finally {
          this.saving = false
        }
      })
    },
    openResetPassword(row) {
      this.currentUser = row
      this.pwdForm.newPassword = ''
      this.pwdVisible = true
    },
    handleResetPassword() {
      this.$refs.pwdForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          await resetPassword(this.currentUser.id, this.pwdForm.newPassword)
          this.$message.success('密码已重置')
          this.pwdVisible = false
        } finally {
          this.saving = false
        }
      })
    },
    async handleExport() {
      try {
        const res = await exportUsers()
        this.downloadBlob(res.data, 'user_export.xlsx')
        this.$message.success('导出成功')
      } catch (e) {
        this.$message.error('导出失败')
      }
    },
    downloadBlob(blob, filename) {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    },
    handleDelete(row) {
      this.$confirm(`确定删除账号「${row.username}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await deleteUser(row.id)
        this.$message.success('删除成功')
        this.fetchUsers()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.query-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}
.danger {
  color: #f56c6c;
}
</style>
