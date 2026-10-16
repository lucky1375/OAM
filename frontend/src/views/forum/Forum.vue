<template>
  <div class="forum-page">
    <!-- 顶部：版块导航 + 搜索 + 发帖 -->
    <div class="forum-top">
      <div class="section-tabs">
        <div class="section-tab" :class="{ active: activeSection === null }" @click="switchSection(null)">全部</div>
        <div class="section-tab" :class="{ active: activeSection === s.id }" v-for="s in sections" :key="s.id" @click="switchSection(s.id)">
          {{ s.name }}
          <span class="tab-count">{{ s.postCount }}</span>
        </div>
        <el-button size="mini" type="text" icon="el-icon-setting" class="section-manage" v-if="hasPerm('forum:update')" @click="openSectionManager">版块管理</el-button>
      </div>
      <div class="forum-actions">
        <el-input v-model="keyword" placeholder="搜索帖子标题/内容" clearable style="width: 220px" size="small" @keyup.enter.native="loadPosts" @clear="loadPosts">
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
        <el-button type="primary" size="small" icon="el-icon-edit" v-if="hasPerm('forum:create')" @click="openPostDialog(null)">发帖</el-button>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div v-loading="loading" class="post-list">
      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子，快来发布第一帖吧"></el-empty>
      <div class="post-item" v-for="p in posts" :key="p.id" @click="openDetail(p.id)">
        <div class="post-main">
          <div class="post-title">{{ p.title }}</div>
          <div class="post-meta">
            <span class="meta-section">{{ p.sectionName }}</span>
            <span class="meta-author"><i class="el-icon-user"></i>{{ p.authorName || '匿名' }}</span>
            <span class="meta-time">{{ p.createdAt }}</span>
          </div>
        </div>
        <div class="post-stats">
          <span class="stat"><i class="el-icon-view"></i>{{ p.commentCount }} 评论</span>
          <span class="stat like"><i class="el-icon-star-on"></i>{{ p.likeCount }} 赞</span>
        </div>
      </div>
    </div>

    <!-- 帖子详情抽屉 -->
    <el-drawer :visible.sync="detailVisible" size="56%" :with-header="false">
      <div v-if="detail" class="post-detail">
        <div class="detail-head">
          <h2 class="detail-title">{{ detail.title }}</h2>
          <div class="detail-meta">
            <el-tag size="mini" effect="plain">{{ detail.sectionName }}</el-tag>
            <span class="d-author">{{ detail.authorName || '匿名' }}</span>
            <span class="d-time">{{ detail.createdAt }}</span>
          </div>
        </div>
        <div class="detail-content">{{ detail.content }}</div>
        <div class="detail-actions">
          <el-button size="small" :type="detail.likedByMe ? 'warning' : 'default'" icon="el-icon-star-on" v-if="hasPerm('forum:like')" @click="doLike">
            {{ detail.likedByMe ? '已赞' : '点赞' }} {{ detail.likeCount }}
          </el-button>
          <el-button size="small" plain icon="el-icon-edit" v-if="hasPerm('forum:update')" @click="openPostDialog(detail)">编辑帖子</el-button>
          <el-button size="small" type="danger" plain icon="el-icon-delete" v-if="canDeletePost" @click="doDeletePost">删除帖子</el-button>
        </div>

        <el-divider content-position="left">评论 ({{ detail.comments.length }})</el-divider>
        <div class="comment-list" v-if="detail.comments.length">
          <div class="comment-item" v-for="c in detail.comments" :key="c.id">
            <div class="comment-body">
              <span class="c-author">{{ c.authorName || '匿名' }}</span>
              <span class="c-time">{{ c.createdAt }}</span>
              <el-button v-if="hasPerm('forum:delete')" class="c-del" size="mini" type="text" icon="el-icon-delete" @click="doDeleteComment(c)">删除</el-button>
            </div>
            <div class="c-content">{{ c.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评论" :image-size="60"></el-empty>

        <div class="comment-input" v-if="hasPerm('forum:reply')">
          <el-input v-model="commentText" type="textarea" :rows="2" maxlength="1000" show-word-limit placeholder="说点什么吧…"></el-input>
          <el-button type="primary" size="small" style="margin-top:8px" @click="doComment">发表评论</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 发帖/编辑弹窗 -->
    <el-dialog :title="postForm.id ? '编辑帖子' : '发布新帖'" :visible.sync="postDialogVisible" width="560px" :close-on-click-modal="false">
      <el-form :model="postForm" label-width="70px" :rules="postRules" ref="postForm">
        <el-form-item label="版块" prop="sectionId">
          <el-select v-model="postForm.sectionId" placeholder="选择版块" style="width: 100%">
            <el-option v-for="s in sections" :key="s.id" :label="s.name" :value="s.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" maxlength="100" show-word-limit placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" :rows="8" maxlength="5000" show-word-limit placeholder="请输入内容"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">提交</el-button>
      </div>
    </el-dialog>

    <!-- 版块管理弹窗 -->
    <el-dialog title="版块管理" :visible.sync="sectionDialogVisible" width="600px">
      <div class="section-manage-row" v-for="s in sections" :key="s.id">
        <div class="sm-info">
          <span class="sm-name">{{ s.name }}</span>
          <span class="sm-desc">{{ s.description || '暂无描述' }}（{{ s.postCount }} 帖）</span>
        </div>
        <div class="sm-actions">
          <el-button size="mini" icon="el-icon-edit" @click="openSectionEdit(s)">编辑</el-button>
          <el-button size="mini" type="danger" plain icon="el-icon-delete" @click="doDeleteSection(s)">删除</el-button>
        </div>
      </div>
      <el-divider></el-divider>
      <div class="section-add">
        <el-input v-model="sectionForm.name" placeholder="新版块名称" style="width: 180px" size="small"></el-input>
        <el-input v-model="sectionForm.description" placeholder="版块描述" style="width: 220px" size="small"></el-input>
        <el-button type="primary" size="small" @click="submitSection">新增版块</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listSections, createSection, updateSection, deleteSection,
  listPosts, getPost, createPost, updatePost, deletePost,
  addComment, deleteComment, toggleLike
} from '@/api/modules/forum'

export default {
  name: 'Forum',
  data() {
    return {
      sections: [],
      activeSection: null,
      keyword: '',
      posts: [],
      loading: false,
      detailVisible: false,
      detail: null,
      commentText: '',
      postDialogVisible: false,
      postForm: { id: null, sectionId: null, title: '', content: '' },
      postRules: {
        sectionId: [{ required: true, message: '请选择版块', trigger: 'change' }],
        title: [{ required: true, message: '标题不能为空', trigger: 'blur' }]
      },
      sectionDialogVisible: false,
      sectionForm: { id: null, name: '', description: '' }
    }
  },
  computed: {
    canDeletePost() {
      if (!this.detail || !this.hasPerm('forum:delete')) return false
      return true
    }
  },
  created() {
    this.loadSections()
    this.loadPosts()
  },
  methods: {
    hasPerm(code) {
      // 复用 store getter：user 模块未启用 namespace，$store.state.user 是模块状态对象而非用户对象
      return this.$store.getters.hasPermission(code)
    },
    async loadSections() {
      try {
        const res = await listSections()
        this.sections = res.data || []
      } catch (e) { /* 权限不足静默 */ }
    },
    switchSection(id) {
      this.activeSection = id
      this.loadPosts()
    },
    async loadPosts() {
      this.loading = true
      try {
        const params = {}
        if (this.activeSection !== null) params.sectionId = this.activeSection
        if (this.keyword) params.keyword = this.keyword
        const res = await listPosts(params)
        this.posts = res.data || []
      } catch (e) {
        this.$message.error(e.message || '加载帖子失败')
      } finally {
        this.loading = false
      }
    },
    openPostDialog(post) {
      if (this.sections.length === 0) {
        this.$message.warning('暂无版块，请先由管理员创建版块')
        return
      }
      this.postForm = post
        ? { id: post.id, sectionId: post.sectionId, title: post.title, content: post.content }
        : { id: null, sectionId: this.activeSection !== null ? this.activeSection : (this.sections[0] && this.sections[0].id), title: '', content: '' }
      this.postDialogVisible = true
    },
    submitPost() {
      this.$refs.postForm.validate(async (valid) => {
        if (!valid) return
        const editingId = this.postForm.id
        try {
          if (editingId) {
            await updatePost(editingId, this.postForm)
            this.$message.success('帖子已更新')
          } else {
            await createPost(this.postForm)
            this.$message.success('发帖成功')
          }
          this.postDialogVisible = false
          this.loadPosts()
          this.loadSections()
          if (editingId) this.openDetail(editingId)
        } catch (e) {
          this.$message.error(e.message || '操作失败')
        }
      })
    },
    async openDetail(id) {
      try {
        const res = await getPost(id)
        this.detail = res.data
        this.commentText = ''
        this.detailVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载帖子详情失败')
      }
    },
    async doLike() {
      try {
        const res = await toggleLike(this.detail.id)
        this.detail.likeCount = res.data
        this.detail.likedByMe = !this.detail.likedByMe
        this.loadPosts()
      } catch (e) {
        this.$message.error(e.message || '操作失败')
      }
    },
    async doComment() {
      if (!this.commentText.trim()) {
        this.$message.warning('评论内容不能为空')
        return
      }
      try {
        await addComment(this.detail.id, { content: this.commentText.trim() })
        this.$message.success('评论成功')
        this.openDetail(this.detail.id)
      } catch (e) {
        this.$message.error(e.message || '评论失败')
      }
    },
    async doDeletePost() {
      try {
        await deletePost(this.detail.id)
        this.$message.success('帖子已删除')
        this.detailVisible = false
        this.loadPosts()
        this.loadSections()
      } catch (e) {
        this.$message.error(e.message || '删除失败')
      }
    },
    async doDeleteComment(c) {
      try {
        await this.$confirm('确认删除该评论？', '提示', { type: 'warning' })
        await deleteComment(c.id)
        this.$message.success('评论已删除')
        this.openDetail(this.detail.id)
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '删除失败')
      }
    },
    // ---------- 版块管理 ----------
    openSectionManager() {
      this.sectionForm = { id: null, name: '', description: '' }
      this.sectionDialogVisible = true
    },
    openSectionEdit(s) {
      this.sectionForm = { id: s.id, name: s.name, description: s.description || '' }
    },
    submitSection() {
      if (!this.sectionForm.name.trim()) {
        this.$message.warning('请输入版块名称')
        return
      }
      const payload = {
        name: this.sectionForm.name.trim(),
        description: this.sectionForm.description || ''
      }
      const done = async () => {
        if (this.sectionForm.id) {
          await updateSection(this.sectionForm.id, payload)
        } else {
          await createSection(payload)
        }
        this.$message.success('保存成功')
        this.loadSections()
        this.sectionForm = { id: null, name: '', description: '' }
      }
      done().catch(e => this.$message.error(e.message || '保存失败'))
    },
    async doDeleteSection(s) {
      try {
        await this.$confirm(`确认删除版块「${s.name}」？`, '提示', { type: 'warning' })
        await deleteSection(s.id)
        this.$message.success('删除成功')
        if (this.activeSection === s.id) this.activeSection = null
        this.loadSections()
        this.loadPosts()
      } catch (e) {
        if (e !== 'cancel' && e.message) this.$message.error(e.message || '删除失败')
      }
    }
  }
}
</script>

<style scoped>
.forum-page { padding: 16px 20px; }
.forum-top {
  display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;
  background: #fff; border: 1px solid #e5e9f0; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px;
}
.section-tabs { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
.section-tab {
  padding: 6px 14px; border-radius: 16px; cursor: pointer; font-size: 13px; color: #4b5563;
  border: 1px solid transparent; transition: all .15s; user-select: none;
}
.section-tab:hover { background: #f3f4f8; }
.section-tab.active { background: linear-gradient(135deg, #7c3aed, #4f46e5); color: #fff; font-weight: 600; box-shadow: 0 3px 8px rgba(79,70,229,.25); }
.tab-count { display: inline-block; margin-left: 4px; font-size: 11px; background: rgba(0,0,0,.08); border-radius: 8px; padding: 0 6px; }
.section-tab.active .tab-count { background: rgba(255,255,255,.25); }
.section-manage { margin-left: 8px; }
.forum-actions { display: flex; gap: 8px; align-items: center; }

.post-list { display: flex; flex-direction: column; gap: 10px; }
.post-item {
  background: #fff; border: 1px solid #e5e9f0; border-radius: 10px; padding: 14px 18px;
  display: flex; justify-content: space-between; align-items: center; cursor: pointer;
  transition: box-shadow .15s, border-color .15s, transform .15s;
}
.post-item:hover { box-shadow: 0 6px 18px rgba(40,60,120,.08); border-color: #c9d4f0; transform: translateY(-1px); }
.post-title { font-size: 15px; font-weight: 600; color: #1f2937; }
.post-meta { margin-top: 6px; display: flex; align-items: center; gap: 12px; font-size: 12px; color: #9aa3b2; }
.meta-section { color: #7c3aed; font-weight: 600; }
.post-stats { display: flex; gap: 14px; font-size: 12px; color: #9aa3b2; flex-shrink: 0; }
.stat i { margin-right: 3px; }
.stat.like { color: #f59e0b; }

.post-detail { padding: 8px 24px 24px; }
.detail-title { font-size: 20px; font-weight: 700; color: #1f2937; margin: 0 0 10px; }
.detail-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #9aa3b2; margin-bottom: 16px; }
.detail-content {
  background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; padding: 16px;
  font-size: 14px; color: #374151; line-height: 1.8; white-space: pre-wrap; min-height: 80px;
}
.detail-actions { margin-top: 16px; }
.comment-list { display: flex; flex-direction: column; gap: 10px; }
.comment-item { background: #f8fafc; border-radius: 8px; padding: 10px 14px; }
.comment-body { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.c-author { font-size: 13px; font-weight: 600; color: #4f46e5; }
.c-time { font-size: 11px; color: #b0b8c4; }
.c-del { margin-left: auto; color: #f56c6c; }
.c-content { font-size: 13px; color: #374151; line-height: 1.6; white-space: pre-wrap; }
.comment-input { margin-top: 14px; }

.section-manage-row {
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  padding: 10px 12px; border: 1px solid #eef2f7; border-radius: 8px; margin-bottom: 8px;
}
.sm-info { display: flex; flex-direction: column; }
.sm-name { font-weight: 600; color: #1f2937; font-size: 14px; }
.sm-desc { font-size: 12px; color: #9aa3b2; }
.section-add { display: flex; gap: 8px; }
</style>
