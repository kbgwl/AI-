<template>
  <div class="sysconfig-page">
    <div class="page-header">
      <h2>系统配置</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 公司信息 -->
      <el-tab-pane label="公司信息" name="company">
        <div class="config-form">
          <div class="field"><label>公司名称</label><el-input v-model="config.company.name" placeholder="济南空白格网络科技有限公司" /></div>
          <div class="field"><label>客服电话</label><el-input v-model="config.company.phone" placeholder="400-xxx-xxxx" /></div>
          <div class="field"><label>客服邮箱</label><el-input v-model="config.company.email" placeholder="support@example.com" /></div>
          <div class="field"><label>公司地址</label><el-input v-model="config.company.address" placeholder="山东省济南市..." /></div>
          <div class="field"><label>官网地址</label><el-input v-model="config.company.website" placeholder="https://www.example.com" /></div>
          <div class="field"><label>Logo URL</label><el-input v-model="config.company.logo" placeholder="https://..." /></div>
          <div class="field"><label>ICP备案号</label><el-input v-model="config.company.icp" placeholder="鲁ICP备XXXXXXXX号" /></div>
        </div>
      </el-tab-pane>

      <!-- AI模型配置 -->
      <el-tab-pane label="AI模型" name="ai">
        <div class="config-form">
          <div class="field-row">
            <div class="field"><label>模型供应商</label>
              <el-select v-model="config.ai.provider" style="width:100%">
                <el-option label="DeepSeek" value="deepseek" />
                <el-option label="OpenAI" value="openai" />
                <el-option label="通义千问" value="qwen" />
                <el-option label="智谱AI" value="zhipu" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </div>
            <div class="field"><label>模型名称</label>
              <el-input v-model="config.ai.model" placeholder="deepseek-chat" />
            </div>
          </div>
          <div class="field"><label>API Key</label><el-input v-model="config.ai.apiKey" type="password" show-password placeholder="sk-..." /></div>
          <div class="field"><label>API Base URL</label><el-input v-model="config.ai.baseUrl" placeholder="https://api.deepseek.com/v1" /></div>
          <div class="field-row">
            <div class="field"><label>最大Token数</label><el-input-number v-model="config.ai.maxTokens" :min="100" :max="8000" /></div>
            <div class="field"><label>温度 (Temperature)</label><el-slider v-model="config.ai.temperature" :min="0" :max="2" :step="0.1" show-input /></div>
          </div>
          <div class="field"><label>系统提示词</label><el-input v-model="config.ai.systemPrompt" type="textarea" :rows="4" placeholder="你是一个专业的AI客服助手..." /></div>
          <div class="field"><label>API超时（秒）</label><el-input-number v-model="config.ai.timeout" :min="5" :max="120" /></div>
          <div class="field-actions">
            <el-button type="primary" @click="testAi" :loading="testingAi">测试AI连接</el-button>
            <span v-if="aiTestResult" :class="aiTestOk ? 'test-ok' : 'test-err'">{{ aiTestResult }}</span>
          </div>
        </div>
      </el-tab-pane>

      <!-- 邮件配置 -->
      <el-tab-pane label="邮件服务" name="email">
        <div class="config-form">
          <div class="field-row">
            <div class="field"><label>SMTP服务器</label><el-input v-model="config.email.host" placeholder="smtp.qq.com" /></div>
            <div class="field"><label>端口</label><el-input-number v-model="config.email.port" :min="1" :max="65535" /></div>
          </div>
          <div class="field-row">
            <div class="field"><label>发件邮箱</label><el-input v-model="config.email.from" placeholder="noreply@example.com" /></div>
            <div class="field"><label>邮箱密码/授权码</label><el-input v-model="config.email.password" type="password" show-password /></div>
          </div>
          <div class="field"><label>发件人名称</label><el-input v-model="config.email.senderName" placeholder="AI客服系统" /></div>
          <el-button @click="testEmail" :loading="testingEmail">发送测试邮件</el-button>
        </div>
      </el-tab-pane>

      <!-- 短信配置 -->
      <el-tab-pane label="短信服务" name="sms">
        <div class="config-form">
          <div class="field"><label>短信服务商</label>
            <el-select v-model="config.sms.provider" style="width:100%">
              <el-option label="阿里云短信" value="aliyun" />
              <el-option label="腾讯云短信" value="tencent" />
              <el-option label="华为云短信" value="huawei" />
            </el-select>
          </div>
          <div class="field-row">
            <div class="field"><label>AccessKey ID</label><el-input v-model="config.sms.accessKeyId" /></div>
            <div class="field"><label>AccessKey Secret</label><el-input v-model="config.sms.accessKeySecret" type="password" show-password /></div>
          </div>
          <div class="field"><label>短信签名</label><el-input v-model="config.sms.signName" placeholder="空白格AI" /></div>
          <div class="field"><label>模板ID（验证码）</label><el-input v-model="config.sms.templateCode" placeholder="SMS_XXXXXX" /></div>
          <el-button @click="testSms" :loading="testingSms">发送测试短信</el-button>
        </div>
      </el-tab-pane>

      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <div class="config-form">
          <div class="field"><label>Session超时（分钟）</label><el-input-number v-model="config.security.sessionTimeout" :min="5" :max="1440" /></div>
          <div class="field"><label>登录失败锁定次数</label><el-input-number v-model="config.security.maxLoginAttempts" :min="3" :max="20" /></div>
          <div class="field"><label>密码最小长度</label><el-input-number v-model="config.security.minPasswordLength" :min="6" :max="32" /></div>
          <div class="field"><label>IP白名单（逗号分隔）</label><el-input v-model="config.security.ipWhitelist" type="textarea" :rows="2" placeholder="192.168.1.0/24,10.0.0.0/8" /></div>
          <div class="field-row">
            <div class="field"><label>开启验证码</label><el-switch v-model="config.security.captchaEnabled" /></div>
            <div class="field"><label>开启IP限制</label><el-switch v-model="config.security.ipLimitEnabled" /></div>
          </div>
          <div class="field"><label>JWT密钥</label><el-input v-model="config.security.jwtSecret" type="password" show-password placeholder="留空则自动生成" /></div>
        </div>
      </el-tab-pane>

      <!-- 客服设置 -->
      <el-tab-pane label="客服设置" name="cs">
        <div class="config-form">
          <div class="field"><label>欢迎语</label><el-input v-model="config.cs.welcomeMessage" type="textarea" :rows="2" placeholder="您好！有什么可以帮您？" /></div>
          <div class="field"><label>离线消息</label><el-input v-model="config.cs.offlineMessage" type="textarea" :rows="2" placeholder="当前为非工作时间..." /></div>
          <div class="field-row">
            <div class="field"><label>工作开始时间</label><el-time-picker v-model="config.cs.workStart" format="HH:mm" value-format="HH:mm" /></div>
            <div class="field"><label>工作结束时间</label><el-time-picker v-model="config.cs.workEnd" format="HH:mm" value-format="HH:mm" /></div>
          </div>
          <div class="field"><label>工作日</label>
            <el-checkbox-group v-model="config.cs.workDays">
              <el-checkbox v-for="d in weekDays" :key="d.value" :value="d.value">{{ d.label }}</el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="field-row">
            <div class="field"><label>最大排队数</label><el-input-number v-model="config.cs.maxQueue" :min="1" :max="100" /></div>
            <div class="field"><label>排队超时（秒）</label><el-input-number v-model="config.cs.queueTimeout" :min="10" :max="300" /></div>
          </div>
          <div class="field-row">
            <div class="field"><label>自动回复开启</label><el-switch v-model="config.cs.autoReplyEnabled" /></div>
            <div class="field"><label>满意度评价</label><el-switch v-model="config.cs.csatEnabled" /></div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 存储设置 -->
      <el-tab-pane label="存储设置" name="storage">
        <div class="config-form">
          <div class="field"><label>存储类型</label>
            <el-select v-model="config.storage.type" style="width:100%">
              <el-option label="本地存储" value="local" />
              <el-option label="阿里云OSS" value="aliyun_oss" />
              <el-option label="腾讯云COS" value="tencent_cos" />
              <el-option label="MinIO" value="minio" />
            </el-select>
          </div>
          <template v-if="config.storage.type !== 'local'">
            <div class="field-row">
              <div class="field"><label>Endpoint</label><el-input v-model="config.storage.endpoint" /></div>
              <div class="field"><label>Bucket</label><el-input v-model="config.storage.bucket" /></div>
            </div>
            <div class="field-row">
              <div class="field"><label>AccessKey</label><el-input v-model="config.storage.accessKey" type="password" show-password /></div>
              <div class="field"><label>SecretKey</label><el-input v-model="config.storage.secretKey" type="password" show-password /></div>
            </div>
          </template>
          <div class="field"><label>上传路径</label><el-input v-model="config.storage.uploadPath" placeholder="/var/www/uploads/" /></div>
          <div class="field"><label>最大文件大小（MB）</label><el-input-number v-model="config.storage.maxFileSize" :min="1" :max="100" /></div>
          <div class="field-actions">
            <el-button @click="testStorage" :loading="testingStorage">测试存储连接</el-button>
            <span v-if="storageTestResult" :class="storageTestOk ? 'test-ok' : 'test-err'">{{ storageTestResult }}</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div class="config-footer">
      <el-button type="danger" @click="resetConfig" plain>重置默认</el-button>
      <div class="footer-right">
        <el-button @click="exportConfig">导出配置</el-button>
        <el-button @click="importConfig">导入配置</el-button>
        <el-button type="primary" @click="saveAll" :loading="saving">保存全部</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/client'

const activeTab = ref('company')
const saving = ref(false)
const testingEmail = ref(false)
const testingSms = ref(false)
const testingAi = ref(false)
const testingStorage = ref(false)
const aiTestResult = ref('')
const aiTestOk = ref(false)
const storageTestResult = ref('')
const storageTestOk = ref(false)

const weekDays = [
  { label: '周一', value: 1 }, { label: '周二', value: 2 }, { label: '周三', value: 3 },
  { label: '周四', value: 4 }, { label: '周五', value: 5 }, { label: '周六', value: 6 }, { label: '周日', value: 0 }
]

const config = reactive({
  company: { name: '', phone: '', email: '', address: '', website: '', logo: '', icp: '' },
  ai: { provider: 'deepseek', model: 'deepseek-chat', apiKey: '', baseUrl: 'https://api.deepseek.com/v1', maxTokens: 2000, temperature: 0.7, systemPrompt: '', timeout: 30 },
  email: { host: '', port: 465, from: '', password: '', senderName: '' },
  sms: { provider: 'aliyun', accessKeyId: '', accessKeySecret: '', signName: '', templateCode: '' },
  security: { sessionTimeout: 30, maxLoginAttempts: 5, minPasswordLength: 8, ipWhitelist: '', captchaEnabled: true, ipLimitEnabled: false, jwtSecret: '' },
  cs: { welcomeMessage: '您好！我是AI客服助手，请问有什么可以帮您？', offlineMessage: '当前为非工作时间，请留言，我们会尽快回复。', workStart: '09:00', workEnd: '18:00', workDays: [1,2,3,4,5], maxQueue: 20, queueTimeout: 120, autoReplyEnabled: true, csatEnabled: true },
  storage: { type: 'local', endpoint: '', bucket: '', accessKey: '', secretKey: '', uploadPath: '/var/www/uploads/', maxFileSize: 10 }
})

async function loadConfig() {
  try {
    const res = await api.get('/admin/system-config/all')
    if (res) {
      for (const [group, values] of Object.entries(res)) {
        if (config[group] && typeof values === 'object') {
          Object.assign(config[group], values)
        }
      }
    }
  } catch (e) { console.error(e) }
}

async function saveAll() {
  saving.value = true
  try {
    await api.post('/admin/system-config/save', config)
    ElMessage.success('配置已保存')
  } catch (e) { ElMessage.error('保存失败') }
  saving.value = false
}

async function testEmail() {
  testingEmail.value = true
  try {
    await api.post('/admin/system-config/test-email', config.email)
    ElMessage.success('测试邮件已发送')
  } catch (e) { ElMessage.error('发送失败') }
  testingEmail.value = false
}

async function testSms() {
  testingSms.value = true
  try {
    await api.post('/admin/system-config/test-sms', config.sms)
    ElMessage.success('测试短信已发送')
  } catch (e) { ElMessage.error('发送失败') }
  testingSms.value = false
}

async function testAi() {
  testingAi.value = true
  aiTestResult.value = ''
  try {
    const res = await api.post('/admin/system-config/test-ai', config.ai)
    aiTestResult.value = res || '连接成功'
    aiTestOk.value = true
    ElMessage.success('AI连接测试成功')
  } catch (e) {
    aiTestResult.value = e.message || '连接失败'
    aiTestOk.value = false
  }
  testingAi.value = false
}

async function testStorage() {
  testingStorage.value = true
  storageTestResult.value = ''
  try {
    const res = await api.post('/admin/system-config/test-storage', config.storage)
    storageTestResult.value = res || '连接成功'
    storageTestOk.value = true
  } catch (e) {
    storageTestResult.value = e.message || '连接失败'
    storageTestOk.value = false
  }
  testingStorage.value = false
}

async function exportConfig() {
  try {
    const res = await api.get('/admin/system-config/export')
    const blob = new Blob([JSON.stringify(res, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'system-config-' + new Date().toISOString().slice(0,10) + '.json'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('配置已导出')
  } catch (e) { ElMessage.error('导出失败') }
}

async function importConfig() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    try {
      const text = await file.text()
      const data = JSON.parse(text)
      await api.post('/admin/system-config/import', data)
      ElMessage.success('配置已导入')
      loadConfig()
    } catch (e) { ElMessage.error('导入失败：文件格式错误') }
  }
  input.click()
}

async function resetConfig() {
  try {
    await ElMessageBox.confirm('确定要重置所有配置为默认值吗？此操作不可撤销。', '重置配置', { type: 'warning' })
    await api.post('/admin/system-config/reset', {})
    ElMessage.success('配置已重置')
    loadConfig()
  } catch (e) {}
}

onMounted(loadConfig)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.config-form { max-width: 700px; padding: 10px 0; }
.field { margin-bottom: 16px; }
.field label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.field-actions { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.test-ok { font-size: 13px; color: #059669; }
.test-err { font-size: 13px; color: #dc2626; }
.config-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; padding: 16px 0; border-top: 1px solid #e5e7eb; }
.footer-right { display: flex; gap: 10px; }
</style>
