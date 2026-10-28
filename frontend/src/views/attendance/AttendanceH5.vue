<template>
  <div class="h5-page">
    <div class="h5-top">
      <div class="h5-date">{{ dateText }} {{ weekdayText }}</div>
      <div class="h5-time">{{ clock }}</div>
      <div class="h5-meta">{{ displayName }} · {{ todayInfo.officeName || '考勤打卡' }}</div>
    </div>

    <div class="punch-area">
      <button class="punch-btn" :class="{ disabled: !canPunch }" :disabled="!canPunch || punching"
              @click="punch">
        <i :class="punching ? 'el-icon-loading' : (actionType === 'out' ? 'el-icon-moon' : 'el-icon-sunny')"></i>
        <span class="punch-label">{{ actionLabel }}</span>
        <span class="punch-sub">{{ punchHint }}</span>
      </button>
      <div class="loc-bar">
        <i class="el-icon-location-outline"></i>
        <span v-if="!location">{{ locating ? '正在获取定位…' : (locationError || '未获取到定位') }}</span>
        <span v-else>距打卡点 <b>{{ distance }}</b> 米
          <em :class="inRange ? 'ok' : 'bad'">{{ inRange ? '范围内' : '超出范围' }}</em>
        </span>
        <a @click="locate">刷新定位</a>
      </div>
    </div>

    <div class="card today-card">
      <div class="cell">
        <label>上班打卡</label>
        <strong>{{ (todayInfo.record && todayInfo.record.checkInTime) || '--:--' }}</strong>
        <em v-if="todayInfo.record && todayInfo.record.checkInStatus === 'LATE'" class="bad">迟到</em>
      </div>
      <div class="divider"></div>
      <div class="cell">
        <label>下班打卡</label>
        <strong>{{ (todayInfo.record && todayInfo.record.checkOutTime) || '--:--' }}</strong>
        <em v-if="todayInfo.record && todayInfo.record.checkOutStatus === 'EARLY'" class="warn">早退</em>
      </div>
    </div>

    <div class="card map-card">
      <div ref="mapBox" class="h5-map"></div>
      <div class="map-mask" v-if="!mapReady">
        <i class="el-icon-map-location"></i>
        <span>{{ mapError || '地图加载中…' }}</span>
      </div>
    </div>

    <div class="card stats-card">
      <div class="stat"><b>{{ stats.workDays || 0 }}</b><span>出勤天数</span></div>
      <div class="stat"><b>{{ stats.lateCount || 0 }}</b><span>迟到</span></div>
      <div class="stat"><b>{{ stats.earlyCount || 0 }}</b><span>早退</span></div>
      <div class="stat"><b>{{ stats.avgDuration || '—' }}</b><span>平均工时</span></div>
    </div>

    <div class="card records-card">
      <div class="card-title">最近记录</div>
      <div class="record" v-for="r in recentRecords" :key="r.id">
        <div class="record-date">{{ r.workDate }}</div>
        <div class="record-times">{{ r.checkInTime || '--:--' }} ~ {{ r.checkOutTime || '--:--' }}</div>
        <div class="record-duration">{{ r.workDuration || '—' }}</div>
      </div>
      <div class="empty" v-if="recentRecords.length === 0">暂无考勤记录</div>
    </div>
  </div>
</template>

<script>
import {
  getTodayAttendance, checkIn, checkOut, listAttendanceRecords, getAttendanceStats
} from '@/api/modules/attendance'

const WEEKDAYS = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

function pad(v) {
  return v < 10 ? '0' + v : '' + v
}

function monthStr() {
  const d = new Date()
  return d.getFullYear() + '-' + pad(d.getMonth() + 1)
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

function wgs84ToBd09(lng, lat) {
  const xPi = Math.PI * 3000 / 180
  const z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * xPi)
  const theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * xPi)
  return { lng: z * Math.cos(theta) + 0.0065, lat: z * Math.sin(theta) + 0.006 }
}

function distanceMeters(lat1, lng1, lat2, lng2) {
  const R = 6371000
  const rad = d => d * Math.PI / 180
  const dLat = rad(lat2 - lat1)
  const dLng = rad(lng2 - lng1)
  const s = Math.sin(dLat / 2) ** 2 +
    Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(s))
}

export default {
  name: 'AttendanceH5',
  data() {
    return {
      clock: '--:--:--',
      dateText: '',
      weekdayText: '',
      todayInfo: { canCheckIn: false, canCheckOut: false },
      location: null,
      locating: false,
      locationError: '',
      distance: 0,
      inRange: false,
      baiduReady: false,
      mapReady: false,
      mapError: '',
      punching: false,
      stats: {},
      records: []
    }
  },
  computed: {
    displayName() {
      return this.$store.getters.nickname || (this.$store.state.user && this.$store.state.user.username) || ''
    },
    actionType() {
      return this.todayInfo.canCheckIn ? 'in' : 'out'
    },
    actionLabel() {
      if (this.todayInfo.canCheckIn) return '上班打卡'
      if (this.todayInfo.canCheckOut) return '下班打卡'
      return '今日已打卡'
    },
    punchHint() {
      if (this.punching) return '打卡中…'
      if (!this.todayInfo.canCheckIn && !this.todayInfo.canCheckOut) return '今日打卡已完成'
      if (!this.location) return this.locating ? '定位中…' : '请先获取定位'
      if (!this.inRange) return '不在打卡范围内'
      return '点击打卡'
    },
    canPunch() {
      return (this.todayInfo.canCheckIn || this.todayInfo.canCheckOut)
        && !!this.location && this.inRange
    },
    recentRecords() {
      return (this.records || []).slice(0, 5)
    }
  },
  created() {
    this.tick()
    this.timer = setInterval(this.tick, 1000)
    this.loadToday()
    this.loadStats()
    this.loadRecords()
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
    if (this._mapTimer) clearTimeout(this._mapTimer)
  },
  methods: {
    tick() {
      const now = new Date()
      this.clock = pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds())
      this.dateText = now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate())
      this.weekdayText = WEEKDAYS[now.getDay()]
    },
    async loadToday() {
      try {
        const res = await getTodayAttendance()
        this.todayInfo = res.data || {}
        if (!this.location) {
          if (this.todayInfo.baiduAk) {
            this.loadBaiduMap(this.todayInfo.baiduAk)
          } else {
            this.mapError = '未配置百度地图 AK，无法显示地图'
            this.locate()
          }
        }
      } catch (e) { /* 已统一提示 */ }
    },
    async loadStats() {
      try {
        const res = await getAttendanceStats(monthStr())
        this.stats = res.data || {}
      } catch (e) { /* 忽略 */ }
    },
    async loadRecords() {
      try {
        const res = await listAttendanceRecords(monthStr())
        this.records = res.data || []
      } catch (e) {
        this.records = []
      }
    },
    loadBaiduMap(ak) {
      if (window.BMap) {
        this.baiduReady = true
        this.locate()
        return
      }
      window.initH5BaiduMap = () => {
        if (this._mapTimer) clearTimeout(this._mapTimer)
        this.baiduReady = true
        this.mapError = ''
        this.locate()
      }
      const script = document.createElement('script')
      script.src = `https://api.map.baidu.com/api?v=3.0&ak=${encodeURIComponent(ak)}&callback=initH5BaiduMap`
      script.onerror = () => {
        if (this._mapTimer) clearTimeout(this._mapTimer)
        this.baiduReady = false
        this.mapError = '地图加载失败'
        this.locate()
      }
      document.head.appendChild(script)
      this._mapTimer = setTimeout(() => {
        if (!window.BMap) {
          this.baiduReady = false
          this.mapError = '地图加载超时'
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
      this.distance = Math.round(distanceMeters(
        lat, lng, this.todayInfo.officeLatitude, this.todayInfo.officeLongitude))
      this.inRange = this.distance <= this.todayInfo.radiusMeters
      this.$nextTick(() => {
        this.initMap()
        this.renderMap()
      })
    },
    initMap() {
      if (this.mapReady || !window.BMap || !window.BMap.Map) return
      const box = this.$refs.mapBox
      if (!box) return
      try {
        const map = new window.BMap.Map(box)
        map.enableScrollWheelZoom(false)
        map.disableDragging()
        this._map = map
        this.mapReady = true
        this.mapError = ''
      } catch (e) {
        this.mapError = '地图初始化失败'
      }
    },
    renderMap() {
      const map = this._map
      if (!map || !this.location) return
      const BMap = window.BMap
      const me = new BMap.Point(this.location.longitude, this.location.latitude)
      const office = new BMap.Point(this.todayInfo.officeLongitude, this.todayInfo.officeLatitude)
      if (!this._meMarker) {
        this._meMarker = new BMap.Marker(me)
        map.addOverlay(this._meMarker)
      } else {
        this._meMarker.setPosition(me)
      }
      if (!this._officeCircle) {
        this._officeCircle = new BMap.Circle(office, this.todayInfo.radiusMeters, {
          strokeColor: '#10b981', strokeWeight: 2, strokeOpacity: 0.8,
          fillColor: '#10b981', fillOpacity: 0.12
        })
        map.addOverlay(this._officeCircle)
      } else {
        this._officeCircle.setRadius(this.todayInfo.radiusMeters)
        this._officeCircle.setCenter(office)
      }
      map.centerAndZoom(me, 16)
    },
    async punch() {
      if (!this.canPunch) {
        this.$message.warning(this.punchHint)
        return
      }
      this.punching = true
      try {
        const isCheckIn = this.todayInfo.canCheckIn
        const res = isCheckIn ? await checkIn(this.location) : await checkOut(this.location)
        this.$message.success(res.message || (isCheckIn ? '上班打卡成功' : '下班打卡成功'))
        await this.loadToday()
        this.loadStats()
        this.loadRecords()
      } catch (e) {
        this.$message.error(e.message || '打卡失败')
      } finally {
        this.punching = false
      }
    }
  }
}
</script>

<style scoped>
.h5-page {
  min-height: 100vh;
  background: #f2f4f8;
  padding: 0 14px 24px;
  box-sizing: border-box;
  max-width: 520px;
  margin: 0 auto;
}
.h5-top {
  margin: 0 -14px;
  padding: 22px 22px 46px;
  background: linear-gradient(160deg, #2e5bff 0%, #1e40d8 100%);
  color: #fff;
  border-radius: 0 0 22px 22px;
}
.h5-date { font-size: 13px; opacity: 0.85; }
.h5-time {
  font-size: 38px; font-weight: 700; letter-spacing: 1px;
  font-variant-numeric: tabular-nums; margin: 4px 0;
}
.h5-meta { font-size: 12px; opacity: 0.85; }
.punch-area {
  margin-top: -32px;
  display: flex; flex-direction: column; align-items: center;
}
.punch-btn {
  width: 132px; height: 132px; border-radius: 50%;
  border: 6px solid rgba(255, 255, 255, 0.85);
  background: linear-gradient(160deg, #2e5bff, #1d4ed8);
  color: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 2px;
  box-shadow: 0 10px 24px rgba(46, 91, 255, 0.35);
  cursor: pointer; outline: none;
}
.punch-btn i { font-size: 24px; }
.punch-label { font-size: 16px; font-weight: 600; }
.punch-sub { font-size: 11px; opacity: 0.85; }
.punch-btn.disabled {
  background: #cbd5e1;
  box-shadow: none;
  cursor: not-allowed;
}
.loc-bar {
  margin-top: 12px; display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: #4b5563;
}
.loc-bar i { color: #2e5bff; }
.loc-bar b { color: #1f2937; }
.loc-bar em { font-style: normal; margin-left: 4px; }
.loc-bar em.ok { color: #10b981; }
.loc-bar em.bad { color: #ef4444; }
.loc-bar a { color: #2e5bff; margin-left: 4px; }
.card {
  background: #fff; border-radius: 12px; margin-top: 14px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
}
.today-card { display: flex; align-items: center; padding: 16px 0; }
.today-card .cell {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; gap: 4px;
}
.today-card label { font-size: 12px; color: #9ca3af; }
.today-card strong { font-size: 20px; color: #1f2937; font-variant-numeric: tabular-nums; }
.today-card em { font-size: 11px; font-style: normal; }
.today-card em.bad { color: #ef4444; }
.today-card em.warn { color: #f59e0b; }
.divider { width: 1px; height: 34px; background: #eef2f7; }
.map-card { position: relative; overflow: hidden; }
.h5-map { width: 100%; height: 180px; }
.map-mask {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  gap: 6px; background: rgba(245, 247, 250, 0.92); color: #6b7280; font-size: 12px;
}
.map-mask i { font-size: 20px; color: #2e5bff; }
.stats-card { display: flex; padding: 14px 0; }
.stats-card .stat {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px;
}
.stats-card b { font-size: 17px; color: #1f2937; }
.stats-card span { font-size: 11px; color: #9ca3af; }
.records-card { padding: 14px 16px; }
.card-title { font-size: 13px; font-weight: 600; color: #1f2937; margin-bottom: 8px; }
.record {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 0; border-bottom: 1px solid #f1f5f9; font-size: 12px; color: #4b5563;
}
.record:last-child { border-bottom: none; }
.record-date { width: 88px; color: #1f2937; }
.record-times { flex: 1; text-align: center; font-variant-numeric: tabular-nums; }
.record-duration { width: 74px; text-align: right; color: #9ca3af; }
.empty { padding: 18px 0; text-align: center; font-size: 12px; color: #9ca3af; }
</style>
