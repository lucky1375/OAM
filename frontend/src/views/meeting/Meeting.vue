<template>
  <div class="meeting-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ================= 会议预定（左列表 + 右时间轴） ================= -->
        <el-tab-pane label="会议预定" name="book">
          <div class="book-toolbar">
            <span class="book-label">会议日期</span>
            <el-date-picker v-model="bookDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期"
                            @change="loadBookData" style="width:160px"></el-date-picker>
            <el-button type="text" @click="bookToday">今天</el-button>
            <div class="book-legend">
              <span><i class="lg lg-scheduled"></i>已预约</span>
              <span><i class="lg lg-disabled"></i>停用</span>
            </div>
            <span class="book-count">{{ freeRoomCount }} 间空闲</span>
          </div>

          <div class="book-layout" v-loading="meetingLoading || roomLoading">
            <!-- 左：会议室列表 -->
            <div class="book-rooms">
              <div class="book-rooms-head">会议室（{{ roomList.length }}）</div>
              <div v-for="r in roomList" :key="r.id" class="book-room-row" :class="{ 'is-off': !r.enabled }">
                <div class="book-room-info">
                  <div class="book-room-name">{{ r.name }}</div>
                  <div class="book-room-meta">
                    <span><i class="el-icon-user"></i>{{ r.capacity }}人</span>
                    <span v-if="r.equipment"><i class="el-icon-cpu"></i>{{ r.equipment }}</span>
                  </div>
                </div>
              </div>
              <div class="book-room-footer">{{ freeRoomCount }} 间可用</div>
            </div>

            <!-- 右：时间轴 -->
            <div class="book-grid">
              <!-- 时间刻度头 -->
              <div class="book-scroll">
                <div class="book-scale">
                  <span v-for="h in scaleHours" :key="h" class="tick" :style="tickStyle(h)">{{ fmtHour(h) }}</span>
                </div>
                <div class="book-body">
                  <div v-for="r in roomList" :key="r.id" class="book-body-row"
                       :class="{ 'is-off': !r.enabled }"
                       ref="bodyRows" :style="{ height: ROW_H + 'px' }"
                       @click.self="clickRow($event, r)">
                    <!-- 已预约块 -->
                    <div v-for="m in bookingsOf(r)" :key="m.id"
                         class="book-block" :class="blockClass(m)"
                         :style="blockStyle(m)" :title="m.title + '（' + m.organizer + '）'"
                         @click.stop="openMeetingDialog(m)">
                      <span class="bb-time">{{ blockTime(m) }}</span>
                      <span class="bb-title">{{ m.title }}</span>
                      <span class="bb-org">{{ m.organizer }}</span>
                    </div>
                    <!-- 当前时间指示线 -->
                    <div v-if="bookDate === todayStr" class="book-now" :style="{ left: nowLeft + '%' }"></div>
                    <!-- 非工作时段灰条（20:00 后） -->
                    <div class="book-grey" :style="{ left: greyLeft, width: greyWidth }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- ================= 会议管理 ================= -->
        <el-tab-pane label="会议管理" name="meeting">
          <div class="toolbar">
            <el-button type="primary" icon="el-icon-plus" v-if="hasPerm('meeting:create')" @click="openMeetingDialog()">新建会议</el-button>
            <el-input v-model="meetingKeyword" placeholder="搜索主题/发起人/参会人" clearable style="width:260px"
                      @clear="loadMeetings" @keyup.enter.native="loadMeetings">
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
            </el-input>
          </div>
          <el-table :data="meetingList" v-loading="meetingLoading" border style="width:100%" empty-text="暂无会议">
            <el-table-column label="主题" prop="title" min-width="180"></el-table-column>
            <el-table-column label="会议室" prop="meetingRoomName" width="120">
              <template slot-scope="{ row }">
                <el-tag v-if="row.meetingRoomName" size="mini">{{ row.meetingRoomName }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="开始时间" prop="startTime" width="165"></el-table-column>
            <el-table-column label="结束时间" prop="endTime" width="165"></el-table-column>
            <el-table-column label="发起人" prop="organizer" width="110">
              <template slot-scope="{ row }">{{ row.organizer || '-' }}</template>
            </el-table-column>
            <el-table-column label="参会人" prop="participants" min-width="160">
              <template slot-scope="{ row }">{{ row.participants || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template slot-scope="{ row }">
                <el-tag size="mini" :type="statusType(row.status)">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template slot-scope="{ row }">
                <el-button type="text" v-if="hasPerm('meeting:update')" @click="openMeetingDialog(row)">编辑</el-button>
                <el-button type="text" v-if="hasPerm('meeting:update') && row.status === 'SCHEDULED'" @click="setStatus(row,'FINISHED')">结束</el-button>
                <el-button type="text" class="danger-text" v-if="hasPerm('meeting:delete')" @click="removeMeeting(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ================= 会议室管理（图形卡片） ================= -->
        <el-tab-pane :label="'会议室管理' + '(' + roomList.length + ')'" name="room">
          <div class="toolbar">
            <el-button type="primary" icon="el-icon-plus" v-if="hasPerm('meeting:room')" @click="openRoomDialog()">新增会议室</el-button>
            <el-input v-model="roomKeyword" placeholder="搜索会议室" clearable style="width:240px"
                      @clear="loadRooms" @keyup.enter.native="loadRooms">
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
            </el-input>
          </div>
          <div class="room-grid" v-loading="roomLoading">
            <div v-for="r in roomList" :key="r.id" class="room-card" :class="{ 'room-off': !r.enabled }">
              <div class="room-head">
                <div class="room-avatar">
                  <i class="el-icon-office-building"></i>
                </div>
                <div class="room-title">
                  <span class="room-name">{{ r.name }}</span>
                  <el-tag size="mini" :type="r.enabled ? 'success' : 'info'">{{ r.enabled ? '可用' : '已停用' }}</el-tag>
                </div>
              </div>
              <div class="room-body">
                <p><i class="el-icon-location-outline"></i>{{ r.location || '未填写地点' }}</p>
                <p><i class="el-icon-user"></i>容纳 <b>{{ r.capacity }}</b> 人</p>
                <p class="room-equip"><i class="el-icon-cpu"></i>{{ r.equipment || '无设备信息' }}</p>
                <p v-if="r.remark" class="room-remark"><i class="el-icon-document"></i>{{ r.remark }}</p>
              </div>
              <div class="room-foot">
                <el-button size="mini" type="primary" icon="el-icon-video-camera" v-if="hasPerm('meeting:room')" @click="useRoom(r)">预定</el-button>
                <el-button size="mini" icon="el-icon-edit" v-if="hasPerm('meeting:room')" @click="openRoomDialog(r)">编辑</el-button>
                <el-button size="mini" class="room-delete" icon="el-icon-delete" v-if="hasPerm('meeting:room')" @click="removeRoom(r)">删除</el-button>
              </div>
            </div>
            <el-empty v-if="!roomLoading && roomList.length === 0" description="暂无会议室"></el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ========== 会议 对话框 ========== -->
    <el-dialog :title="meetingForm.id ? '编辑会议' : '新建会议'" :visible.sync="meetingDialogVisible" width="560px" @closed="resetMeetingForm">
      <el-form :model="meetingForm" :rules="meetingRules" ref="meetingFormRef" label-width="90px">
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="meetingForm.title" placeholder="请输入会议主题"></el-input>
        </el-form-item>
        <el-form-item label="会议室">
          <el-select v-model="meetingForm.meetingRoomId" placeholder="选择会议室" clearable style="width:100%">
            <el-option v-for="r in roomList" :key="r.id" :label="r.name + (r.location ? '（' + r.location + '）' : '')" :value="r.id" :disabled="!r.enabled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="meetingForm.meetingRoomId" label="已预约时段">
          <div class="busy-box">
            <div v-for="m in roomBusyList" :key="m.id" class="busy-item" :class="{ 'busy-conflict': timeConflict(m) }">
              <i class="el-icon-time"></i>
              <span class="busy-time">{{ m.startTime }} ~ {{ m.endTime }}</span>
              <span class="busy-title">{{ m.title }}</span>
              <el-tag v-if="timeConflict(m)" size="mini" type="danger">时间冲突</el-tag>
            </div>
            <span v-if="roomBusyList.length === 0" class="busy-empty">该会议室暂时没有已预约时段，可直接预定</span>
          </div>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="meetingForm.startTime" type="datetime" placeholder="选择开始时间"
                          value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="meetingForm.endTime" type="datetime" placeholder="选择结束时间"
                          value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="发起人">
          <el-input v-model="meetingForm.organizer" placeholder="会议发起人"></el-input>
        </el-form-item>
        <el-form-item label="参会人">
          <el-input v-model="meetingForm.participants" placeholder="多个用逗号分隔，如：张三,李四"></el-input>
        </el-form-item>
        <el-form-item label="议程/内容">
          <el-input v-model="meetingForm.agenda" type="textarea" :rows="3" placeholder="会议议程或内容"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="meetingForm.remark" placeholder="备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="meetingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="meetingSaving" @click="saveMeeting">保存</el-button>
      </div>
    </el-dialog>

    <!-- ========== 会议室 对话框 ========== -->
    <el-dialog :title="roomForm.id ? '编辑会议室' : '新增会议室'" :visible.sync="roomDialogVisible" width="480px" @closed="resetRoomForm">
      <el-form :model="roomForm" :rules="roomRules" ref="roomFormRef" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="roomForm.name" placeholder="如：第一会议室"></el-input>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="roomForm.location" placeholder="如：1号楼 3 层"></el-input>
        </el-form-item>
        <el-form-item label="容纳人数">
          <el-input-number v-model="roomForm.capacity" :min="1" :max="1000" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="设备">
          <el-input v-model="roomForm.equipment" placeholder="如：投影、白板、视频会议"></el-input>
        </el-form-item>
        <el-form-item label="是否可用">
          <el-switch v-model="roomForm.enabled"></el-switch>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roomForm.remark" placeholder="备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roomSaving" @click="saveRoom">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMeetings, createMeeting, updateMeeting, deleteMeeting,
         listMeetingRooms, createMeetingRoom, updateMeetingRoom, deleteMeetingRoom } from '@/api/modules/meeting'

export default {
  name: 'Meeting',
  data() {
    return {
      activeTab: 'book',
      // 会议预定（时间轴）
      bookDate: this.fmtDate(new Date()),
      TODAY_MIN: 8 * 60,          // 时间轴起点 08:00
      TOOL_END_MIN: 20 * 60,      // 正常工作段截止 20:00
      ROW_H: 64,                  // 每会议室行高
      SCALE_END_MIN: 22 * 60,     // 刻度终点 22:00
      nowLeft: '',
      // 会议
      meetingList: [], meetingLoading: false, meetingKeyword: '', meetingDialogVisible: false, meetingSaving: false,
      meetingForm: { id: null, title: '', meetingRoomId: null, startTime: '', endTime: '', organizer: '', participants: '', agenda: '', status: 'SCHEDULED', remark: '' },
      meetingRules: {
        title: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      },
      // 会议室
      roomList: [], roomLoading: false, roomKeyword: '', roomDialogVisible: false, roomSaving: false,
      roomForm: { id: null, name: '', location: '', capacity: 10, equipment: '', enabled: true, remark: '' },
      roomRules: { name: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }] }
    }
  },
  created() {
    this.nowLeft = this.calcNowLeft()
    this.loadMeetings()
    this.loadRooms()
  },
  computed: {
    // 当前所选会议室已被预约的时段（排除当前正在编辑的这场）
    roomBusyList() {
      const roomId = this.meetingForm.meetingRoomId
      if (!roomId) return []
      return (this.meetingList || []).filter(m =>
        m.meetingRoomId === roomId && m.status !== 'CANCELLED' && m.id !== this.meetingForm.id
      )
    },
    // ============ 会议预定（时间轴） ============
    todayStr() { return this.fmtDate(new Date()) },
    scaleHours() {
      const start = this.TODAY_MIN / 60
      const end = this.SCALE_END_MIN / 60
      const arr = []
      for (let h = start; h <= end; h++) arr.push(h)
      return arr
    },
    // 正常工作段内的分钟跨度（用于定位）
    dayLengthMin() { return this.SCALE_END_MIN - this.TODAY_MIN },
    greyLeft() {
      return ((this.TOOL_END_MIN - this.TODAY_MIN) / this.dayLengthMin * 100).toFixed(2) + '%'
    },
    greyWidth() {
      return ((this.SCALE_END_MIN - this.TOOL_END_MIN) / this.dayLengthMin * 100).toFixed(2) + '%'
    },
    freeRoomCount() {
      if (!this.roomList.length) return 0
      return this.roomList.filter(r => !r.enabled || this.bookingsOf(r).length === 0).length
    },
  },
  methods: {
    hasPerm(code) { return this.$store.getters.hasPermission(code) },
    // ---------- 会议预定（时间轴）工具 ----------
    fmtDate(d) {
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${d.getFullYear()}-${m}-${day}`
    },
    calcNowLeft() {
      const n = new Date()
      const mins = n.getHours() * 60 + n.getMinutes()
      return ((Math.max(mins - this.TODAY_MIN, 0)) / this.dayLengthMin * 100).toFixed(2)
    },
    oneDayKey(m) {
      const s = new Date(String(m.startTime).replace(' ', 'T'))
      return this.fmtDate(s)
    },
    // 某个会议室在选定日期当天、未取消的会议
    bookingsOf(r) {
      if (!this.bookDate) return []
      return (this.meetingList || []).filter(m =>
        m.meetingRoomId === r.id && m.status !== 'CANCELLED' && this.oneDayKey(m) === this.bookDate
      )
    },
    tickStyle(h) {
      // 刻度线落在时间轴起点+tickMin 处
      const min = h * 60
      return { left: ((min - this.TODAY_MIN) / this.dayLengthMin * 100).toFixed(2) + '%' }
    },
    fmtHour(h) {
      const hh = ('0' + h).slice(-2)
      return hh + ':00'
    },
    // 会议块的横向定位
    blockStyle(m) {
      const s = new Date(String(m.startTime).replace(' ', 'T'))
      const e = new Date(String(m.endTime).replace(' ', 'T'))
      if (isNaN(s.getTime()) || isNaN(e.getTime())) return {}
      let sMin = Math.max(s.getHours() * 60 + s.getMinutes(), this.TODAY_MIN)
      let eMin = Math.min(e.getHours() * 60 + e.getMinutes(), this.SCALE_END_MIN)
      eMin = Math.max(eMin, sMin)
      const total = this.dayLengthMin
      const left = ((sMin - this.TODAY_MIN) / total * 100).toFixed(2) + '%'
      const width = ((eMin - sMin) / total * 100).toFixed(2) + '%'
      return { left, width }
    },
    blockClass(m) {
      if (m.status === 'FINISHED') return 'is-finished'
      if (m.status === 'CANCELLED') return 'is-cancelled'
      return 'is-scheduled'
    },
    blockTime(m) {
      const s = String(m.startTime).slice(11, 16)
      const e = String(m.endTime).slice(11, 16)
      return `${s}-${e}`
    },
    bookToday() {
      this.bookDate = this.fmtDate(new Date())
      this.loadBookData()
    },
    loadBookData() {
      this.nowLeft = this.calcNowLeft()
      this.loadMeetings()
      this.loadRooms()
    },
    // 点击时间轴空白处：按点击位置换算开始时间并弹出预定
    clickRow($event, room) {
      if (!room.enabled) {
        this.$message.warning('该会议室已停用')
        return
      }
      const rowEl = $event.currentTarget
      const rect = rowEl.getBoundingClientRect()
      const ratio = Math.min(Math.max(($event.clientX - rect.left) / rect.width, 0), 1)
      let totalMin = this.TODAY_MIN + Math.floor(ratio * this.dayLengthMin / 30) * 30
      totalMin = Math.min(totalMin, this.TOOL_END_MIN - 60)   // 预留 1 小时，不落到非工作段末端
      if (totalMin < this.TODAY_MIN) totalMin = this.TODAY_MIN
      const hh = ('0' + Math.floor(totalMin / 60)).slice(-2)
      const mm = ('0' + (totalMin % 60)).slice(-2)
      const start = `${this.bookDate} ${hh}:${mm}:00`
      const endHour = totalMin + 60
      const eh = ('0' + Math.floor(endHour / 60)).slice(-2)
      const em = ('0' + (endHour % 60)).slice(-2)
      this.openMeetingDialog()
      this.meetingForm.meetingRoomId = room.id
      this.meetingForm.startTime = start
      this.meetingForm.endTime = `${this.bookDate} ${eh}:${em}:00`
    },
    statusText(s) { return { SCHEDULED: '待开', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s },
    statusType(s) { return { SCHEDULED: 'warning', FINISHED: 'success', CANCELLED: 'info' }[s] || 'info' },
    // 判断一条已预约时段是否与当前表单所选时间冲突
    timeConflict(m) {
      const s = this.meetingForm.startTime
      const e = this.meetingForm.endTime
      if (!s || !e) return false
      return m.startTime < e && m.endTime > s
    },
    // ============ 会议 ============
    async loadMeetings() {
      this.meetingLoading = true
      try {
        const res = await listMeetings({ keyword: this.meetingKeyword || undefined })
        this.meetingList = res.data || []
      } finally {
        this.meetingLoading = false
      }
    },
    openMeetingDialog(row) {
      if (row) {
        this.meetingForm = { ...row }
      } else {
        this.resetMeetingForm()
      }
      this.meetingDialogVisible = true
    },
    resetMeetingForm() {
      this.meetingForm = { id: null, title: '', meetingRoomId: null, startTime: '', endTime: '', organizer: '', participants: '', agenda: '', status: 'SCHEDULED', remark: '' }
      if (this.$refs.meetingFormRef) this.$refs.meetingFormRef.clearValidate()
    },
    saveMeeting() {
      this.$refs.meetingFormRef.validate(async valid => {
        if (!valid) return
        this.meetingSaving = true
        try {
          if (this.meetingForm.id) {
            await updateMeeting(this.meetingForm.id, this.meetingForm)
            this.$message.success('会议更新成功')
          } else {
            await createMeeting(this.meetingForm)
            this.$message.success('会议创建成功')
          }
          this.meetingDialogVisible = false
          this.loadMeetings()
        } finally {
          this.meetingSaving = false
        }
      })
    },
    async setStatus(row, status) {
      await updateMeeting(row.id, { ...row, status })
      this.$message.success(status === 'FINISHED' ? '会议已结束' : '操作成功')
      this.loadMeetings()
    },
    removeMeeting(row) {
      this.$confirm(`确定删除会议「${row.title}」吗？`, '提示', { type: 'warning' }).then(async () => {
        await deleteMeeting(row.id)
        this.$message.success('删除成功')
        this.loadMeetings()
      }).catch(() => {})
    },
    // 从会议室卡片跳去预定（切到会议 Tab 并预填该会议室）
    useRoom(r) {
      if (!r.enabled) {
        this.$message.warning('该会议室已停用')
        return
      }
      this.activeTab = 'meeting'
      this.openMeetingDialog()
      this.meetingForm.meetingRoomId = r.id
    },
    // ============ 会议室 ============
    async loadRooms() {
      this.roomLoading = true
      try {
        const res = await listMeetingRooms({ keyword: this.roomKeyword || undefined })
        this.roomList = res.data || []
      } finally {
        this.roomLoading = false
      }
    },
    openRoomDialog(row) {
      if (row) {
        this.roomForm = { ...row }
      } else {
        this.resetRoomForm()
      }
      this.roomDialogVisible = true
    },
    resetRoomForm() {
      this.roomForm = { id: null, name: '', location: '', capacity: 10, equipment: '', enabled: true, remark: '' }
      if (this.$refs.roomFormRef) this.$refs.roomFormRef.clearValidate()
    },
    saveRoom() {
      this.$refs.roomFormRef.validate(async valid => {
        if (!valid) return
        this.roomSaving = true
        try {
          if (this.roomForm.id) {
            await updateMeetingRoom(this.roomForm.id, this.roomForm)
            this.$message.success('更新成功')
          } else {
            await createMeetingRoom(this.roomForm)
            this.$message.success('会议室创建成功')
          }
          this.roomDialogVisible = false
          this.loadRooms()
        } finally {
          this.roomSaving = false
        }
      })
    },
    removeRoom(row) {
      this.$confirm(`确定删除会议室「${row.name}」吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          await deleteMeetingRoom(row.id)
          this.$message.success('删除成功')
          this.loadRooms()
        } catch (e) {
          // 被会议占用等错误由拦截器提示
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.meeting-page { padding: 16px; }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.danger-text { color: #f56c6c; }
/* ---------- 会议室图形卡片 ---------- */
.room-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(262px, 1fr)); gap: 18px; }
.room-card { border: 1px solid #e5e9f0; border-radius: 12px; padding: 18px; background: #fff; transition: box-shadow .2s, transform .2s, border-color .2s; position: relative; }
.room-card:hover { box-shadow: 0 10px 28px rgba(40,60,120,.10); transform: translateY(-3px); border-color: #c9d4f0; }
.room-card.room-off { opacity: .55; background: #f8f9fb; box-shadow: none; }
.room-head { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.room-avatar { flex-shrink: 0; width: 46px; height: 46px; border-radius: 12px; background: linear-gradient(135deg, #06b6d4, #0ea5e9); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 22px; box-shadow: 0 4px 10px rgba(6,182,212,.28); }
.room-off .room-avatar { background: #c0c4cc; box-shadow: none; }
.room-title { display: flex; flex-direction: column; align-items: flex-start; gap: 5px; }
.room-name { font-size: 15px; font-weight: 600; color: #1f2937; }
.room-title .el-tag.el-tag--success { background: #e6f7ef; border-color: #d0f0e0; color: #0c8b5c; font-weight: 600; }
.room-title .el-tag.el-tag--info { background: #f1f3f6; border-color: #e0e4ea; color: #6b7280; font-weight: 600; }
.room-body p { margin: 5px 0; font-size: 13px; color: #4b5563; display: flex; align-items: center; }
.room-body p i { width: 18px; flex-shrink: 0; color: #9aa3b2; font-size: 14px; text-align: center; margin-right: 6px; }
.room-body p b { color: #1f2937; font-weight: 600; margin: 0 2px; }
.room-equip, .room-remark { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.room-foot { margin-top: 16px; padding-top: 14px; border-top: 1px solid #f0f2f6; display: flex; gap: 10px; }
.room-foot .el-button { margin-left: 0; border-radius: 6px; }
.room-foot .el-button + .el-button { margin-left: 0; }
.room-foot .el-button--mini { padding: 6px 12px; }
.room-delete { color: #e5484d; border-color: #f3cdce; background: #fff; }
.room-delete:hover, .room-delete:focus { color: #fff; background: #e5484d; border-color: #e5484d; }
.room-foot .el-button:not(.el-button--primary):not(.room-delete) { color: #3b82f6; border-color: #bfd6fb; background: #fff; }
.room-foot .el-button:not(.el-button--primary):not(.room-delete):hover { color: #fff; background: #3b82f6; border-color: #3b82f6; }
/* ---------- 已预约时段列表 ---------- */
.busy-box { max-height: 160px; overflow-y: auto; padding: 4px; }
.busy-item { display: flex; align-items: center; gap: 8px; font-size: 13px; padding: 6px 8px; border: 1px solid #eee; border-radius: 6px; margin-bottom: 6px; color: #606266; }
.busy-item i { color: #06b6d4; }
.busy-time { color: #303133; font-weight: 600; white-space: nowrap; }
.busy-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.busy-item.busy-conflict { background: #fef0f0; border-color: #f56c6c; }
.busy-empty { color: #909399; font-size: 13px; }
/* ---------- 会议预定（左列表 + 右时间轴） ---------- */
.book-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; flex-wrap: wrap; }
.book-label { color: #606266; font-size: 13px; }
.book-legend { display: flex; gap: 16px; margin-left: 8px; font-size: 12px; color: #909399; }
.book-legend .lg { display: inline-block; width: 14px; height: 8px; border-radius: 2px; margin-right: 4px; vertical-align: middle; }
.lg-scheduled { background: linear-gradient(135deg, #5b7cfa, #7d6ffa); border: none; }
.lg-disabled { background: #ecf0f5; border: 1px solid #cdd3db; }
.book-count { margin-left: auto; color: #e65a54; font-weight: 600; font-size: 13px; }
.book-layout { display: flex; border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; background: #fff; }
.book-rooms { width: 260px; flex-shrink: 0; border-right: 1px solid #ebeef5; display: flex; flex-direction: column; }
.book-rooms-head { padding: 12px 16px; font-weight: 600; color: #303133; border-bottom: 1px solid #ebeef5; background: #fafbfc; }
.book-room-row { padding: 14px 16px; border-bottom: 1px solid #f2f3f5; height: 64px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: center; }
.book-room-row.is-off { opacity: .55; background: #fafbfc; }
.book-room-name { font-size: 14px; font-weight: 600; color: #303133; }
.book-room-meta { margin-top: 4px; display: flex; gap: 12px; font-size: 12px; color: #909399; }
.book-room-meta span i { margin-right: 3px; color: #b0b4bb; }
.book-room-footer { padding: 10px 12px; color: #909399; font-size: 12px; background: #fafbfc; margin-top: auto; border-top: 1px solid #ebeef5; }
.book-grid { flex: 1; overflow: hidden; background: #fff; }
.book-scale { position: relative; height: 34px; border-bottom: 1px solid #ebeef5; background: #fafbfc; }
.book-scale .tick { position: absolute; transform: translateX(-50%); font-size: 11px; color: #909399; white-space: nowrap; }
.book-body { position: relative; }
.book-body-row { position: relative; border-bottom: 1px solid #f2f3f5; cursor: pointer; overflow: hidden; }
.book-body-row.is-off { cursor: not-allowed; }
.book-block { position: absolute; top: 5px; bottom: 5px; border-radius: 6px; padding: 4px 8px; box-sizing: border-box; overflow: hidden; cursor: pointer; display: flex; flex-direction: column; justify-content: center; line-height: 1.35; box-shadow: 0 2px 6px rgba(86,102,252,.18); transition: transform .12s, box-shadow .12s; }
.book-block:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(86,102,252,.26); }
.book-block.is-scheduled { background: linear-gradient(135deg, #5b7cfa, #7d6ffa); color: #fff; border: none; }
.book-block.is-finished { background: #eef1f6; border: 1px solid #dde2ea; color: #9aa2ad; box-shadow: none; }
.book-block.is-cancelled { background: repeating-linear-gradient(45deg, #fbeeee, #fbeeee 6px, #f6dede 6px, #f6dede 12px); border: 1px solid #ecbebe; color: #b58a8a; box-shadow: none; }
.bb-time { font-size: 10px; font-weight: 600; opacity: .95; }
.book-block.is-scheduled .bb-time { color: #dce4ff; }
.book-block.is-finished .bb-time, .book-block.is-cancelled .bb-time { color: inherit; }
.bb-title { font-size: 12px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-block.is-scheduled .bb-title { color: #fff; }
.bb-org { font-size: 10px; opacity: .92; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-block.is-scheduled .bb-org { color: #e3e7ff; }
.book-now { position: absolute; top: 0; bottom: 0; width: 2px; background: #e65a54; z-index: 3; pointer-events: none; }
.book-now::after { content: ''; position: absolute; left: -3px; top: 0; border: 4px solid transparent; border-top-color: #e65a54; border-bottom: none; }
.book-grey { position: absolute; top: 0; bottom: 0; background: #f5f5f6; opacity: .55; z-index: 0; pointer-events: none; }
</style>