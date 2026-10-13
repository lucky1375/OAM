<template>
  <div class="calendar-page">
    <el-card shadow="never">
      <!-- 月历工具栏 -->
      <div class="cal-toolbar">
        <el-button-group>
          <el-button size="mini" icon="el-icon-arrow-left" @click="prevMonth"></el-button>
          <el-button size="mini" @click="today">今天</el-button>
          <el-button size="mini" icon="el-icon-arrow-right" @click="nextMonth"></el-button>
        </el-button-group>
        <span class="cal-title">{{ year }} 年 {{ month + 1 }} 月</span>
        <el-button type="primary" size="small" icon="el-icon-plus" class="cal-add-btn"
                   v-if="hasPerm('calendar:create')" @click="openCreate()">新建日程</el-button>
      </div>

      <!-- 星期头 -->
      <div class="cal-weekdays">
        <div class="cal-wd" v-for="(w, i) in weekNames" :key="i"
             :class="{ 'cal-wknd': i === 0 || i === 6 }">{{ w }}</div>
      </div>

      <!-- 月历网格 -->
      <div class="cal-grid">
        <div v-for="(cell, idx) in cells" :key="idx" class="cal-cell"
             :class="{
               'is-out': !cell.inMonth,
               'is-today': cell.isToday,
               'is-weekend': cell.weekday === 0 || cell.weekday === 6
             }" @click="openCreate(cell.date)">
          <div class="cal-day-head">
            <span class="cal-day-num">{{ cell.dateLabel }}</span>
            <span class="cal-day-type" v-if="cell.holy.kind === 'holiday'">{{ cell.holy.name }}</span>
            <span class="cal-day-type is-rest" v-else-if="cell.holy.kind === 'rest'">休</span>
            <span class="cal-day-type is-makeup" v-else-if="cell.holy.kind === 'work'" title="调休补班">班</span>
          </div>
          <ul class="cal-events">
            <li v-for="ev in cell.events" :key="ev.id" class="cal-ev"
                :class="ev.allDay ? 'is-all' : 'is-time'"
                :title="(ev.startTime || '') + ': ' + ev.title" @click.stop="openEdit(ev)">
              <span v-if="!ev.allDay" class="ev-time">{{ timeOf(ev.startTime) }}</span>
              {{ ev.title }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 底部统计 -->
      <div class="cal-footer">
        <span>本月共 <b>{{ list.length }}</b> 条日程</span>
      </div>
    </el-card>

    <!-- 新建/编辑 对话框 -->
    <el-dialog :title="form.id ? '编辑日程' : '新建日程'" :visible.sync="dialogVisible" width="520px" @closed="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入日程标题"></el-input>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间"
                          value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间"
                          value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="全天">
          <el-switch v-model="form.allDay"></el-switch>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" placeholder="会议地点 / 线上链接"></el-input>
        </el-form-item>
        <el-form-item label="提前提醒">
          <el-select v-model="form.remindMinutes" placeholder="不提醒" clearable style="width:100%">
            <el-option label="不提醒" :value="null"></el-option>
            <el-option label="提前 10 分钟" :value="10"></el-option>
            <el-option label="提前 30 分钟" :value="30"></el-option>
            <el-option label="提前 1 小时" :value="60"></el-option>
            <el-option label="提前 1 天" :value="1440"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="日程说明"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button v-if="form.id && hasPerm('calendar:delete')" type="danger" plain @click="removeCurrent">删除</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCalendars, createCalendar, updateCalendar, deleteCalendar } from '@/api/modules/calendar'
import { getDayType } from '@/utils/holiday'

const WEEK_NAMES = ['日', '一', '二', '三', '四', '五', '六']

export default {
  name: 'Calendar',
  data() {
    const now = new Date()
    return {
      loading: false,
      saving: false,
      year: now.getFullYear(),
      month: now.getMonth(),
      weekNames: WEEK_NAMES,
      list: [],
      dialogVisible: false,
      form: { id: null, title: '', startTime: '', endTime: '', allDay: false, location: '', remindMinutes: null, remark: '' },
      rules: {
        title: [{ required: true, message: '请输入日程标题', trigger: 'blur' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      }
    }
  },
  computed: {
    // 生成 42 格（6 行 x 7 列）的月历单元格
    cells() {
      const firstDay = new Date(this.year, this.month, 1)
      const startGrid = new Date(firstDay)
      startGrid.setDate(1 - firstDay.getDay()) // 前移到本月第一周的周日
      const cells = []
      for (let i = 0; i < 42; i++) {
        const d = new Date(startGrid)
        d.setDate(startGrid.getDate() + i)
        const inMonth = d.getMonth() === this.month
        const weekday = d.getDay()
        const today = new Date()
        const isToday = inMonth && d.getFullYear() === today.getFullYear()
          && d.getMonth() === today.getMonth() && d.getDate() === today.getDate()
        const holy = getDayType(d.getFullYear(), d.getMonth(), d.getDate())
        cells.push({
          date: d,
          weekday,
          inMonth,
          isToday,
          holy,
          dateLabel: `${d.getMonth() + 1}月${d.getDate()}日`,
          events: this.eventsOf(d)
        })
      }
      return cells
    }
  },
  created() {
    this.load()
  },
  methods: {
    hasPerm(code) { return this.$store.getters.hasPermission(code) },
    // 把日程归到各自日期
    eventsOf(d) {
      const ymd = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      return this.list.filter(ev => (ev.startTime || '').startsWith(ymd))
    },
    timeOf(t) { return t ? t.slice(11, 16) : '' },
    async load() {
      this.loading = true
      try {
        // 未传 ownerUserId，后端默认返回当前登录人员自己的日程
        const res = await listCalendars()
        this.list = res.data || []
      } finally {
        this.loading = false
      }
    },
    prevMonth() {
      this.month--
      if (this.month < 0) { this.month = 11; this.year-- }
    },
    nextMonth() {
      this.month++
      if (this.month > 11) { this.month = 0; this.year++ }
    },
    today() {
      const now = new Date()
      this.year = now.getFullYear()
      this.month = now.getMonth()
    },
    // 点击格子新建，默认选中当天 9:00~10:00
    openCreate(date) {
      this.resetForm()
      if (date) {
        const day = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
        this.form.startTime = day + ' 09:00:00'
        this.form.endTime = day + ' 10:00:00'
      }
      this.dialogVisible = true
    },
    openEdit(ev) {
      this.form = { ...ev }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = { id: null, title: '', startTime: '', endTime: '', allDay: false, location: '', remindMinutes: null, remark: '' }
      if (this.$refs.formRef) this.$refs.formRef.clearValidate()
    },
    save() {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.form.id) {
            await updateCalendar(this.form.id, this.form)
            this.$message.success('更新成功')
          } else {
            await createCalendar(this.form)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          this.load()
        } finally {
          this.saving = false
        }
      })
    },
    removeCurrent() {
      this.$confirm('确定删除该日程吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteCalendar(this.form.id)
        this.$message.success('删除成功')
        this.dialogVisible = false
        this.load()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.calendar-page { padding: 16px; }
.cal-toolbar { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
.cal-title { font-size: 18px; font-weight: 600; color: #303133; }
.cal-add-btn { margin-left: auto; }
.cal-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); background: #f5f7fa; border-radius: 6px; }
.cal-wd { text-align: center; padding: 8px 0; font-size: 13px; color: #606266; font-weight: 600; }
.cal-wknd { color: #f56c6c; }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); margin-top: 4px; border: 1px solid #ebeef5; border-radius: 6px; overflow: hidden; }
.cal-cell { min-height: 104px; border-right: 1px solid #ebeef5; border-bottom: 1px solid #ebeef5; padding: 4px; cursor: pointer; background: #fff; box-sizing: border-box; }
.cal-cell:nth-child(7n) { border-right: none; }
.cal-cell:hover { background: #f5f7fa; }
.cal-cell.is-out { background: #fafbfc; color: #c0c4cc; }
.cal-cell.is-weekend { background: #fcfcfd; }
.cal-cell.is-today { box-shadow: inset 0 0 0 2px #06b6d4; }
.cal-day-head { display: flex; align-items: center; justify-content: space-between; gap: 4px; padding-right: 4px; }
.cal-day-num { font-size: 12px; color: #606266; }
.cal-day-type { flex-shrink: 0; font-size: 10px; line-height: 1; padding: 2px 4px; border-radius: 3px; background: #fdecea; color: #d32f2f; }
.cal-day-type.is-rest { background: #e8f5e9; color: #2e7d32; }
.cal-day-type.is-makeup { background: #e0e0e0; color: #424242; }
.cal-cell.is-today .cal-day-num { color: #06b6d4; font-weight: 700; }
.cal-events { list-style: none; margin: 2px 0 0; padding: 0; display: flex; flex-direction: column; gap: 2px; }
.cal-ev { font-size: 12px; line-height: 1.4; padding: 1px 6px; border-radius: 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer; }
.cal-ev.is-all { background: #06b6d4; color: #fff; }
.cal-ev.is-time { background: #e0f2fe; color: #0c4a6e; }
.cal-ev .ev-time { margin-right: 4px; opacity: .85; }
.cal-footer { margin-top: 12px; color: #909399; font-size: 13px; }
</style>