<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">👤 个人设置</h2>
    </div>

    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="triggerUpload">
            <el-avatar :size="80" :src="avatarUrl" :style="{background: avatarUrl ? 'transparent' : 'var(--primary-gradient)'}" class="profile-avatar">
              {{ !avatarUrl ? authStore.userInitial : '' }}
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <input type="file" ref="fileInput" style="display:none" accept="image/*" @change="handleAvatarUpload" />
          <el-button size="small" type="primary" plain @click="triggerUpload" :loading="uploading">
            {{ uploading ? '上传中...' : '更换头像' }}
          </el-button>
        </div>
        <div class="profile-info">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ authStore.user?.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">角色</span>
            <el-tag :type="authStore.userRole === 'admin' ? 'danger' : authStore.userRole === 'manager' ? 'warning' : 'info'" size="small">
              {{ authStore.userRoleName }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">技能组</span>
            <span class="info-value">{{ authStore.user?.skillGroup || 'default' }}</span>
          </div>
        </div>
      </div>

      <el-divider />

      <el-form :model="form" label-width="100px" class="profile-form">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="请输入个人简介" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const saving = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const avatarUrl = ref(authStore.user?.avatar || '')

const form = reactive({
  nickname: '',
  email: '',
  phone: '',
  bio: ''
})

onMounted(() => {
  if (authStore.user) {
    form.nickname = authStore.user.nickname || ''
    form.email = authStore.user.email || ''
    form.phone = authStore.user.phone || ''
    form.bio = authStore.user.bio || ''
    avatarUrl.value = authStore.user.avatar || ''
  }
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小 (最大2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  uploading.value = true
  try {
    // 转换为Base64
    const reader = new FileReader()
    reader.onload = async (event) => {
      const base64 = event.target.result
      avatarUrl.value = base64

      // 保存到本地存储和用户信息
      if (authStore.user) {
        authStore.user.avatar = base64
        authStore.persist()
      }

      ElMessage.success('头像更新成功')
      uploading.value = false
    }
    reader.readAsDataURL(file)
  } catch (e) {
    ElMessage.error('上传失败')
    uploading.value = false
  }
  e.target.value = ''
}

async function saveProfile() {
  saving.value = true
  try {
    // 更新用户信息
    if (authStore.user) {
      authStore.user.nickname = form.nickname
      authStore.user.email = form.email
      authStore.user.phone = form.phone
      authStore.user.bio = form.bio
      authStore.persist()
    }
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function resetForm() {
  if (authStore.user) {
    form.nickname = authStore.user.nickname || ''
    form.email = authStore.user.email || ''
    form.phone = authStore.user.phone || ''
    form.bio = authStore.user.bio || ''
    avatarUrl.value = authStore.user.avatar || ''
  }
}
</script>

<style scoped>
.profile-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-card);
}

.profile-header {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 50%;
}

.avatar-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.profile-avatar {
  font-size: 32px;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-label {
  font-size: 13px;
  color: var(--text-muted);
  width: 60px;
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.profile-form {
  max-width: 500px;
}
</style>
