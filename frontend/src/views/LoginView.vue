<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-card">
      <!-- Logo区域 -->
      <div class="login-header">
        <div class="login-logo">
          <span class="logo-emoji">🤖</span>
          <div class="logo-glow"></div>
        </div>
        <h1 class="login-title">AI 客服管理后台</h1>
        <p class="login-subtitle">空白格·AI · 智能客服平台</p>
      </div>

      <!-- 登录表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" autocomplete="username" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
            :prefix-icon="Lock" show-password autocomplete="current-password"
            @keyup.enter="handleLogin" />
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住账号</el-checkbox>
        </div>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部信息 -->
      <div class="login-footer">
        <span class="footer-link" @click="$router.push('/chat')">← 返回客服对话</span>
        <span class="footer-copy">© 2026 济南空白格网络科技有限公司</span>
      </div>

      <!-- 预设账号提示（Mock模式） -->
      <div class="mock-hint" v-if="showMockHint">
        <el-divider>Demo账号</el-divider>
        <div class="mock-accounts">
          <div class="mock-item" v-for="acc in mockAccounts" :key="acc.username" @click="fillAccount(acc)">
            <el-tag :type="acc.role === 'admin' ? 'danger' : acc.role === 'manager' ? 'warning' : 'info'" size="small">
              {{ acc.roleName }}
            </el-tag>
            <span class="mock-user">{{ acc.username }}</span>
            <span class="mock-pass">{{ acc.password }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const showMockHint = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码至少4位', trigger: 'blur' }
  ]
}

const mockAccounts = [
  { username: 'admin', password: 'admin123', role: 'admin', roleName: '超级管理员' },
  { username: 'manager', password: 'manager123', role: 'manager', roleName: '运营主管' },
  { username: 'agent01', password: 'agent123', role: 'agent', roleName: '客服坐席' }
]

function fillAccount(acc) {
  form.username = acc.username
  form.password = acc.password
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }

  loading.value = true
  try {
    const res = await authStore.login(form.username, form.password)
    if (rememberMe.value) {
      localStorage.setItem('ai_cs_remember_user', form.username)
    } else {
      localStorage.removeItem('ai_cs_remember_user')
    }

    ElMessage.success(`欢迎回来，${res.nickname || res.username}！`)
    const redirect = route.query.redirect || '/admin/dashboard'
    router.push(redirect)
  } catch (e) {
    ElMessage.error(e.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const saved = localStorage.getItem('ai_cs_remember_user')
  if (saved) {
    form.username = saved
    rememberMe.value = true
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-bg { position: absolute; inset: 0; pointer-events: none; }
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  background: linear-gradient(135deg, #4F6EF7, #7B5EF7);
}
.c1 { width: 600px; height: 600px; top: -200px; right: -100px; animation: float 8s ease-in-out infinite; }
.c2 { width: 400px; height: 400px; bottom: -100px; left: -80px; animation: float 6s ease-in-out infinite reverse; }
.c3 { width: 200px; height: 200px; top: 50%; left: 10%; animation: float 10s ease-in-out infinite 2s; }
.bg-grid {
  position: absolute; inset: 0;
  background-image: 
    linear-gradient(rgba(79,110,247,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(79,110,247,0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 24px;
  padding: 40px 36px 32px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3), 0 0 0 1px rgba(255,255,255,0.1);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(40px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.login-header { text-align: center; margin-bottom: 32px; }
.login-logo {
  width: 72px; height: 72px; border-radius: 20px;
  background: var(--primary-gradient);
  display: flex; align-items: center; justify-content: center;
  font-size: 36px; margin: 0 auto 16px;
  box-shadow: 0 8px 32px rgba(79,110,247,0.4);
  position: relative;
  animation: glow 3s ease-in-out infinite;
}
.logo-emoji { position: relative; z-index: 1; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2)); }
.logo-glow {
  position: absolute; inset: -4px; border-radius: 24px;
  background: var(--primary-gradient);
  opacity: 0.3; filter: blur(12px);
  animation: pulse 2s ease-in-out infinite;
}
.login-title { font-size: 22px; font-weight: 700; color: #1a1a2e; margin: 0 0 6px; }
.login-subtitle { font-size: 13px; color: #8c8c8c; margin: 0; }

.login-options {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 4px;
}

.login-btn {
  width: 100%; height: 48px; font-size: 16px; font-weight: 600;
  border-radius: 12px;
  background: var(--primary-gradient);
  border: none; letter-spacing: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative; overflow: hidden;
}
.login-btn::before {
  content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: left 0.5s ease;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(79,110,247,0.4);
}
.login-btn:hover::before { left: 100%; }
.login-btn:active { transform: translateY(0); }

.login-footer {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 20px; font-size: 12px;
}
.footer-link { color: var(--primary); cursor: pointer; transition: all 0.2s; }
.footer-link:hover { opacity: 0.8; transform: translateX(2px); }
.footer-copy { color: #bfbfbf; }

.mock-hint { margin-top: 12px; }
.mock-hint :deep(.el-divider__text) { font-size: 11px; color: #bfbfbf; background: transparent; }
.mock-accounts { display: flex; flex-direction: column; gap: 8px; }
.mock-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; border-radius: 8px; cursor: pointer;
  background: #f7f8fa; transition: all 0.2s; font-size: 13px;
}
.mock-item:hover { background: var(--primary-light); transform: translateX(4px); }
.mock-user { font-weight: 600; color: #1a1a2e; min-width: 70px; }
.mock-pass { color: #8c8c8c; }

/* 移动端适配 */
@media (max-width: 480px) {
  .login-card { width: 92%; padding: 28px 20px 24px; border-radius: 20px; }
  .login-title { font-size: 18px; }
  .login-logo { width: 64px; height: 64px; font-size: 32px; border-radius: 18px; }
}
</style>
