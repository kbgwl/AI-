<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">📋 操作日志</h2>
      <div class="page-actions">
        <el-button type="success" plain :loading="exporting" @click="exportExcel"><el-icon><Download /></el-icon> 导出Excel</el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索关键词..." clearable size="small" style="width:200px" @input="applyFilter" :prefix-icon="Search" />
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" size="default" style="width:240px" />
      <el-select v-model="moduleFilter" placeholder="模块" style="width:120px" clearable @change="applyFilter">
        <el-option label="全部" value="" /><el-option label="知识库" value="kb" /><el-option label="客服" value="agent" /><el-option label="意图" value="intent" /><el-option label="渠道" value="channel" /><el-option label="工单" value="ticket" />
      </el-select>
      <el-select v-model="operatorFilter" placeholder="操作者" style="width:120px" clearable @change="applyFilter">
        <el-option label="全部" value="" /><el-option label="管理员" value="admin" /><el-option label="系统" value="system" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table :data="pagedLogs" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="module" label="模块" width="90">
          <template #default="{ row }"><el-tag :type="moduleType[row.module] || 'info'" size="small">{{ moduleMap[row.module] || row.module }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
        <el-table-column prop="operatorType" label="操作者" width="80">
          <template #default="{ row }">
            <span :class="['op-badge', row.operatorType]">{{ row.operatorType === 'admin' ? '管理员' : '系统' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="80">
          <template #default="{ row }">{{ row.operatorName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>

      <!-- 分页 -->
      <div v-if="allFilteredLogs.length > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="allFilteredLogs.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          small
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Download, Search } from '@element-plus/icons-vue'

const moduleMap = { kb: '知识库', agent: '客服', intent: '意图', channel: '渠道', ticket: '工单' }
const moduleType = { kb: 'primary', agent: 'success', intent: 'warning', channel: 'danger', ticket: '' }

const allLogs = ref([])
const dateRange = ref(null)
const moduleFilter = ref('')
const operatorFilter = ref('')
const keyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

const allFilteredLogs = computed(() => {
  let list = allLogs.value
  if (moduleFilter.value) list = list.filter(l => l.module === moduleFilter.value)
  if (operatorFilter.value) list = list.filter(l => l.operatorType === operatorFilter.value)
 if (keyword.value) {
  const kw = keyword.value.toLowerCase()
  list = list.filter(l => {
    const moduleName = moduleMap[l.module] || l.module
    return (l.detail || '').toLowerCase().includes(kw)
      || (l.action || '').toLowerCase().includes(kw)
      || moduleName.toLowerCase().includes(kw)
  })
 }
  return list
})

const pagedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return allFilteredLogs.value.slice(start, start + pageSize.value)
})

function applyFilter() { currentPage.value = 1 }

const exporting = ref(false)

function exportExcel() {
 if (allFilteredLogs.value.length === 0) { ElMessage.warning('没有可导出的数据'); return }
 if (exporting.value) return
 exporting.value = true
 // 必须同步执行 a.click()，不能放在异步回调里，否则浏览器会认为是非用户手势触发而拦截下载
 try {
  const headers = ['ID','模块','操作','详情','操作者类型','操作人','时间']
  const rows = allFilteredLogs.value.map(l => [l.id, moduleMap[l.module]||l.module, l.action, l.detail, l.operatorType, l.operatorName||'', l.createTime])
  const csv = [headers, ...rows].map(r => r.map(c => `"${(c||'').toString().replace(/"/g,'""')}"`).join(',')).join('\n')
  const filename = `操作日志_${new Date().toISOString().slice(0,10)}.csv`
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  // 关键：同步创建+点击+清理，整个过程必须在同一个事件循环内完成
  const a = document.createElement('a')
  a.setAttribute('href', url)
  a.setAttribute('download', filename)
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  // 延迟清理（不能立即removeChild，否则下载来不及开始）
  setTimeout(() => {
   document.body.removeChild(a)
   URL.revokeObjectURL(url)
   exporting.value = false
   ElMessage.success({ message: `导出成功！已下载 ${allFilteredLogs.value.length} 条记录`, duration: 4000, offset: 80 })
  }, 1000)
 } catch (e) {
  exporting.value = false
  console.error('导出失败:', e)
  ElMessage.error({ message: '导出失败，请重试', duration: 3000, offset: 80 })
 }
}

async function loadLogs() {
  const params = { module: moduleFilter.value || undefined }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0].toISOString().slice(0, 10)
    params.endDate = dateRange.value[1].toISOString().slice(0, 10)
  }
  try { const res = await adminApi.listLogs(params); allLogs.value = res.records || res || [] } catch (e) {}
}

watch(dateRange, () => { loadLogs() })
// moduleFilter已在allFilteredLogs前端过滤，不需要重新请求API
// watch(moduleFilter, () => { loadLogs() })

onMounted(() => { loadLogs() })
</script>

<style scoped>
.page-wrap {}
.page-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.page-actions { display: flex; gap: 10px; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }
.pagination-wrap { padding: 12px 16px; display: flex; justify-content: flex-end; }

.op-badge { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.op-badge.admin { background: var(--primary-light); color: var(--primary); }
.op-badge.system { background: #F2F3F5; color: var(--text-muted); }
</style>
