<template>
  <div class="workbench">
    <!-- 顶部欢迎区 -->
    <div class="welcome-banner">
      <div class="welcome-info">
        <h1 class="greeting">{{ greetingText }}，{{ nickname }}</h1>
        <p class="sub-greeting">{{ currentDate }}</p>
      </div>
      <div class="welcome-stats">
        <div class="stat-item">
          <span class="stat-num">{{ pendingCount }}</span>
          <span class="stat-label">待办事项</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ todayMeeting }}</span>
          <span class="stat-label">今日会议</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ myTask }}</span>
          <span class="stat-label">我的任务</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section-title">常用功能</div>
    <div class="quick-grid">
      <template v-for="entry in quickEntries">
        <div
          v-if="!entry.permission || hasPerm(entry.permission)"
          :key="entry.path"
          class="quick-card"
          @click="$router.push(entry.path)"
        >
          <div class="quick-icon" :style="{ background: entry.color }">
            <i :class="entry.icon"></i>
          </div>
          <div class="quick-label">{{ entry.label }}</div>
          <div class="quick-desc">{{ entry.desc }}</div>
        </div>
      </template>
    </div>

    <!-- 下方三栏 -->
    <el-row :gutter="16" class="bottom-row">
      <!-- 我的待办 -->
      <el-col :span="8">
        <el-card class="bottom-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-s-order"></i> 我的待办</span>
            <el-button type="text" size="mini" @click="$router.push('/workflow')">查看全部 →</el-button>
          </div>
          <div v-loading="todoLoading" class="feed">
            <div v-for="w in todoFlows" :key="w.id" class="feed-item" @click="goWorkflow">
              <span class="feed-dot is-todo"></span>
              <div class="feed-body">
                <div class="feed-title">
                  <span class="type-chip">{{ typeLabel(w.type) }}</span>{{ w.title }}
                </div>
                <div class="feed-meta">
                  {{ w.applicantName }} · {{ w.createdAt }} · 第 {{ w.currentStep }}/{{ w.totalNodes }} 级
                </div>
              </div>
            </div>
            <div v-if="!todoLoading && todoFlows.length === 0" class="empty-state">
              <i class="el-icon-circle-check"></i>
              <p>暂无待办任务</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 公告通知 -->
      <el-col :span="8">
        <el-card class="bottom-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-bell"></i> 公告通知</span>
            <el-button type="text" size="mini" @click="$router.push('/announcement')">更多 →</el-button>
          </div>
          <div v-loading="announcementLoading" class="feed">
            <div v-for="a in announcements" :key="a.id" class="feed-item" @click="$router.push('/announcement')">
              <span class="feed-dot" :class="{ 'is-top': a.top }"></span>
              <div class="feed-body">
                <div class="feed-title">
                  <el-tag v-if="a.top" size="mini" type="danger" effect="dark">置顶</el-tag>
                  {{ a.title }}
                </div>
                <div class="feed-meta">{{ a.publishTime }} · {{ a.readCount }}人已读</div>
              </div>
            </div>
            <div v-if="!announcementLoading && announcements.length === 0" class="empty-state">
              <i class="el-icon-document"></i>
              <p>暂无最新公告</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 企业论坛 -->
      <el-col :span="8">
        <el-card class="bottom-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-s-comment forum-icon"></i> 企业论坛</span>
            <el-button type="text" size="mini" @click="$router.push('/forum')">更多 →</el-button>
          </div>
          <div v-loading="forumLoading" class="feed">
            <div v-for="p in forumPosts" :key="p.id" class="feed-item" @click="$router.push('/forum')">
              <span class="feed-dot is-forum"></span>
              <div class="feed-body">
                <div class="feed-title">{{ p.title }}</div>
                <div class="feed-meta">
                  <span class="feed-section">{{ p.sectionName }}</span>
                  {{ p.createdAt }} ·
                  <i class="el-icon-chat-dot-square"></i>{{ p.commentCount }}
                  <i class="el-icon-star-off"></i>{{ p.likeCount }}
                </div>
              </div>
            </div>
            <div v-if="!forumLoading && forumPosts.length === 0" class="empty-state">
              <i class="el-icon-chat-line-square"></i>
              <p>暂无帖子</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { listAnnouncements } from '@/api/modules/announcement'
import { listPosts } from '@/api/modules/forum'
import { listWorkflows } from '@/api/modules/workflow'

export default {
  name: 'Home',
  data() {
    return {
      pendingCount: 0,
      todayMeeting: 0,
      myTask: 0,
      todoFlows: [],
      todoLoading: false,
      announcements: [],
      announcementLoading: false,
      forumPosts: [],
      forumLoading: false
    }
  },
  created() {
    this.loadTodos()
    this.loadAnnouncements()
    this.loadForumPosts()
  },
  methods: {
    // 待我审批的流程
    async loadTodos() {
      this.todoLoading = true
      try {
        const res = await listWorkflows({ scope: 'todo' })
        const list = res.data || []
        this.todoFlows = list.slice(0, 5)
        this.pendingCount = list.length
      } catch (e) {
        // 无权限或加载失败时静默
      } finally {
        this.todoLoading = false
      }
    },
    goWorkflow() {
      this.$router.push({ path: '/workflow', query: { tab: 'todo' } })
    },
    typeLabel(type) {
      return { LEAVE: '请假', EXPENSE: '报销', PURCHASE: '采购', OTHER: '其他' }[type] || '其他'
    },
    // 首页展示最新已发布公告
    async loadAnnouncements() {
      this.announcementLoading = true
      try {
        const res = await listAnnouncements({ status: 'PUBLISHED' })
        this.announcements = (res.data || []).slice(0, 5)
      } catch (e) {
        // 无权限或加载失败时静默
      } finally {
        this.announcementLoading = false
      }
    },
    // 首页展示最新论坛帖子（后端按发布时间倒序）
    async loadForumPosts() {
      this.forumLoading = true
      try {
        const res = await listPosts({})
        this.forumPosts = (res.data || []).slice(0, 5)
      } catch (e) {
        // 加载失败时静默
      } finally {
        this.forumLoading = false
      }
    }
  },
  computed: {
    ...mapGetters(['nickname', 'hasPermission']),
    greetingText() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了'
      if (h < 12) return '早上好'
      if (h < 14) return '中午好'
      if (h < 18) return '下午好'
      return '晚上好'
    },
    currentDate() {
      const now = new Date()
      const week = ['日', '一', '二', '三', '四', '五', '六']
      return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${week[now.getDay()]}`
    },
    hasPerm() {
      return code => this.hasPermission(code)
    },
    quickEntries() {
      return [
        { path: '/attendance', label: '考勤打卡', icon: 'el-icon-time', color: 'linear-gradient(135deg,#2e5bff,#1a3dd4)', desc: '上下班打卡', permission: 'attendance:view' },
        { path: '/workflow', label: '流程审批', icon: 'el-icon-s-platform', color: 'linear-gradient(135deg,#8b5cf6,#7c3aed)', desc: '待办 / 已办', permission: 'workflow:view' },
        { path: '/expense', label: '报销管理', icon: 'el-icon-wallet', color: 'linear-gradient(135deg,#f59e0b,#d97706)', desc: '费用报销', permission: 'expense:view' },
        { path: '/announcement', label: '公告通知', icon: 'el-icon-bell', color: 'linear-gradient(135deg,#6366f1,#4f46e5)', desc: '最新通知', permission: 'announcement:view' },
        { path: '/task', label: '任务协作', icon: 'el-icon-s-order', color: 'linear-gradient(135deg,#22c55e,#16a34a)', desc: '任务管理', permission: 'task:view' },
        { path: '/forum', label: '企业论坛', icon: 'el-icon-s-comment', color: 'linear-gradient(135deg,#ec4899,#db2777)', desc: '内部社区', permission: 'forum:view' },
        { path: '/calendar', label: '日程安排', icon: 'el-icon-calendar', color: 'linear-gradient(135deg,#06b6d4,#0891b2)', desc: '日历视图', permission: 'calendar:view' },
        { path: '/suggestion', label: '建议反馈', icon: 'el-icon-chat-dot-round', color: 'linear-gradient(135deg,#10b981,#059669)', desc: '意见提交', permission: 'suggestion:view' }
      ]
    }
  }
}
</script>

<style scoped>
.workbench {
  max-width: 1200px;
  margin: 0 auto;
}

/* ========== 欢迎 Banner ========== */
.welcome-banner {
  background: linear-gradient(135deg, #2e5bff 0%, #1a3dd4 100%);
  border-radius: 12px;
  padding: 28px 32px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 16px rgba(46, 91, 255, 0.3);
}
.welcome-info .greeting {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 6px;
}
.welcome-info .sub-greeting {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}
.welcome-stats {
  display: flex;
  gap: 36px;
}
.stat-item {
  text-align: center;
  min-width: 72px;
}
.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  opacity: 0.75;
}

/* ========== 快捷入口 ========== */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 24px 0 12px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.quick-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #eef0f4;
}
.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  border-color: #2e5bff;
}
.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.quick-icon i {
  font-size: 24px;
  color: #fff;
}
.quick-label {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}
.quick-desc {
  font-size: 12px;
  color: #9ca3af;
}

/* ========== 下方卡片 ========== */
.bottom-row {
  margin-top: 24px;
}
.bottom-card {
  border-radius: 10px;
  border: 1px solid #eef0f4;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}
.card-title i {
  color: #2e5bff;
  margin-right: 6px;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
}
.empty-state i {
  font-size: 40px;
  margin-bottom: 10px;
  color: #d1d5db;
}
.empty-state p {
  font-size: 13px;
  margin: 0;
}

/* ========== 首页信息流（公告 / 论坛）========== */
.feed { min-height: 180px; }
.feed-item {
  display: flex;
  gap: 10px;
  padding: 9px 4px;
  border-bottom: 1px solid #f2f4f8;
  cursor: pointer;
  transition: background 0.15s;
}
.feed-item:last-child { border-bottom: none; }
.feed-item:hover { background: #f8faff; }
.feed-item:hover .feed-title { color: #2e5bff; }
.feed-dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-top: 6px;
  background: #6366f1;
}
.feed-dot.is-top { background: #f56c6c; }
.feed-dot.is-forum { background: #ec4899; }
.feed-dot.is-todo { background: #f59e0b; }
.type-chip {
  display: inline-block;
  font-size: 11px;
  color: #b45309;
  background: #fef3c7;
  border-radius: 4px;
  padding: 0 5px;
  margin-right: 5px;
  line-height: 16px;
}
.feed-body { min-width: 0; flex: 1; }
.feed-title {
  font-size: 13px;
  color: #1f2937;
  font-weight: 500;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.feed-title .el-tag { margin-right: 4px; height: 18px; line-height: 16px; }
.feed-meta {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 3px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.feed-meta i { margin-left: 4px; }
.feed-section {
  color: #6366f1;
  background: #eef0ff;
  border-radius: 4px;
  padding: 0 6px;
  font-size: 11px;
}
.forum-icon { color: #ec4899 !important; }
</style>
