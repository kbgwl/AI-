<template>
  <div class="tenant-page">
    <div class="page-header">
      <h2>租户管理</h2>
      <el-button type="primary" @click="openForm()">新增租户</el-button>
    </div>

    <el-table :data="tenants" v-loading="loading" stripe>
      <el-table-column prop="tenantCode" label="租户编码" width="140" />
      <el-table-column prop="tenantName" label="租户名称" min-width="150" />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="contactPhone" label="联系电话" width="130" />
      <el-table-column label="套餐" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ getPlanName(row.planId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maxAgents" label="坐席" width="70" />
      <el-table-column prop="maxSessions" label="会话" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : row.status === 2 ? '过期' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expireTime" label="到期时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openForm(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" size="small" @click="deleteTenant(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > 20" :total="total" :page-size="20" v-model:current-page="page" @current-change="loadTenants" style="margin-top:16px" />

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑租户' : '新增租户'" width="500px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="租户名称"><el-input v-model="editForm.tenantName" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="editForm.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="editForm.contactPhone" /></el-form-item>
        <el-form-item label="联系邮箱"><el-input v-model="editForm.contactEmail" /></el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="editForm.planId" @change="onPlanChange">
            <el-option v-for="p in planList" :key="p.id" :label="p.planName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大坐席"><el-input-number v-model="editForm.maxAgents" :min="1" /></el-form-item>
        <el-form-item label="最大会话"><el-input-number v-model="editForm.maxSessions" :min="1" /></el-form-item>
        <el-form-item label="到期时间"><el-date-picker v-model="editForm.expireTime" type="datetime" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTenant">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tenants = ref([])
const planList = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const dialogVisible = ref(false)
const editForm = reactive({ id: null, tenantName: '', contactName: '', contactPhone: '', contactEmail: '', planId: null, maxAgents: 5, maxSessions: 100, expireTime: '', remark: '' })

function getPlanName(planId) {
  const p = planList.value.find(x => x.id === planId)
  return p ? p.planName : '-'
}

function onPlanChange(planId) {
  const p = planList.value.find(x => x.id === planId)
  if (p) { editForm.maxAgents = p.maxAgents; editForm.maxSessions = p.maxSessions }
}

async function loadTenants() {
  loading.value = true
  const res = await fetch(`/api/admin/tenant/list?page=${page.value}&size=20`)
  const data = await res.json()
  if (data.code === 200) { tenants.value = data.data?.records || []; total.value = data.data?.total || 0 }
  loading.value = false
}

async function loadPlans() {
  const res = await fetch('/api/admin/plan/list')
  const data = await res.json()
  if (data.code === 200) planList.value = data.data || []
}

function openForm(tenant) {
  if (tenant) {
    Object.assign(editForm, tenant)
  } else {
    Object.assign(editForm, { id: null, tenantName: '', contactName: '', contactPhone: '', contactEmail: '', planId: null, maxAgents: 5, maxSessions: 100, expireTime: '', remark: '' })
  }
  dialogVisible.value = true
}

async function saveTenant() {
  const method = editForm.id ? 'PUT' : 'POST'
  const res = await fetch('/api/admin/tenant', { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(editForm) })
  const data = await res.json()
  if (data.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadTenants() }
  else ElMessage.error(data.message || '保存失败')
}

async function toggleStatus(tenant) {
  const newStatus = tenant.status === 1 ? 0 : 1
  await fetch(`/api/admin/tenant/${tenant.id}/status?status=${newStatus}`, { method: 'PUT' })
  loadTenants()
}

async function deleteTenant(tenant) {
  await ElMessageBox.confirm('确定删除该租户？', '提示')
  await fetch(`/api/admin/tenant/${tenant.id}`, { method: 'DELETE' })
  ElMessage.success('删除成功')
  loadTenants()
}

onMounted(() => { loadPlans(); loadTenants() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
</style>
