<template>
  <div class="pay-config-page">
    <div class="page-header">
      <h2>支付方式配置</h2>
    </div>

    <div class="config-grid">
      <!-- 支付宝 -->
      <div class="config-card" :class="{ active: config.alipay?.enabled }">
        <div class="card-header">
          <div class="pay-icon alipay">💳</div>
          <div class="card-title">
            <h3>支付宝</h3>
            <el-switch v-model="config.alipay.enabled" @change="saveConfig" />
          </div>
        </div>
        <div class="card-body" v-if="config.alipay.enabled">
          <div class="field">
            <label>App ID</label>
            <el-input v-model="config.alipay.appId" placeholder="支付宝应用APPID" />
          </div>
          <div class="field">
            <label>应用私钥</label>
            <el-input v-model="config.alipay.privateKey" type="textarea" :rows="3" placeholder="RSA2私钥" show-password />
          </div>
          <div class="field">
            <label>支付宝公钥</label>
            <el-input v-model="config.alipay.publicKey" type="textarea" :rows="3" placeholder="支付宝公钥" show-password />
          </div>
          <div class="field">
            <label>回调地址</label>
            <el-input v-model="config.alipay.notifyUrl" placeholder="https://your-domain.com/api/payment/alipay/notify" />
          </div>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
          <el-button @click="testConnection('alipay')" :loading="testing">测试连接</el-button>
        </div>
      </div>

      <!-- 微信支付 -->
      <div class="config-card" :class="{ active: config.wechat?.enabled }">
        <div class="card-header">
          <div class="pay-icon wechat">💚</div>
          <div class="card-title">
            <h3>微信支付</h3>
            <el-switch v-model="config.wechat.enabled" @change="saveConfig" />
          </div>
        </div>
        <div class="card-body" v-if="config.wechat.enabled">
          <div class="field">
            <label>商户号 (mch_id)</label>
            <el-input v-model="config.wechat.mchId" placeholder="微信支付商户号" />
          </div>
          <div class="field">
            <label>API密钥 (API Key)</label>
            <el-input v-model="config.wechat.apiKey" type="password" placeholder="微信支付API密钥" show-password />
          </div>
          <div class="field">
            <label>AppID</label>
            <el-input v-model="config.wechat.appId" placeholder="微信公众号/小程序AppID" />
          </div>
          <div class="field">
            <label>证书序列号</label>
            <el-input v-model="config.wechat.serialNo" placeholder="apiclient_cert.pem 序列号" />
          </div>
          <div class="field">
            <label>回调地址</label>
            <el-input v-model="config.wechat.notifyUrl" placeholder="https://your-domain.com/api/payment/wechat/notify" />
          </div>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
          <el-button @click="testConnection('wechat')" :loading="testing">测试连接</el-button>
        </div>
      </div>
    </div>

    <!-- 支付测试 -->
    <div class="test-section">
      <h3>支付测试</h3>
      <el-form :inline="true">
        <el-form-item label="支付渠道">
          <el-select v-model="testForm.channel" style="width:140px">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试金额">
          <el-input-number v-model="testForm.amount" :min="0.01" :step="0.01" :precision="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="testPay" :loading="testLoading">发起测试支付</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/client'

const saving = ref(false)
const testing = ref(false)
const testLoading = ref(false)

const config = reactive({
  alipay: { enabled: false, appId: '', privateKey: '', publicKey: '', notifyUrl: '' },
  wechat: { enabled: false, mchId: '', apiKey: '', appId: '', serialNo: '', notifyUrl: '' }
})

const testForm = reactive({ channel: 'alipay', amount: 0.01 })

async function loadConfig() {
  try {
    const res = await api.get('/admin/payment/config')
    if (res) {
      if (res.alipay) config.alipay = { ...config.alipay, ...res.alipay }
      if (res.wechat) config.wechat = { ...config.wechat, ...res.wechat }
    }
  } catch (e) { console.error(e) }
}

async function saveConfig() {
  saving.value = true
  try {
    await api.post('/admin/payment/config', config)
    ElMessage.success('配置已保存')
  } catch (e) { ElMessage.error('保存失败') }
  saving.value = false
}

async function testConnection(channel) {
  testing.value = true
  try {
    const res = await api.post('/admin/payment/test', { channel })
    ElMessage.success(res || '连接正常')
  } catch (e) { ElMessage.error('连接失败：' + (e.message || '请检查配置')) }
  testing.value = false
}

async function testPay() {
  testLoading.value = true
  try {
    const res = await api.post('/admin/payment/test-pay', testForm)
    if (res && res.qrCode) {
      ElMessage.success('测试支付二维码已生成')
    } else {
      ElMessage.success('测试支付请求已发送')
    }
  } catch (e) { ElMessage.error('测试失败：' + (e.message || '')) }
  testLoading.value = false
}

onMounted(loadConfig)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.config-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.config-card { background: #fff; border-radius: 12px; border: 2px solid #e5e7eb; overflow: hidden; transition: all .2s; }
.config-card.active { border-color: #6366f1; box-shadow: 0 4px 20px rgba(99,102,241,0.1); }
.card-header { display: flex; align-items: center; gap: 14px; padding: 20px 24px; border-bottom: 1px solid #f3f4f6; }
.pay-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.pay-icon.alipay { background: linear-gradient(135deg, #1677ff, #4096ff); }
.pay-icon.wechat { background: linear-gradient(135deg, #07c160, #2aae67); }
.card-title { flex: 1; display: flex; align-items: center; justify-content: space-between; }
.card-title h3 { font-size: 16px; margin: 0; }
.card-body { padding: 20px 24px; }
.field { margin-bottom: 14px; }
.field label { display: block; font-size: 12px; font-weight: 500; color: #6b7280; margin-bottom: 5px; }
.field-row { margin-bottom: 14px; }
.test-section { background: #fff; border-radius: 12px; padding: 20px 24px; }
.test-section h3 { font-size: 15px; margin: 0 0 16px; }
@media (max-width: 768px) { .config-grid { grid-template-columns: 1fr; } }
</style>
