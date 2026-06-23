<template>
  <div class="license-page">
    <div class="license-card">
      <!-- 顶部Logo -->
      <div class="card-header">
        <div class="logo-ring">
          <span class="logo-emoji">🤖</span>
        </div>
        <h1>AI智能客服系统</h1>
        <p class="subtitle">空白格·AI 授权管理</p>
      </div>

      <!-- 已激活 -->
      <div v-if="licenseStatus === 'active'" class="active-section">
        <div class="status-badge active">
          <span class="badge-dot"></span>
          系统已授权
        </div>

        <div class="info-card">
          <div class="info-row">
            <div class="info-col">
              <span class="info-label">客户名称</span>
              <span class="info-value">{{ licenseInfo.customerName || '-' }}</span>
            </div>
            <div class="info-col">
              <span class="info-label">当前套餐</span>
              <span class="plan-tag" :class="licenseInfo.plan">{{ planName }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-col">
              <span class="info-label">到期时间</span>
              <span class="info-value">{{ formatExpire(licenseInfo.expireTime) }}</span>
            </div>
            <div class="info-col">
              <span class="info-label">最大坐席</span>
              <span class="info-value highlight">{{ licenseInfo.maxAgents || '-' }} 人</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-col full">
              <span class="info-label">机器码</span>
              <span class="info-value mono">{{ machineCode }}</span>
            </div>
          </div>
        </div>

        <!-- 功能列表 -->
        <div class="section-block">
          <div class="section-title">
            <span class="title-icon">📦</span>
            当前套餐功能
          </div>
          <div class="feature-tags">
            <span v-for="item in featureList" :key="item.key" class="ftag" :class="item.enabled ? 'on' : 'off'">
              {{ item.label }}
            </span>
          </div>
        </div>

        <!-- 升级套餐 -->
        <div v-if="upgradePlans.length > 0" class="section-block">
          <div class="section-title">
            <span class="title-icon">🚀</span>
            升级套餐
          </div>
          <div class="upgrade-list">
            <div v-for="plan in upgradePlans" :key="plan.id" class="upgrade-item">
              <div class="upgrade-left">
                <div class="upgrade-name">{{ plan.planName }}</div>
                <div class="upgrade-meta">
                  坐席 {{ plan.maxAgents }} · 会话 {{ plan.maxSessions }} · 知识 {{ plan.maxKnowledgeItems }}
                </div>
              </div>
              <div class="upgrade-right">
                <div class="upgrade-price">¥{{ plan.price }}<small>/{{ plan.priceUnit }}</small></div>
                <button class="btn-upgrade" @click="requestUpgrade(plan)">升级</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 未激活 -->
      <div v-else class="inactive-section">
        <div class="status-badge inactive">
          <span class="badge-dot"></span>
          {{ statusText }}
        </div>
        <p class="inactive-hint">请联系供应商获取授权码，或在此输入激活</p>

        <div class="activate-card">
          <div class="field">
            <label>机器码（发给供应商）</label>
            <div class="field-row">
              <input type="text" :value="machineCode" readonly class="field-input mono" />
              <button class="btn-icon" @click="copyMachineCode" title="复制">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
              </button>
            </div>
          </div>
          <div class="field">
            <label>授权码</label>
            <input v-model="licenseKey" type="text" placeholder="AICS-XXXX-XXXX-XXXX" class="field-input" />
          </div>
          <button class="btn-primary" @click="activate" :disabled="activating">
            <span v-if="activating" class="spinner"></span>
            {{ activating ? '激活中...' : '激活授权' }}
          </button>
          <p v-if="activateMsg" class="form-msg" :class="activateSuccess ? 'ok' : 'err'">{{ activateMsg }}</p>
        </div>
      </div>

      <!-- 功能对比 -->
      <div class="section-block compare-section">
        <div class="section-title">
          <span class="title-icon">📊</span>
          套餐功能对比
        </div>
        <div class="compare-wrap">
          <table class="compare-table">
            <thead>
              <tr>
                <th>功能</th>
                <th>试用版</th>
                <th>基础版</th>
                <th>专业版</th>
                <th>企业版</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in compareItems" :key="item.key">
                <td class="feat-name">{{ item.label }}</td>
                <td :class="item.trial ? 'yes' : 'no'">{{ item.trial ? '✓' : '-' }}</td>
                <td :class="item.basic ? 'yes' : 'no'">{{ item.basic ? '✓' : '-' }}</td>
                <td :class="item.pro ? 'yes' : 'no'">{{ item.pro ? '✓' : '-' }}</td>
                <td :class="item.enterprise ? 'yes' : 'no'">{{ item.enterprise ? '✓' : '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer">济南空白格网络科技有限公司 · 空白格·AI</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const machineCode = ref('')
const licenseKey = ref('')
const licenseStatus = ref('loading')
const licenseInfo = ref({})
const activating = ref(false)
const activateMsg = ref('')
const activateSuccess = ref(false)
const allPlans = ref([])

const FEATURE_KEYS = [
  { key: 'login', label: '系统登录' },
  { key: 'license_manage', label: '授权管理' },
  { key: 'ai_chat', label: 'AI对话' },
  { key: 'chat_history', label: '对话历史' },
  { key: 'dashboard', label: '数据概览' },
  { key: 'knowledge_base', label: '知识库' },
  { key: 'intent_manage', label: '意图管理' },
  { key: 'agent_manage', label: '客服管理' },
  { key: 'channel_manage', label: '渠道管理' },
  { key: 'dialog_flow', label: '对话流程' },
  { key: 'ticket_manage', label: '工单管理' },
  { key: 'operation_log', label: '操作日志' },
  { key: 'smart_routing', label: '智能路由' },
  { key: 'quality_check', label: '质量抽查' },
  { key: 'marketing_rules', label: '营销规则' },
  { key: 'advanced_reports', label: '高级报表' },
  { key: 'multi_tenant', label: '多租户' },
  { key: 'api_access', label: 'API接入' },
  { key: 'custom_deployment', label: '定制部署' },
]

const PLAN_ORDER = ['trial', 'basic', 'pro', 'enterprise']
const PLAN_MAP = { trial: '试用版', basic: '基础版', pro: '专业版', enterprise: '企业版', lifetime: '永久版' }
const featuresRaw = ref({})

const featureList = computed(() => FEATURE_KEYS.map(i => ({ key: i.key, label: i.label, enabled: !!featuresRaw.value[i.key] })))

const upgradePlans = computed(() => {
  const idx = PLAN_ORDER.indexOf(licenseInfo.value.plan || '')
  return allPlans.value.filter(p => PLAN_ORDER.indexOf(p.planCode) > idx && p.status === 1)
})

const planName = computed(() => PLAN_MAP[licenseInfo.value.plan] || licenseInfo.value.plan || '-')

const statusText = computed(() => {
  const m = { unlicensed: '系统未授权', invalid: '授权无效', expired: '授权已过期', disabled: '授权已禁用', inactive: '授权未激活', mismatch: '服务器不匹配', loading: '检测中...' }
  return m[licenseStatus.value] || '系统未授权'
})

const compareItems = [
  { key: 'ai_chat', label: 'AI智能对话', trial: true, basic: true, pro: true, enterprise: true },
  { key: 'knowledge_base', label: '知识库管理', trial: false, basic: true, pro: true, enterprise: true },
  { key: 'intent_manage', label: '意图管理', trial: false, basic: true, pro: true, enterprise: true },
  { key: 'agent_manage', label: '客服管理', trial: false, basic: true, pro: true, enterprise: true },
  { key: 'ticket_manage', label: '工单管理', trial: false, basic: true, pro: true, enterprise: true },
  { key: 'dialog_flow', label: '对话流程', trial: false, basic: true, pro: true, enterprise: true },
  { key: 'smart_routing', label: '智能路由', trial: false, basic: false, pro: true, enterprise: true },
  { key: 'quality_check', label: '质量抽查', trial: false, basic: false, pro: true, enterprise: true },
  { key: 'marketing_rules', label: '营销规则', trial: false, basic: false, pro: true, enterprise: true },
  { key: 'api_access', label: 'API接入', trial: false, basic: false, pro: false, enterprise: true },
  { key: 'custom_deployment', label: '定制部署', trial: false, basic: false, pro: false, enterprise: true },
]

function formatExpire(t) {
  if (!t) return '永久'
  return t.replace('T', ' ').substring(0, 16)
}

function copyMachineCode() {
  navigator.clipboard.writeText(machineCode.value)
  ElMessage.success('已复制')
}

function requestUpgrade(plan) {
  ElMessageBox.confirm(
    `升级到「${plan.planName}」（¥${plan.price}/${plan.priceUnit}），坐席提升至 ${plan.maxAgents}，会话提升至 ${plan.maxSessions}`,
    '升级套餐',
    { confirmButtonText: '联系客服升级', cancelButtonText: '取消', type: 'info' }
  ).then(() => ElMessage.info('请联系供应商客服：400-xxx-xxxx')).catch(() => {})
}

async function activate() {
  if (!licenseKey.value.trim()) { ElMessage.warning('请输入授权码'); return }
  activating.value = true; activateMsg.value = ''
  try {
    const res = await fetch('/api/license/activate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ licenseKey: licenseKey.value.trim(), machineCode: machineCode.value }) })
    const data = await res.json()
    activateSuccess.value = data.code === 200
    activateMsg.value = data.message || '操作完成'
    if (data.code === 200) {
      licenseStatus.value = 'active'
      const vd = await (await fetch('/api/license/validate')).json()
      licenseInfo.value = vd.data || {}
      const fd = await (await fetch('/api/license/features')).json()
      if (fd.data && fd.data.features) featuresRaw.value = fd.data.features
    }
  } catch (e) { activateSuccess.value = false; activateMsg.value = '网络错误' }
  finally { activating.value = false }
}

onMounted(async () => {
  try { machineCode.value = (await (await fetch('/api/license/machine-code')).json()).data.machineCode || '' } catch (e) {}
  try {
    const v = await (await fetch('/api/license/validate')).json()
    if (v.data && v.data.valid) { licenseStatus.value = 'active'; licenseInfo.value = v.data }
    else licenseStatus.value = v.data ? v.data.status : 'unlicensed'
  } catch (e) { licenseStatus.value = 'unlicensed' }
  try { const f = await (await fetch('/api/license/features')).json(); if (f.data && f.data.features) featuresRaw.value = f.data.features } catch (e) {}
  try { const p = await (await fetch('/api/license/plans')).json(); if (p.code === 200 && p.data) allPlans.value = p.data } catch (e) {}
})
</script>

<style scoped>
.license-page {
  min-height: 100%;
  display: flex; align-items: flex-start; justify-content: center;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
.license-card {
  background: #fff; border-radius: 16px; padding: 0; max-width: 100%; width: 100%;
  box-shadow: 0 25px 60px rgba(0,0,0,0.15); overflow: hidden;
}
.card-header {
  text-align: center; padding: 36px 32px 28px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff;
}
.logo-ring {
  width: 64px; height: 64px; border-radius: 50%; background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center; margin: 0 auto 14px;
  backdrop-filter: blur(10px); border: 2px solid rgba(255,255,255,0.3);
}
.logo-emoji { font-size: 32px; }
.card-header h1 { font-size: 22px; font-weight: 700; margin: 0 0 4px; }
.card-header .subtitle { font-size: 13px; opacity: 0.85; margin: 0; }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px; padding: 6px 16px;
  border-radius: 20px; font-size: 13px; font-weight: 600;
}
.status-badge.active { background: #ecfdf5; color: #059669; }
.status-badge.inactive { background: #fef2f2; color: #dc2626; }
.badge-dot { width: 7px; height: 7px; border-radius: 50%; }
.status-badge.active .badge-dot { background: #10b981; }
.status-badge.inactive .badge-dot { background: #ef4444; }

.active-section, .inactive-section { padding: 24px 32px; text-align: center; }

.info-card {
  margin-top: 20px; background: #f9fafb; border-radius: 12px; padding: 16px 20px;
  text-align: left;
}
.info-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 12px; }
.info-row:last-child { margin-bottom: 0; }
.info-col.full { grid-column: span 2; }
.info-label { display: block; font-size: 11px; color: #9ca3af; margin-bottom: 3px; text-transform: uppercase; letter-spacing: 0.5px; }
.info-value { font-size: 14px; color: #1f2937; font-weight: 500; }
.info-value.highlight { color: #6366f1; }
.info-value.mono { font-family: 'SF Mono', monospace; font-size: 12px; word-break: break-all; }
.plan-tag {
  display: inline-block; padding: 2px 10px; border-radius: 6px; font-size: 12px; font-weight: 600;
}
.plan-tag.trial { background: #fff7ed; color: #ea580c; }
.plan-tag.basic { background: #eff6ff; color: #2563eb; }
.plan-tag.pro { background: #f0fdf4; color: #16a34a; }
.plan-tag.enterprise { background: #faf5ff; color: #9333ea; }

.section-block { padding: 0 32px; margin-top: 24px; }
.section-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; }
.title-icon { font-size: 15px; }

.feature-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.ftag {
  display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 6px;
  font-size: 12px; font-weight: 500;
}
.ftag.on { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }
.ftag.off { background: #f3f4f6; color: #9ca3af; border: 1px solid #e5e7eb; }

.upgrade-list { display: flex; flex-direction: column; gap: 10px; }
.upgrade-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px; border: 1px solid #e5e7eb; border-radius: 10px;
  transition: all .2s; cursor: default;
}
.upgrade-item:hover { border-color: #6366f1; box-shadow: 0 2px 12px rgba(99,102,241,0.1); }
.upgrade-name { font-size: 14px; font-weight: 600; color: #1f2937; }
.upgrade-meta { font-size: 11px; color: #9ca3af; margin-top: 2px; }
.upgrade-right { display: flex; align-items: center; gap: 14px; }
.upgrade-price { font-size: 18px; font-weight: 700; color: #6366f1; white-space: nowrap; }
.upgrade-price small { font-size: 12px; font-weight: 400; color: #9ca3af; }
.btn-upgrade {
  padding: 6px 16px; border-radius: 6px; border: none; font-size: 12px; font-weight: 600;
  background: #6366f1; color: #fff; cursor: pointer; transition: all .2s; white-space: nowrap;
}
.btn-upgrade:hover { background: #4f46e5; }

.inactive-hint { font-size: 13px; color: #9ca3af; margin: 12px 0 20px; }

.activate-card { text-align: left; }
.field { margin-bottom: 14px; }
.field label { display: block; font-size: 12px; font-weight: 500; color: #6b7280; margin-bottom: 5px; }
.field-row { display: flex; gap: 8px; }
.field-input {
  width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px;
  font-size: 13px; background: #f9fafb; transition: border .2s; box-sizing: border-box;
}
.field-input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }
.field-input.mono { font-family: 'SF Mono', monospace; letter-spacing: 0.3px; }
.btn-icon {
  width: 38px; height: 38px; border: 1px solid #d1d5db; border-radius: 8px;
  background: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: #6b7280; transition: all .2s; flex-shrink: 0;
}
.btn-icon:hover { background: #f3f4f6; color: #374151; }

.btn-primary {
  width: 100%; padding: 11px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px; transition: all .2s;
}
.btn-primary:hover { box-shadow: 0 4px 16px rgba(99,102,241,0.35); transform: translateY(-1px); }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; transform: none; }
.spinner { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,.3); border-top-color: #fff; border-radius: 50%; animation: spin .6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.form-msg { text-align: center; font-size: 13px; margin-top: 10px; padding: 8px; border-radius: 6px; }
.form-msg.ok { color: #059669; background: #ecfdf5; }
.form-msg.err { color: #dc2626; background: #fef2f2; }

.compare-section { margin-top: 28px; border-top: 1px solid #f3f4f6; padding-top: 24px; }
.compare-wrap { overflow-x: auto; }
.compare-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.compare-table th {
  text-align: center; padding: 8px 6px; background: #f9fafb; font-weight: 600; color: #6b7280;
  border-bottom: 1px solid #e5e7eb;
}
.compare-table th:first-child { text-align: left; padding-left: 10px; }
.compare-table td { text-align: center; padding: 7px 6px; border-bottom: 1px solid #f3f4f6; }
.compare-table td.yes { color: #059669; font-weight: 600; }
.compare-table td.no { color: #d1d5db; }
.compare-table td.feat-name { text-align: left; padding-left: 10px; color: #374151; font-weight: 500; }

.card-footer { text-align: center; padding: 16px 32px 20px; font-size: 11px; color: #d1d5db; }

@media (max-width: 640px) {
  .license-card { border-radius: 12px; }
  .card-header { padding: 28px 20px 22px; }
  .active-section, .inactive-section { padding: 20px; }
  .section-block { padding: 0 20px; }
  .info-row { grid-template-columns: 1fr; }
  .info-col.full { grid-column: span 1; }
  .upgrade-item { flex-direction: column; gap: 10px; align-items: flex-start; }
  .upgrade-right { width: 100%; justify-content: space-between; }
  .compare-section { padding: 20px; }
}
</style>
