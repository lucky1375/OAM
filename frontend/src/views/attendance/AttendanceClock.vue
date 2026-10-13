<template>
  <div class="placeholder-page">
    <el-card class="placeholder-card" shadow="never">
      <div class="icon-wrap">
        <i class="el-icon-time"></i>
      </div>
      <h2 class="title">考勤打卡</h2>
      <p class="desc">上下班打卡、考勤记录查询与统计、异常补卡申请</p>

      <el-divider></el-divider>

      <div class="coming-soon">
        <el-tag type="info" size="medium">功能开发中</el-tag>
        <ul class="feature-list">
          <li><i class="el-icon-check"></i> 定位打卡 / WiFi 打卡</li>
          <li><i class="el-icon-check"></i> 考勤记录查询与统计</li>
          <li><i class="el-icon-check"></i> 异常补卡申请</li>
          <li><i class="el-icon-check"></i> 月度考勤报表导出</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script>
export default { name: 'AttendanceClock' }
</script>

<style scoped>
.placeholder-page { max-width: 640px; margin: 48px auto; }
.placeholder-card { text-align: center; padding: 40px 32px; }
.icon-wrap {
  width: 80px; height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #2e5bff 0%, #1a3dd4 100%);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.icon-wrap i { font-size: 36px; color: #fff; }
.title { font-size: 22px; font-weight: 600; margin: 0 0 10px; color: #1f2937; }
.desc { font-size: 14px; color: #6b7280; margin: 0; }
.coming-soon { margin-top: 16px; }
.feature-list {
  list-style: none; padding: 0; margin: 20px 0 0;
  display: flex; flex-direction: column; gap: 10px;
}
.feature-list li { font-size: 14px; color: #4b5563; }
.feature-list i { color: #10b981; margin-right: 8px; }
</style>
