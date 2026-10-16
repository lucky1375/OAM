<template>
  <div class="announcement-page">
    <!-- 顶部工具条 -->
    <div class="ann-top">
      <div class="ann-title">
        <i class="el-icon-bell"></i> 公告通知
        <span class="ann-count" v-if="list.length">共 {{ list.length }} 条</span>
      </div>
      <div class="ann-actions">
        <el-select
          v-if="canManage"
          v-model="statusFilter"
          size="small"
          style="width: 110px"
          @change="loadList"
        >
          <el-option label="全部" value=""></el-option>
          <el-option label="已发布" value="PUBLISHED"></el-option>
          <el-option label="草稿" value="DRAFT"></el-option>
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索公告标题"
          clearable
          size="small"
          style="width: 220px"
          @keyup.enter.native="loadList"
          @clear="loadList"
        >
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-edit"
          v-if="canManage"
          @click="openDialog(null)"
        >发布公告</el-button>
      </div>
    </div>

    <!-- 公告列表 -->
    <div v-loading="loading" class="ann-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无公告"></el-empty>
      <div
        class="ann-item"
        :class="{ 'is-top': a.top, 'is-draft': a.status === 'DRAFT' }"
        v-for="a in list"
        :key="a.id"
        @click="openDetail(a.id)"
      >
        <div class="ann-item-main">
          <div class="ann-item-head">
            <el-tag v-if="a.top" size="mini" type="danger" effect="dark" class="tag-top">置顶</el-tag>
            <el-tag v-if="a.status === 'DRAFT'" size="mini" type="info" class="tag-draft">草稿</el-tag>
            <span class="ann-item-title">{{ a.title }}</span>
            <span v-if="a.status === 'PUBLISHED' && !a.readByMe" class="unread-dot">未读</span>
          </div>
          <div class="ann-item-summary">{{ a.summary || '（无正文）' }}</div>
          <div class="ann-item-meta">
            <span><i class="el-icon-user"></i>{{ a.publisherName || '系统' }}</span>
            <span><i class="el-icon-time"></i>{{ a.publishTime || '未发布' }}</span>
          </div>
        </div>
        <div class="ann-item-right">
          <span class="read-stat"><i class="el-icon-view"></i>{{ a.readCount }} 人已读</span>
        </div>
      </div>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer :visible.sync="detailVisible" size="52%" :with-header="false">
      <div v-if="detail" class="ann-detail">
        <div class="detail-head">
          <h2 class="detail-title">
            <el-tag v-if="detail.top" size="mini" type="danger" effect="dark">置顶</el-tag>
            <el-tag v-if="detail.status === 'DRAFT'" size="mini" type="info">草稿</el-tag>
            {{ detail.title }}
          </h2>
          <div class="detail-meta">
            <span><i class="el-icon-user"></i>{{ detail.publisherName || '系统' }}</span>
            <span><i class="el-icon-time"></i>{{ detail.publishTime || '未发布' }}</span>
            <span><i class="el-icon-view"></i>{{ detail.readCount }} 人已读</span>
          </div>
        </div>
        <div class="detail-content">{{ detail.content || '（无正文）' }}</div>
        <div class="detail-actions">
          <el-button size="small" plain icon="el-icon-data-line" @click="openReadStats">阅读统计</el-button>
          <el-button size="small" plain icon="el-icon-edit" v-if="canManage" @click="openDialog(detail)">编辑</el-button>
          <el-button size="small" type="danger" plain icon="el-icon-delete" v-if="canManage" @click="doDelete">删除</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 发布/编辑弹窗 -->
    <el-dialog
      :title="form.id ? '编辑公告' : '发布公告'"
      :visible.sync="dialogVisible"
      width="620px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="70px" :rules="rules" ref="form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入公告标题"></el-input>
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            maxlength="5000"
            show-word-limit
            placeholder="请输入公告正文"
          ></el-input>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.top"></el-switch>
          <span class="form-tip">置顶公告将排在列表最前</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="submit('DRAFT')">存为草稿</el-button>
        <el-button type="primary" @click="submit('PUBLISHED')">发布</el-button>
      </div>
    </el-dialog>

    <!-- 阅读统计弹窗 -->
    <el-dialog title="阅读统计" :visible.sync="statsVisible" width="520px">
      <div class="stats-head">
        <span>共 <b>{{ readers.length }}</b> 人已读</span>
      </div>
      <el-table :data="readers" size="small" max-height="360" v-loading="statsLoading" empty-text="暂无阅读记录">
        <el-table-column type="index" label="#" width="50"></el-table-column>
        <el-table-column prop="userName" label="阅读人"></el-table-column>
        <el-table-column prop="readTime" label="阅读时间"></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAnnouncements, getAnnouncement, createAnnouncement, updateAnnouncement,
  deleteAnnouncement, getReadStats
} from '@/api/modules/announcement'

export default {
  name: 'Announcement',
  data() {
    return {
      list: [],
      loading: false,
      keyword: '',
      statusFilter: '',
      detailVisible: false,
      detail: null,
      dialogVisible: false,
      form: { id: null, title: '', content: '', top: false },
      rules: {
        title: [{ required: true, message: '标题不能为空', trigger: 'blur' }]
      },
      statsVisible: false,
      statsLoading: false,
      readers: []
    }
  },
  computed: {
    canManage() {
      return this.hasPerm('announcement:create') || this.hasPerm('announcement:update')
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    hasPerm(code) {
      // 复用 store getter：user 模块未启用 namespace
      return this.$store.getters.hasPermission(code)
    },
    async loadList() {
      this.loading = true
      try {
        const params = {}
        if (this.keyword) params.keyword = this.keyword
        // 无管理权限的用户只能看到已发布公告
        if (this.canManage && this.statusFilter) {
          params.status = this.statusFilter
        } else if (!this.canManage) {
          params.status = 'PUBLISHED'
        }
        const res = await listAnnouncements(params)
        this.list = res.data || []
      } catch (e) {
        this.$message.error(e.message || '加载公告失败')
      } finally {
        this.loading = false
      }
    },
    async openDetail(id) {
      try {
        const res = await getAnnouncement(id)
        this.detail = res.data
        this.detailVisible = true
        // 打开后刷新已读状态
        this.loadList()
      } catch (e) {
        this.$message.error(e.message || '加载公告详情失败')
      }
    },
    openDialog(a) {
      this.form = a
        ? { id: a.id, title: a.title, content: a.content, top: !!a.top }
        : { id: null, title: '', content: '', top: false }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit(status) {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        const payload = {
          title: this.form.title,
          content: this.form.content,
          top: this.form.top,
          status
        }
        try {
          if (this.form.id) {
            await updateAnnouncement(this.form.id, payload)
          } else {
            await createAnnouncement(payload)
          }
          this.$message.success(status === 'DRAFT' ? '已保存草稿' : '发布成功')
          this.dialogVisible = false
          this.loadList()
          if (this.detail && this.form.id === this.detail.id) this.openDetail(this.detail.id)
        } catch (e) {
          this.$message.error(e.message || '操作失败')
        }
      })
    },
    async doDelete() {
      try {
        await this.$confirm('确认删除该公告？删除后阅读记录将一并清除。', '提示', { type: 'warning' })
        await deleteAnnouncement(this.detail.id)
        this.$message.success('删除成功')
        this.detailVisible = false
        this.loadList()
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '删除失败')
      }
    },
    async openReadStats() {
      this.statsVisible = true
      this.statsLoading = true
      this.readers = []
      try {
        const res = await getReadStats(this.detail.id)
        this.readers = res.data || []
      } catch (e) {
        this.$message.error(e.message || '加载阅读统计失败')
      } finally {
        this.statsLoading = false
      }
    }
  }
}
</script>

<style scoped>
.announcement-page { padding: 16px 20px; }
.ann-top {
  display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;
  background: #fff; border: 1px solid #e5e9f0; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px;
}
.ann-title { font-size: 16px; font-weight: 600; color: #1f2937; display: flex; align-items: center; gap: 8px; }
.ann-title i { color: #4f46e5; }
.ann-count { font-size: 12px; font-weight: 400; color: #9aa3b2; }
.ann-actions { display: flex; gap: 8px; align-items: center; }

.ann-list { display: flex; flex-direction: column; gap: 10px; }
.ann-item {
  background: #fff; border: 1px solid #e5e9f0; border-radius: 10px; padding: 14px 18px;
  display: flex; justify-content: space-between; align-items: center; cursor: pointer;
  transition: box-shadow .15s, border-color .15s, transform .15s;
}
.ann-item:hover { box-shadow: 0 6px 18px rgba(40,60,120,.08); border-color: #c9d4f0; transform: translateY(-1px); }
.ann-item.is-top { border-left: 3px solid #f56c6c; }
.ann-item.is-draft { background: #fafbfc; }
.ann-item-main { flex: 1; min-width: 0; }
.ann-item-head { display: flex; align-items: center; gap: 8px; }
.tag-top { margin-right: 2px; }
.ann-item-title { font-size: 15px; font-weight: 600; color: #1f2937; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.unread-dot {
  flex-shrink: 0; font-size: 11px; color: #fff; background: #f56c6c;
  border-radius: 8px; padding: 1px 7px;
}
.ann-item-summary { margin-top: 6px; font-size: 13px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ann-item-meta { margin-top: 8px; display: flex; gap: 16px; font-size: 12px; color: #9aa3b2; }
.ann-item-meta i { margin-right: 4px; }
.ann-item-right { flex-shrink: 0; margin-left: 16px; }
.read-stat { font-size: 12px; color: #4f46e5; }
.read-stat i { margin-right: 3px; }

.ann-detail { padding: 8px 24px 24px; }
.detail-title { font-size: 20px; font-weight: 700; color: #1f2937; margin: 0 0 10px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.detail-meta { display: flex; flex-wrap: wrap; gap: 16px; font-size: 12px; color: #9aa3b2; margin-bottom: 16px; }
.detail-meta i { margin-right: 4px; }
.detail-content {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 16px;
  font-size: 14px; color: #374151; line-height: 1.8; white-space: pre-wrap; min-height: 120px;
}
.detail-actions { margin-top: 18px; }

.form-tip { margin-left: 12px; font-size: 12px; color: #9aa3b2; }
.stats-head { margin-bottom: 12px; font-size: 13px; color: #4b5563; }
.stats-head b { color: #4f46e5; font-size: 16px; }
</style>
