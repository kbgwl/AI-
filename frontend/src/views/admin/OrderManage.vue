<template>
  <div class="order-page">
    <div class="page-header">
      <h2>订单管理</h2>
      <el-button type="primary" @click="showCreate = true">新建订单</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="default">
        <el-option label="待支付" value="pending" />
        <el-option label="已支付" value="paid" />
        <el-option label="已退款" value="refunded" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button @click="loadOrders">刷新</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="planName" label="套餐" width="90" />
      <el-table-column label="原价" width="80">
        <template #default="{ row }"><span class="price-old">¥{{ row.amount }}</span></template>
      </el-table-column>
      <el-table-column label="优惠" width="70">
        <template #default="{ row }"><span v-if="row.discountAmount > 0" class="price-discount">-¥{{ row.discountAmount }}</span><span v-else>-</span></template>
      </el-table-column>
      <el-table-column label="实付" width="90">
        <template #default="{ row }"><span class="price">¥{{ row.payAmount }}</span></template>
      </el-table-column>
      <el-table-column prop="quantity" label="月数" width="60" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payChannel" label="渠道" width="80">
        <template #default="{ row }">{{ row.payChannel || '-' }}</template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="160">
        <template #default="{ row }">{{ row.payTime ? row.payTime.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="expireTime" label="到期时间" width="160">
        <template #default="{ row }">{{ row.expireTime ? row.expireTime.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">{{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" type="primary" size="small" @click="payOrder(row)">去支付</el-button>
          <el-button v-if="row.status === 'pending'" type="info" size="small" @click="cancelOrder(row)">取消</el-button>
          <el-button v-if="row.status === 'paid'" type="danger" size="small" @click="refundOrder(row)">退款</el-button>
          <el-button v-if="row.status === 'cancelled' || row.status === 'refunded'" size="small" @click="deleteOrder(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > 20" :total="total" :page-size="20" v-model:current-page="page" @current-change="loadOrders" style="margin-top:16px" />

    <!-- 新建订单弹窗 -->
    <el-dialog v-model="showCreate" title="新建订单" width="480px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="选择套餐">
          <el-select v-model="createForm.planId" style="width:100%">
            <el-option v-for="p in planList" :key="p.id" :label="`${p.planName} ¥${p.price}/${p.priceUnit}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="租户ID">
          <el-input-number v-model="createForm.tenantId" :min="1" />
        </el-form-item>
        <el-form-item label="购买月数">
          <el-input-number v-model="createForm.quantity" :min="1" :max="12" />
        </el-form-item>
        <el-form-item label="合计金额" v-if="selectedPlan">
          <span class="price total">¥{{ (selectedPlan.price * createForm.quantity).toFixed(2) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="createOrder">创建订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api/client'

const orders = ref([])
const planList = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const filterStatus = ref('')
const showCreate = ref(false)
const createForm = ref({ planId: null, tenantId: 1, quantity: 1 })

const statusMap = { pending: '待支付', paid: '已支付', refunded: '已退款', cancelled: '已取消' }
const statusType = { pending: 'warning', paid: 'success', refunded: 'info', cancelled: 'danger' }

const selectedPlan = computed(() => planList.value.find(p => p.id === createForm.value.planId))

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await api.get('/admin/order/list', { params })
    orders.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) { console.error(e) }
  loading.value = false
}

async function loadPlans() {
  try {
    const res = await api.get('/admin/plan/list')
    planList.value = (res || []).filter(p => p.status === 1 && p.price > 0)
  } catch (e) { console.error(e) }
}

async function createOrder() {
  if (!createForm.value.planId) { ElMessage.warning('请选择套餐'); return }
  try {
    await api.post('/admin/order/create', createForm.value)
    ElMessage.success('订单创建成功')
    showCreate.value = false
    loadOrders()
  } catch (e) { ElMessage.error(e.message || '创建失败') }
}

async function payOrder(order) {
  try {
    const config = await api.get('/admin/payment/config')
    const alipayEnabled = config?.alipay?.enabled
    const wechatEnabled = config?.wechat?.enabled
    if (!alipayEnabled && !wechatEnabled) {
      ElMessageBox.alert(
        '尚未配置支付渠道，请先在「支付配置」中配置支付宝或微信支付后再进行支付。',
        '支付未配置',
        { confirmButtonText: '去配置', type: 'warning' }
      ).then(() => { window.location.hash = ''; window.location.href = '/admin/payment-config' }).catch(() => {})
      return
    }
    const channel = alipayEnabled ? 'alipay' : 'wechat'
    const channelName = alipayEnabled ? '支付宝' : '微信支付'
    await ElMessageBox.confirm(
      `确认使用${channelName}支付订单 ${order.orderNo}（¥${order.payAmount}）？`,
      '确认支付',
      { type: 'info' }
    )
    await api.post('/admin/order/pay', { orderNo: order.orderNo, channel })
    ElMessage.success('支付成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(e.message || '支付失败')
    }
  }
}

async function cancelOrder(order) {
  await ElMessageBox.confirm('确认取消该订单？', '确认取消')
  try {
    await api.put(`/admin/order/${order.id}/cancel`)
    ElMessage.success('已取消')
    loadOrders()
  } catch (e) { ElMessage.error('操作失败') }
}

async function refundOrder(order) {
  try {
    const config = await api.get('/admin/payment/config')
    const channel = order.payChannel
    const channelConfig = config?.[channel]
    if (!channelConfig || !channelConfig.enabled) {
      ElMessageBox.alert(
        `${channel === 'alipay' ? '支付宝' : '微信支付'}渠道未配置，无法发起退款。请先在「支付配置」中配置对应渠道。`,
        '支付渠道未配置',
        { confirmButtonText: '去配置', type: 'warning' }
      ).then(() => { window.location.href = '/admin/payment-config' }).catch(() => {})
      return
    }
    await ElMessageBox.confirm(`确认退款订单 ${order.orderNo}（¥${order.payAmount}）？退款将原路返回到用户账户。`, '确认退款', { type: 'warning' })
    await api.put(`/admin/order/${order.id}/refund`)
    ElMessage.success('退款成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(e.message || '退款失败')
    }
  }
}

async function deleteOrder(order) {
  await ElMessageBox.confirm('确认删除该订单？删除后不可恢复。', '确认删除', { type: 'warning' })
  try {
    await api.delete(`/admin/order/${order.id}`)
    ElMessage.success('已删除')
    loadOrders()
  } catch (e) { ElMessage.error('删除失败') }
}

onMounted(() => { loadOrders(); loadPlans() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.price { color: #f56c6c; font-weight: 600; }
.price.total { font-size: 18px; }
</style>
