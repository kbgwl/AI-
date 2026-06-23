<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">🔒 修改密码</h2>
    </div>

    <div class="password-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="password-form">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="loading">确认修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="password-tips">
        <h4>密码安全提示</h4>
        <ul>
          <li>密码长度至少8位</li>
          <li>建议包含大小写字母、数字和特殊字符</li>
          <li>不要使用与其他网站相同的密码</li>
          <li>定期更换密码，建议每3个月更换一次</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function changePassword() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }

  loading.value = true
  try {
    // TODO: 调用后端API修改密码
    await new Promise(r => setTimeout(r, 500))
    ElMessage.success('密码修改成功，请重新登录')
    authStore.logout()
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}
</script>

<style scoped>
.password-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-card);
  display: flex;
  gap: 48px;
}

.password-form {
  flex: 1;
  max-width: 450px;
}

.password-tips {
  flex: 1;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.password-tips h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
}

.password-tips li {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
}
</style>
