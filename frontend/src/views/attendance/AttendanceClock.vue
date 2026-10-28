<template>
  <div class="att-page">
    <!-- 打卡卡片 -->
    <el-card shadow="never" class="punch-card">
      <div class="punch-left">
        <div class="clock">{{ clock }}</div>
        <div class="date-line">{{ todayInfo.date }} {{ weekText }}</div>
        <div class="work-line">工作时间 {{ todayInfo.workStart }} - {{ todayInfo.workEnd }}</div>
        <div class="work-line">
          <i class="el-icon-location-outline"></i>
          {{ todayInfo.officeName }} · 打卡范围 {{ todayInfo.radiusMeters }} 米
        </div>
        <div class="today-tags">
          <el-tag v-if="record && record.checkInTime" size="small"
                  :type="record.checkInStatus === 'LATE' ? 'warning' : 'success'">
            上班 {{ record.checkInTime }}{{ record.checkInStatus === 'LATE' ? ' · 迟到' : '' }}
          </el-tag>
          <el-tag v-else size="small" type="info">未打上班卡</el-tag>
          <el-tag v-if="record && record.checkOutTime" size="small"
                  :type="record.checkOutStatus === 'EARLY' ? 'warning' : 'success'">
            下班 {{ record.checkOutTime }}{{ record.checkOutStatus === 'EARLY' ? ' · 早退' : '' }}
          </el-tag>
          <el-tag v-else size="small" type="info">未打下班卡</el-tag>
        </div>
        <div class="loc-line" :class="{ 'loc-bad': location && !inRange, 'loc-ok': location && inRange }">
          <template v-if="locating"><i class="el-icon-loading"></i> 正在定位…</template>
          <template v-else-if="location">
            <i class="el-icon-location"></i>
            距打卡点约 {{ distance }} 米
            <span v-if="inRange">· 在打卡范围内</span>
            <span v-else>· 超出范围（允许 {{ todayInfo.radiusMeters }} 米）</span>
            <el-button type="text" class="relocate" @click="locate">重新定位</el-button>
          </template>
          <template v-else>
            <i class="el-icon-warning-outline"></i>
            {{ locationError || '尚未获取定位' }}
            <el-button type="text" class="relocate" @click="locate">获取定位</el-button>
          </template>
        </div>
      </div>
      <div class="punch-right">
        <el-button type="primary" round icon="el-icon-sunny"
                   :disabled="!todayInfo.canCheckIn" :loading="checking"
                   @click="doCheckIn">上班打卡</el-button>
        <el-button type="success" round icon="el-icon-moon"
                   :disabled="!todayInfo.canCheckOut" :loading="checking"
                   @click="doCheckOut">下班打卡</el-button>
      </div>
    </el-card>

    <!-- 当前位置地图 -->
    <el-card shadow="never" class="map-card">
      <div slot="header" class="map-header">
        <span class="map-title">
          <i class="el-icon-map-location"></i> 当前位置
          <span class="office-chip" v-if="todayInfo.officeName">
            {{ todayInfo.officeName }} · 半径 {{ todayInfo.radiusMeters }} 米
          </span>
        </span>
        <span class="map-meta">
          <template v-if="location">
            {{ location.longitude.toFixed(6) }}, {{ location.latitude.toFixed(6) }}
            <el-tag size="mini" :type="inRange ? 'success' : 'danger'" effect="plain" class="ml6">
              {{ inRange ? '在打卡范围内' : '超出打卡范围' }}
            </el-tag>
            <el-button type="text" size="mini" @click="locate">重新定位</el-button>
          </template>
          <el-button v-if="canManage" type="text" size="mini" icon="el-icon-setting"
                     @click="openOfficeConfig">打卡点配置</el-button>
        </span>
      </div>
      <div class="map-wrap">
        <div ref="mapBox" class="map-box"></div>
        <div class="map-mask" v-if="!mapReady">
          <i class="el-icon-map-location"></i>
          <p>{{ mapError || (locating ? '正在定位…' : '暂未获取到位置') }}</p>
          <el-button size="small" type="primary" plain icon="el-icon-location" @click="locate">获取定位</el-button>
        </div>
      </div>
    </el-card>

    <!-- 月度统计 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon blue"><i class="el-icon-date"></i></div>
        <div><div class="stat-value">{{ stats.workDays }}</div><div class="stat-label">本月出勤天数</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><i class="el-icon-alarm-clock"></i></div>
        <div><div class="stat-value">{{ stats.lateCount }}</div><div class="stat-label">迟到次数</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple"><i class="el-icon-time"></i></div>
        <div><div class="stat-value">{{ stats.earlyCount }}</div><div class="stat-label">早退次数</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><i class="el-icon-timer"></i></div>
        <div><div class="stat-value">{{ stats.avgDuration || '0小时' }}</div><div class="stat-label">平均工时</div></div>
      </div>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-click="onTabClick">
        <!-- 我的考勤 -->
        <el-tab-pane label="我的考勤" name="records">
          <div class="tab-toolbar">
            <el-date-picker v-model="month" type="month" value-format="yyyy-MM"
                            placeholder="选择月份" :clearable="false"
                            @change="loadRecords" style="width:160px"></el-date-picker>
            <span class="toolbar-tip">共 {{ records.length }} 条记录</span>
          </div>
          <el-table :data="records" v-loading="recordLoading" size="small" border>
            <el-table-column prop="workDate" label="日期" width="120"></el-table-column>
            <el-table-column label="上班打卡" width="150">
              <template slot-scope="{ row }">
                <template v-if="row.checkInTime">
                  <span>{{ row.checkInTime }}</span>
                  <el-tag size="mini" :type="row.checkInStatus === 'LATE' ? 'warning' : 'success'"
                          class="ml6">{{ row.checkInStatus === 'LATE' ? '迟到' : '正常' }}</el-tag>
                </template>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="下班打卡" width="150">
              <template slot-scope="{ row }">
                <template v-if="row.checkOutTime">
                  <span>{{ row.checkOutTime }}</span>
                  <el-tag size="mini" :type="row.checkOutStatus === 'EARLY' ? 'warning' : 'success'"
                          class="ml6">{{ row.checkOutStatus === 'EARLY' ? '早退' : '正常' }}</el-tag>
                </template>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="workDuration" label="工时" width="110">
              <template slot-scope="{ row }">{{ row.workDuration || '—' }}</template>
            </el-table-column>
            <el-table-column label="打卡地点" show-overflow-tooltip>
              <template slot-scope="{ row }">
                {{ row.checkOutAddress || row.checkInAddress || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" width="100">
              <template slot-scope="{ row }">{{ row.remark || '—' }}</template>
            </el-table-column>
          </el-table>
          <div v-if="!recordLoading && records.length === 0" class="empty-tip">该月暂无考勤记录</div>
        </el-tab-pane>

        <!-- 补卡申请 -->
        <el-tab-pane label="补卡申请" name="patches">
          <div class="tab-toolbar">
            <el-button type="primary" size="small" icon="el-icon-plus" @click="openPatch">新建补卡申请</el-button>
            <span class="toolbar-tip">补卡申请统一走流程审批，审批通过后自动修正考勤记录</span>
          </div>
          <el-table :data="myPatches" v-loading="patchLoading" size="small" border>
            <el-table-column prop="workDate" label="补卡日期" width="110"></el-table-column>
            <el-table-column label="类型" width="90">
              <template slot-scope="{ row }">{{ row.patchType === 'IN' ? '上班卡' : '下班卡' }}</template>
            </el-table-column>
            <el-table-column prop="patchTime" label="补卡时间" width="90"></el-table-column>
            <el-table-column prop="reason" label="原因" show-overflow-tooltip></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="{ row }">
                <el-tag size="mini" :type="patchTag(row.status)">{{ patchLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审批流程" width="110">
              <template slot-scope="{ row }">
                <el-button v-if="row.flowId" type="text" size="mini" @click="goFlow(row)">
                  #{{ row.flowId }}
                </el-button>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="approveComment" label="审批意见" show-overflow-tooltip>
              <template slot-scope="{ row }">{{ row.approveComment || '—' }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="140"></el-table-column>
          </el-table>
          <div v-if="!patchLoading && myPatches.length === 0" class="empty-tip">暂无补卡申请</div>
        </el-tab-pane>

        <!-- 全员考勤（管理员） -->
        <el-tab-pane label="全员考勤" name="overview" v-if="canManage">
          <div class="tab-toolbar">
            <el-date-picker v-model="overviewDate" type="date" value-format="yyyy-MM-dd"
                            :clearable="false" @change="loadOverview" style="width:160px"></el-date-picker>
            <span class="toolbar-tip">已打卡 {{ overviewCheckedCount }} / {{ overview.length }} 人</span>
          </div>
          <el-table :data="overview" v-loading="overviewLoading" size="small" border>
            <el-table-column prop="userName" label="姓名" width="110"></el-table-column>
            <el-table-column prop="username" label="账号" width="120"></el-table-column>
            <el-table-column label="上班打卡" width="150">
              <template slot-scope="{ row }">
                <template v-if="row.checkInTime">
                  {{ row.checkInTime }}
                  <el-tag size="mini" :type="row.checkInStatus === 'LATE' ? 'warning' : 'success'">{{ row.checkInStatus === 'LATE' ? '迟到' : '正常' }}</el-tag>
                </template>
                <el-tag v-else size="mini" type="info">未打卡</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下班打卡" width="150">
              <template slot-scope="{ row }">
                <template v-if="row.checkOutTime">
                  {{ row.checkOutTime }}
                  <el-tag size="mini" :type="row.checkOutStatus === 'EARLY' ? 'warning' : 'success'">{{ row.checkOutStatus === 'EARLY' ? '早退' : '正常' }}</el-tag>
                </template>
                <el-tag v-else size="mini" type="info">未打卡</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="workDuration" label="工时" width="110">
              <template slot-scope="{ row }">{{ row.workDuration || '—' }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注">
              <template slot-scope="{ row }">{{ row.remark || '—' }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 补卡申请弹窗 -->
    <el-dialog title="新建补卡申请" :visible.sync="patchVisible" width="500px">
      <el-form :model="patchForm" label-width="90px">
        <el-form-item label="补卡日期" required>
          <el-date-picker v-model="patchForm.workDate" type="date" value-format="yyyy-MM-dd"
                          :picker-options="dateOptions" placeholder="选择日期" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="补卡类型" required>
          <el-radio-group v-model="patchForm.patchType">
            <el-radio label="IN">上班卡</el-radio>
            <el-radio label="OUT">下班卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="补卡时间" required>
          <el-time-picker v-model="patchForm.patchTime" value-format="HH:mm"
                          placeholder="选择时间" style="width:100%"></el-time-picker>
        </el-form-item>
        <el-form-item label="补卡原因" required>
          <el-input v-model="patchForm.reason" type="textarea" :rows="3" maxlength="200"
                    show-word-limit placeholder="如：忘记打卡 / 外出办公"></el-input>
        </el-form-item>
        <el-form-item label="审批人" required>
          <div v-if="patchNodes.length" class="fixed-chain">
            <el-tag v-for="(n, i) in patchNodes" :key="i" size="small" type="info">
              {{ i + 1 }}. {{ n.nodeName }}（{{ n.approverName }}）
            </el-tag>
          </div>
          <el-select v-else v-model="patchForm.approverUserId" filterable
                     placeholder="该类型未配置固定节点，请选择审批人" style="width:100%">
            <el-option v-for="a in accountList" :key="a.id"
                       :label="a.nickname || a.username" :value="a.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="patchVisible = false">取消</el-button>
        <el-button type="primary" :loading="patchSubmitting" @click="doSubmitPatch">提交</el-button>
      </span>
    </el-dialog>

    <!-- 打卡点配置 -->
    <el-dialog title="打卡点配置" :visible.sync="officeVisible" width="460px" :close-on-click-modal="false">
      <el-form :model="officeForm" label-width="90px">
        <el-form-item label="打卡点名称" required>
          <el-input v-model="officeForm.officeName" maxlength="50" show-word-limit
                    placeholder="如：公司总部"></el-input>
        </el-form-item>
        <el-form-item label="纬度" required>
          <el-input-number v-model="officeForm.latitude" :precision="6" :step="0.000001"
                           :controls="false" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="经度" required>
          <el-input-number v-model="officeForm.longitude" :precision="6" :step="0.000001"
                           :controls="false" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="打卡半径" required>
          <el-input-number v-model="officeForm.radiusMeters" :min="10" :max="10000"
                           :step="50" style="width:160px"></el-input-number>
          <span class="unit-tip">米</span>
        </el-form-item>
        <div class="office-tip">
          <el-button type="text" size="mini" icon="el-icon-location" @click="useCurrentPosition">
            使用我当前定位
          </el-button>
          <span>坐标为百度 BD09 坐标系，保存后立即生效</span>
        </div>
      </el-form>
      <span slot="footer">
        <el-button @click="officeVisible = false">取消</el-button>
        <el-button type="primary" :loading="officeSaving" @click="saveOfficeConfig">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getTodayAttendance, checkIn, checkOut, listAttendanceRecords, getAttendanceStats,
  getAttendanceOverview, submitAttendancePatch, listAttendancePatches,
  getOfficeConfig, updateOfficeConfig
} from '@/api/modules/attendance'
import { listAccounts, listWorkflowNodes } from '@/api/modules/workflow'

const WEEK = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

function nowMonth() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0')
}

function todayStr() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

/** 浏览器定位为 WGS84，百度地图为 BD09，降级时做近似转换 */
function wgs84ToBd09(lng, lat) {
  const xPi = Math.PI * 3000 / 180
  const a = 6378245.0
  const ee = 0.00669342162296594323
  const transformLat = (x, y) => {
    let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0
    return ret
  }
  const transformLng = (x, y) => {
    let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
    return ret
  }
  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLng = transformLng(lng - 105.0, lat - 35.0)
  const radLat = lat / 180.0 * Math.PI
  let magic = Math.sin(radLat)
  magic = 1 - ee * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
  dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
  const gcjLat = lat + dLat
  const gcjLng = lng + dLng
  const z = Math.sqrt(gcjLng * gcjLng + gcjLat * gcjLat) + 0.00002 * Math.sin(gcjLat * xPi)
  const theta = Math.atan2(gcjLat, gcjLng) + 0.000003 * Math.cos(gcjLng * xPi)
  return { lng: z * Math.cos(theta) + 0.0065, lat: z * Math.sin(theta) + 0.006 }
}

/** 百度定位返回的 address 可能是对象（省/市/区/街道），统一转成字符串 */
function formatBaiduAddress(address) {
  if (!address) return ''
  if (typeof address === 'string') return address
  return [address.province, address.city, address.district, address.street,
    address.streetNumber || address.street_number]
    .filter(v => v)
    .join('')
}

function distanceMeters(lat1, lng1, lat2, lng2) {
  const R = 6371000
  const rad = d => d * Math.PI / 180
  const dLat = rad(lat2 - lat1)
  const dLng = rad(lng2 - lng1)
  const s = Math.sin(dLat / 2) ** 2 +
    Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(s), Math.sqrt(1 - s))
}

export default {
  name: 'AttendanceClock',
  data() {
    return {
      clock: '--:--:--',
      weekText: '',
      timer: null,
      checking: false,
      locating: false,
      baiduReady: false,
      mapReady: false,
      mapError: '',
      location: null,
      distance: 0,
      inRange: false,
      locationError: '',
      todayInfo: {
        date: '', serverTime: '', workStart: '09:00', workEnd: '18:00',
        record: null, canCheckIn: false, canCheckOut: false,
        officeName: '公司总部', officeLatitude: 39.908823, officeLongitude: 116.39747,
        radiusMeters: 500, baiduAk: ''
      },
      stats: { workDays: 0, lateCount: 0, earlyCount: 0, avgDuration: '0小时' },
      activeTab: 'records',
      month: nowMonth(),
      records: [],
      recordLoading: false,
      myPatches: [],
      patchLoading: false,
      overview: [],
      overviewLoading: false,
      overviewDate: todayStr(),
      patchVisible: false,
      patchSubmitting: false,
      patchNodes: [],
      accountList: [],
      patchForm: { workDate: todayStr(), patchType: 'IN', patchTime: '', reason: '', approverUserId: null },
      officeVisible: false,
      officeSaving: false,
      officeForm: { officeName: '', latitude: null, longitude: null, radiusMeters: 500 },
      dateOptions: {
        disabledDate(time) { return time.getTime() > Date.now() }
      }
    }
  },
  computed: {
    record() {
      return this.todayInfo.record || null
    },
    canManage() {
      return this.$store.getters.hasPermission('attendance:update')
    },
    overviewCheckedCount() {
      return this.overview.filter(o => o.checked).length
    }
  },
  created() {
    this.tick()
    this.timer = setInterval(this.tick, 1000)
    this.loadToday()
    this.loadStats()
    this.loadRecords()
    this.loadMyPatches()
    this.loadAccounts()
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
    if (this._mapTimer) clearTimeout(this._mapTimer)
  },
  methods: {
    tick() {
      const d = new Date()
      this.clock = [d.getHours(), d.getMinutes(), d.getSeconds()]
        .map(n => String(n).padStart(2, '0')).join(':')
      this.weekText = WEEK[d.getDay()]
    },
    async loadToday() {
      try {
        const res = await getTodayAttendance()
        this.todayInfo = { ...this.todayInfo, ...(res.data || {}) }
        if (!this.location) {
          if (this.todayInfo.baiduAk) {
            this.loadBaiduMap(this.todayInfo.baiduAk)
          } else {
            this.mapError = '未配置百度地图 AK，暂无法显示地图'
            this.locate()
          }
        }
      } catch (e) {
        this.$message.error(e.message || '加载打卡状态失败')
      }
    },
    async loadAccounts() {
      try {
        const res = await listAccounts()
        this.accountList = res.data || []
      } catch (e) { /* ignore */ }
    },
    loadBaiduMap(ak) {
      if (window.BMap) {
        this.baiduReady = true
        this.locate()
        return
      }
      window.initBaiduMap = () => {
        if (this._mapTimer) clearTimeout(this._mapTimer)
        this.baiduReady = true
        this.mapError = ''
        this.locate()
      }
      const script = document.createElement('script')
      script.src = `https://api.map.baidu.com/api?v=3.0&ak=${encodeURIComponent(ak)}&callback=initBaiduMap`
      script.onerror = () => {
        if (this._mapTimer) clearTimeout(this._mapTimer)
        this.baiduReady = false
        this.mapError = '百度地图脚本加载失败，请检查 AK 配置'
        this.locate()
      }
      document.head.appendChild(script)
      // AK 未开通「浏览器端」服务时脚本不会回调，超时后降级为浏览器定位
      this._mapTimer = setTimeout(() => {
        if (!window.BMap) {
          this.baiduReady = false
          this.mapError = '百度地图初始化超时，请检查 AK 是否已开通「浏览器端（JS API）」服务'
          if (!this.location) this.locate()
        }
      }, 8000)
    },
    locate() {
      this.locating = true
      this.locationError = ''
      if (this.baiduReady && window.BMap && window.BMap.Geolocation) {
        const geolocation = new window.BMap.Geolocation()
        geolocation.getCurrentPosition(
          res => {
            if (res && res.point && res.point.lat != null && res.point.lng != null) {
              this.applyLocation(res.point.lat, res.point.lng, formatBaiduAddress(res.address))
            } else {
              // 百度定位未返回坐标（权限被拒 / 非安全上下文等），降级浏览器定位
              this.locateByBrowser()
            }
          },
          () => this.locateByBrowser(),
          { enableHighAccuracy: true }
        )
      } else {
        this.locateByBrowser()
      }
    },
    locateByBrowser() {
      if (!navigator.geolocation) {
        this.locating = false
        this.locationError = '当前浏览器不支持定位'
        return
      }
      navigator.geolocation.getCurrentPosition(
        pos => {
          const bd = wgs84ToBd09(pos.coords.longitude, pos.coords.latitude)
          this.applyLocation(bd.lat, bd.lng, '')
        },
        err => {
          this.locating = false
          this.location = null
          this.locationError = '定位失败：' + (err.message || '请检查定位权限')
        },
        { enableHighAccuracy: true, timeout: 10000 }
      )
    },
    applyLocation(lat, lng, address) {
      this.location = { latitude: lat, longitude: lng, address: address || '' }
      this.locating = false
      this.recalcDistance()
      this.$nextTick(() => {
        this.initMap()
        this.renderMap()
      })
    },
    recalcDistance() {
      if (!this.location) return
      this.distance = Math.round(distanceMeters(
        this.location.latitude, this.location.longitude,
        this.todayInfo.officeLatitude, this.todayInfo.officeLongitude))
      this.inRange = this.distance <= this.todayInfo.radiusMeters
    },
    initMap() {
      if (this.mapReady || !window.BMap || !window.BMap.Map) return
      const box = this.$refs.mapBox
      if (!box) return
      try {
        const map = new window.BMap.Map(box)
        map.enableScrollWheelZoom(true)
        map.addControl(new window.BMap.NavigationControl())
        this._map = map
        this.mapReady = true
        this.mapError = ''
      } catch (e) {
        this.mapError = '地图初始化失败：' + (e.message || e)
      }
    },
    renderMap() {
      const map = this._map
      if (!map || !this.location) return
      const BMap = window.BMap
      const me = new BMap.Point(this.location.longitude, this.location.latitude)
      const office = new BMap.Point(this.todayInfo.officeLongitude, this.todayInfo.officeLatitude)
      // 我的位置
      if (!this._meMarker) {
        this._meMarker = new BMap.Marker(me)
        const label = new BMap.Label('我在这里', { offset: new BMap.Size(20, -10) })
        label.setStyle({
          color: '#fff', background: '#2e5bff', border: 'none',
          borderRadius: '10px', padding: '2px 8px', fontSize: '12px'
        })
        this._meMarker.setLabel(label)
        map.addOverlay(this._meMarker)
      } else {
        this._meMarker.setPosition(me)
      }
      // 打卡点围栏
      if (!this._officeCircle) {
        this._officeCircle = new BMap.Circle(office, this.todayInfo.radiusMeters, {
          strokeColor: '#10b981', strokeWeight: 2, strokeOpacity: 0.8,
          fillColor: '#10b981', fillOpacity: 0.12
        })
        map.addOverlay(this._officeCircle)
      } else {
        this._officeCircle.setCenter(office)
        this._officeCircle.setRadius(this.todayInfo.radiusMeters)
      }
      if (!this._officeLabel) {
        this._officeLabel = new BMap.Label(this.todayInfo.officeName, {
          position: office, offset: new BMap.Size(-26, -8)
        })
        this._officeLabel.setStyle({
          color: '#065f46', background: '#d1fae5', border: 'none',
          borderRadius: '10px', padding: '2px 8px', fontSize: '12px'
        })
        map.addOverlay(this._officeLabel)
      } else {
        this._officeLabel.setContent(this.todayInfo.officeName)
        this._officeLabel.setPosition(office)
      }
      map.centerAndZoom(me, 17)
    },
    async openOfficeConfig() {
      this.officeForm = {
        officeName: this.todayInfo.officeName || '',
        latitude: this.todayInfo.officeLatitude,
        longitude: this.todayInfo.officeLongitude,
        radiusMeters: this.todayInfo.radiusMeters
      }
      this.officeVisible = true
      try {
        const res = await getOfficeConfig()
        if (res.data) {
          this.officeForm = {
            officeName: res.data.officeName,
            latitude: res.data.latitude,
            longitude: res.data.longitude,
            radiusMeters: res.data.radiusMeters
          }
        }
      } catch (e) { /* 仅管理员可读取，失败时沿用当前配置 */ }
    },
    useCurrentPosition() {
      if (!this.location) {
        this.$message.warning('尚未获取到定位，请先点击「获取定位」')
        return
      }
      this.officeForm.latitude = Number(this.location.latitude.toFixed(6))
      this.officeForm.longitude = Number(this.location.longitude.toFixed(6))
    },
    async saveOfficeConfig() {
      const f = this.officeForm
      if (!f.officeName || !f.officeName.trim()) {
        this.$message.warning('请填写打卡点名称')
        return
      }
      if (typeof f.latitude !== 'number' || typeof f.longitude !== 'number') {
        this.$message.warning('请填写完整的经纬度')
        return
      }
      if (!f.radiusMeters) {
        this.$message.warning('请填写打卡半径')
        return
      }
      this.officeSaving = true
      try {
        const res = await updateOfficeConfig(f)
        this.$message.success(res.message || '打卡点配置已保存')
        this.officeVisible = false
        const data = res.data || {}
        this.todayInfo = {
          ...this.todayInfo,
          officeName: data.officeName,
          officeLatitude: data.latitude,
          officeLongitude: data.longitude,
          radiusMeters: data.radiusMeters
        }
        this.recalcDistance()
        this.renderMap()
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.officeSaving = false
      }
    },
    async loadStats() {
      try {
        const res = await getAttendanceStats(this.month)
        if (res.data) this.stats = res.data
      } catch (e) { /* ignore */ }
    },
    async loadRecords() {
      this.recordLoading = true
      try {
        const res = await listAttendanceRecords(this.month)
        this.records = res.data || []
      } catch (e) {
        this.records = []
        this.$message.error(e.message || '加载考勤记录失败')
      } finally {
        this.recordLoading = false
      }
    },
    async loadMyPatches() {
      this.patchLoading = true
      try {
        const res = await listAttendancePatches()
        this.myPatches = res.data || []
      } catch (e) {
        this.myPatches = []
      } finally {
        this.patchLoading = false
      }
    },
    async loadOverview() {
      this.overviewLoading = true
      try {
        const res = await getAttendanceOverview(this.overviewDate)
        this.overview = res.data || []
      } catch (e) {
        this.overview = []
      } finally {
        this.overviewLoading = false
      }
    },
    onTabClick(tab) {
      if (tab.name === 'patches') this.loadMyPatches()
      if (tab.name === 'overview') this.loadOverview()
    },
    ensureLocation() {
      if (!this.location) {
        this.$message.warning('请先获取定位后再打卡')
        this.locate()
        return false
      }
      if (!this.inRange) {
        this.$message.error(`当前不在打卡范围内，距打卡点约 ${this.distance} 米`)
        return false
      }
      return true
    },
    async doCheckIn() {
      if (!this.ensureLocation()) return
      this.checking = true
      try {
        const res = await checkIn(this.location)
        this.$message.success(res.message || '上班打卡成功')
        this.loadToday()
        this.loadRecords()
        this.loadStats()
      } catch (e) {
        this.$message.error(e.message || '打卡失败')
      } finally {
        this.checking = false
      }
    },
    async doCheckOut() {
      if (!this.ensureLocation()) return
      this.checking = true
      try {
        const res = await checkOut(this.location)
        this.$message.success(res.message || '下班打卡成功')
        this.loadToday()
        this.loadRecords()
        this.loadStats()
      } catch (e) {
        this.$message.error(e.message || '打卡失败')
      } finally {
        this.checking = false
      }
    },
    async openPatch() {
      this.patchForm = { workDate: todayStr(), patchType: 'IN', patchTime: '', reason: '', approverUserId: null }
      this.patchNodes = []
      this.patchVisible = true
      try {
        const res = await listWorkflowNodes('PATCH')
        this.patchNodes = res.data || []
      } catch (e) { /* ignore */ }
    },
    async doSubmitPatch() {
      const f = this.patchForm
      if (!f.workDate || !f.patchTime || !f.reason.trim()) {
        this.$message.warning('请填写完整的补卡信息')
        return
      }
      if (!this.patchNodes.length && !f.approverUserId) {
        this.$message.warning('请选择审批人')
        return
      }
      this.patchSubmitting = true
      try {
        const res = await submitAttendancePatch(f)
        this.$message.success(res.message || '补卡申请已提交')
        this.patchVisible = false
        this.loadMyPatches()
      } catch (e) {
        this.$message.error(e.message || '提交失败')
      } finally {
        this.patchSubmitting = false
      }
    },
    goFlow(row) {
      this.$router.push({ path: '/workflow', query: { tab: 'mine' } })
    },
    patchLabel(status) {
      return { PENDING: '审批中', APPROVED: '已通过', REJECTED: '已驳回' }[status] || status
    },
    patchTag(status) {
      return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[status] || 'info'
    }
  }
}
</script>

<style scoped>
.att-page { display: flex; flex-direction: column; gap: 16px; }
.punch-card ::v-deep .el-card__body {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #2e5bff 0%, #1a3dd4 100%);
  border-radius: 8px;
}
.punch-left { color: #fff; }
.clock { font-size: 44px; font-weight: 600; letter-spacing: 2px; line-height: 1; font-variant-numeric: tabular-nums; }
.date-line { margin-top: 10px; font-size: 14px; opacity: 0.9; }
.work-line { margin-top: 4px; font-size: 12px; opacity: 0.75; }
.today-tags { margin-top: 14px; display: flex; gap: 10px; }
.loc-line { margin-top: 10px; font-size: 12px; opacity: 0.9; display: flex; align-items: center; gap: 4px; }
.loc-ok { color: #bbf7d0; }
.loc-bad { color: #fecaca; }
.relocate { color: #fff; text-decoration: underline; padding: 0 4px; }
.punch-right { display: flex; gap: 12px; }
.punch-right .el-button { width: 120px; height: 44px; }
.map-card ::v-deep .el-card__header { padding: 12px 20px; }
.map-header { display: flex; align-items: center; justify-content: space-between; }
.map-title { font-size: 14px; font-weight: 600; color: #1f2937; }
.office-chip {
  margin-left: 8px; font-size: 12px; font-weight: 400; color: #6b7280;
}
.map-meta { font-size: 12px; color: #6b7280; display: flex; align-items: center; gap: 6px; }
.unit-tip { margin-left: 8px; font-size: 12px; color: #6b7280; }
.office-tip {
  display: flex; align-items: center; justify-content: space-between;
  padding-left: 90px; font-size: 12px; color: #909399;
}
.map-wrap { position: relative; }
.map-box { width: 100%; height: 320px; border-radius: 6px; overflow: hidden; background: #f5f7fa; }
.map-mask {
  position: absolute; inset: 0; z-index: 10;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  background: rgba(245, 247, 250, 0.94); border-radius: 6px;
  color: #6b7280; font-size: 13px;
}
.map-mask i { font-size: 30px; color: #2e5bff; }
.map-mask p { margin: 0; max-width: 80%; text-align: center; }
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 8px; padding: 18px 20px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}
.stat-icon {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; font-size: 20px; color: #fff;
}
.stat-icon.blue { background: #2e5bff; }
.stat-icon.orange { background: #f59e0b; }
.stat-icon.purple { background: #8b5cf6; }
.stat-icon.green { background: #10b981; }
.stat-value { font-size: 20px; font-weight: 600; color: #1f2937; line-height: 1.2; }
.stat-label { font-size: 12px; color: #6b7280; margin-top: 4px; }
.tab-toolbar { display: flex; align-items: center; gap: 14px; margin-bottom: 14px; }
.toolbar-tip { font-size: 12px; color: #909399; }
.text-muted { color: #c0c4cc; }
.ml6 { margin-left: 6px; }
.empty-tip { text-align: center; color: #909399; font-size: 13px; padding: 24px 0; }
.fixed-chain { display: flex; flex-wrap: wrap; gap: 6px; }
</style>
