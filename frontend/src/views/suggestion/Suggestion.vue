<template>
  <div class="suggestion-page">
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

    <!-- 工具条 -->
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
        <el-input v-model="query.keyword" size="small" clearable placeholder="搜索标题或内容" style="width:220px"
                  @keyup.enter.native="loadList" @clear="loadList">
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
      </div>
      <el-button type="primary" size="small" icon="el-icon-edit-outline"
                 v-if="hasPerm('suggestion:create')" @click="openSubmit">我要提建议</el-button>
    </div>

    <!-- 列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" size="small" empty-text="暂无建议反馈"
                @row-click="row => openDetail(row.id)" class="sug-table">
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip>
          <template slot-scope="{ row }">
            <span class="sug-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="categoryTag(row.category)" effect="plain">
              {{ categoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="提交人" width="120"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="150"></el-table-column>
        <el-table-column label="操作" width="160" align="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click.stop="openDetail(row.id)">详情</el-button>
            <el-button type="text" size="mini" v-if="canReply" @click.stop="openReply(row)">回复</el-button>
            <el-button type="text" size="mini" class="danger-text"
                       v-if="row.mine && row.status === 'PENDING'"
                       @click.stop="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer :visible.sync="detailVisible" size="48%" :with-header="false">
      <div v-if="detail" class="sug-detail">
        <h2 class="detail-title">{{ detail.title }}</h2>
        <div class="detail-tags">
          <el-tag size="mini" :type="categoryTag(detail.category)" effect="plain">{{ categoryLabel(detail.category) }}</el-tag>
          <el-tag size="mini" :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          <span class="detail-meta">{{ detail.submitterName }} · {{ detail.createdAt }}</span>
        </div>
        <div class="detail-block">
          <div class="block-label">反馈内容</div>
          <div class="detail-content">{{ detail.content }}</div>
        </div>
        <div class="detail-block" v-if="detail.replyContent">
          <div class="block-label">官方回复</div>
          <div class="reply-box">
            <div class="reply-text">{{ detail.replyContent }}</div>
            <div class="reply-meta">{{ detail.replierName }} · {{ detail.repliedAt }}</div>
          </div>
        </div>
        <div class="detail-actions">
          <el-button size="small" type="primary" plain icon="el-icon-chat-line-round"
                     v-if="canReply" @click="openReply(detail)">回复处理</el-button>
          <el-button size="small" plain icon="el-icon-edit"
                     v-if="detail.mine && detail.status === 'PENDING'" @click="openEdit(detail)">编辑</el-button>
          <el-button size="small" type="danger" plain icon="el-icon-delete"
                     v-if="detail.mine || canReply" @click="doDelete">删除</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 提交/编辑弹窗 -->
    <el-dialog :title="form.id ? '编辑建议' : '提交建议反馈'" :visible.sync="dialogVisible"
               width="600px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="70px">
        <el-form-item label="分类" prop="category">
          <el-radio-group v-model="form.category">
            <el-radio-button v-for="c in categoryOptions" :key="c.value" :label="c.value">{{ c.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话概括你的建议"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="7" maxlength="5000" show-word-limit
                    placeholder="请描述你的建议、遇到的问题或期望的改进"></el-input>
        </el-form-item>
        <el-form-item label="匿名">
          <el-switch v-model="form.anonymous"></el-switch>
          <span class="form-tip">开启后其他人看不到你的姓名</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ form.id ? '保存' : '提交' }}</el-button>
      </div>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog title="回复处理" :visible.sync="replyVisible" width="600px" :close-on-click-modal="false">
      <div class="reply-target" v-if="replyTarget">
        <b>{{ replyTarget.title }}</b>
        <p>{{ replyTarget.content }}</p>
      </div>
      <el-form :model="replyForm" :rules="replyRules" ref="replyForm" label-width="80px">
        <el-form-item label="回复内容" prop="replyContent">
          <el-input v-model="replyForm.replyContent" type="textarea" :rows="6" maxlength="2000" show-word-limit
                    placeholder="填写处理意见或回复内容"></el-input>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-radio-group v-model="replyForm.status">
            <el-radio label="PROCESSING">处理中</el-radio>
            <el-radio label="RESOLVED">已解决</el-radio>
            <el-radio label="CLOSED">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">提交回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listSuggestions, getSuggestion, createSuggestion, updateSuggestion,
  replySuggestion, deleteSuggestion, getSuggestionStats
} from '@/api/modules/suggestion'

const CATEGORY_OPTIONS = [
  { value: 'IMPROVEMENT', label: '改进建议' },
  { value: 'FEATURE', label: '功能需求' },
  { value: 'BUG', label: '问题反馈' },
  { value: 'OTHER', label: '其他' }
]
const STATUS_OPTIONS = [
  { value: 'PENDING', label: '待处理' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'RESOLVED', label: '已解决' },
  { value: 'CLOSED', label: '已关闭' }
]

export default {
  name: 'Suggestion',
  data() {
    return {
      categoryOptions: CATEGORY_OPTIONS,
      statusOptions: STATUS_OPTIONS,
      query: { keyword: '', status: '', category: '' },
      list: [],
      loading: false,
      stats: {},
      statCards: [
        { key: 'total', label: '总反馈', icon: 'el-icon-data-analysis', bg: 'linear-gradient(135deg,#6366f1,#4f46e5)' },
        { key: 'pending', label: '待处理', icon: 'el-icon-time', bg: 'linear-gradient(135deg,#f59e0b,#d97706)' },
        { key: 'processing', label: '处理中', icon: 'el-icon-loading', bg: 'linear-gradient(135deg,#3b82f6,#2563eb)' },
        { key: 'resolved', label: '已解决', icon: 'el-icon-circle-check', bg: 'linear-gradient(135deg,#10b981,#059669)' }
      ],
      detailVisible: false,
      detail: null,
      dialogVisible: false,
      saving: false,
      form: { id: null, title: '', content: '', category: 'IMPROVEMENT', anonymous: false },
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      replyVisible: false,
      replying: false,
      replyTarget: null,
      replyForm: { replyContent: '', status: 'RESOLVED' },
      replyRules: {
        replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    canReply() {
      return this.$store.getters.hasPermission('suggestion:reply')
    }
  },
  created() {
    this.loadList()
    this.loadStats()
  },
  methods: {
    hasPerm(code) {
      return this.$store.getters.hasPermission(code)
    },
    categoryLabel(v) {
      const c = CATEGORY_OPTIONS.find(x => x.value === v)
      return c ? c.label : v || '其他'
    },
    categoryTag(v) {
      return { IMPROVEMENT: 'success', FEATURE: 'primary', BUG: 'danger', OTHER: 'info' }[v] || 'info'
    },
    statusLabel(v) {
      const s = STATUS_OPTIONS.find(x => x.value === v)
      return s ? s.label : v
    },
    statusTag(v) {
      return { PENDING: 'warning', PROCESSING: 'primary', RESOLVED: 'success', CLOSED: 'info' }[v] || 'info'
    },
    async loadList() {
      this.loading = true
      try {
        const params = {}
        if (this.query.keyword) params.keyword = this.query.keyword
        if (this.query.status) params.status = this.query.status
        if (this.query.category) params.category = this.query.category
        const res = await listSuggestions(params)
        this.list = res.data || []
      } catch (e) {
        this.$message.error(e.message || '加载建议反馈失败')
      } finally {
        this.loading = false
      }
    },
    async loadStats() {
      try {
        const res = await getSuggestionStats()
        this.stats = res.data || {}
      } catch (e) { /* 静默 */ }
    },
    async openDetail(id) {
      try {
        const res = await getSuggestion(id)
        this.detail = res.data
        this.detailVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载详情失败')
      }
    },
    openSubmit() {
      this.form = { id: null, title: '', content: '', category: 'IMPROVEMENT', anonymous: false }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    openEdit(row) {
      this.form = {
        id: row.id, title: row.title, content: row.content,
        category: row.category || 'IMPROVEMENT', anonymous: !!row.anonymous
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.saving = true
        const payload = {
          title: this.form.title, content: this.form.content,
          category: this.form.category, anonymous: this.form.anonymous
        }
        try {
          if (this.form.id) {
            await updateSuggestion(this.form.id, payload)
            this.$message.success('更新成功')
          } else {
            await createSuggestion(payload)
            this.$message.success('提交成功，我们会尽快处理')
          }
          this.dialogVisible = false
          this.loadList()
          this.loadStats()
          if (this.detail && this.form.id && this.detail.id === this.form.id) this.openDetail(this.form.id)
        } catch (e) {
          this.$message.error(e.message || '提交失败')
        } finally {
          this.saving = false
        }
      })
    },
    openReply(row) {
      this.replyTarget = row
      this.replyForm = { replyContent: row.replyContent || '', status: row.status === 'PENDING' ? 'RESOLVED' : row.status }
      this.replyVisible = true
      this.$nextTick(() => this.$refs.replyForm && this.$refs.replyForm.clearValidate())
    },
    submitReply() {
      this.$refs.replyForm.validate(async (valid) => {
        if (!valid) return
        this.replying = true
        try {
          await replySuggestion(this.replyTarget.id, this.replyForm)
          this.$message.success('回复成功')
          this.replyVisible = false
          this.loadList()
          this.loadStats()
          if (this.detail && this.detail.id === this.replyTarget.id) this.openDetail(this.replyTarget.id)
        } catch (e) {
          this.$message.error(e.message || '回复失败')
        } finally {
          this.replying = false
        }
      })
    },
    async doDelete() {
      try {
        await this.$confirm('确认删除这条建议反馈？', '提示', { type: 'warning' })
        await deleteSuggestion(this.detail.id)
        this.$message.success('删除成功')
        this.detailVisible = false
        this.loadList()
        this.loadStats()
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '删除失败')
      }
    }
  }
}
</script>

<style scoped>
.suggestion-page { padding: 16px 20px; }

/* 统计卡 */
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

/* 工具条 */
.toolbar {
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  background: #fff; border: 1px solid #e5e9f0; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px;
}
.toolbar-left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }

.table-card { border-radius: 10px; }
.sug-table >>> .el-table__row { cursor: pointer; }
.sug-title { font-weight: 500; color: #1f2937; }
.danger-text { color: #f56c6c; }

/* 详情 */
.sug-detail { padding: 8px 24px 24px; }
.detail-title { font-size: 19px; font-weight: 700; color: #1f2937; margin: 0 0 10px; }
.detail-tags { display: flex; align-items: center; gap: 8px; margin-bottom: 18px; flex-wrap: wrap; }
.detail-meta { font-size: 12px; color: #9aa3b2; }
.detail-block { margin-bottom: 18px; }
.block-label { font-size: 12px; font-weight: 600; color: #6b7280; margin-bottom: 8px; letter-spacing: .5px; }
.detail-content {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 14px;
  font-size: 14px; color: #374151; line-height: 1.8; white-space: pre-wrap;
}
.reply-box { background: #f0fdf4; border: 1px solid #d1fae5; border-radius: 8px; padding: 14px; }
.reply-text { font-size: 14px; color: #166534; line-height: 1.8; white-space: pre-wrap; }
.reply-meta { margin-top: 8px; font-size: 12px; color: #6b7280; text-align: right; }
.detail-actions { margin-top: 20px; }

.form-tip { margin-left: 12px; font-size: 12px; color: #9aa3b2; }
.reply-target {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 12px; margin-bottom: 16px;
}
.reply-target b { font-size: 14px; color: #1f2937; }
.reply-target p { margin: 6px 0 0; font-size: 13px; color: #6b7280; line-height: 1.6; max-height: 80px; overflow: auto; }
</style>
