<template>
  <div class="task-page">
    <!-- 概览 -->
    <div class="stat-row">
      <div class="stat-card" v-for="s in statCards" :key="s.key">
        <div class="stat-icon" :style="{ background: s.bg }"><i :class="s.icon"></i></div>
        <div class="stat-body">
          <div class="stat-value">{{ stats[s.key] || 0 }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 任务列表 -->
    <el-card shadow="never" class="main-card">
      <el-tabs v-model="scope" @tab-click="loadList">
        <el-tab-pane label="我负责的" name="assigned"></el-tab-pane>
        <el-tab-pane label="我创建的" name="created"></el-tab-pane>
        <el-tab-pane label="全部任务" name="all"></el-tab-pane>
      </el-tabs>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.status" size="small" clearable placeholder="全部状态" style="width:120px"
                     @change="loadList">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"></el-option>
          </el-select>
          <el-input v-model="query.keyword" size="small" clearable placeholder="搜索任务标题或描述" style="width:220px"
                    @keyup.enter.native="loadList" @clear="loadList">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </div>
        <el-button type="primary" size="small" icon="el-icon-plus" v-if="hasPerm('task:create')"
                   @click="openCreate">新建任务</el-button>
      </div>

      <el-table :data="list" v-loading="loading" size="small" empty-text="暂无任务" class="task-table">
        <el-table-column label="任务" min-width="260" show-overflow-tooltip>
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="priorityTag(row.priority)" effect="plain" class="mr6">
              {{ priorityLabel(row.priority) }}
            </el-tag>
            <span class="task-title" @click="openDetail(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100"></el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100"></el-table-column>
        <el-table-column label="截止日期" width="120">
          <template slot-scope="{ row }">
            <span :class="{ 'overdue-text': row.overdue }">{{ row.dueDate || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="140">
          <template slot-scope="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="8"
                         :status="row.status === 'DONE' ? 'success' : ''"></el-progress>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="140"></el-table-column>
        <el-table-column label="操作" width="250" align="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click="openDetail(row)">详情</el-button>
            <el-button type="text" size="mini" v-if="row.canUpdateProgress && hasPerm('task:progress')"
                       @click="openProgress(row)">进度</el-button>
            <el-button type="text" size="mini" v-if="row.canEdit && hasPerm('task:update')"
                       @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" v-if="row.canAssign && hasPerm('task:assign')"
                       @click="openAssign(row)">分派</el-button>
            <el-button type="text" size="mini" class="danger-text" v-if="row.canDelete && hasPerm('task:delete')"
                       @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑 -->
    <el-dialog :title="formMode === 'create' ? '新建任务' : '编辑任务'" :visible.sync="formVisible"
               width="580px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="taskForm" label-width="80px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit
                    placeholder="如：整理三季度运营数据"></el-input>
        </el-form-item>
        <el-form-item label="负责人" v-if="formMode === 'create'">
          <el-select v-model="form.assigneeUserId" filterable clearable placeholder="默认自己" style="width:100%">
            <el-option v-for="u in accountList" :key="u.id" :label="u.displayName" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio-button v-for="p in priorityOptions" :key="p.value" :label="p.value">{{ p.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" type="date" value-format="yyyy-MM-dd"
                          placeholder="选择截止日期" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="5" maxlength="2000" show-word-limit
                    placeholder="补充任务背景、交付要求等"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </span>
    </el-dialog>

    <!-- 更新进度 -->
    <el-dialog title="更新进度" :visible.sync="progressVisible" width="520px" :close-on-click-modal="false">
      <div class="dialog-task" v-if="currentTask">
        <b>{{ currentTask.title }}</b>
        <span>负责人：{{ currentTask.assigneeName }}</span>
      </div>
      <el-form label-width="80px">
        <el-form-item label="当前进度">
          <el-slider v-model="progressForm.progress" :step="5" show-stops></el-slider>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-radio-group v-model="progressForm.status" @change="onStatusChange">
            <el-radio-button v-for="s in statusOptions" :key="s.value" :label="s.value">{{ s.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="进度说明">
          <el-input v-model="progressForm.note" type="textarea" :rows="3" maxlength="500" show-word-limit
                    placeholder="说明本次进展或遇到的问题（可不填）"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="progressVisible = false">取消</el-button>
        <el-button type="primary" :loading="progressSaving" @click="submitProgress">保存进度</el-button>
      </span>
    </el-dialog>

    <!-- 分派 -->
    <el-dialog title="分派任务" :visible.sync="assignVisible" width="480px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="任务">
          <span>{{ currentTask && currentTask.title }}</span>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="assignForm.assigneeUserId" filterable placeholder="选择负责人" style="width:100%">
            <el-option v-for="u in accountList" :key="u.id" :label="u.displayName" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="submitAssign">确认分派</el-button>
      </span>
    </el-dialog>

    <!-- 任务详情 -->
    <el-drawer title="任务详情" :visible.sync="detailVisible" size="480px">
      <div class="detail-body" v-if="detail && detail.task" v-loading="detailLoading">
        <h3 class="detail-title">{{ detail.task.title }}</h3>
        <div class="detail-grid">
          <div><label>负责人</label><span>{{ detail.task.assigneeName }}</span></div>
          <div><label>创建人</label><span>{{ detail.task.creatorName }}</span></div>
          <div><label>优先级</label>
            <el-tag size="mini" :type="priorityTag(detail.task.priority)" effect="plain">
              {{ priorityLabel(detail.task.priority) }}
            </el-tag>
          </div>
          <div><label>状态</label>
            <el-tag size="mini" :type="statusTag(detail.task.status)">{{ statusLabel(detail.task.status) }}</el-tag>
          </div>
          <div><label>截止日期</label>
            <span :class="{ 'overdue-text': detail.task.overdue }">{{ detail.task.dueDate || '—' }}</span>
          </div>
          <div><label>创建时间</label><span>{{ detail.task.createdAt }}</span></div>
        </div>
        <div class="detail-progress">
          <label>完成进度</label>
          <el-progress :percentage="detail.task.progress" :stroke-width="10"
                       :status="detail.task.status === 'DONE' ? 'success' : ''"></el-progress>
        </div>
        <div class="detail-desc">
          <label>任务描述</label>
          <p>{{ detail.task.description || '暂无描述' }}</p>
        </div>
        <div class="detail-timeline">
          <label>进度跟踪</label>
          <el-timeline>
            <el-timeline-item v-for="r in detail.records" :key="r.id" :timestamp="r.createdAt" placement="top">
              <div class="record-line">
                <b>{{ r.operatorName }}</b>
                <span>进度 {{ r.progress }}% · {{ statusLabel(r.status) }}</span>
              </div>
              <div class="record-note" v-if="r.note">{{ r.note }}</div>
            </el-timeline-item>
            <el-timeline-item v-if="!detail.records || detail.records.length === 0" timestamp="">
              暂无进度记录
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  listTasks, getTaskStats, getTask, createTask, updateTask, deleteTask,
  updateTaskProgress, assignTask
} from '@/api/modules/task'
import { listAccounts } from '@/api/modules/workflow'

const STATUS_OPTIONS = [
  { value: 'TODO', label: '待处理' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'DONE', label: '已完成' }
]
const PRIORITY_OPTIONS = [
  { value: 'LOW', label: '低' },
  { value: 'NORMAL', label: '普通' },
  { value: 'HIGH', label: '高' },
  { value: 'URGENT', label: '紧急' }
]

export default {
  name: 'TaskPage',
  data() {
    return {
      scope: 'assigned',
      query: { status: '', keyword: '' },
      statusOptions: STATUS_OPTIONS,
      priorityOptions: PRIORITY_OPTIONS,
      list: [],
      loading: false,
      stats: { todo: 0, inProgress: 0, done: 0, overdue: 0 },
      accountList: [],
      formMode: 'create',
      formVisible: false,
      saving: false,
      form: { title: '', description: '', assigneeUserId: null, priority: 'NORMAL', dueDate: '' },
      rules: {
        title: [{ required: true, message: '请填写任务标题', trigger: 'blur' }]
      },
      currentTask: null,
      progressVisible: false,
      progressSaving: false,
      progressForm: { progress: 0, status: 'TODO', note: '' },
      assignVisible: false,
      assignSaving: false,
      assignForm: { assigneeUserId: null },
      detailVisible: false,
      detailLoading: false,
      detail: null
    }
  },
  computed: {
    statCards() {
      return [
        { key: 'todo', label: '待处理', icon: 'el-icon-s-order', bg: 'linear-gradient(135deg,#f59e0b,#f97316)' },
        { key: 'inProgress', label: '进行中', icon: 'el-icon-loading', bg: 'linear-gradient(135deg,#3b82f6,#2563eb)' },
        { key: 'done', label: '已完成', icon: 'el-icon-circle-check', bg: 'linear-gradient(135deg,#10b981,#059669)' },
        { key: 'overdue', label: '已逾期', icon: 'el-icon-alarm-clock', bg: 'linear-gradient(135deg,#ef4444,#dc2626)' }
      ]
    }
  },
  created() {
    this.loadStats()
    this.loadList()
    this.loadAccounts()
  },
  methods: {
    hasPerm(code) {
      return this.$store.getters.hasPermission(code)
    },
    async loadStats() {
      try {
        const res = await getTaskStats()
        this.stats = res.data || { todo: 0, inProgress: 0, done: 0, overdue: 0 }
      } catch (e) { /* 已统一提示 */ }
    },
    async loadList() {
      this.loading = true
      try {
        const res = await listTasks({
          scope: this.scope,
          status: this.query.status || undefined,
          keyword: this.query.keyword || undefined
        })
        this.list = res.data || []
      } catch (e) {
        this.list = []
      } finally {
        this.loading = false
      }
    },
    async loadAccounts() {
      try {
        const res = await listAccounts()
        this.accountList = res.data || []
      } catch (e) { /* 忽略 */ }
    },
    openCreate() {
      this.formMode = 'create'
      this.form = { title: '', description: '', assigneeUserId: null, priority: 'NORMAL', dueDate: '' }
      this.formVisible = true
      this.$nextTick(() => this.$refs.taskForm && this.$refs.taskForm.clearValidate())
    },
    openEdit(row) {
      this.formMode = 'edit'
      this.currentTask = row
      this.form = {
        title: row.title,
        description: row.description || '',
        assigneeUserId: row.assigneeUserId,
        priority: row.priority || 'NORMAL',
        dueDate: row.dueDate || ''
      }
      this.formVisible = true
      this.$nextTick(() => this.$refs.taskForm && this.$refs.taskForm.clearValidate())
    },
    submitForm() {
      this.$refs.taskForm.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.formMode === 'create') {
            const res = await createTask(this.form)
            this.$message.success(res.message || '任务已创建')
          } else {
            const res = await updateTask(this.currentTask.id, this.form)
            this.$message.success(res.message || '任务已更新')
          }
          this.formVisible = false
          this.loadList()
          this.loadStats()
        } catch (e) { /* 已统一提示 */ } finally {
          this.saving = false
        }
      })
    },
    openProgress(row) {
      this.currentTask = row
      this.progressForm = { progress: row.progress || 0, status: row.status || 'TODO', note: '' }
      this.progressVisible = true
    },
    onStatusChange(value) {
      if (value === 'DONE') this.progressForm.progress = 100
      else if (value === 'TODO') this.progressForm.progress = 0
      else if (this.progressForm.progress >= 100) this.progressForm.progress = 90
    },
    async submitProgress() {
      this.progressSaving = true
      try {
        const res = await updateTaskProgress(this.currentTask.id, this.progressForm)
        this.$message.success(res.message || '进度已更新')
        this.progressVisible = false
        this.loadList()
        this.loadStats()
        if (this.detailVisible && this.detail && this.detail.task.id === this.currentTask.id) {
          this.detail = res.data
        }
      } catch (e) { /* 已统一提示 */ } finally {
        this.progressSaving = false
      }
    },
    openAssign(row) {
      this.currentTask = row
      this.assignForm = { assigneeUserId: row.assigneeUserId }
      this.assignVisible = true
    },
    async submitAssign() {
      if (!this.assignForm.assigneeUserId) {
        this.$message.warning('请选择负责人')
        return
      }
      this.assignSaving = true
      try {
        const res = await assignTask(this.currentTask.id, this.assignForm)
        this.$message.success(res.message || '任务已分派')
        this.assignVisible = false
        this.loadList()
        this.loadStats()
      } catch (e) { /* 已统一提示 */ } finally {
        this.assignSaving = false
      }
    },
    async doDelete(row) {
      try {
        await this.$confirm(`确认删除任务「${row.title}」？`, '删除任务', { type: 'warning' })
      } catch (e) {
        return
      }
      try {
        const res = await deleteTask(row.id)
        this.$message.success(res.message || '任务已删除')
        this.loadList()
        this.loadStats()
      } catch (e) { /* 已统一提示 */ }
    },
    async openDetail(row) {
      this.detailVisible = true
      this.detailLoading = true
      try {
        const res = await getTask(row.id)
        this.detail = res.data
      } catch (e) {
        this.detail = null
      } finally {
        this.detailLoading = false
      }
    },
    statusLabel(status) {
      const item = STATUS_OPTIONS.find(s => s.value === status)
      return item ? item.label : status
    },
    statusTag(status) {
      if (status === 'DONE') return 'success'
      if (status === 'IN_PROGRESS') return 'primary'
      return 'info'
    },
    priorityLabel(priority) {
      const item = PRIORITY_OPTIONS.find(p => p.value === priority)
      return item ? item.label : priority
    },
    priorityTag(priority) {
      if (priority === 'URGENT') return 'danger'
      if (priority === 'HIGH') return 'warning'
      if (priority === 'LOW') return 'info'
      return ''
    }
  }
}
</script>

<style scoped>
.task-page { padding: 4px; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 10px; padding: 16px 18px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}
.stat-icon {
  width: 42px; height: 42px; border-radius: 10px; color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 20px;
}
.stat-value { font-size: 22px; font-weight: 700; color: #1f2937; line-height: 1.2; }
.stat-label { font-size: 12px; color: #6b7280; margin-top: 2px; }
.main-card { border-radius: 10px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; gap: 12px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.task-title { cursor: pointer; color: #1f2937; }
.task-title:hover { color: #2e5bff; }
.overdue-text { color: #ef4444; font-weight: 600; }
.mr6 { margin-right: 6px; }
.danger-text { color: #ef4444; }
.dialog-task {
  display: flex; justify-content: space-between; align-items: center;
  background: #f8fafc; border-radius: 6px; padding: 8px 12px; margin-bottom: 14px;
  color: #475569; font-size: 13px;
}
.detail-body { padding: 0 20px 24px; }
.detail-title { margin: 4px 0 14px; font-size: 17px; color: #1f2937; }
.detail-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px 16px;
  font-size: 13px; color: #374151; margin-bottom: 16px;
}
.detail-grid label, .detail-progress label, .detail-desc label, .detail-timeline label {
  display: block; color: #9ca3af; font-size: 12px; margin-bottom: 4px;
}
.detail-progress { margin-bottom: 16px; }
.detail-desc p {
  margin: 0; font-size: 13px; color: #4b5563; line-height: 1.7;
  white-space: pre-wrap; background: #f8fafc; border-radius: 6px; padding: 10px 12px;
}
.detail-desc { margin-bottom: 16px; }
.record-line { display: flex; gap: 10px; align-items: baseline; font-size: 13px; color: #374151; }
.record-line span { color: #6b7280; font-size: 12px; }
.record-note { margin-top: 4px; font-size: 12px; color: #6b7280; }
</style>
