<template>
  <div class="org-overview">
    <!-- 顶部统计 -->
    <div class="stat-row">
      <div class="stat-card stat-dept">
        <div class="stat-icon"><i class="el-icon-office-building"></i></div>
        <div class="stat-body">
          <div class="stat-num">{{ stats.dept }}</div>
          <div class="stat-label">部门总数</div>
        </div>
      </div>
      <div class="stat-card stat-pos">
        <div class="stat-icon"><i class="el-icon-postcard"></i></div>
        <div class="stat-body">
          <div class="stat-num">{{ stats.position }}</div>
          <div class="stat-label">岗位总数</div>
        </div>
      </div>
      <div class="stat-card stat-emp">
        <div class="stat-icon"><i class="el-icon-user-solid"></i></div>
        <div class="stat-body">
          <div class="stat-num">{{ stats.employee }}</div>
          <div class="stat-label">人员总数</div>
        </div>
      </div>
      <div class="stat-card stat-account">
        <div class="stat-icon"><i class="el-icon-user"></i></div>
        <div class="stat-body">
          <div class="stat-num">{{ stats.account }}</div>
          <div class="stat-label">系统账号</div>
        </div>
      </div>
    </div>

    <!-- 主体 左右分栏 -->
    <el-row :gutter="16" class="main-row">
      <!-- 左：部门树 -->
      <el-col :span="7">
        <el-card shadow="never" class="side-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-share"></i> 部门结构</span>
            <el-input
              v-model="treeQuery"
              placeholder="搜索部门"
              clearable
              size="mini"
              prefix-icon="el-icon-search"
              style="width:160px"
            />
          </div>
          <el-input v-if="treeQuery" v-model="treeQuery" placeholder="搜索部门" clearable
                    style="width:100%;margin-bottom:10px" prefix-icon="el-icon-search" size="small" />
          <div v-if="deptTree.length === 0" class="empty">
            <i class="el-icon-folder-opened"></i>
            <p>暂无部门，请先到部门管理创建</p>
          </div>
          <el-tree
            v-else
            ref="deptTreeRef"
            :data="filteredTree"
            :props="{ label: 'name', children: 'children' }"
            :node-key="'id'"
            :expand-on-click-node="false"
            node-content-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          >
            <span class="dept-node" slot-scope="{ node, data }">
              <span class="dept-name">{{ data.name }}</span>
              <span class="dept-badges">
                <span v-if="getDeptEmpCount(data.id) > 0" class="emp-badge" :title="'人员 ' + getDeptEmpCount(data.id)">{{ getDeptEmpCount(data.id) }}</span>
                <span v-if="getDeptPosCount(data.id) > 0" class="pos-badge" :title="'岗位 ' + getDeptPosCount(data.id)">{{ getDeptPosCount(data.id) }}</span>
              </span>
            </span>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右：详情 -->
      <el-col :span="17">
        <el-card v-if="selectedDept" shadow="never" class="detail-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-info"></i> 部门详情：{{ selectedDept.name }}</span>
            <el-tag :type="selectedDept.enabled ? 'success' : 'info'" size="small">
              {{ selectedDept.enabled ? '启用中' : '已停用' }}
            </el-tag>
          </div>

          <!-- 基本信息 -->
          <div class="info-section">
            <h4 class="section-title">基本信息</h4>
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="部门名称">{{ selectedDept.name }}</el-descriptions-item>
              <el-descriptions-item label="层级">第 {{ selectedDept.level + 1 }} 级</el-descriptions-item>
              <el-descriptions-item label="排序号">{{ selectedDept.sort }}</el-descriptions-item>
              <el-descriptions-item label="上级部门">{{ getParentName(selectedDept.parentId) || '—' }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ selectedDept.leaderName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ selectedDept.phone || '—' }}</el-descriptions-item>
              <el-descriptions-item label="完整路径" :span="3">{{ selectedDept.pathNames }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="3">{{ selectedDept.remark || '—' }}</el-descriptions-item>
              <el-descriptions-item label="创建人">{{ selectedDept.createdBy || '—' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ selectedDept.createdAt }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ selectedDept.updatedAt || '—' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 统计小卡 -->
          <div class="sub-stat">
            <div class="sub-stat-item">
              <i class="el-icon-user-solid" style="color:#2e5bff"></i>
              <span>{{ getDeptEmpCount(selectedDept.id) }} 人</span>
            </div>
            <div class="sub-stat-item">
              <i class="el-icon-postcard" style="color:#f59e0b"></i>
              <span>{{ getDeptPosCount(selectedDept.id) }} 个岗位</span>
            </div>
            <div class="sub-stat-item">
              <i class="el-icon-folder-opened" style="color:#10b981"></i>
              <span>{{ getChildCount(selectedDept.id) }} 个子部门</span>
            </div>
          </div>

          <!-- 岗位列表 -->
          <div class="info-section">
            <h4 class="section-title">
              岗位列表（{{ positions.length }}）
              <span class="hint">当前部门及所有子孙部门</span>
            </h4>
            <el-table v-if="positions.length > 0" :data="positions" border stripe size="small">
              <el-table-column prop="name" label="岗位名称" min-width="140" />
              <el-table-column prop="departmentName" label="所属部门" min-width="160" />
              <el-table-column label="状态" width="90" align="center">
                <template slot-scope="{ row }">
                  <el-tag :type="row.enabled ? 'success' : 'info'" size="mini">{{ row.enabled ? '启用' : '停用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="200">
                <template slot-scope="{ row }">{{ row.description || '—' }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="当前部门下暂无岗位" :image-size="80" />
          </div>

          <!-- 人员列表 -->
          <div class="info-section">
            <h4 class="section-title">
              人员列表（{{ employees.length }}）
              <span class="hint">当前部门及所有子孙部门</span>
            </h4>
            <el-table v-if="employees.length > 0" :data="employees" border stripe size="small">
              <el-table-column prop="name" label="姓名" min-width="100" />
              <el-table-column prop="employeeNo" label="工号" min-width="100">
                <template slot-scope="{ row }">{{ row.employeeNo || '—' }}</template>
              </el-table-column>
              <el-table-column label="性别" width="70" align="center">
                <template slot-scope="{ row }">
                  <el-tag size="mini" :type="row.gender === 'M' ? 'primary' : 'danger'">{{ row.gender === 'M' ? '男' : '女' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="positionName" label="岗位" min-width="140">
                <template slot-scope="{ row }">{{ row.positionName || '—' }}</template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" min-width="130">
                <template slot-scope="{ row }">{{ row.phone || '—' }}</template>
              </el-table-column>
              <el-table-column prop="username" label="系统账号" min-width="130">
                <template slot-scope="{ row }">
                  <span v-if="row.username">{{ row.username }}
                    <el-tag size="mini" :type="row.userRole === 'ADMIN' ? 'danger' : 'success'" style="margin-left:4px">
                      {{ row.userRole === 'ADMIN' ? '管理员' : '普通' }}
                    </el-tag>
                  </span>
                  <span v-else style="color:#9ca3af">—</span>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="入职时间" min-width="160" />
            </el-table>
            <el-empty v-else description="当前部门下暂无人员" :image-size="80" />
          </div>
        </el-card>

        <!-- 默认空态 -->
        <el-empty v-else description="请在左侧选择一个部门查看详情" :image-size="140" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDepartmentTree, getDepartmentFlat } from '@/api/modules/department'
import { listPositions } from '@/api/modules/position'
import { listEmployees } from '@/api/modules/employee'

export default {
  name: 'OrgOverview',
  data() {
    return {
      deptTree: [],
      flatDepts: [],
      allPositions: [],
      allEmployees: [],
      treeQuery: '',
      selectedDept: null,
      stats: { dept: 0, position: 0, employee: 0, account: 0 }
    }
  },
  computed: {
    filteredTree() {
      if (!this.treeQuery) return this.deptTree
      const kw = this.treeQuery.trim().toLowerCase()
      // 递归过滤：匹配自身或匹配后代时保留
      const filter = (list) => list
        .map(n => ({ ...n, children: n.children ? filter(n.children) : [] }))
        .filter(n => n.name.toLowerCase().includes(kw)
                || (n.children && n.children.length > 0))
      return filter(this.deptTree)
    },
    // 选中部门 + 所有子孙的 ids（一次递归收集）
    selectedDeptIds() {
      if (!this.selectedDept) return []
      const ids = []
      const collect = (list) => {
        for (const n of list) {
          ids.push(n.id)
          if (n.children && n.children.length) collect(n.children)
        }
      }
      const target = this.findNode(this.deptTree, this.selectedDept.id)
      if (target) collect([target])
      return ids
    },
    positions() {
      const ids = new Set(this.selectedDeptIds)
      return this.allPositions.filter(p => ids.has(p.departmentId))
    },
    employees() {
      const ids = new Set(this.selectedDeptIds)
      return this.allEmployees.filter(e => ids.has(e.departmentId))
    }
  },
  created() {
    this.fetchAll()
  },
  methods: {
    async fetchAll() {
      try {
        const [treeRes, flatRes, posRes, empRes] = await Promise.all([
          getDepartmentTree(),
          getDepartmentFlat(),
          listPositions(),
          listEmployees()
        ])
        this.deptTree = treeRes.data
        this.flatDepts = flatRes.data
        this.allPositions = posRes.data
        this.allEmployees = empRes.data
        this.stats.dept = this.flatDepts.length
        this.stats.position = this.allPositions.length
        this.stats.employee = this.allEmployees.length
        this.stats.account = this.allEmployees.filter(e => e.userId != null).length
        // 默认选中根部门
        if (this.deptTree.length > 0) this.selectedDept = this.deptTree[0]
      } catch (e) { /* 拦截器提示 */ }
    },
    findNode(list, id) {
      for (const n of list) {
        if (n.id === id) return n
        if (n.children && n.children.length) {
          const r = this.findNode(n.children, id)
          if (r) return r
        }
      }
      return null
    },
    findNodeFlat(id) {
      return this.flatDepts.find(d => d.id === id)
    },
    getParentName(parentId) {
      if (!parentId) return null
      const p = this.findNodeFlat(parentId)
      return p ? p.name : null
    },
    getDeptEmpCount(id) {
      // 含子孙
      const ids = new Set()
      const collect = (list) => { for (const n of list) { ids.add(n.id); if (n.children) collect(n.children) } }
      const node = this.findNode(this.deptTree, id)
      if (node) collect([node])
      return this.allEmployees.filter(e => ids.has(e.departmentId)).length
    },
    getDeptPosCount(id) {
      const ids = new Set()
      const collect = (list) => { for (const n of list) { ids.add(n.id); if (n.children) collect(n.children) } }
      const node = this.findNode(this.deptTree, id)
      if (node) collect([node])
      return this.allPositions.filter(p => ids.has(p.departmentId)).length
    },
    getChildCount(id) {
      const node = this.findNode(this.deptTree, id)
      if (!node) return 0
      const count = (list) => list.reduce((sum, n) => sum + 1 + count(n.children || []), 0)
      return count(node.children || [])
    },
    handleNodeClick(data) {
      this.selectedDept = data
    }
  }
}
</script>

<style scoped>
.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card {
  background: #fff; border-radius: 10px; padding: 18px 22px;
  display: flex; align-items: center; gap: 14px;
  border: 1px solid #eef0f4;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff;
}
.stat-dept .stat-icon { background: linear-gradient(135deg,#2e5bff,#1a3dd4); }
.stat-pos .stat-icon { background: linear-gradient(135deg,#f59e0b,#d97706); }
.stat-emp .stat-icon { background: linear-gradient(135deg,#10b981,#059669); }
.stat-account .stat-icon { background: linear-gradient(135deg,#6366f1,#4f46e5); }
.stat-num { font-size: 26px; font-weight: 600; color: #1f2937; line-height: 1.1; }
.stat-label { font-size: 13px; color: #6b7280; }

.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.main-row { margin-top: 0; }
.side-card { min-height: 580px; }
.detail-card { min-height: 580px; }

.dept-node {
  display: flex; justify-content: space-between; align-items: center; flex: 1; font-size: 14px;
}
.dept-badges { display: flex; gap: 4px; }
.dept-badges span {
  font-size: 11px; padding: 1px 6px; border-radius: 10px; font-weight: 500;
}
.emp-badge { background: #dbeafe; color: #2563eb; }
.pos-badge { background: #fef3c7; color: #d97706; }
.empty { text-align: center; padding: 40px 0; color: #9ca3af; }
.empty i { font-size: 48px; display: block; margin-bottom: 8px; color: #d1d5db; }

.info-section { margin-top: 20px; }
.section-title {
  font-size: 15px; font-weight: 600; color: #1f2937;
  margin: 0 0 10px; padding-left: 10px; border-left: 3px solid #2e5bff;
  display: flex; align-items: center; gap: 8px;
}
.section-title .hint {
  font-weight: normal; font-size: 12px; color: #9ca3af; margin-left: 8px;
}

.sub-stat {
  display: flex; gap: 24px; padding: 14px 18px; background: #f8fafc;
  border-radius: 8px; margin-top: 14px;
}
.sub-stat-item {
  display: flex; align-items: center; gap: 8px; font-size: 14px; color: #4b5563;
}
</style>
