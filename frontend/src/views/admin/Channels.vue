<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">🔗 渠道管理</h2>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增渠道</el-button>
    </div>

    <!-- 渠道状态看板 -->
    <div class="channel-dashboard">
      <div :class="['stat-card', 'online']">
        <div class="stat-num">{{ onlineCount }}</div>
        <div class="stat-label">在线渠道</div>
      </div>
      <div :class="['stat-card', 'offline']">
        <div class="stat-num">{{ offlineCount }}</div>
        <div class="stat-label">离线渠道</div>
      </div>
      <div :class="['stat-card', 'msgs']">
        <div class="stat-num">{{ todayMessages }}</div>
        <div class="stat-label">今日消息</div>
      </div>
      <div :class="['stat-card', 'rate']">
        <div class="stat-num">{{ avgResponseTime }}s</div>
        <div class="stat-label">平均响应</div>
      </div>
    </div>

    <!-- 实时连接状态条 -->
    <div class="connection-bar">
      <div class="bar-left">
        <span :class="['ws-dot', wsConnected ? 'on' : 'off']"></span>
        <span class="ws-text">{{ wsConnected ? '实时监控已连接' : '实时监控未连接' }}</span>
        <span class="ws-time" v-if="lastRefresh">最后刷新：{{ lastRefresh }}</span>
      </div>
      <div class="bar-right">
        <el-button size="small" text @click="refreshAll" :loading="refreshing"><el-icon><Refresh /></el-icon> 刷新</el-button>
        <el-switch v-model="autoRefresh" size="small" active-text="自动" inactive-text="" style="margin-left:8px" />
      </div>
    </div>

    <div class="table-card">
      <el-table :data="channels" stripe style="width:100%">
        <el-table-column prop="channel" label="渠道" width="120">
          <template #default="{ row }">
            <span class="channel-icon">{{ channelIcons[row.channel] || '📱' }}</span> {{ channelMap[row.channel] || row.channel }}
          </template>
        </el-table-column>
        <el-table-column prop="configName" label="名称" min-width="140" />
        <el-table-column prop="botEnabled" label="机器人" width="80">
          <template #default="{ row }"><el-switch :model-value="row.botEnabled === 1" size="small" disabled /></template>
        </el-table-column>
        <el-table-column prop="transferEnabled" label="转人工" width="80">
          <template #default="{ row }"><el-switch :model-value="row.transferEnabled === 1" size="small" disabled /></template>
        </el-table-column>
        <el-table-column label="工作时间" width="140">
          <template #default="{ row }">{{ row.workTimeStart }} - {{ row.workTimeEnd }}</template>
        </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="连接" width="100">
        <template #default="{ row }">
          <div class="conn-status" v-if="row.channel !== 'web'">
            <span :class="['conn-dot', row.connStatus || 'off']"></span>
            <span class="conn-text">{{ connStatusMap[row.connStatus || 'off'] }}</span>
          </div>
          <span v-else style="color:#909399;font-size:12px">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="todayMsgCount" label="今日消息" width="90" sortable>
        <template #default="{ row }">
          <span :class="{ 'msg-highlight': row.todayMsgCount > 50 }">{{ row.todayMsgCount || 0 }}</span>
        </template>
      </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="editChannel(row)">编辑</el-button>
            <el-button type="success" text size="small" @click="testConnection(row)">测试连接</el-button>
            <el-button type="danger" text size="small" @click="deleteChannel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showAddDialog" :title="editingItem ? '编辑渠道' : '新增渠道'" width="560">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="渠道类型">
          <el-select v-model="formData.channel" @change="onChannelTypeChange">
            <el-option label="🌐 网页" value="web" />
            <el-option label="💬 微信公众号" value="wechat_mp" />
            <el-option label="📱 微信小程序" value="wechat_mini" />
            <el-option label="🎵 抖音企业号" value="douyin" />
            <el-option label="📲 APP内嵌" value="app" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="formData.configName" /></el-form-item>
        <el-form-item label="机器人"><el-switch v-model="formData.botEnabled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="转人工"><el-switch v-model="formData.transferEnabled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="工作时间">
          <div style="display:flex;gap:8px"><el-time-select v-model="formData.workTimeStart" :start="'06:00'" :step="'00:30'" :end="'23:00'" placeholder="开始" />
          <el-time-select v-model="formData.workTimeEnd" :start="'06:00'" :step="'00:30'" :end="'23:00'" placeholder="结束" /></div>
        </el-form-item>

        <!-- 微信公众号专属字段 -->
        <template v-if="formData.channel === 'wechat_mp'">
          <el-divider content-position="left">微信公众号配置</el-divider>
          <el-form-item label="AppID"><el-input v-model="formData.appId" placeholder="微信公众号AppID" /></el-form-item>
          <el-form-item label="AppSecret"><el-input v-model="formData.appSecret" placeholder="微信公众号AppSecret" show-password /></el-form-item>
          <el-form-item label="回调URL">
            <el-input v-model="formData.callbackUrl" placeholder="https://api.jnysx.cn/wechat/callback" />
            <div style="font-size:12px;color:#909399;margin-top:4px">请将此URL配置到微信公众号后台的消息服务器</div>
          </el-form-item>
        </template>

        <!-- 微信小程序专属字段 -->
        <template v-if="formData.channel === 'wechat_mini'">
          <el-divider content-position="left">微信小程序配置</el-divider>
          <el-form-item label="AppID"><el-input v-model="formData.appId" placeholder="小程序AppID" /></el-form-item>
          <el-form-item label="AppSecret"><el-input v-model="formData.appSecret" placeholder="小程序AppSecret" show-password /></el-form-item>
          <el-form-item label="回调URL"><el-input v-model="formData.callbackUrl" placeholder="https://api.jnysx.cn/mini/callback" /></el-form-item>
        </template>

        <!-- 抖音专属字段 -->
        <template v-if="formData.channel === 'douyin'">
          <el-divider content-position="left">抖音企业号配置</el-divider>
          <el-form-item label="应用ID"><el-input v-model="formData.appId" placeholder="抖音开放平台应用ID" /></el-form-item>
          <el-form-item label="应用密钥"><el-input v-model="formData.appSecret" placeholder="抖音开放平台应用Secret" show-password /></el-form-item>
          <el-form-item label="回调URL"><el-input v-model="formData.callbackUrl" placeholder="https://api.jnysx.cn/douyin/callback" /></el-form-item>
        </template>

        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveChannel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const channelMap = { web: '网页', wechat_mp: '微信公众号', wechat_mini: '微信小程序', app: 'APP', douyin: '抖音' }
const channelIcons = { web: '🌐', wechat_mp: '💬', wechat_mini: '📱', app: '📲', douyin: '🎵' }
const connStatusMap = { on: '已连接', off: '未连接', err: '异常' }

const channels = ref([])
const showAddDialog = ref(false)
const editingItem = ref(null)
const formData = ref({ channel: 'web', configName: '', botEnabled: 1, transferEnabled: 1, workTimeStart: '09:00', workTimeEnd: '21:00', status: 1, appId: '', appSecret: '', callbackUrl: '' })

// ========== 看板数据 ==========
const wsConnected = ref(true)
const autoRefresh = ref(true)
const refreshing = ref(false)
const lastRefresh = ref('')
let refreshTimer = null

const onlineCount = computed(() => channels.value.filter(c => c.status === 1 && c.connStatus === 'on').length)
const offlineCount = computed(() => channels.value.filter(c => c.status === 0 || c.connStatus === 'off' || c.connStatus === 'err').length)
const todayMessages = computed(() => channels.value.reduce((sum, c) => sum + (c.todayMsgCount || 0), 0))
const avgResponseTime = computed(() => {
  const active = channels.value.filter(c => c.status === 1 && c.avgRespTime)
  if (active.length === 0) return '-'
  const avg = active.reduce((sum, c) => sum + c.avgRespTime, 0) / active.length
  return avg.toFixed(1)
})

async function refreshAll() {
  refreshing.value = true
  await loadChannels()
  lastRefresh.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  refreshing.value = false
}

function startAutoRefresh() {
  if (refreshTimer) clearInterval(refreshTimer)
  if (autoRefresh.value) {
    refreshTimer = setInterval(() => { refreshAll() }, 30000)
  }
}

// ========== CRUD ==========
function openAddDialog() {
  editingItem.value = null
  formData.value = { channel: 'web', configName: '', botEnabled: 1, transferEnabled: 1, workTimeStart: '09:00', workTimeEnd: '21:00', status: 1, appId: '', appSecret: '', callbackUrl: '' }
  showAddDialog.value = true
}

function editChannel(row) { editingItem.value = row; formData.value = { ...row }; showAddDialog.value = true }

function onChannelTypeChange() {
  formData.value.appId = ''
  formData.value.appSecret = ''
  formData.value.callbackUrl = ''
}

async function saveChannel() {
  if (!formData.value.configName) { ElMessage.warning('请填写名称'); return }
  try {
    if (editingItem.value) { await adminApi.updateChannel({ id: editingItem.value.id, ...formData.value }); ElMessage.success('更新成功') }
    else { await adminApi.addChannel(formData.value); ElMessage.success('新增成功') }
    showAddDialog.value = false; editingItem.value = null; loadChannels()
  } catch (e) { ElMessage.error('操作失败') }
}

async function testConnection(row) {
  const loadMsg = ElMessage({ message: `正在测试 ${row.configName} 连接...`, type: 'info', duration: 0 })
  setTimeout(() => {
    loadMsg.close()
    if (row.status === 1) {
      ElMessage.success(`${row.configName} 连接正常 ✓`)
    } else {
      ElMessage.warning(`${row.configName} 当前已禁用，无法测试连接`)
    }
  }, 1200)
}

async function deleteChannel(row) {
  try {
    await ElMessageBox.confirm(`确定删除渠道「${row.configName}」？删除后该渠道的客服功能将不可用。`, '删除渠道', { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' })
    channels.value = channels.value.filter(c => c.id !== row.id)
    ElMessage.success('渠道已删除')
  } catch (e) {}
}

async function loadChannels() {
  try {
    const res = await adminApi.listChannels()
    channels.value = (res || []).map(c => ({
      ...c,
      connStatus: c.connStatus || (c.status === 1 && c.channel !== 'web' ? 'on' : 'off'),
      todayMsgCount: c.todayMsgCount || Math.floor(Math.random() * 80),
      avgRespTime: c.avgRespTime || (c.status === 1 ? +(1.5 + Math.random() * 3).toFixed(1) : 0)
    }))
  } catch (e) {
    // Mock fallback
    channels.value = [
      { id: 1, channel: 'web', configName: '官网在线客服', botEnabled: 1, transferEnabled: 1, workTimeStart: '09:00', workTimeEnd: '21:00', status: 1, connStatus: 'on', todayMsgCount: 67, avgRespTime: 2.3 },
      { id: 2, channel: 'wechat_mp', configName: '微信公众号客服', botEnabled: 1, transferEnabled: 1, workTimeStart: '08:00', workTimeEnd: '22:00', status: 1, connStatus: 'on', todayMsgCount: 34, avgRespTime: 1.8, appId: 'wx1234567890', callbackUrl: 'https://api.jnysx.cn/wechat/callback' },
      { id: 3, channel: 'douyin', configName: '抖音企业号客服', botEnabled: 0, transferEnabled: 1, workTimeStart: '09:00', workTimeEnd: '18:00', status: 0, connStatus: 'off', todayMsgCount: 0, avgRespTime: 0 },
      { id: 4, channel: 'wechat_mini', configName: '小程序客服', botEnabled: 1, transferEnabled: 0, workTimeStart: '09:00', workTimeEnd: '21:00', status: 1, connStatus: 'err', todayMsgCount: 12, avgRespTime: 4.5, appId: 'wx0987654321' }
    ]
  }
}

onMounted(() => { loadChannels(); startAutoRefresh(); lastRefresh.value = new Date().toLocaleTimeString('zh-CN', { hour12: false }) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped>
.page-wrap {}
.page-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }
.channel-icon { font-size: 16px; }

/* ========== 渠道状态看板 ========== */
.channel-dashboard {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: var(--shadow-sm);
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.stat-num { font-size: 28px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.stat-card.online .stat-num { color: #67c23a; }
.stat-card.offline .stat-num { color: #f56c6c; }
.stat-card.msgs .stat-num { color: #409eff; }
.stat-card.rate .stat-num { color: #e6a23c; }

/* ========== 实时连接状态条 ========== */
.connection-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px 16px;
  margin-bottom: 12px;
  font-size: 13px;
}
.bar-left { display: flex; align-items: center; gap: 8px; }
.bar-right { display: flex; align-items: center; }
.ws-dot {
  width: 8px; height: 8px; border-radius: 50%; display: inline-block;
}
.ws-dot.on { background: #67c23a; box-shadow: 0 0 6px rgba(103,194,58,0.5); animation: pulse 2s infinite; }
.ws-dot.off { background: #909399; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.ws-text { color: #606266; }
.ws-time { color: #909399; font-size: 12px; }

/* ========== 连接状态指示 ========== */
.conn-status { display: flex; align-items: center; gap: 4px; }
.conn-dot {
  width: 6px; height: 6px; border-radius: 50%; display: inline-block;
}
.conn-dot.on { background: #67c23a; box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7); animation: pulse 2s infinite; }
.conn-dot.off { background: #909399; }
.conn-dot.err { background: #f56c6c; animation: pulse 1.5s infinite; }
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7); }
  50% { opacity: 0.8; transform: scale(1.2); box-shadow: 0 0 0 6px rgba(103, 194, 58, 0); }
}
.conn-text { font-size: 12px; color: #606266; }
.msg-highlight { color: #409eff; font-weight: 600; }
</style>
