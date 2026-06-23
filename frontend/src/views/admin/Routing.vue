<template>
 <div class="routing-page">
  <!-- 排队监控看板 -->
  <div class="queue-monitor">
    <div class="queue-card">
      <div class="queue-label">🚶 等待中</div>
      <div class="queue-value">{{ queueStats.waiting }}</div>
      <div class="queue-trend up">↑ {{ queueStats.waitingTrend }}%</div>
    </div>
    <div class="queue-card">
      <div class="queue-label">⏱ 平均等待</div>
      <div class="queue-value">{{ queueStats.avgWaitTime }}s</div>
      <div class="queue-trend down">↓ {{ queueStats.waitTrend }}%</div>
    </div>
    <div class="queue-card">
      <div class="queue-label">👥 空闲坐席</div>
      <div class="queue-value">{{ queueStats.availableAgents }}</div>
      <div class="queue-trend">{{ queueStats.availableTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(queueStats.availableTrend) }}%</div>
    </div>
    <div class="queue-card">
      <div class="queue-label">⚡ 最大排队</div>
      <div class="queue-value">{{ queueStats.maxQueue }}</div>
      <div class="queue-trend">今日峰值 {{ queueStats.peakTime }}</div>
    </div>
  </div>

  <!-- 顶部操作栏 -->
  <div class="page-header">
   <div class="header-left">
    <h2>智能路由</h2>
    <span class="header-desc">配置会话路由规则，实现技能匹配、VIP优先、负载均衡和关键词分流</span>
   </div>
   <div class="header-right">
    <el-select v-model="conditionFilter" placeholder="条件类型" clearable size="default" style="width:130px;margin-right:10px">
     <el-option v-for="(label,key) in conditionTypeMap" :key="key" :label="label" :value="key" />
    </el-select>
    <el-input v-model="searchKw" placeholder="搜索规则名称" clearable size="default" style="width:180px;margin-right:10px" :prefix-icon="Search" />
    <el-button type="success" plain :loading="exporting" @click="exportExcel">
     <el-icon><Download /></el-icon> 导出
    </el-button>
    <el-button type="primary" @click="openDialog(null)">
     <el-icon><Plus /></el-icon> 新增规则
    </el-button>
   </div>
  </div>

  <!-- 规则表格 -->
  <el-table :data="pagedRules" stripe border style="width:100%" :default-sort="{ prop: 'priority', order: 'descending' }" @sort-change="onSortChange">
   <el-table-column prop="ruleName" label="规则名称" min-width="180" sortable="custom" show-overflow-tooltip />
   <el-table-column prop="conditionType" label="条件类型" width="120" sortable="custom">
    <template #default="{ row }">
     <el-tag :type="conditionTagType(row.conditionType)" size="small">{{ conditionTypeMap[row.conditionType] || row.conditionType }}</el-tag>
    </template>
   </el-table-column>
   <el-table-column prop="conditionValue" label="条件值" min-width="200" show-overflow-tooltip>
    <template #default="{ row }">
     <code class="cond-value">{{ formatConditionValue(row.conditionType, row.conditionValue) }}</code>
    </template>
   </el-table-column>
   <el-table-column prop="targetGroup" label="目标技能组" width="120">
    <template #default="{ row }">
     <el-tag type="info" size="small">{{ targetGroupMap[row.targetGroup] || row.targetGroup }}</el-tag>
    </template>
   </el-table-column>
   <el-table-column prop="priority" label="优先级" width="90" sortable="custom" align="center">
    <template #default="{ row }">
     <span :class="['priority-badge', row.priority >= 80 ? 'high' : row.priority >= 50 ? 'mid' : 'low']">{{ row.priority }}</span>
    </template>
   </el-table-column>
   <el-table-column prop="enabled" label="状态" width="80" align="center">
    <template #default="{ row }">
     <el-switch v-model="row.enabled" size="small" @change="onToggleRule(row)" />
    </template>
   </el-table-column>
   <el-table-column prop="createTime" label="创建时间" width="170" sortable="custom" />
   <el-table-column label="操作" width="140" fixed="right" align="center">
    <template #default="{ row }">
     <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
     <el-button type="danger" link size="small" @click="deleteRule(row)">删除</el-button>
    </template>
   </el-table-column>
  </el-table>

  <!-- 分页 -->
  <div class="pagination-wrap">
   <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredRules.length" layout="total, sizes, prev, pager, next, jumper" background />
  </div>

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="editingRule ? '编辑路由规则' : '新增路由规则'" width="560px" destroy-on-close>
   <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px" label-position="right">
    <el-form-item label="规则名称" prop="ruleName">
     <el-input v-model="formData.ruleName" placeholder="如：订单问题路由售后" maxlength="50" show-word-limit />
    </el-form-item>
    <el-form-item label="条件类型" prop="conditionType">
     <el-select v-model="formData.conditionType" placeholder="选择条件类型" style="width:100%" @change="onConditionTypeChange">
      <el-option v-for="(label,key) in conditionTypeMap" :key="key" :label="label" :value="key" />
     </el-select>
    </el-form-item>
    <el-form-item label="条件值" prop="conditionValue">
     <div v-if="formData.conditionType === 'skill'" style="width:100%">
      <el-select v-model="formData.conditionValue.skillGroup" placeholder="选择技能组" style="width:100%">
       <el-option v-for="(label,key) in targetGroupMap" :key="key" :label="label" :value="key" />
      </el-select>
     </div>
     <div v-else-if="formData.conditionType === 'vip'" style="width:100%">
      <el-checkbox-group v-model="formData.conditionValue.vipLevel">
       <el-checkbox label="gold">金牌会员</el-checkbox>
       <el-checkbox label="platinum">铂金会员</el-checkbox>
       <el-checkbox label="diamond">钻石会员</el-checkbox>
      </el-checkbox-group>
     </div>
     <div v-else-if="formData.conditionType === 'load'" style="width:100%">
      坐席最大负载：<el-input-number v-model="formData.conditionValue.maxLoad" :min="1" :max="50" :step="1" /> 个会话
     </div>
     <div v-else-if="formData.conditionType === 'keyword'" style="width:100%">
      <el-select v-model="formData.conditionValue.keywords" multiple filterable allow-create default-first-option placeholder="输入关键词后回车" style="width:100%">
      </el-select>
     </div>
     <div v-else style="width:100%">
      <el-input type="textarea" v-model="conditionValueText" :rows="3" placeholder='JSON格式，如 {"skillGroup":"after_sale"}' />
     </div>
    </el-form-item>
    <el-form-item label="目标技能组" prop="targetGroup">
     <el-select v-model="formData.targetGroup" placeholder="选择目标技能组" style="width:100%">
      <el-option v-for="(label,key) in targetGroupMap" :key="key" :label="label" :value="key" />
     </el-select>
    </el-form-item>
    <el-form-item label="优先级" prop="priority">
     <el-input-number v-model="formData.priority" :min="0" :max="100" :step="5" />
     <span class="form-hint">数字越大优先级越高，同条件下高优先级规则先匹配</span>
    </el-form-item>
    <el-form-item label="启用" prop="enabled">
     <el-switch v-model="formData.enabled" />
    </el-form-item>
   </el-form>
   <template #footer>
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button type="primary" @click="submitForm">确认</el-button>
   </template>
  </el-dialog>
 </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Plus } from '@element-plus/icons-vue'

// ==================== 排队监控数据 ====================
const queueStats = ref({
  waiting: 12,
  waitingTrend: 8,
  avgWaitTime: 45,
  waitTrend: 15,
  availableAgents: 5,
  availableTrend: -10,
  maxQueue: 28,
  peakTime: '14:30'
})

// ==================== 常量映射 ====================
const conditionTypeMap = { skill: '技能匹配', vip: 'VIP优先', load: '负载均衡', keyword: '关键词匹配' }
const targetGroupMap = { after_sale: '售后组', sales: '售前组', vip: 'VIP组', default: '默认组' }

function conditionTagType(type) {
 const map = { skill: '', vip: 'warning', load: 'info', keyword: 'success' }
 return map[type] || ''
}

function formatConditionValue(type, val) {
 if (!val || typeof val === 'string') return val || '-'
 if (type === 'skill') return `技能组: ${targetGroupMap[val.skillGroup] || val.skillGroup}`
 if (type === 'vip') return `VIP等级: ${(val.vipLevel || []).map(l => ({gold:'金牌',platinum:'铂金',diamond:'钻石'}[l] || l)).join('/')}`
 if (type === 'load') return `最大负载: ${val.maxLoad || 10}`
 if (type === 'keyword') return `关键词: ${(val.keywords || []).join('、')}`
 return JSON.stringify(val)
}

// ==================== Mock数据 ====================
const allRules = ref([
 { id: 1, ruleName: '订单问题路由售后', conditionType: 'skill', conditionValue: { skillGroup: 'after_sale' }, targetGroup: 'after_sale', priority: 90, enabled: true, createTime: '2026-05-22 10:00:00' },
 { id: 2, ruleName: 'VIP客户优先服务', conditionType: 'vip', conditionValue: { vipLevel: ['gold', 'platinum'] }, targetGroup: 'vip', priority: 95, enabled: true, createTime: '2026-05-22 10:05:00' },
 { id: 3, ruleName: '负载均衡分配', conditionType: 'load', conditionValue: { maxLoad: 10 }, targetGroup: 'default', priority: 50, enabled: true, createTime: '2026-05-22 10:10:00' },
 { id: 4, ruleName: '退款关键词路由', conditionType: 'keyword', conditionValue: { keywords: ['退款', '退钱', '不想要'] }, targetGroup: 'after_sale', priority: 80, enabled: true, createTime: '2026-05-22 10:15:00' },
 { id: 5, ruleName: '价格咨询路由售前', conditionType: 'skill', conditionValue: { skillGroup: 'sales' }, targetGroup: 'sales', priority: 70, enabled: false, createTime: '2026-05-22 10:20:00' },
 { id: 6, ruleName: '投诉关键词升级', conditionType: 'keyword', conditionValue: { keywords: ['投诉', '不满意', '差评'] }, targetGroup: 'vip', priority: 85, enabled: true, createTime: '2026-05-22 10:25:00' }
])

// ==================== 筛选/搜索/排序 ====================
const conditionFilter = ref('')
const searchKw = ref('')
const sortProp = ref('priority')
const sortOrder = ref('descending')

const filteredRules = computed(() => {
 let list = [...allRules.value]
 if (conditionFilter.value) list = list.filter(r => r.conditionType === conditionFilter.value)
 if (searchKw.value) {
  const kw = searchKw.value.toLowerCase()
  list = list.filter(r => r.ruleName.toLowerCase().includes(kw))
 }
 // 排序
 if (sortProp.value) {
  list.sort((a, b) => {
   const va = a[sortProp.value]
   const vb = b[sortProp.value]
   let cmp = 0
   if (typeof va === 'number' && typeof vb === 'number') cmp = va - vb
   else cmp = String(va || '').localeCompare(String(vb || ''))
   return sortOrder.value === 'ascending' ? cmp : -cmp
  })
 }
 return list
})

function onSortChange({ prop, order }) {
 sortProp.value = prop || 'priority'
 sortOrder.value = order || 'descending'
}

// ==================== 分页 ====================
const currentPage = ref(1)
const pageSize = ref(10)
const pagedRules = computed(() => {
 const start = (currentPage.value - 1) * pageSize.value
 return filteredRules.value.slice(start, start + pageSize.value)
})

// ==================== 弹窗/表单 ====================
const dialogVisible = ref(false)
const editingRule = ref(null)
const formRef = ref(null)
const conditionValueText = ref('')

const formData = ref({
 ruleName: '',
 conditionType: 'skill',
 conditionValue: { skillGroup: 'after_sale' },
 targetGroup: 'after_sale',
 priority: 50,
 enabled: true
})

const formRules = {
 ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
 conditionType: [{ required: true, message: '请选择条件类型', trigger: 'change' }],
 targetGroup: [{ required: true, message: '请选择目标技能组', trigger: 'change' }]
}

function getDefaultConditionValue(type) {
 if (type === 'skill') return { skillGroup: 'after_sale' }
 if (type === 'vip') return { vipLevel: [] }
 if (type === 'load') return { maxLoad: 10 }
 if (type === 'keyword') return { keywords: [] }
 return {}
}

function onConditionTypeChange(type) {
 formData.value.conditionValue = getDefaultConditionValue(type)
 conditionValueText.value = ''
}

function openDialog(rule) {
 editingRule.value = rule
 if (rule) {
  formData.value = {
   ruleName: rule.ruleName,
   conditionType: rule.conditionType,
   conditionValue: JSON.parse(JSON.stringify(rule.conditionValue)),
   targetGroup: rule.targetGroup,
   priority: rule.priority,
   enabled: rule.enabled
  }
  conditionValueText.value = JSON.stringify(rule.conditionValue, null, 2)
 } else {
  formData.value = {
   ruleName: '',
   conditionType: 'skill',
   conditionValue: { skillGroup: 'after_sale' },
   targetGroup: 'after_sale',
   priority: 50,
   enabled: true
  }
  conditionValueText.value = ''
 }
 dialogVisible.value = true
}

function submitForm() {
 if (!formRef.value) return
 formRef.value.validate((valid) => {
  if (!valid) return
  // 如果是其他类型（无专用编辑器），尝试从textarea解析JSON
  if (!['skill','vip','load','keyword'].includes(formData.value.conditionType) && conditionValueText.value) {
   try { formData.value.conditionValue = JSON.parse(conditionValueText.value) }
   catch { ElMessage.warning('条件值JSON格式不正确'); return }
  }
  if (editingRule.value) {
   // 编辑
   const idx = allRules.value.findIndex(r => r.id === editingRule.value.id)
   if (idx !== -1) {
    allRules.value[idx] = { ...allRules.value[idx], ...formData.value }
   }
   ElMessage.success({ message: '规则已更新', duration: 2000, offset: 80 })
  } else {
   // 新增
   const newRule = {
    id: Date.now(),
    ...formData.value,
    createTime: new Date().toLocaleString('zh-CN', { hour12: false })
   }
   allRules.value.unshift(newRule)
   ElMessage.success({ message: '规则已添加', duration: 2000, offset: 80 })
  }
  dialogVisible.value = false
 })
}

function deleteRule(rule) {
 ElMessageBox.confirm(`确定删除规则「${rule.ruleName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }).then(() => {
  allRules.value = allRules.value.filter(r => r.id !== rule.id)
  ElMessage.success({ message: '已删除', duration: 2000, offset: 80 })
 }).catch(() => {})
}

function onToggleRule(rule) {
 ElMessage.success({ message: rule.enabled ? '规则已启用' : '规则已禁用', duration: 2000, offset: 80 })
}

// 自动刷新排队数据（每 30 秒）
let queueTimer = null
onMounted(() => {
  queueTimer = setInterval(() => {
    // Mock 刷新排队数据
    queueStats.value.waiting = Math.floor(Math.random() * 20 + 5)
    queueStats.value.avgWaitTime = Math.floor(Math.random() * 60 + 30)
    queueStats.value.availableAgents = Math.floor(Math.random() * 8 + 3)
  }, 30000)
})
onUnmounted(() => {
  if (queueTimer) clearInterval(queueTimer)
})

// ==================== 导出CSV ====================
const exporting = ref(false)

function exportExcel() {
 if (filteredRules.value.length === 0) { ElMessage.warning('没有可导出的数据'); return }
 if (exporting.value) return
 exporting.value = true
 // 同步执行 a.click()，必须在用户手势同步调用栈内
 try {
  const headers = ['规则名称','条件类型','条件值','目标技能组','优先级','状态','创建时间']
  const rows = filteredRules.value.map(r => [
   r.ruleName,
   conditionTypeMap[r.conditionType] || r.conditionType,
   formatConditionValue(r.conditionType, r.conditionValue),
   targetGroupMap[r.targetGroup] || r.targetGroup,
   r.priority,
   r.enabled ? '启用' : '禁用',
   r.createTime
  ])
  const csv = [headers, ...rows].map(r => r.map(c => `"${(c||'').toString().replace(/"/g,'""')}"`).join(',')).join('\n')
  const filename = `智能路由规则_${new Date().toISOString().slice(0,10)}.csv`
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.style.display = 'none'
  a.setAttribute('href', url)
  a.setAttribute('download', filename)
  document.body.appendChild(a)
  a.click()
  setTimeout(() => {
   document.body.removeChild(a)
   URL.revokeObjectURL(url)
   exporting.value = false
   ElMessage.success({ message: `导出成功！已下载 ${filteredRules.value.length} 条规则`, duration: 4000, offset: 80 })
  }, 1000)
 } catch (e) {
  exporting.value = false
  console.error('导出失败:', e)
  ElMessage.error({ message: '导出失败，请重试', duration: 3000, offset: 80 })
 }
}
</script>

<style scoped>
.routing-page { padding: 20px; }

/* 排队监控看板 */
.queue-monitor { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; animation: fadeInUp 0.4s ease; }
.queue-card { 
  background: #fff; border-radius: var(--radius-lg); padding: 20px; 
  box-shadow: var(--shadow-card); position: relative; overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(79, 110, 247, 0.08);
}
.queue-card::after { 
  content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 4px; 
  background: linear-gradient(90deg, #4F6EF7 0%, #7B5EF7 50%, #0FC6C2 100%);
  transition: height 0.3s ease;
}
.queue-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(79, 110, 247, 0.2); }
.queue-card:hover::after { height: 6px; }
.queue-label { font-size: 13px; color: var(--text-muted); margin-bottom: 8px; font-weight: 500; }
.queue-value { font-size: 32px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; background: linear-gradient(135deg, #4F6EF7, #7B5EF7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.queue-trend { font-size: 12px; font-weight: 500; }
.queue-trend.up { color: #F53F3F; }
.queue-trend.down { color: #00B42A; }
.queue-trend:not(.up):not(.down) { color: var(--text-muted); }

@keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; flex-wrap: wrap; gap: 10px; }
.header-left h2 { margin: 0 0 4px 0; font-size: 20px; color: #303133; }
.header-desc { font-size: 13px; color: #909399; }
.header-right { display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.cond-value { font-size: 12px; color: #606266; background: #f5f7fa; padding: 2px 6px; border-radius: 3px; }
.priority-badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.priority-badge.high { background: #fef0f0; color: #f56c6c; }
.priority-badge.mid { background: #fdf6ec; color: #e6a23c; }
.priority-badge.low { background: #f0f9eb; color: #67c23a; }
.form-hint { margin-left: 10px; font-size: 12px; color: #909399; }
</style>
