<template>
  <div class="home">
    <el-card class="welcome-card">
      <div slot="header" class="card-header">
        <span>欢迎使用</span>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="当前用户">{{ nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
        <el-descriptions-item label="温馨提示">
          {{ isAdmin ? '您是管理员，可通过顶部菜单进入「账号管理」。' : '您当前为普通成员账号。' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Home',
  computed: {
    nickname () {
      return this.$store.getters.nickname || '—'
    },
    isAdmin () {
      return this.$store.getters.isAdmin
    },
    roleText () {
      return this.isAdmin ? '管理员（ADMIN）' : '普通用户（USER）'
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 720px;
  margin: 24px auto;
}
.welcome-card .card-header {
  font-weight: 600;
}
</style>
