<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">🎫 工单管理</h2>
      <div class="page-actions">
        <el-button type="success" plain :loading="exporting" @click="exportExcel"><el-icon><Download /></el-icon> 导出Excel</el-button>
        <el-button @click="loadTickets"><el-icon><Refresh /></el-icon> 刷新</el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width:120px" @change="applyFilter">
        <el-option label="待处理" :value="0" /><el-option label="处理中" :value="1" /><el-option label="已完成" :value="2" /><el-option label="已关闭" :value="3" />
      </el-select>
      <el-select v-model="filterCategory" placeholder="分类筛选" clearable size="small" style="width:120px" @change="applyFilter">
        <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
      </el-select>
      <el-select v-model="filterPriority" placeholder="优先级" clearable size="small" style="width:110px" @change="applyFilter">
        <el-option label="一般" :value="0" /><el-option label="紧急" :value="1" /><el-option label="非常紧急" :value="2" />
      </el-select>
    </div>

    <div class="table-card">
      <el-empty v-if="allFilteredTickets.length === 0" description="暂无工单" :image-size="80">
        <template #description>
          <p style="color:var(--text-muted);font-size:13px;margin:0">工单由用户从前台聊天页提交，此处显示所有已提交的工单。</p>
        </template>
      </el-empty>

      <el-table v-else :data="pagedTickets" stripe style="width:100%">
        <el-table-column prop="ticketNo" label="工单号" width="160" />
        <el-table-column prop="subject" label="标题" min-width="140" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }"><el-tag size="small">{{ row.category }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }"><el-tag :type="priorityType[row.priority]" size="small">{{ priorityMap[row.priority] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="ticketStatusType[row.status]" size="small">{{ ticketStatusMap[row.status] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="assignee" label="处理人" width="80">
          <template #default="{ row }">{{ row.assignee || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" text size="small" @click="resolveTicket(row.id)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="allFilteredTickets.length > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="allFilteredTickets.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          small
        />
      </div>
    </div>

    <!-- 工单详情弹窗 -->
    <el-dialog v-model="showDetail" title="工单详情" width="640" destroy-on-close>
      <template v-if="currentTicket">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="工单号">{{ currentTicket.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentTicket.subject }}</el-descriptions-item>
          <el-descriptions-item label="分类"><el-tag size="small">{{ currentTicket.category }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="优先级"><el-tag :type="priorityType[currentTicket.priority]" size="small">{{ priorityMap[currentTicket.priority] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="ticketStatusType[currentTicket.status]" size="small">{{ ticketStatusMap[currentTicket.status] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTicket.createTime }}</el-descriptions-item>
          <el-descriptions-item label="联系方式" v-if="currentTicket.contact">{{ currentTicket.contact }}</el-descriptions-item>
          <el-descriptions-item label="来源渠道">{{ channelMap[currentTicket.channel] || currentTicket.channel }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-bottom:16px">
          <h4 style="margin:0 0 6px;font-size:14px;color:var(--text-primary)">问题描述</h4>
          <div style="background:#f5f7fa;border-radius:6px;padding:10px 14px;font-size:13px;color:#606266;line-height:1.6">{{ currentTicket.description || '暂无描述' }}</div>
        </div>
        <div v-if="currentTicket.resolution" style="margin-bottom:16px">
          <h4 style="margin:0 0 6px;font-size:14px;color:var(--text-primary)">处理结果</h4>
          <div style="background:#f0f9eb;border-radius:6px;padding:10px 14px;font-size:13px;color:#67c23a;line-height:1.6">{{ currentTicket.resolution }}</div>
        </div>
        <div style="margin-bottom:16px">
          <h4 style="margin:0 0 8px;font-size:14px;color:var(--text-primary)">操作</h4>
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">
            <el-select v-model="assignAgentId" placeholder="选择客服" style="width:140px" size="small">
              <el-option v-for="a in agents" :key="a.id" :label="a.nickname" :value="a.id" />
            </el-select>
            <el-button type="warning" size="small" @click="assignTicket" :disabled="!assignAgentId || currentTicket.status > 1">分配</el-button>
            <el-input v-model="resolveText" type="textarea" :rows="1" placeholder="处理说明" style="width:180px" size="small" />
            <el-button type="success" size="small" @click="resolveInDetail" :disabled="!resolveText || currentTicket.status > 1">处理完成</el-button>
            <el-button type="danger" size="small" @click="closeTicket" :disabled="currentTicket.status === 3">关闭</el-button>
          </div>
        </div>
        <div>
          <h4 style="margin:0 0 6px;font-size:14px;color:var(--text-primary)">操作日志</h4>
          <el-timeline v-if="ticketLogs.length">
            <el-timeline-item v-for="(log, i) in ticketLogs" :key="i" :timestamp="log.time || log.createTime" placement="top">
              {{ log.action || log.content }}
            </el-timeline-item>
          </el-timeline>
          <div v-else style="color:#909399;font-size:13px;padding:8px 0">暂无操作记录</div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ticketApi, adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const priorityMap = { 0: '一般', 1: '紧急', 2: '非常紧急' }
const priorityType = { 0: 'info', 1: 'warning', 2: 'danger' }
const ticketStatusMap = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
const ticketStatusType = { 0: 'danger', 1: 'warning', 2: 'success', 3: 'info' }
const channelMap = { web: '网页', wechat_mp: '微信公众号', wechat_mini: '微信小程序', app: 'APP', douyin: '抖音' }
const categories = ['订单问题', '退换货', '退款问题', '产品咨询', '技术支持', '投诉建议', '其他']

const allTickets = ref([])
const filterStatus = ref(null)
const filterCategory = ref(null)
const filterPriority = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

const allFilteredTickets = computed(() => {
  let list = allTickets.value
  if (filterStatus.value !== null && filterStatus.value !== '') list = list.filter(t => t.status === filterStatus.value)
  if (filterCategory.value) list = list.filter(t => t.category === filterCategory.value)
  if (filterPriority.value !== null && filterPriority.value !== '') list = list.filter(t => t.priority === filterPriority.value)
  return list
})

const pagedTickets = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return allFilteredTickets.value.slice(start, start + pageSize.value)
})

function applyFilter() { currentPage.value = 1 }

const showDetail = ref(false)
const currentTicket = ref(null)
const ticketLogs = ref([])
const assignAgentId = ref(null)
const resolveText = ref('')
const agents = ref([])

async function openDetail(row) {
  currentTicket.value = row
  showDetail.value = true
  assignAgentId.value = null
  resolveText.value = ''
  loadLogs(row.id)
  loadAgents()
}

async function loadLogs(id) { try { const res = await ticketApi.getLogs(id); ticketLogs.value = res || [] } catch (e) { ticketLogs.value = [] } }
async function loadAgents() { try { const res = await adminApi.listAgents({ page: 1, size: 50 }); agents.value = res.records || res || [] } catch (e) { agents.value = [] } }

async function assignTicket() {
  if (!assignAgentId.value) { ElMessage.warning('请选择客服'); return }
  try { await ticketApi.assign(currentTicket.value.id, assignAgentId.value); ElMessage.success('分配成功'); loadTickets(); showDetail.value = false } catch (e) { ElMessage.error('分配失败') }
}

async function resolveInDetail() {
  if (!resolveText.value) { ElMessage.warning('请填写处理说明'); return }
  try { await ticketApi.resolve(currentTicket.value.id, resolveText.value); ElMessage.success('处理成功'); loadTickets(); showDetail.value = false } catch (e) { ElMessage.error('处理失败') }
}

async function closeTicket() {
  try {
    await ElMessageBox.confirm('确定要关闭该工单吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await ticketApi.close(currentTicket.value.id)
    ElMessage.success('工单已关闭'); loadTickets(); showDetail.value = false
  } catch (e) {}
}

async function resolveTicket(id) {
  try { await ticketApi.resolve(id, '已处理'); ElMessage.success('已处理'); loadTickets() } catch (e) { ElMessage.error('操作失败') }
}

const exporting = ref(false)

function exportExcel() {
 if (allFilteredTickets.value.length === 0) { ElMessage.warning('没有可导出的数据'); return }
 if (exporting.value) return
 exporting.value = true
 // 必须同步执行 a.click()，不能放在异步回调里
 try {
  const headers = ['工单号','标题','分类','优先级','状态','处理人','联系方式','创建时间']
  const rows = allFilteredTickets.value.map(t => [t.ticketNo, t.subject, t.category, priorityMap[t.priority], ticketStatusMap[t.status], t.assignee||'', t.contact||'', t.createTime])
  const csv = [headers, ...rows].map(r => r.join(',')).join('\n')
  const filename = `工单导出_${new Date().toISOString().slice(0,10)}.csv`
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.setAttribute('href', url)
  a.setAttribute('download', filename)
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  setTimeout(() => {
   document.body.removeChild(a)
   URL.revokeObjectURL(url)
   exporting.value = false
   ElMessage.success({ message: `导出成功！已下载 ${allFilteredTickets.value.length} 条记录`, duration: 4000, offset: 80 })
  }, 1000)
 } catch (e) {
  exporting.value = false
  console.error('导出失败:', e)
  ElMessage.error({ message: '导出失败，请重试', duration: 3000, offset: 80 })
 }
}

async function loadTickets() {
  try { const res = await ticketApi.list({ page: 1, size: 200 }); allTickets.value = res.records || res || [] } catch (e) { allTickets.value = [] }
}

onMounted(() => { loadTickets() })
</script>

<style scoped>
.page-wrap {}
.page-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.page-actions { display: flex; gap: 10px; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }
.pagination-wrap { padding: 12px 16px; display: flex; justify-content: flex-end; }
</style>
