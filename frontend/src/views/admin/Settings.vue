<template>
  <div class="settings-page">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>AI 服务配置</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="DeepSeek API Key">
          <el-input 
            v-model="form.deepseekApiKey" 
            placeholder="输入 DeepSeek API Key"
            type="password"
            show-password
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>API Key 保存后立即生效，无需重启服务</span>
          </div>
        </el-form-item>

        <el-form-item label="API 地址">
          <el-input 
            v-model="form.deepseekApiUrl" 
            placeholder="https://api.deepseek.com/chat/completions"
          />
        </el-form-item>

        <el-form-item label="模型">
          <el-select v-model="form.deepseekModel" style="width: 100%">
            <el-option label="deepseek-chat" value="deepseek-chat" />
            <el-option label="deepseek-coder" value="deepseek-coder" />
            <el-option label="deepseek-reasoner" value="deepseek-reasoner" />
          </el-select>
        </el-form-item>

        <el-form-item label="请求超时(秒)">
          <el-input-number v-model="form.deepseekTimeout" :min="10" :max="120" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
          <el-button @click="handleTest" :loading="testing">
            <el-icon><CaretRight /></el-icon>
            测试连接
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="settings-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>其他系统设置</span>
        </div>
      </template>

      <el-form :model="otherForm" label-width="120px">
        <el-form-item label="工单通知">
          <el-switch v-model="otherForm.ticketNotification" />
        </el-form-item>
        <el-form-item label="未解决问题提醒">
          <el-input-number v-model="otherForm.unknownQuestionReminder" :min="1" :max="24" />
          <span style="margin-left: 10px">小时</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="testDialogVisible" title="测试结果" width="400px">
      <div v-if="testResult.success" style="color: #67C23A">
        <el-icon><CircleCheckFilled /></el-icon> 连接成功！
      </div>
      <div v-else style="color: #F56C6C">
        <el-icon><CircleCloseFilled /></el-icon> 连接失败：{{ testResult.message }}
      </div>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Check, CaretRight, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { systemConfigApi } from '@/api/adminApi'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const testDialogVisible = ref(false)
const testResult = reactive({ success: false, message: '' })

const form = reactive({
  deepseekApiKey: '',
  deepseekApiUrl: 'https://api.deepseek.com/chat/completions',
  deepseekModel: 'deepseek-chat',
  deepseekTimeout: 30
})

const otherForm = reactive({
  ticketNotification: true,
  unknownQuestionReminder: 24
})

onMounted(async () => {
  await loadConfig()
})

async function loadConfig() {
  loading.value = true
  try {
    const res = await systemConfigApi.getConfig('deepseek.api-key')
    if (res.data?.configValue) {
      form.deepseekApiKey = res.data.configValue
    }
  } catch (e) {
    console.error('Failed to load config:', e)
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await systemConfigApi.setConfig({
      configKey: 'deepseek.api-key',
      configValue: form.deepseekApiKey,
      configName: 'DeepSeek API Key',
      description: 'DeepSeek API 密钥'
    })
    ElMessage.success('配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function handleTest() {
  testing.value = true
  try {
    const res = await systemConfigApi.testConnection()
    testResult.success = res.data?.success || false
    testResult.message = res.data?.message || '未知结果'
    testDialogVisible.value = true
  } catch (e) {
    testResult.success = false
    testResult.message = e.message || '请求失败'
    testDialogVisible.value = true
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.settings-card {
  max-width: 600px;
}

.card-header {
  font-weight: 600;
}

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
