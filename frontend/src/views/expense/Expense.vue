<template>
  <div class="expense-page">
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

    <el-card shadow="never" class="main-card">
      <el-tabs v-model="scope" @tab-click="loadList">
        <el-tab-pane label="我提交的" name="mine"></el-tab-pane>
        <el-tab-pane :label="'待我审批' + (stats.todo ? ' (' + stats.todo + ')' : '')" name="todo"></el-tab-pane>
        <el-tab-pane label="全部报销" name="all" v-if="isAdmin"></el-tab-pane>
      </el-tabs>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.status" size="small" clearable placeholder="全部状态" style="width:120px"
                     @change="loadList">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"></el-option>
          </el-select>
          <el-select v-model="query.category" size="small" clearable placeholder="全部分类" style="width:120px"
                     @change="loadList">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value"></el-option>
          </el-select>
          <el-input v-model="query.keyword" size="small" clearable placeholder="搜索事由或说明" style="width:220px"
                    @keyup.enter.native="loadList" @clear="loadList">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </div>
        <el-button type="primary" size="small" icon="el-icon-plus"
                   v-if="hasPerm('expense:create')" @click="openCreate">新建报销单</el-button>
      </div>

      <el-table :data="list" v-loading="loading" size="small" empty-text="暂无报销单"
                @row-click="row => openDetail(row.id)" class="exp-table">
        <el-table-column prop="claimNo" label="单号" width="110"></el-table-column>
        <el-table-column prop="title" label="报销事由" min-width="200" show-overflow-tooltip>
          <template slot-scope="{ row }">
            <el-tag size="mini" effect="plain" class="cat-tag">{{ categoryLabel(row.category) }}</el-tag>
            <span class="exp-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template slot-scope="{ row }">
            <span class="amount-text">¥ {{ money(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceCount" label="发票" width="70" align="center"></el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="110"></el-table-column>
        <el-table-column label="当前审批人" width="130">
          <template slot-scope="{ row }">
            <span>{{ row.currentApproverName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="150"></el-table-column>
        <el-table-column label="操作" width="200" align="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click.stop="openDetail(row.id)">详情</el-button>
            <el-button type="text" size="mini" v-if="row.canApprove"
                       @click.stop="openAction(row, 'approve')">审批</el-button>
            <el-button type="text" size="mini" v-if="row.canEdit && hasPerm('expense:update')"
                       @click.stop="openEdit(row)">编辑</el-button>
            <el-button type="text" size="mini" v-if="row.canSubmit && hasPerm('expense:submit')"
                       @click.stop="openSubmitDialog(row)">提交</el-button>
            <el-button type="text" size="mini" class="danger-text" v-if="row.canDelete && hasPerm('expense:delete')"
                       @click.stop="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 报销单表单 -->
    <el-dialog :title="form.id ? '编辑报销单' : '新建报销单'" :visible.sync="dialogVisible"
               width="780px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="报销事由" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit
                    placeholder="如：10月北京出差交通费报销"></el-input>
        </el-form-item>
        <el-form-item label="费用分类" prop="category">
          <el-radio-group v-model="form.category">
            <el-radio-button v-for="c in categoryOptions" :key="c.value" :label="c.value">{{ c.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报销说明">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit
                    placeholder="补充说明费用用途、行程等信息（可不填）"></el-input>
        </el-form-item>
        <el-form-item label="发票明细">
          <div class="invoice-panel">
            <div class="invoice-head">
              <span class="invoice-summary">
                共 {{ formInvoiceCount }} 张发票，合计
                <b class="amount-text">¥ {{ money(formTotal) }}</b>
              </span>
              <div class="invoice-actions">
                <el-button size="small" icon="el-icon-plus" @click="openInvoiceDialog(-1)">添加发票</el-button>
              </div>
            </div>
            <el-table :data="form.invoices" size="small" empty-text="请添加发票，提交审批前至少需要一张发票">
              <el-table-column label="发票类型" width="120">
                <template slot-scope="{ row }">{{ invoiceTypeLabel(row.invoiceType) }}</template>
              </el-table-column>
              <el-table-column prop="invoiceNumber" label="发票号码" min-width="140" show-overflow-tooltip></el-table-column>
              <el-table-column prop="invoiceDate" label="开票日期" width="110"></el-table-column>
              <el-table-column prop="sellerName" label="销售方" min-width="150" show-overflow-tooltip></el-table-column>
              <el-table-column label="价税合计" width="110" align="right">
                <template slot-scope="{ row }">¥ {{ money(row.totalAmount) }}</template>
              </el-table-column>
              <el-table-column label="校验" width="90" align="center">
                <template slot-scope="{ row }">
                  <el-tag size="mini" :type="verifyTag(row.verifyStatus)">
                    {{ verifyLabel(row.verifyStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="right">
                <template slot-scope="{ row, $index }">
                  <el-button type="text" size="mini" @click="openInvoiceDialog($index)">编辑</el-button>
                  <el-button type="text" size="mini" class="danger-text" @click="removeInvoice($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveForm">{{ form.id ? '保存' : '保存草稿' }}</el-button>
      </div>
    </el-dialog>

    <!-- 发票录入 -->
    <el-dialog :title="invoiceIndex >= 0 ? '编辑发票' : '添加发票'" :visible.sync="invoiceVisible"
               width="640px" :close-on-click-modal="false">
      <div class="ocr-bar">
        <el-upload :show-file-list="false" accept="image/*,.pdf" :before-upload="beforeOcrUpload"
                   :http-request="handleOcr">
          <el-button size="small" icon="el-icon-camera" :loading="recognizing"
                     :disabled="!ocrEnabled || !hasPerm('expense:invoice')">上传发票识别</el-button>
        </el-upload>
        <span class="form-tip">
          {{ ocrEnabled ? '支持 jpg / png / pdf（图片不超过 4MB，PDF 不超过 10MB）；本地 Tesseract 识别后请核对票面信息' : '未检测到本地 Tesseract OCR 环境，可手工录入' }}
        </span>
      </div>
      <el-form :model="invoiceForm" :rules="invoiceRules" ref="invoiceForm" label-width="90px">
        <el-form-item label="发票类型" prop="invoiceType">
          <el-select v-model="invoiceForm.invoiceType" style="width:100%">
            <el-option v-for="t in invoiceTypeOptions" :key="t.value" :label="t.label" :value="t.value"></el-option>
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="发票代码">
              <el-input v-model="invoiceForm.invoiceCode" maxlength="30" placeholder="选填"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票号码" prop="invoiceNumber">
              <el-input v-model="invoiceForm.invoiceNumber" maxlength="30" placeholder="必填"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开票日期">
              <el-date-picker v-model="invoiceForm.invoiceDate" type="date" value-format="yyyy-MM-dd"
                              placeholder="选择日期" style="width:100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据来源">
              <el-tag size="small" :type="invoiceForm.source === 'OCR' ? 'success' : 'info'">
                {{ invoiceForm.source === 'OCR' ? '发票识别' : '手工录入' }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="销售方">
          <el-input v-model="invoiceForm.sellerName" maxlength="100" placeholder="选填"></el-input>
        </el-form-item>
        <el-form-item label="购买方">
          <el-input v-model="invoiceForm.buyerName" maxlength="100" placeholder="选填"></el-input>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="金额">
              <el-input-number v-model="invoiceForm.amount" :min="0" :precision="2" :controls="false"
                               placeholder="不含税" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税额">
              <el-input-number v-model="invoiceForm.taxAmount" :min="0" :precision="2" :controls="false"
                               placeholder="选填" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="价税合计" prop="totalAmount">
              <el-input-number v-model="invoiceForm.totalAmount" :min="0" :precision="2" :controls="false"
                               placeholder="必填" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-alert v-if="invoiceVerifyResult" :title="invoiceVerifyResult.message" :closable="false"
                :type="invoiceVerifyResult.status === 'VERIFIED' ? 'success' : 'error'" show-icon></el-alert>
      <div slot="footer">
        <el-button size="small" :loading="verifyingInvoice" @click="verifyInvoiceForm">校验发票</el-button>
        <el-button @click="invoiceVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInvoice">确定</el-button>
      </div>
    </el-dialog>

    <!-- 提交审批 -->
    <el-dialog title="提交报销审批" :visible.sync="submitVisible" width="520px" :close-on-click-modal="false">
      <div class="submit-target" v-if="submitClaim">
        <b>{{ submitClaim.title }}</b>
        <p>{{ submitClaim.claimNo }} · ¥ {{ money(submitClaim.amount) }} · {{ submitClaim.invoiceCount }} 张发票</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="审批流程" v-if="submitNodes.length">
          <div class="fixed-flow">
            <el-tag v-for="(n, i) in submitNodes" :key="i" size="mini" type="info" class="flow-node">
              {{ i + 1 }}. {{ n.nodeName }}（{{ n.approverName }}）
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="审批人" v-else required>
          <el-select v-model="submitForm.approverUserId" filterable placeholder="该类型未配置固定节点，请选择审批人"
                     style="width:100%">
            <el-option v-for="u in accountList" :key="u.id" :label="u.displayName" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmSubmit">确认提交</el-button>
      </div>
    </el-dialog>

    <!-- 审批意见 -->
    <el-dialog :title="actionMode === 'approve' ? '审批通过' : '驳回申请'" :visible.sync="actionVisible"
               width="520px" :close-on-click-modal="false">
      <div class="submit-target" v-if="actionTarget">
        <b>{{ actionTarget.title }}</b>
        <p>{{ actionTarget.claimNo }} · ¥ {{ money(actionTarget.amount) }}</p>
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

    <!-- 详情抽屉 -->
    <el-drawer :visible.sync="detailVisible" size="52%" :with-header="false">
      <div v-if="detail" class="exp-detail" v-loading="detailLoading">
        <div class="detail-head">
          <div class="detail-title">
            <el-tag size="mini" effect="plain" class="cat-tag">{{ categoryLabel(detail.claim.category) }}</el-tag>
            {{ detail.claim.title }}
          </div>
          <div class="detail-meta">
            <span>{{ detail.claim.claimNo }}</span>
            <el-tag size="mini" :type="statusTag(detail.claim.status)">{{ statusLabel(detail.claim.status) }}</el-tag>
            <span v-if="detail.flow && detail.flow.status === 'PENDING'">
              第 {{ detail.flow.currentStep }}/{{ detail.flow.totalNodes }} 级 · {{ detail.flow.currentNodeName || '审批' }}
            </span>
          </div>
        </div>

        <div class="amount-banner">
          <span class="amount-label">报销金额</span>
          <span class="amount-value">¥ {{ money(detail.claim.amount) }}</span>
          <span class="amount-sub">{{ detail.claim.invoiceCount }} 张发票</span>
        </div>

        <div class="info-grid">
          <div class="info-item"><span class="label">申请人</span><span>{{ detail.claim.applicantName }}</span></div>
          <div class="info-item"><span class="label">当前审批人</span><span>{{ detail.claim.currentApproverName || '—' }}</span></div>
          <div class="info-item"><span class="label">提交时间</span><span>{{ detail.claim.submittedAt || '—' }}</span></div>
          <div class="info-item"><span class="label">完结时间</span><span>{{ detail.claim.finishedAt || '—' }}</span></div>
        </div>

        <div class="detail-block" v-if="detail.claim.description">
          <div class="block-label">报销说明</div>
          <div class="detail-content">{{ detail.claim.description }}</div>
        </div>

        <div class="detail-block">
          <div class="block-label">发票明细</div>
          <el-table :data="detail.invoices" size="small" empty-text="暂无发票">
            <el-table-column label="类型" width="110">
              <template slot-scope="{ row }">{{ invoiceTypeLabel(row.invoiceType) }}</template>
            </el-table-column>
            <el-table-column prop="invoiceNumber" label="发票号码" min-width="130" show-overflow-tooltip></el-table-column>
            <el-table-column prop="invoiceDate" label="开票日期" width="105"></el-table-column>
            <el-table-column prop="sellerName" label="销售方" min-width="140" show-overflow-tooltip></el-table-column>
            <el-table-column label="价税合计" width="105" align="right">
              <template slot-scope="{ row }">¥ {{ money(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column label="校验" width="100" align="center">
              <template slot-scope="{ row }">
                <el-tag size="mini" :type="verifyTag(row.verifyStatus)" :title="row.verifyMessage">
                  {{ verifyLabel(row.verifyStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-block" v-if="detail.records && detail.records.length">
          <div class="block-label">审批轨迹</div>
          <el-timeline>
            <el-timeline-item v-for="r in detail.records" :key="r.id" :timestamp="r.createdAt" placement="top"
                              :color="recordColor(r.action)">
              <div class="record-line">
                <b>{{ r.operatorName }}</b>
                <span class="record-action">{{ actionLabel(r.action) }}</span>
              </div>
              <div v-if="r.comment" class="record-comment">{{ r.comment }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="detail-actions">
          <template v-if="detail.claim.canApprove">
            <el-button type="primary" size="small" icon="el-icon-check"
                       @click="openAction(detail.claim, 'approve')">通过</el-button>
            <el-button type="danger" size="small" plain icon="el-icon-close"
                       @click="openAction(detail.claim, 'reject')">驳回</el-button>
          </template>
          <el-button size="small" plain type="warning" icon="el-icon-refresh-left"
                     v-if="detail.claim.canCancel" @click="doCancel(detail.claim)">撤回申请</el-button>
          <el-button size="small" plain icon="el-icon-edit"
                     v-if="detail.claim.canEdit && hasPerm('expense:update')"
                     @click="openEdit(detail.claim)">编辑</el-button>
          <el-button size="small" plain icon="el-icon-s-promotion"
                     v-if="detail.claim.canSubmit && hasPerm('expense:submit')"
                     @click="openSubmitDialog(detail.claim)">提交审批</el-button>
          <el-button size="small" type="danger" plain icon="el-icon-delete"
                     v-if="detail.claim.canDelete && hasPerm('expense:delete')"
                     @click="doDelete(detail.claim)">删除</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  listExpenses, getExpenseStats, getExpense, createExpense, updateExpense, deleteExpense,
  submitExpense, cancelExpense, approveExpense, rejectExpense, recognizeInvoice,
  verifyInvoice, getOcrStatus
} from '@/api/modules/expense'
import { listWorkflowNodes, listAccounts } from '@/api/modules/workflow'

const CATEGORY_OPTIONS = [
  { value: 'TRAVEL', label: '差旅费' },
  { value: 'OFFICE', label: '办公费' },
  { value: 'MEETING', label: '会议费' },
  { value: 'OTHER', label: '其他' }
]
const STATUS_OPTIONS = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING', label: '审批中' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'CANCELLED', label: '已撤回' }
]
const INVOICE_TYPE_OPTIONS = [
  { value: 'VAT_SPECIAL', label: '增值税专用发票' },
  { value: 'VAT_NORMAL', label: '增值税普通发票' },
  { value: 'VAT_ELECTRONIC', label: '电子发票' },
  { value: 'OTHER', label: '其他' }
]

function emptyInvoiceForm() {
  return {
    invoiceType: 'VAT_NORMAL',
    invoiceCode: '',
    invoiceNumber: '',
    invoiceDate: '',
    sellerName: '',
    buyerName: '',
    amount: null,
    taxAmount: null,
    totalAmount: null,
    source: 'MANUAL',
    verifyStatus: 'UNVERIFIED',
    verifyMessage: ''
  }
}

export default {
  name: 'Expense',
  data() {
    return {
      scope: 'mine',
      query: { keyword: '', status: '', category: '' },
      list: [],
      loading: false,
      stats: {},
      statCards: [
        { key: 'draft', label: '草稿', icon: 'el-icon-edit', bg: 'linear-gradient(135deg,#94a3b8,#64748b)' },
        { key: 'pending', label: '审批中', icon: 'el-icon-time', bg: 'linear-gradient(135deg,#6366f1,#4f46e5)' },
        { key: 'approved', label: '已通过', icon: 'el-icon-circle-check', bg: 'linear-gradient(135deg,#10b981,#059669)' },
        { key: 'rejected', label: '已驳回', icon: 'el-icon-circle-close', bg: 'linear-gradient(135deg,#ef4444,#dc2626)' },
        { key: 'todo', label: '待我审批', icon: 'el-icon-s-check', bg: 'linear-gradient(135deg,#f59e0b,#d97706)' }
      ],
      categoryOptions: CATEGORY_OPTIONS,
      statusOptions: STATUS_OPTIONS,
      invoiceTypeOptions: INVOICE_TYPE_OPTIONS,
      ocrEnabled: false,
      // 报销单表单
      dialogVisible: false,
      saving: false,
      form: { id: null, title: '', category: 'OTHER', description: '', invoices: [] },
      rules: {
        title: [{ required: true, message: '请填写报销事由', trigger: 'blur' }],
        category: [{ required: true, message: '请选择费用分类', trigger: 'change' }]
      },
      // 发票录入
      invoiceVisible: false,
      invoiceIndex: -1,
      invoiceForm: emptyInvoiceForm(),
      invoiceRules: {
        invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
        invoiceNumber: [{ required: true, message: '请填写发票号码', trigger: 'blur' }],
        totalAmount: [{ required: true, message: '请填写价税合计', trigger: 'blur' }]
      },
      recognizing: false,
      verifyingInvoice: false,
      invoiceVerifyResult: null,
      // 提交审批
      submitVisible: false,
      submitting: false,
      submitClaim: null,
      submitNodes: [],
      accountList: [],
      submitForm: { approverUserId: null },
      // 审批意见
      actionVisible: false,
      actionMode: 'approve',
      actionTarget: null,
      actionComment: '',
      acting: false,
      // 详情
      detailVisible: false,
      detailLoading: false,
      detail: null
    }
  },
  computed: {
    isAdmin() {
      return this.$store.getters.isAdmin
    },
    formTotal() {
      return (this.form.invoices || []).reduce((sum, i) => sum + Number(i.totalAmount || 0), 0)
    },
    formInvoiceCount() {
      return (this.form.invoices || []).length
    }
  },
  created() {
    this.loadList()
    this.loadStats()
    this.loadOcrStatus()
  },
  methods: {
    hasPerm(code) {
      return this.$store.getters.hasPermission(code)
    },
    money(value) {
      return Number(value || 0).toFixed(2)
    },
    categoryLabel(v) {
      const c = CATEGORY_OPTIONS.find(x => x.value === v)
      return c ? c.label : v || '其他'
    },
    statusLabel(v) {
      const s = STATUS_OPTIONS.find(x => x.value === v)
      return s ? s.label : v
    },
    statusTag(v) {
      return { DRAFT: 'info', PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }[v] || 'info'
    },
    invoiceTypeLabel(v) {
      const t = INVOICE_TYPE_OPTIONS.find(x => x.value === v)
      return t ? t.label : v || '其他'
    },
    verifyLabel(v) {
      return { VERIFIED: '通过', INVALID: '不通过', UNVERIFIED: '待校验' }[v] || '待校验'
    },
    verifyTag(v) {
      return { VERIFIED: 'success', INVALID: 'danger', UNVERIFIED: 'info' }[v] || 'info'
    },
    actionLabel(v) {
      return { SUBMIT: '提交申请', APPROVE: '审批通过', REJECT: '驳回', CANCEL: '撤回', DELEGATE: '转办' }[v] || v
    },
    recordColor(v) {
      return { SUBMIT: '#6366f1', APPROVE: '#10b981', REJECT: '#ef4444', CANCEL: '#f59e0b', DELEGATE: '#3b82f6' }[v] || '#cbd5e1'
    },
    async loadList() {
      this.loading = true
      try {
        const params = { scope: this.scope }
        if (this.query.keyword) params.keyword = this.query.keyword
        if (this.query.status) params.status = this.query.status
        if (this.query.category) params.category = this.query.category
        const res = await listExpenses(params)
        this.list = res.data || []
      } catch (e) {
        this.$message.error(e.message || '加载报销单失败')
      } finally {
        this.loading = false
      }
    },
    async loadStats() {
      try {
        const res = await getExpenseStats()
        this.stats = res.data || {}
      } catch (e) { /* 静默 */ }
    },
    async loadOcrStatus() {
      try {
        const res = await getOcrStatus()
        this.ocrEnabled = !!(res.data && res.data.enabled)
      } catch (e) { this.ocrEnabled = false }
    },
    refreshAll() {
      this.loadList()
      this.loadStats()
      if (this.detailVisible && this.detail) {
        this.openDetail(this.detail.claim.id)
      }
    },
    openCreate() {
      this.form = { id: null, title: '', category: 'OTHER', description: '', invoices: [] }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    async openEdit(row) {
      try {
        const res = await getExpense(row.id)
        const data = res.data
        if (!data || !data.claim) return
        this.form = {
          id: data.claim.id,
          title: data.claim.title,
          category: data.claim.category || 'OTHER',
          description: data.claim.description || '',
          invoices: (data.invoices || []).map(i => ({ ...i }))
        }
        this.dialogVisible = true
        this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      } catch (e) {
        this.$message.error(e.message || '加载报销单失败')
      }
    },
    saveForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.saving = true
        const payload = {
          title: this.form.title,
          category: this.form.category,
          description: this.form.description,
          invoices: this.form.invoices.map(i => ({
            invoiceType: i.invoiceType,
            invoiceCode: i.invoiceCode,
            invoiceNumber: i.invoiceNumber,
            invoiceDate: i.invoiceDate,
            sellerName: i.sellerName,
            buyerName: i.buyerName,
            amount: i.amount,
            taxAmount: i.taxAmount,
            totalAmount: i.totalAmount,
            source: i.source
          }))
        }
        try {
          if (this.form.id) {
            await updateExpense(this.form.id, payload)
            this.$message.success('报销单已更新')
          } else {
            await createExpense(payload)
            this.$message.success('报销单已保存为草稿')
          }
          this.dialogVisible = false
          this.refreshAll()
        } catch (e) {
          this.$message.error(e.message || '保存失败')
        } finally {
          this.saving = false
        }
      })
    },
    removeInvoice(index) {
      this.form.invoices.splice(index, 1)
    },
    openInvoiceDialog(index) {
      this.invoiceIndex = index
      this.invoiceForm = index >= 0 ? { ...emptyInvoiceForm(), ...this.form.invoices[index] } : emptyInvoiceForm()
      this.invoiceVerifyResult = null
      this.invoiceVisible = true
      this.$nextTick(() => this.$refs.invoiceForm && this.$refs.invoiceForm.clearValidate())
    },
    beforeOcrUpload(file) {
      if (!this.ocrEnabled) {
        this.$message.warning('未检测到本地 Tesseract OCR 环境，请手工录入')
        return false
      }
      const isPdf = file.type === 'application/pdf' || /\.pdf$/i.test(file.name)
      const isImage = file.type && file.type.startsWith('image/')
      if (!isPdf && !isImage) {
        this.$message.warning('仅支持上传发票图片（jpg / png）或 PDF 文件')
        return false
      }
      const maxSize = isPdf ? 10 * 1024 * 1024 : 4 * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.warning(isPdf ? '发票 PDF 不能超过 10MB' : '发票图片不能超过 4MB')
        return false
      }
      return true
    },
    async handleOcr(option) {
      this.recognizing = true
      try {
        const res = await recognizeInvoice(option.file)
        const d = res.data || {}
        this.invoiceForm = {
          ...this.invoiceForm,
          invoiceType: d.invoiceType || 'VAT_NORMAL',
          invoiceCode: d.invoiceCode || '',
          invoiceNumber: d.invoiceNumber || '',
          invoiceDate: d.invoiceDate || '',
          sellerName: d.sellerName || '',
          buyerName: d.buyerName || '',
          amount: d.amount === null || d.amount === undefined ? null : Number(d.amount),
          taxAmount: d.taxAmount === null || d.taxAmount === undefined ? null : Number(d.taxAmount),
          totalAmount: d.totalAmount === null || d.totalAmount === undefined ? null : Number(d.totalAmount),
          source: 'OCR'
        }
        this.invoiceVerifyResult = null
        this.$message.success('识别完成，请核对票面信息')
      } catch (e) {
        this.$message.error(e.message || '发票识别失败')
      } finally {
        this.recognizing = false
      }
    },
    invoicePayload() {
      const f = this.invoiceForm
      return {
        invoiceType: f.invoiceType,
        invoiceCode: f.invoiceCode,
        invoiceNumber: f.invoiceNumber,
        invoiceDate: f.invoiceDate,
        sellerName: f.sellerName,
        buyerName: f.buyerName,
        amount: f.amount,
        taxAmount: f.taxAmount,
        totalAmount: f.totalAmount,
        source: f.source
      }
    },
    verifyInvoiceForm() {
      this.$refs.invoiceForm.validate(async (valid) => {
        if (!valid) return
        this.verifyingInvoice = true
        try {
          const res = await verifyInvoice({ claimId: this.form.id, invoice: this.invoicePayload() })
          this.invoiceVerifyResult = res.data
        } catch (e) {
          this.invoiceVerifyResult = { status: 'INVALID', message: e.message || '校验失败' }
        } finally {
          this.verifyingInvoice = false
        }
      })
    },
    confirmInvoice() {
      this.$refs.invoiceForm.validate((valid) => {
        if (!valid) return
        const invoice = { ...this.invoicePayload() }
        if (this.invoiceVerifyResult) {
          invoice.verifyStatus = this.invoiceVerifyResult.status
          invoice.verifyMessage = this.invoiceVerifyResult.message
        } else {
          invoice.verifyStatus = 'UNVERIFIED'
          invoice.verifyMessage = ''
        }
        if (this.invoiceIndex >= 0) {
          this.form.invoices.splice(this.invoiceIndex, 1, invoice)
        } else {
          this.form.invoices.push(invoice)
        }
        this.invoiceVisible = false
      })
    },
    async openSubmitDialog(row) {
      this.submitClaim = row
      this.submitForm = { approverUserId: null }
      this.submitVisible = true
      try {
        const [nodesRes, accountsRes] = await Promise.all([
          listWorkflowNodes('EXPENSE'),
          listAccounts()
        ])
        this.submitNodes = nodesRes.data || []
        this.accountList = accountsRes.data || []
      } catch (e) {
        this.submitNodes = []
        this.accountList = []
      }
    },
    async confirmSubmit() {
      if (!this.submitNodes.length && !this.submitForm.approverUserId) {
        this.$message.warning('请选择审批人')
        return
      }
      this.submitting = true
      try {
        await submitExpense(this.submitClaim.id, { approverUserId: this.submitForm.approverUserId })
        this.$message.success('报销单已提交审批')
        this.submitVisible = false
        this.refreshAll()
      } catch (e) {
        this.$message.error(e.message || '提交失败')
      } finally {
        this.submitting = false
      }
    },
    async doCancel(row) {
      try {
        await this.$confirm('确认撤回该报销单？撤回后可重新编辑提交。', '提示', { type: 'warning' })
        await cancelExpense(row.id)
        this.$message.success('已撤回')
        this.refreshAll()
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '撤回失败')
      }
    },
    async doDelete(row) {
      try {
        await this.$confirm('确认删除该报销单？删除后不可恢复。', '提示', { type: 'warning' })
        await deleteExpense(row.id)
        this.$message.success('删除成功')
        if (this.detail && this.detail.claim.id === row.id) this.detailVisible = false
        this.refreshAll()
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '删除失败')
      }
    },
    openAction(row, mode) {
      this.actionMode = mode
      this.actionTarget = row
      this.actionComment = ''
      this.actionVisible = true
    },
    async submitAction() {
      if (this.actionMode === 'reject' && !this.actionComment.trim()) {
        this.$message.warning('请填写驳回原因')
        return
      }
      this.acting = true
      try {
        const payload = { comment: this.actionComment }
        if (this.actionMode === 'approve') {
          await approveExpense(this.actionTarget.id, payload)
          this.$message.success('审批通过')
        } else {
          await rejectExpense(this.actionTarget.id, payload)
          this.$message.success('已驳回')
        }
        this.actionVisible = false
        this.refreshAll()
      } catch (e) {
        this.$message.error(e.message || '操作失败')
      } finally {
        this.acting = false
      }
    },
    async openDetail(id) {
      this.detailLoading = true
      try {
        const res = await getExpense(id)
        this.detail = res.data
        this.detailVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载详情失败')
      } finally {
        this.detailLoading = false
      }
    }
  }
}
</script>

<style scoped>
.expense-page { padding: 16px 20px; }

/* 统计卡 */
.stat-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 16px; }
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
.toolbar {
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  padding: 4px 0 14px;
}
.toolbar-left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

.exp-table >>> .el-table__row { cursor: pointer; }
.cat-tag { margin-right: 8px; }
.exp-title { font-weight: 500; color: #1f2937; }
.amount-text { color: #d97706; font-weight: 600; }
.danger-text { color: #f56c6c; }

/* 表单发票区 */
.invoice-panel { width: 100%; }
.invoice-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.invoice-summary { font-size: 13px; color: #6b7280; }
.invoice-summary b { margin-left: 4px; }
.invoice-actions { display: flex; gap: 8px; align-items: center; }

.ocr-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.form-tip { font-size: 12px; color: #9aa3b2; }

/* 提交/审批弹窗 */
.submit-target {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 12px; margin-bottom: 16px;
}
.submit-target b { font-size: 14px; color: #1f2937; }
.submit-target p { margin: 6px 0 0; font-size: 13px; color: #6b7280; }
.fixed-flow { display: flex; flex-wrap: wrap; gap: 6px; }
.flow-node { margin-bottom: 4px; }

/* 详情 */
.exp-detail { padding: 8px 24px 24px; }
.detail-head { margin-bottom: 16px; }
.detail-title { font-size: 19px; font-weight: 700; color: #1f2937; line-height: 1.5; }
.detail-meta { display: flex; align-items: center; gap: 10px; margin-top: 8px; font-size: 12px; color: #9aa3b2; flex-wrap: wrap; }
.amount-banner {
  display: flex; align-items: baseline; gap: 10px; padding: 16px 18px; border-radius: 10px; margin-bottom: 16px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7); border: 1px solid #fde68a;
}
.amount-label { font-size: 13px; color: #92400e; }
.amount-value { font-size: 26px; font-weight: 700; color: #b45309; }
.amount-sub { font-size: 12px; color: #a16207; }
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px 24px; margin-bottom: 16px; }
.info-item { display: flex; gap: 10px; font-size: 13px; color: #374151; }
.info-item .label { color: #9aa3b2; min-width: 72px; }
.detail-block { margin-bottom: 18px; }
.block-label { font-size: 12px; font-weight: 600; color: #6b7280; margin-bottom: 8px; letter-spacing: .5px; }
.detail-content {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.8; white-space: pre-wrap;
}
.record-line { font-size: 13px; color: #374151; }
.record-action { margin-left: 8px; color: #6b7280; }
.record-comment { margin-top: 4px; font-size: 12px; color: #6b7280; }
.detail-actions { margin-top: 20px; display: flex; gap: 8px; flex-wrap: wrap; }
</style>
