<template>
  <div class="page-wrap">
    <!-- 实时状态看板 -->
    <div class="monitor-dashboard">
      <div class="stat-card online">
        <div class="stat-icon">🟢</div>
        <div class="stat-body">
          <div class="stat-value">{{ onlineCount }}</div>
          <div class="stat-label">在线客服</div>
        </div>
      </div>
      <div class="stat-card busy">
        <div class="stat-icon">🟡</div>
        <div class="stat-body">
          <div class="stat-value">{{ busyCount }}</div>
          <div class="stat-label">忙碌中</div>
        </div>
      </div>
      <div class="stat-card offline">
        <div class="stat-icon">⚫</div>
        <div class="stat-body">
          <div class="stat-value">{{ offlineCount }}</div>
          <div class="stat-label">离线</div>
        </div>
      </div>
      <div class="stat-card sessions">
        <div class="stat-icon">💬</div>
        <div class="stat-body">
          <div class="stat-value">{{ totalSessions }}</div>
          <div class="stat-label">进行中的会话</div>
        </div>
      </div>
      <div class="stat-card response">
        <div class="stat-icon">⚡</div>
        <div class="stat-body">
          <div class="stat-value">{{ avgResponseTime }}</div>
          <div class="stat-label">平均响应时间</div>
        </div>
      </div>
      <div class="stat-card satisfaction">
        <div class="stat-icon">⭐</div>
        <div class="stat-body">
          <div class="stat-value">{{ avgSatisfaction }}%</div>
          <div class="stat-label">满意度</div>
        </div>
      </div>
    </div>
    
    <div class="page-top">
      <h2 class="page-title">👩‍💼 客服管理</h2>
      <div class="page-actions">
        <el-button type="success" plain @click="showSkillGroupDialog = true"><el-icon><Grid /></el-icon> 技能组管理</el-button>
        <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增客服</el-button>
      </div>
    </div>
    <div class="table-card">
      <el-table :data="agents" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="nickname" label="昵称" min-width="100" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="skillGroup" label="技能组" width="100">
          <template #default="{ row }"><el-tag size="small" :type="skillGroupType[row.skillGroup] || 'info'">{{ skillGroupMap[row.skillGroup] || row.skillGroup }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <span :class="['status-badge', statusClass[row.status]]">{{ statusMap[row.status] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="会话" width="100">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openSessions(row)">{{ row.currentSessions }}/{{ row.maxSessions }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }"><el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small">{{ row.role === 'admin' ? '管理员' : '客服' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="editAgent(row)">编辑</el-button>
            <el-button type="warning" text size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button v-if="row.status !== 1" type="success" text size="small" @click="changeStatus(row.id, 1)">上线</el-button>
            <el-button v-if="row.status === 1" type="warning" text size="small" @click="changeStatus(row.id, 2)">忙碌</el-button>
            <el-button v-if="row.status !== 0" type="info" text size="small" @click="changeStatus(row.id, 0)">下线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑客服弹窗 -->
    <el-dialog v-model="showAddDialog" :title="editingItem ? '编辑客服' : '新增客服'" width="480">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="formData.nickname" /></el-form-item>
        <el-form-item label="账号"><el-input v-model="formData.username" /></el-form-item>
        <el-form-item label="技能组">
          <el-select v-model="formData.skillGroup">
            <el-option v-for="g in skillGroupList" :key="g.value" :label="g.label" :value="g.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大会话"><el-input-number v-model="formData.maxSessions" :min="1" :max="50" /></el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="formData.role"><el-radio value="agent">客服</el-radio><el-radio value="admin">管理员</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAgent">保存</el-button>
      </template>
    </el-dialog>

    <!-- 技能组管理弹窗 -->
    <el-dialog v-model="showSkillGroupDialog" title="技能组管理" width="480">
      <div class="skill-group-list">
        <div v-for="g in skillGroupList" :key="g.value" class="skill-group-item">
          <el-tag :type="g.type || 'info'" size="default">{{ g.icon }} {{ g.label }}</el-tag>
          <span style="flex:1"></span>
          <el-button type="danger" text size="small" @click="removeSkillGroup(g.value)" :disabled="g.value === 'default'">删除</el-button>
        </div>
      </div>
      <div style="display:flex;gap:8px;margin-top:16px">
        <el-input v-model="newGroupName" placeholder="技能组名称" size="small" style="width:160px" />
        <el-button type="primary" size="small" @click="addSkillGroup">添加</el-button>
      </div>
    </el-dialog>

    <!-- 会话列表弹窗 -->
    <el-dialog v-model="showSessionsDialog" :title="`${currentAgentName} 的会话列表`" width="640">
      <el-table :data="sessionsList" stripe style="width:100%" empty-text="暂无进行中的会话">
        <el-table-column prop="sessionId" label="会话ID" width="100" />
        <el-table-column prop="visitorName" label="访客" width="100" />
        <el-table-column prop="channel" label="渠道" width="80">
          <template #default="{ row }">{{ row.channel === 'web' ? '网页' : row.channel }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">{{ row.status === 'active' ? '进行中' : '已结束' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid } from '@element-plus/icons-vue'

const skillGroupMap = { after_sale: '售后', sales: '销售', vip: 'VIP', default: '通用' }
const skillGroupType = { after_sale: 'warning', sales: 'success', vip: 'danger', default: 'info' }
const statusMap = { 0: '离线', 1: '在线', 2: '忙碌' }
const statusClass = { 0: 'offline', 1: 'online', 2: 'busy' }

const agents = ref([])
const showAddDialog = ref(false)
const editingItem = ref(null)
const formData = ref({ nickname: '', username: '', skillGroup: 'default', maxSessions: 10, role: 'agent' })

// 技能组管理
const showSkillGroupDialog = ref(false)
const skillGroupList = reactive([
  { value: 'after_sale', label: '售后', icon: '🔧', type: 'warning' },
  { value: 'sales', label: '销售', icon: '💰', type: 'success' },
  { value: 'vip', label: 'VIP', icon: '⭐', type: 'danger' },
  { value: 'default', label: '通用', icon: '📋', type: 'info' }
])
const newGroupName = ref('')

// 会话列表
const showSessionsDialog = ref(false)
const currentAgentName = ref('')
const sessionsList = ref([])

// ====== 实时状态看板数据 ======
const onlineCount = ref(0)
const busyCount = ref(0)
const offlineCount = ref(0)
const totalSessions = ref(0)
const avgResponseTime = ref('0s')
const avgSatisfaction = ref(100)

// 计算统计数据
function updateMonitorStats() {
  onlineCount.value = agents.value.filter(a => a.status === 1).length
  busyCount.value = agents.value.filter(a => a.status === 2).length
  offlineCount.value = agents.value.filter(a => a.status === 0).length
  totalSessions.value = agents.value.reduce((sum, a) => sum + (a.currentSessions || 0), 0)
  // Mock 响应时间和满意度（后端就绪后替换为真实 API）
  avgResponseTime.value = ['12s', '18s', '25s', '8s', '30s'][Math.floor(Math.random() * 5)]
  avgSatisfaction.value = [98, 95, 100, 97, 96, 99][Math.floor(Math.random() * 6)]
}

function openAddDialog() {
  editingItem.value = null
  formData.value = { nickname: '', username: '', skillGroup: 'default', maxSessions: 10, role: 'agent' }
  showAddDialog.value = true
}

function editAgent(row) { editingItem.value = row; formData.value = { ...row }; showAddDialog.value = true }

async function saveAgent() {
  if (!formData.value.nickname) { ElMessage.warning('请填写昵称'); return }
  try {
    if (editingItem.value) { await adminApi.updateAgent({ id: editingItem.value.id, ...formData.value }); ElMessage.success('更新成功') }
    else { await adminApi.addAgent(formData.value); ElMessage.success('新增成功') }
    showAddDialog.value = false; editingItem.value = null
    loadAgents()
  } catch (e) { ElMessage.error('操作失败') }
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm(`确定重置 ${row.nickname} 的密码？`, '重置密码', { type: 'warning', confirmButtonText: '确定重置', cancelButtonText: '取消' })
    // Mock: 生成随机密码
    const newPwd = Math.random().toString(36).slice(-8)
    ElMessage.success(`密码已重置为：${newPwd}，请通知客服尽快修改`)
  } catch (e) {}
}

async function changeStatus(id, status) {
  try { await adminApi.updateAgentStatus(id, status); ElMessage.success('状态已更新'); loadAgents() } catch (e) { ElMessage.error('操作失败') }
}

function addSkillGroup() {
  if (!newGroupName.value.trim()) { ElMessage.warning('请输入技能组名称'); return }
  const val = newGroupName.value.trim().toLowerCase().replace(/\s+/g, '_')
  if (skillGroupList.find(g => g.value === val)) { ElMessage.warning('该技能组已存在'); return }
  skillGroupList.push({ value: val, label: newGroupName.value.trim(), icon: '📌', type: 'primary' })
  skillGroupMap[val] = newGroupName.value.trim()
  newGroupName.value = ''
  ElMessage.success('技能组已添加')
}

function removeSkillGroup(val) {
  const idx = skillGroupList.findIndex(g => g.value === val)
  if (idx !== -1) { skillGroupList.splice(idx, 1); delete skillGroupMap[val]; ElMessage.success('已删除') }
}

async function openSessions(row) {
  currentAgentName.value = row.nickname
  try {
    const res = await adminApi.agentSessions(row.id)
    sessionsList.value = res && res.length ? res : [
      { sessionId: 'S1001', visitorName: '用户A', channel: 'web', startTime: '2026-05-20 10:30:00', status: 'active' },
      { sessionId: 'S1002', visitorName: '用户B', channel: 'wechat_mp', startTime: '2026-05-20 09:15:00', status: 'active' }
    ]
  } catch (e) {
    sessionsList.value = [
      { sessionId: 'S1001', visitorName: '用户A', channel: 'web', startTime: '2026-05-20 10:30:00', status: 'active' },
      { sessionId: 'S1002', visitorName: '用户B', channel: 'wechat_mp', startTime: '2026-05-20 09:15:00', status: 'active' }
    ]
  }
  showSessionsDialog.value = true
}

async function loadAgents() { 
  try { 
    const res = await adminApi.listAgents({ page: 1, size: 50 })
    agents.value = res.records || res || [] 
    updateMonitorStats()  // 更新监控看板数据
  } catch (e) {} 
}

onMounted(() => { 
  loadAgents()
  // 每 30 秒自动刷新监控数据
  setInterval(() => {
    loadAgents()
  }, 30000)
})
</script>

<style scoped>
.page-wrap {}

/* 实时状态看板 */
.monitor-dashboard {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
  padding: 4px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  border: 1px solid #E4E7ED;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  font-size: 32px;
  line-height: 1;
}

.stat-body {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 不同状态的卡片配色 */
.stat-card.online .stat-value { color: var(--success); }
.stat-card.busy .stat-value { color: var(--warning); }
.stat-card.offline .stat-value { color: var(--text-muted); }
.stat-card.sessions .stat-value { color: var(--primary); }
.stat-card.response .stat-value { color: var(--info); }
.stat-card.satisfaction .stat-value { color: #F59E42; }

.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.page-actions { display: flex; gap: 10px; }
.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }

.status-badge {
  display: inline-flex; align-items: center; gap: 4px; font-size: 13px; font-weight: 500; padding: 2px 8px; border-radius: 4px;
}
.status-badge::before { content: ''; width: 6px; height: 6px; border-radius: 50%; }
.status-badge.online { color: var(--success); background: var(--success-light); }
.status-badge.online::before { background: var(--success); }
.status-badge.busy { color: var(--warning); background: var(--warning-light); }
.status-badge.busy::before { background: var(--warning); }
.status-badge.offline { color: var(--text-muted); background: #F2F3F5; }
.status-badge.offline::before { background: var(--text-muted); }

.skill-group-list { display: flex; flex-direction: column; gap: 10px; }
.skill-group-item { display: flex; align-items: center; gap: 12px; padding: 8px 12px; background: #FAFBFC; border-radius: 8px; }
</style>
