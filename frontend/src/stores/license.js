import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const DEFAULT_FEATURES = {
  login: true,
  license_manage: true,
  ai_chat: true,
  chat_history: false,
  dashboard: false,
  knowledge_base: false,
  intent_manage: false,
  agent_manage: false,
  channel_manage: false,
  dialog_flow: false,
  ticket_manage: false,
  operation_log: false,
  smart_routing: false,
  quality_check: false,
  marketing_rules: false,
  advanced_reports: false,
  multi_tenant: false,
  api_access: false,
  custom_deployment: false,
}

export const useLicenseStore = defineStore('license', () => {
  const valid = ref(false)
  const plan = ref('none')
  const features = ref({ ...DEFAULT_FEATURES })
  const maxAgents = ref(0)
  const maxSessions = ref(0)
  const expireTime = ref(null)
  const machineCode = ref('')

  const planName = computed(() => {
    const map = { trial: '试用版', basic: '基础版', pro: '专业版', enterprise: '企业版', none: '未授权' }
    return map[plan.value] || plan.value
  })

  function hasFeature(feature) {
    return features.value[feature] === true
  }

  async function fetchFeatures() {
    try {
      const res = await fetch('/api/license/features')
      const json = await res.json()
      if (json.code === 200 && json.data) {
        valid.value = json.data.valid === true
        plan.value = json.data.plan || 'none'
        features.value = { ...DEFAULT_FEATURES, ...(json.data.features || {}) }
        maxAgents.value = json.data.maxAgents || 0
        maxSessions.value = json.data.maxSessions || 0
        expireTime.value = json.data.expireTime || null
      }
    } catch (e) {
      valid.value = false
      plan.value = 'none'
      features.value = { ...DEFAULT_FEATURES }
    }
  }

  async function fetchMachineCode() {
    try {
      const res = await fetch('/api/license/machine-code')
      const json = await res.json()
      if (json.code === 200 && json.data) {
        machineCode.value = json.data.machineCode || ''
      }
    } catch (e) {
      console.error('Failed to fetch machine code:', e)
    }
  }

  function $reset() {
    valid.value = false
    plan.value = 'none'
    features.value = { ...DEFAULT_FEATURES }
    maxAgents.value = 0
    maxSessions.value = 0
    expireTime.value = null
    machineCode.value = ''
  }

  return {
    valid, plan, features, maxAgents, maxSessions, expireTime, machineCode,
    planName, hasFeature, fetchFeatures, fetchMachineCode, $reset
  }
})
