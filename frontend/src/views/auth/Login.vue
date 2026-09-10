<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-icon">🗂</span>
          <span class="logo-text">OAM</span>
        </div>
        <h1 class="brand-title">
          办公自动化管理系统，<br />
          让企业协作更高效
        </h1>
        <p class="brand-subtitle">
          统一管理组织内部的办公流程与信息，一体化提升团队协作效率。
        </p>

        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>账号与角色管理，按模块按操作授权</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>数据审计字段，创建更新全可追溯</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>前后端分离，开箱即用</span>
          </div>
        </div>
      </div>

      <div class="brand-footer">© 2026 OAM Inc. All rights reserved.</div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-panel">
      <div class="login-card">
        <h2 class="card-title">欢迎回来</h2>
        <p class="card-subtitle">请登录以继续使用办公自动化管理系统</p>

        <el-form
          ref="loginForm"
          :model="form"
          :rules="rules"
          label-width="0"
          @keyup.enter.native="handleLogin"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="form.username"
              placeholder="请输入账号"
              prefix-icon="el-icon-user"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>

          <el-form-item class="btn-item">
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tip">默认管理员账号：admin / admin123</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          await this.$store.dispatch('login', { ...this.form })
          this.$message.success('登录成功')
          this.$router.push('/')
        } catch (e) {
          // 错误已在拦截器中提示
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  flex: 1;
  background: linear-gradient(160deg, #2e5bff 0%, #1a3dd4 60%, #0f2ba8 100%);
  color: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 56px 64px;
  overflow: hidden;
}

/* 装饰光斑 */
.brand-panel::before {
  content: '';
  position: absolute;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  top: -80px;
  left: -80px;
}
.brand-panel::after {
  content: '';
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  bottom: -60px;
  right: -40px;
}

.brand-content {
  position: relative;
  z-index: 1;
  margin-top: 40px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 64px;
}
.logo-icon {
  font-size: 32px;
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-title {
  font-size: 38px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 20px;
  letter-spacing: 0.5px;
}

.brand-subtitle {
  font-size: 16px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.8);
  max-width: 420px;
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}
.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff6b5b;
  flex-shrink: 0;
}

.brand-footer {
  position: relative;
  z-index: 1;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}

/* ===== 右侧表单区 ===== */
.form-panel {
  flex: 1;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 420px;
}

.card-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 10px;
}

.card-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 40px;
}

.login-form >>> .el-form-item {
  margin-bottom: 22px;
}
.login-form >>> .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  border-color: #e5e7eb;
  font-size: 14px;
  padding-left: 42px;
}
.login-form >>> .el-input__inner:focus {
  border-color: #2e5bff;
  box-shadow: 0 0 0 3px rgba(46, 91, 255, 0.12);
}
.login-form >>> .el-input__prefix {
  left: 14px;
}

.btn-item {
  margin-top: 32px;
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #2e5bff 0%, #1a3dd4 100%);
  border: none;
}
.login-btn:hover {
  opacity: 0.92;
}

.login-tip {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

/* 窄屏降级：宽度 < 900px 时堆叠 */
@media (max-width: 900px) {
  .login-page {
    flex-direction: column;
  }
  .brand-panel {
    flex: 0 0 auto;
    min-height: 220px;
    padding: 40px 32px;
  }
  .brand-content {
    margin-top: 0;
  }
  .brand-logo {
    margin-bottom: 24px;
  }
  .brand-title {
    font-size: 24px;
  }
  .brand-subtitle,
  .brand-features {
    display: none;
  }
  .brand-footer {
    display: none;
  }
}
</style>
