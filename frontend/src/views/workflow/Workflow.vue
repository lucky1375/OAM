<template>
  <div class="workflow-page">
    <!-- 统计卡 -->
    <div class="stat-row">
      <div class="stat-card" v-for="s in statCards" :key="s.key">
        <div class="stat-icon" :style="{ background: s.bg }"><i :class="s.icon"></i></div>
        <div class="stat-body">
          <div class="stat-value">{{ stats[s.key] || 0 }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 主体 -->
    <el-card shadow="never" class="main-card">
      <el-tabs v-model="scope" @tab-click="loadList">
        <el-tab-pane label="我发起的" name="mine"></el-tab-pane>
        <el-tab-pane :label="'待我审批' + (stats.todo ? ' (' + stats.todo + ')' : '')" name="todo"></el-tab-pane>
        <el-tab-pane label="我已处理" name="done"></el-tab-pane>
      </el-tabs>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.type" size="small" clearable placeholder="全部类型" style="width:120px"
                     @change="loadList">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value"></el-option>
          </el-select>
          <el-select v-model="query.status" size="small" clearable placeholder="全部状态" style="width:120px"
                     @change="loadList">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"></el-option>
          </el-select>
          <el-input v-model="query.keyword" size="small" clearable placeholder="搜索标题或内容" style="width:220px"
                    @keyup.enter.native="loadList" @clear="loadList">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </div>
        <div class="toolbar-right">
          <el-button size="small" icon="el-icon-set-up" v-if="hasPerm('workflow:manage')"
                     @click="openNodeConfig">流程配置</el-button>
          <el-button type="primary" size="small" icon="el-icon-plus"
                     v-if="hasPerm('workflow:submit')" @click="openSubmit">发起审批</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" size="small" empty-text="暂无流程数据"
                @row-click="row => openDetail(row.id)" class="wf-table">
        <el-table-column prop="flowNo" label="编号" width="100"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip>
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="typeTag(row.type)" effect="plain" class="type-tag">{{ typeLabel(row.type) }}</el-tag>
            <span class="wf-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="110"></el-table-column>
        <el-table-column label="当前审批人" width="150">
          <template slot-scope="{ row }">
            <div>{{ row.approverName }}</div>
            <div class="node-progress" v-if="row.status === 'PENDING'">
              第 {{ row.currentStep }}/{{ row.totalNodes }} 级 · {{ row.currentNodeName || '审批' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="150"></el-table-column>
        <el-table-column label="操作" width="170" align="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click.stop="openDetail(row.id)">详情</el-button>
            <el-button type="text" size="mini" v-if="row.canApprove && hasPerm('workflow:approve')"
                       @click.stop="openAction(row, 'approve')">审批</el-button>
            <el-button type="text" size="mini" class="danger-text" v-if="row.canCancel"
                       @click.stop="doCancel(row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发起审批 -->
    <el-dialog title="发起审批" :visible.sync="submitVisible" width="600px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="onFormTypeChange">
            <el-radio-button v-for="t in typeOptions" :key="t.value" :label="t.value">{{ t.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="如：10月出差报销申请"></el-input>
        </el-form-item>
        <el-form-item label="请假时间" v-if="form.type === 'LEAVE'" required>
          <el-date-picker v-model="form.timeRange" type="datetimerange" style="width:100%"
                          value-format="yyyy-MM-dd HH:mm:ss" range-separator="至"
                          start-placeholder="开始时间" end-placeholder="结束时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="审批流程" v-if="fixedNodes.length > 0">
          <div class="fixed-flow">
            <el-tag v-for="(n, i) in fixedNodes" :key="n.id || i" size="mini" type="info" class="flow-node">
              {{ i + 1 }}. {{ n.nodeName }}（{{ n.approverName }}）
            </el-tag>
          </div>
          <div class="form-tip">该类型已配置固定审批节点，将按上述顺序逐级审批</div>
        </el-form-item>
        <el-form-item label="审批人" prop="approverUserId" v-else>
          <el-select v-model="form.approverUserId" filterable placeholder="选择审批人" style="width:100%">
            <el-option v-for="u in accountList" :key="u.id" :label="u.displayName" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="2000" show-word-limit
                    placeholder="请填写申请事由、金额或时间等明细"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doSubmit">提交审批</el-button>
      </div>
    </el-dialog>

    <!-- 审批/驳回 -->
    <el-dialog :title="actionMode === 'approve' ? '审批通过' : '驳回申请'" :visible.sync="actionVisible"
               width="520px" :close-on-click-modal="false">
      <div class="action-target" v-if="actionTarget">
        <b>{{ actionTarget.title }}</b>
        <p>{{ actionTarget.content }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="actionComment" type="textarea" :rows="4" maxlength="500" show-word-limit
                    :placeholder="actionMode === 'approve' ? '同意，请知悉（可不填）' : '请填写驳回原因'"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button :type="actionMode === 'approve' ? 'primary' : 'danger'" :loading="acting"
                   @click="submitAction">{{ actionMode === 'approve' ? '确认通过' : '确认驳回' }}</el-button>
      </div>
    </el-dialog>

    <!-- 转办 -->
    <el-dialog title="转办" :visible.sync="delegateVisible" width="520px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="转办给">
          <el-select v-model="delegateForm.targetUserId" filterable placeholder="选择接收人" style="width:100%">
            <el-option v-for="u in accountList" :key="u.id" :label="u.displayName" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="转办说明">
          <el-input v-model="delegateForm.comment" type="textarea" :rows="3" maxlength="500" show-word-limit
                    placeholder="说明转办原因（可不填）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="delegateVisible = false">取消</el-button>
        <el-button type="primary" :loading="delegating" @click="doDelegate">确认转办</el-button>
      </div>
    </el-dialog>

    <!-- 流程配置：固定审批节点 -->
    <el-dialog title="流程配置 · 固定审批节点" :visible.sync="nodeConfigVisible" width="680px"
               :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="流程类型">
          <el-radio-group v-model="nodeType" @change="loadNodeConfig">
            <el-radio-button v-for="t in typeOptions" :key="t.value" :label="t.value">{{ t.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div class="node-tip">
        按列表顺序逐级审批，任一级驳回即流程结束；未配置节点的类型在发起时手动选择审批人。
      </div>
      <el-table :data="nodeDraft" size="small" empty-text="暂无固定节点，该类型发起时手动选择审批人">
        <el-table-column label="顺序" width="60" align="center">
          <template slot-scope="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column label="节点名称" min-width="180">
          <template slot-scope="{ row }">
            <el-input v-model="row.nodeName" size="small" maxlength="50" placeholder="如：直属主管审批"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="固定审批人" min-width="180">
          <template slot-scope="{ row }">
            <el-select v-model="row.approverUserId" filterable size="small" placeholder="选择审批人" style="width:100%">
              <el-option v-for="u in accountList" :key="u.id" :label="u.displayName" :value="u.id"></el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="right">
          <template slot-scope="{ $index }">
            <el-button type="text" size="mini" :disabled="$index === 0" @click="moveNode($index, -1)">上移</el-button>
            <el-button type="text" size="mini" class="danger-text" @click="removeNode($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button size="small" icon="el-icon-plus" class="add-node-btn" @click="addNode">添加节点</el-button>
      <div slot="footer">
        <el-button @click="nodeConfigVisible = false">取消</el-button>
        <el-button type="primary" :loading="nodeSaving" @click="saveNodeConfig">保存配置</el-button>
      </div>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer :visible.sync="detailVisible" size="46%" :with-header="false">
      <div v-if="detail" class="wf-detail">
        <div class="detail-head">
          <div class="detail-title">
            <el-tag size="mini" :type="typeTag(detail.flow.type)" effect="plain">{{ typeLabel(detail.flow.type) }}</el-tag>
            {{ detail.flow.title }}
          </div>
          <div class="detail-meta">
            <span>{{ detail.flow.flowNo }}</span>
            <el-tag size="mini" :type="statusTag(detail.flow.status)">{{ statusLabel(detail.flow.status) }}</el-tag>
            <span v-if="detail.flow.status === 'PENDING'">
              第 {{ detail.flow.currentStep }}/{{ detail.flow.totalNodes }} 级 · {{ detail.flow.currentNodeName || '审批' }}
            </span>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item"><span class="label">申请人</span><span>{{ detail.flow.applicantName }}</span></div>
          <div class="info-item"><span class="label">当前审批人</span><span>{{ detail.flow.approverName }}</span></div>
          <div class="info-item"><span class="label">提交时间</span><span>{{ detail.flow.createdAt }}</span></div>
          <div class="info-item"><span class="label">完结时间</span><span>{{ detail.flow.finishedAt || '—' }}</span></div>
          <div class="info-item" v-if="detail.flow.leaveStartTime" style="grid-column: span 2">
            <span class="label">请假时间</span>
            <span>{{ detail.flow.leaveStartTime }} 至 {{ detail.flow.leaveEndTime }}</span>
          </div>
        </div>

        <div class="detail-block">
          <div class="block-label">申请内容</div>
          <div class="detail-content">{{ detail.flow.content }}</div>
        </div>

        <div class="detail-block">
          <div class="block-label">审批轨迹</div>
          <el-timeline>
            <el-timeline-item v-for="r in detail.records" :key="r.id"
                              :timestamp="r.createdAt" placement="top"
                              :color="recordColor(r.action)">
              <div class="record-line">
                <b>{{ r.operatorName }}</b>
                <span class="record-action">{{ actionLabel(r.action) }}</span>
                <span v-if="r.targetName" class="record-target">→ {{ r.targetName }}</span>
              </div>
              <div v-if="r.comment" class="record-comment">{{ r.comment }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="detail-actions">
          <template v-if="detail.flow.canApprove">
            <el-button type="primary" size="small" icon="el-icon-check"
                       v-if="hasPerm('workflow:approve')" @click="openAction(detail.flow, 'approve')">通过</el-button>
            <el-button type="danger" size="small" plain icon="el-icon-close"
                       v-if="hasPerm('workflow:approve')" @click="openAction(detail.flow, 'reject')">驳回</el-button>
            <el-button size="small" plain icon="el-icon-share"
                       v-if="hasPerm('workflow:delegate')" @click="openDelegate">转办</el-button>
          </template>
          <el-button size="small" plain type="warning" icon="el-icon-refresh-left"
                     v-if="detail.flow.canCancel" @click="doCancel(detail.flow)">撤回申请</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  listWorkflows, getWorkflow, getWorkflowStats, submitWorkflow,
  approveWorkflow, rejectWorkflow, delegateWorkflow, cancelWorkflow, listAccounts,
  listWorkflowNodes, saveWorkflowNodes
} from '@/api/modules/workflow'

const TYPE_OPTIONS = [
  { value: 'LEAVE', label: '请假' },
  { value: 'EXPENSE', label: '报销' },
  { value: 'PURCHASE', label: '采购' },
  { value: 'OTHER', label: '其他' }
]
const STATUS_OPTIONS = [
  { value: 'PENDING', label: '审批中' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'CANCELLED', label: '已撤回' }
]

export default {
  name: 'Workflow',
  data() {
    return {
      typeOptions: TYPE_OPTIONS,
      statusOptions: STATUS_OPTIONS,
      scope: 'mine',
      query: { keyword: '', status: '', type: '' },
      list: [],
      loading: false,
      stats: {},
      statCards: [
        { key: 'todo', label: '待我审批', icon: 'el-icon-s-check', bg: 'linear-gradient(135deg,#f59e0b,#d97706)' },
        { key: 'myPending', label: '我审批中', icon: 'el-icon-time', bg: 'linear-gradient(135deg,#6366f1,#4f46e5)' },
        { key: 'approved', label: '已通过', icon: 'el-icon-circle-check', bg: 'linear-gradient(135deg,#10b981,#059669)' },
        { key: 'rejected', label: '已驳回', icon: 'el-icon-circle-close', bg: 'linear-gradient(135deg,#ef4444,#dc2626)' }
      ],
      accountList: [],
      submitVisible: false,
      submitting: false,
      form: { type: 'LEAVE', title: '', approverUserId: null, content: '', timeRange: [] },
      rules: {
        type: [{ required: true, message: '请选择类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请填写申请内容', trigger: 'blur' }]
      },
      detailVisible: false,
      detail: null,
      actionVisible: false,
      actionMode: 'approve',
      actionTarget: null,
      actionComment: '',
      acting: false,
      delegateVisible: false,
      delegating: false,
      delegateForm: { targetUserId: null, comment: '' },
      // 固定审批节点
      fixedNodes: [],
      nodeConfigVisible: false,
      nodeType: 'LEAVE',
      nodeDraft: [],
      nodeSaving: false
    }
  },
  created() {
    const tab = this.$route.query.tab
    if (tab === 'todo' || tab === 'done' || tab === 'mine') {
      this.scope = tab
    }
    this.loadList()
    this.loadStats()
    this.loadAccounts()
  },
  methods: {
    hasPerm(code) {
      return this.$store.getters.hasPermission(code)
    },
    typeLabel(v) {
      const t = TYPE_OPTIONS.find(x => x.value === v)
      return t ? t.label : v || '其他'
    },
    typeTag(v) {
      return { LEAVE: 'primary', EXPENSE: 'success', PURCHASE: 'warning', OTHER: 'info' }[v] || 'info'
    },
    statusLabel(v) {
      const s = STATUS_OPTIONS.find(x => x.value === v)
      return s ? s.label : v
    },
    statusTag(v) {
      return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }[v] || 'info'
    },
    actionLabel(a) {
      return { SUBMIT: '提交申请', APPROVE: '审批通过', REJECT: '驳回申请', DELEGATE: '转办给', CANCEL: '撤回申请' }[a] || a
    },
    recordColor(a) {
      return { APPROVE: '#10b981', REJECT: '#ef4444', DELEGATE: '#f59e0b', CANCEL: '#9ca3af' }[a] || '#6366f1'
    },
    async loadList() {
      this.loading = true
      try {
        const params = { scope: this.scope }
        if (this.query.keyword) params.keyword = this.query.keyword
        if (this.query.status) params.status = this.query.status
        if (this.query.type) params.type = this.query.type
        const res = await listWorkflows(params)
        this.list = res.data || []
      } catch (e) {
        this.$message.error(e.message || '加载流程失败')
      } finally {
        this.loading = false
      }
    },
    async loadStats() {
      try {
        const res = await getWorkflowStats()
        this.stats = res.data || {}
      } catch (e) { /* 静默 */ }
    },
    async loadAccounts() {
      try {
        const res = await listAccounts()
        this.accountList = res.data || []
      } catch (e) { /* 静默 */ }
    },
    openSubmit() {
      this.form = { type: 'LEAVE', title: '', approverUserId: null, content: '', timeRange: [] }
      this.submitVisible = true
      this.loadFixedNodes('LEAVE')
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    onFormTypeChange(type) {
      this.form.approverUserId = null
      this.form.timeRange = []
      this.loadFixedNodes(type)
    },
    async loadFixedNodes(type) {
      try {
        const res = await listWorkflowNodes(type)
        this.fixedNodes = res.data || []
      } catch (e) {
        this.fixedNodes = []
      }
    },
    async openNodeConfig() {
      this.nodeType = 'LEAVE'
      this.nodeConfigVisible = true
      await this.loadNodeConfig()
    },
    async loadNodeConfig() {
      try {
        const res = await listWorkflowNodes(this.nodeType)
        this.nodeDraft = (res.data || []).map(n => ({
          nodeName: n.nodeName,
          approverUserId: n.approverUserId
        }))
      } catch (e) {
        this.nodeDraft = []
        this.$message.error(e.message || '加载流程节点失败')
      }
    },
    addNode() {
      if (this.nodeDraft.length >= 10) {
        this.$message.warning('最多配置 10 个审批节点')
        return
      }
      this.nodeDraft.push({ nodeName: '', approverUserId: null })
    },
    removeNode(index) {
      this.nodeDraft.splice(index, 1)
    },
    moveNode(index, offset) {
      const target = index + offset
      if (target < 0 || target >= this.nodeDraft.length) return
      const arr = this.nodeDraft
      const tmp = arr[index]
      this.$set(arr, index, arr[target])
      this.$set(arr, target, tmp)
    },
    async saveNodeConfig() {
      for (let i = 0; i < this.nodeDraft.length; i++) {
        const n = this.nodeDraft[i]
        if (!n.nodeName || !n.nodeName.trim()) {
          this.$message.warning(`请填写第 ${i + 1} 个节点的名称`)
          return
        }
        if (!n.approverUserId) {
          this.$message.warning(`请选择第 ${i + 1} 个节点的审批人`)
          return
        }
      }
      this.nodeSaving = true
      try {
        const payload = this.nodeDraft.map(n => ({ nodeName: n.nodeName, approverUserId: n.approverUserId }))
        await saveWorkflowNodes(this.nodeType, payload)
        this.$message.success('流程节点已保存')
        this.nodeConfigVisible = false
        this.loadFixedNodes(this.nodeType)
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.nodeSaving = false
      }
    },
    doSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        if (this.fixedNodes.length === 0 && !this.form.approverUserId) {
          this.$message.warning('请选择审批人')
          return
        }
        if (this.form.type === 'LEAVE' && (!this.form.timeRange || this.form.timeRange.length !== 2)) {
          this.$message.warning('请选择请假开始和结束时间')
          return
        }
        this.submitting = true
        try {
          const payload = {
            type: this.form.type,
            title: this.form.title,
            content: this.form.content,
            approverUserId: this.form.approverUserId
          }
          if (this.form.type === 'LEAVE') {
            payload.startTime = this.form.timeRange[0]
            payload.endTime = this.form.timeRange[1]
          }
          await submitWorkflow(payload)
          this.$message.success('流程已提交')
          this.submitVisible = false
          this.scope = 'mine'
          this.loadList()
          this.loadStats()
        } catch (e) {
          this.$message.error(e.message || '提交失败')
        } finally {
          this.submitting = false
        }
      })
    },
    async openDetail(id) {
      try {
        const res = await getWorkflow(id)
        this.detail = res.data
        this.detailVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载详情失败')
      }
    },
    openAction(row, mode) {
      this.actionTarget = row
      this.actionMode = mode
      this.actionComment = ''
      this.actionVisible = true
    },
    submitAction() {
      this.acting = true
      const fn = this.actionMode === 'approve' ? approveWorkflow : rejectWorkflow
      fn(this.actionTarget.id, { comment: this.actionComment })
        .then(async () => {
          this.$message.success(this.actionMode === 'approve' ? '审批通过' : '已驳回')
          this.actionVisible = false
          this.loadList()
          this.loadStats()
          if (this.detail && this.detail.flow.id === this.actionTarget.id) this.openDetail(this.actionTarget.id)
        })
        .catch(e => this.$message.error(e.message || '操作失败'))
        .finally(() => { this.acting = false })
    },
    openDelegate() {
      this.delegateForm = { targetUserId: null, comment: '' }
      this.delegateVisible = true
    },
    doDelegate() {
      if (!this.delegateForm.targetUserId) {
        this.$message.warning('请选择转办人')
        return
      }
      this.delegating = true
      delegateWorkflow(this.detail.flow.id, this.delegateForm)
        .then(async () => {
          this.$message.success('已转办')
          this.delegateVisible = false
          this.loadList()
          this.loadStats()
          this.openDetail(this.detail.flow.id)
        })
        .catch(e => this.$message.error(e.message || '转办失败'))
        .finally(() => { this.delegating = false })
    },
    async doCancel(row) {
      try {
        await this.$confirm('确认撤回该申请？撤回后流程结束。', '提示', { type: 'warning' })
        await cancelWorkflow(row.id)
        this.$message.success('已撤回')
        this.loadList()
        this.loadStats()
        if (this.detail && this.detail.flow.id === row.id) this.openDetail(row.id)
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '撤回失败')
      }
    }
  }
}
</script>

<style scoped>
.workflow-page { padding: 16px 20px; }

.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 16px; }
.stat-card {
  background: #fff; border: 1px solid #e5e9f0; border-radius: 10px; padding: 14px 16px;
  display: flex; align-items: center; gap: 14px;
}
.stat-icon {
  width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px; flex-shrink: 0;
}
.stat-value { font-size: 22px; font-weight: 700; color: #1f2937; line-height: 1.2; }
.stat-label { font-size: 12px; color: #9aa3b2; margin-top: 2px; }

.main-card { border-radius: 10px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin: 4px 0 14px; }
.toolbar-left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.toolbar-right { display: flex; gap: 8px; flex-shrink: 0; }
.wf-table >>> .el-table__row { cursor: pointer; }
.wf-title { font-weight: 500; color: #1f2937; }
.type-tag { margin-right: 6px; }
.node-progress { font-size: 11px; color: #9aa3b2; margin-top: 2px; }
.danger-text { color: #f56c6c; }

/* 固定审批节点 */
.fixed-flow { display: flex; flex-wrap: wrap; gap: 6px; }
.flow-node { margin: 0; }
.form-tip { font-size: 12px; color: #9aa3b2; margin-top: 4px; }
.node-tip {
  font-size: 12px; color: #6b7280; background: #f8fafc; border: 1px solid #eef2f7;
  border-radius: 6px; padding: 8px 12px; margin-bottom: 12px;
}
.add-node-btn { margin-top: 10px; }

/* 详情 */
.wf-detail { padding: 8px 24px 24px; }
.detail-head { margin-bottom: 16px; }
.detail-title { font-size: 19px; font-weight: 700; color: #1f2937; display: flex; align-items: center; gap: 8px; }
.detail-meta { margin-top: 8px; display: flex; align-items: center; gap: 10px; font-size: 12px; color: #9aa3b2; }
.info-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px 20px;
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 14px; margin-bottom: 18px;
}
.info-item { font-size: 13px; color: #374151; display: flex; gap: 10px; }
.info-item .label { color: #9aa3b2; width: 76px; flex-shrink: 0; }
.detail-block { margin-bottom: 18px; }
.block-label { font-size: 12px; font-weight: 600; color: #6b7280; margin-bottom: 8px; letter-spacing: .5px; }
.detail-content {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 14px;
  font-size: 14px; color: #374151; line-height: 1.8; white-space: pre-wrap;
}
.record-line { font-size: 13px; color: #374151; display: flex; align-items: center; gap: 8px; }
.record-action { font-size: 12px; color: #6366f1; }
.record-target { font-size: 12px; color: #f59e0b; }
.record-comment { margin-top: 4px; font-size: 12px; color: #6b7280; background: #f8fafc; border-radius: 6px; padding: 6px 10px; }
.detail-actions { margin-top: 20px; display: flex; gap: 8px; flex-wrap: wrap; }

.action-target {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 12px; margin-bottom: 16px;
}
.action-target b { font-size: 14px; color: #1f2937; }
.action-target p { margin: 6px 0 0; font-size: 13px; color: #6b7280; line-height: 1.6; max-height: 90px; overflow: auto; }
</style>
