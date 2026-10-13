<template>
  <div class="placeholder-page">
    <el-card class="placeholder-card" shadow="never">
      <div class="icon-wrap"><i class="el-icon-bell"></i></div>
      <h2 class="title">公告通知</h2>
      <p class="desc">公告发布、阅读统计，重要信息触达全员</p>
      <el-divider></el-divider>
      <div class="coming-soon">
        <el-tag type="info" size="medium">功能开发中</el-tag>
        <ul class="feature-list">
          <li><i class="el-icon-check"></i> 公告发布 / 置顶 / 撤回</li>
          <li><i class="el-icon-check"></i> 按部门定向推送</li>
          <li><i class="el-icon-check"></i> 阅读统计与已读未读</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>
<script>export default { name: 'Announcement' }</script>
<style scoped>
.placeholder-page { max-width: 640px; margin: 48px auto; }
.placeholder-card { text-align: center; padding: 40px 32px; }
.icon-wrap { width: 80px; height: 80px; margin: 0 auto 20px; background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.icon-wrap i { font-size: 36px; color: #fff; }
.title { font-size: 22px; font-weight: 600; margin: 0 0 10px; color: #1f2937; }
.desc { font-size: 14px; color: #6b7280; margin: 0; }
.coming-soon { margin-top: 16px; }
.feature-list { list-style: none; padding: 0; margin: 20px 0 0; display: flex; flex-direction: column; gap: 10px; }
.feature-list li { font-size: 14px; color: #4b5563; }
.feature-list i { color: #10b981; margin-right: 8px; }
</style>
