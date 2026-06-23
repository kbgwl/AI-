<template>
  <div class="plan-page">
    <div class="page-header">
      <h2>套餐管理</h2>
      <el-button type="primary" @click="openForm()">新增套餐</el-button>
    </div>

    <div class="plan-grid">
      <div v-for="plan in plans" :key="plan.id" class="plan-card" :class="{ disabled: plan.status === 0 }">
        <div class="plan-card-header">
          <h3>{{ plan.planName }}</h3>
          <el-tag :type="plan.status === 1 ? 'success' : 'info'" size="small">{{ plan.status === 1 ? '启用' : '禁用' }}</el-tag>
        </div>
        <div class="plan-price">
          <span class="amount">¥{{ plan.price }}</span>
          <span class="unit">/{{ plan.priceUnit }}</span>
        </div>
        <p class="plan-desc">{{ plan.description }}</p>
        <div class="plan-limits">
          <div class="limit-item"><span class="label">坐席数</span><span class="value">{{ plan.maxAgents }}</span></div>
          <div class="limit-item"><span class="label">会话数</span><span class="value">{{ plan.maxSessions }}</span></div>
          <div class="limit-item"><span class="label">知识条目</span><span class="value">{{ plan.maxKnowledgeItems }}</span></div>
          <div class="limit-item"><span class="label">有效期</span><span class="value">{{ plan.durationDays }}天</span></div>
        </div>
        <div class="plan-features" v-if="plan.features">
          <div v-for="(enabled, key) in parseFeatures(plan.features)" :key="key" class="feature-tag" :class="{ active: enabled }">
            {{ featureLabels[key] || key }} {{ enabled ? '✓' : '✗' }}
          </div>
        </div>
        <div class="plan-actions">
          <el-button size="small" @click="openForm(plan)">编辑</el-button>
          <el-button size="small" :type="plan.status === 1 ? 'warning' : 'success'" @click="toggleStatus(plan)">
            {{ plan.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deletePlan(plan)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑套餐' : '新增套餐'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="套餐编码"><el-input v-model="editForm.planCode" placeholder="如 basic" /></el-form-item>
        <el-form-item label="套餐名称"><el-input v-model="editForm.planName" placeholder="如 基础版" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" /></el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="editForm.price" :min="0" :step="100" />
          <el-select v-model="editForm.priceUnit" style="margin-left:10px;width:80px">
            <el-option label="月" value="月" /><el-option label="年" value="年" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期(天)"><el-input-number v-model="editForm.durationDays" :min="1" /></el-form-item>
        <el-form-item label="最大坐席"><el-input-number v-model="editForm.maxAgents" :min="1" /></el-form-item>
        <el-form-item label="最大会话"><el-input-number v-model="editForm.maxSessions" :min="1" /></el-form-item>
        <el-form-item label="知识条目"><el-input-number v-model="editForm.maxKnowledgeItems" :min="1" /></el-form-item>
        <el-form-item label="功能配置">
          <div class="feature-checkboxes">
            <el-checkbox v-for="(label, key) in featureLabels" :key="key" v-model="featureChecks[key]">{{ label }}</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const plans = ref([])
const dialogVisible = ref(false)
const editForm = reactive({ id: null, planCode: '', planName: '', description: '', price: 0, priceUnit: '月', durationDays: 30, maxAgents: 5, maxSessions: 100, maxKnowledgeItems: 200, sort: 0 })
const featureChecks = reactive({})

const featureLabels = {
  dashboard: '数据概览', knowledge_base: '知识库', intent_manage: '意图管理', agent_manage: '客服管理',
  channel_manage: '渠道管理', dialog_flow: '对话流程', ticket_manage: '工单管理', operation_log: '操作日志',
  smart_routing: '智能路由', quality_check: '质量抽查', marketing_rules: '营销规则', advanced_reports: '高级报表',
  multi_tenant: '多租户', api_access: 'API接入', custom_deployment: '定制部署'
}

function parseFeatures(str) {
  try { return JSON.parse(str) } catch { return {} }
}

async function loadPlans() {
  const res = await fetch('/api/admin/plan/list')
  const data = await res.json()
  if (data.code === 200) plans.value = data.data || []
}

function openForm(plan) {
  if (plan) {
    Object.assign(editForm, plan)
    const features = parseFeatures(plan.features || '{}')
    Object.keys(featureLabels).forEach(k => { featureChecks[k] = !!features[k] })
  } else {
    Object.assign(editForm, { id: null, planCode: '', planName: '', description: '', price: 0, priceUnit: '月', durationDays: 30, maxAgents: 5, maxSessions: 100, maxKnowledgeItems: 200, sort: 0 })
    Object.keys(featureLabels).forEach(k => { featureChecks[k] = false })
  }
  dialogVisible.value = true
}

async function savePlan() {
  const features = {}
  Object.keys(featureLabels).forEach(k => { features[k] = !!featureChecks[k] })
  const body = { ...editForm, features: JSON.stringify(features) }
  const method = editForm.id ? 'PUT' : 'POST'
  const res = await fetch('/api/admin/plan', { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
  const data = await res.json()
  if (data.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadPlans() }
  else ElMessage.error(data.message || '保存失败')
}

async function toggleStatus(plan) {
  const newStatus = plan.status === 1 ? 0 : 1
  await fetch(`/api/admin/plan/${plan.id}/status?status=${newStatus}`, { method: 'PUT' })
  loadPlans()
}

async function deletePlan(plan) {
  await ElMessageBox.confirm('确定删除该套餐？', '提示')
  await fetch(`/api/admin/plan/${plan.id}`, { method: 'DELETE' })
  ElMessage.success('删除成功')
  loadPlans()
}

onMounted(loadPlans)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.plan-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.plan-card { background: #fff; border-radius: 12px; padding: 24px; border: 2px solid #f0f0f0; transition: all .2s; }
.plan-card:hover { border-color: #667eea; box-shadow: 0 4px 20px rgba(102,126,234,0.15); }
.plan-card.disabled { opacity: 0.6; }
.plan-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.plan-card-header h3 { font-size: 18px; margin: 0; }
.plan-price { margin-bottom: 8px; }
.plan-price .amount { font-size: 28px; font-weight: 700; color: #667eea; }
.plan-price .unit { font-size: 14px; color: #999; }
.plan-desc { font-size: 13px; color: #999; margin-bottom: 16px; }
.plan-limits { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 16px; }
.limit-item { display: flex; justify-content: space-between; font-size: 13px; padding: 4px 0; }
.limit-item .label { color: #999; }
.limit-item .value { font-weight: 600; }
.plan-features { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 16px; }
.feature-tag { font-size: 11px; padding: 2px 6px; border-radius: 4px; background: #f5f5f5; color: #999; }
.feature-tag.active { background: #f6ffed; color: #52c41a; }
.plan-actions { display: flex; gap: 8px; }
.feature-checkboxes { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
