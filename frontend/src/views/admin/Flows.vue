<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">🔀 对话流程</h2>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增流程</el-button>
    </div>

    <div v-if="flows.length === 0" class="empty-state">
      <div class="empty-icon">🔀</div>
      <p class="empty-text">暂无对话流程，点击新增创建</p>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增流程</el-button>
    </div>

    <div v-else class="table-card">
      <el-table :data="flows" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="flowName" label="流程名称" min-width="160" />
        <el-table-column prop="triggerType" label="触发类型" width="120">
          <template #default="{ row }">
            <el-tag :type="triggerTypeMap[row.triggerType]?.type || 'info'" size="small">{{ triggerTypeMap[row.triggerType]?.label || row.triggerType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="节点数" width="80">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ (row.nodes || []).length }}个</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openFlowEditor(row)">编排</el-button>
            <el-button type="primary" text size="small" @click="editFlow(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="deleteFlow(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑流程弹窗 -->
    <el-dialog v-model="showAddDialog" :title="editingItem ? '编辑流程' : '新增流程'" width="500">
      <el-form :model="formData" label-width="90px">
        <el-form-item label="流程名称"><el-input v-model="formData.flowName" /></el-form-item>
        <el-form-item label="触发类型">
          <el-select v-model="formData.triggerType">
            <el-option label="意图触发" value="intent" /><el-option label="定时触发" value="time" /><el-option label="事件触发" value="event" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFlow">保存</el-button>
      </template>
    </el-dialog>

    <!-- 流程编排器 -->
    <el-dialog v-model="showEditor" :title="'流程编排 — ' + (editingFlow?.flowName || '')" width="900" top="3vh" :close-on-click-modal="false">
      <div class="flow-editor">
        <!-- 节点画布 -->
        <div class="flow-canvas">
          <div v-for="(node, idx) in editingNodes" :key="node.id" class="flow-node-group">
            <!-- 节点卡片 -->
            <div :class="['flow-node', 'node-' + node.type, { active: selectedNode?.id === node.id }]" @click="selectNode(node)">
              <div class="node-icon">{{ nodeIcons[node.type] }}</div>
              <div class="node-info">
                <div class="node-type-label">{{ nodeTypeLabels[node.type] }}</div>
                <div class="node-name">{{ node.name }}</div>
                <div v-if="node.type === 'intent' && node.config?.keywords" class="node-config-preview">
                  关键词：{{ node.config.keywords }}
                </div>
                <div v-if="node.type === 'reply' && node.config?.template" class="node-config-preview">
                  {{ node.config.template.substring(0, 30) }}{{ node.config.template.length > 30 ? '...' : '' }}
                </div>
                <div v-if="node.type === 'transfer' && node.config?.skillGroup" class="node-config-preview">
                  技能组：{{ node.config.skillGroup }}
                </div>
              </div>
              <div class="node-actions">
                <el-button v-if="idx > 0" text size="small" @click.stop="moveNode(idx, -1)">↑</el-button>
                <el-button v-if="idx < editingNodes.length - 1" text size="small" @click.stop="moveNode(idx, 1)">↓</el-button>
                <el-button v-if="node.type !== 'start' && node.type !== 'end'" type="danger" text size="small" @click.stop="removeNode(idx)">✕</el-button>
              </div>
            </div>
            <!-- 连接线 -->
            <div v-if="idx < editingNodes.length - 1" class="flow-connector">
              <div class="connector-line"></div>
              <div class="connector-arrow">▼</div>
            </div>
          </div>

          <!-- 添加节点按钮 -->
          <div class="add-node-area">
            <el-dropdown trigger="click" @command="addNode">
              <el-button type="primary" plain round><el-icon><Plus /></el-icon> 添加节点</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="intent">🔍 意图判断</el-dropdown-item>
                  <el-dropdown-item command="reply">💬 回复节点</el-dropdown-item>
                  <el-dropdown-item command="transfer">👩‍💼 转人工</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 节点属性面板 -->
        <div class="flow-props" v-if="selectedNode">
          <div class="props-title">📝 节点属性</div>
          <el-form label-width="80px" size="small">
            <el-form-item label="节点类型">
              <el-tag :type="nodeTagTypes[selectedNode.type]" size="small">{{ nodeTypeLabels[selectedNode.type] }}</el-tag>
            </el-form-item>
            <el-form-item label="节点名称">
              <el-input v-model="selectedNode.name" />
            </el-form-item>
            <!-- 意图判断配置 -->
            <template v-if="selectedNode.type === 'intent'">
              <el-form-item label="匹配关键词">
                <el-input v-model="selectedNode.config.keywords" placeholder="用逗号分隔，如：退货,退款,退" />
              </el-form-item>
              <el-form-item label="意图编码">
                <el-input v-model="selectedNode.config.intentCode" placeholder="如：refund" />
              </el-form-item>
            </template>
            <!-- 回复节点配置 -->
            <template v-if="selectedNode.type === 'reply'">
              <el-form-item label="回复模板">
                <el-input v-model="selectedNode.config.template" type="textarea" :rows="4" placeholder="输入AI回复内容..." />
              </el-form-item>
            </template>
            <!-- 转人工配置 -->
            <template v-if="selectedNode.type === 'transfer'">
              <el-form-item label="技能组">
                <el-select v-model="selectedNode.config.skillGroup" placeholder="选择技能组">
                  <el-option label="售后" value="after_sale" />
                  <el-option label="销售" value="sales" />
                  <el-option label="VIP" value="vip" />
                  <el-option label="默认" value="default" />
                </el-select>
              </el-form-item>
              <el-form-item label="转接提示">
                <el-input v-model="selectedNode.config.transferMsg" placeholder="如：正在为您转接售后客服..." />
              </el-form-item>
            </template>
          </el-form>
        </div>
        <div class="flow-props flow-props-empty" v-else>
          <div class="props-title">📝 节点属性</div>
          <p class="empty-props">← 点击左侧节点查看属性</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showEditor = false">取消</el-button>
        <el-button type="primary" @click="saveFlowNodes">保存流程</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const triggerTypeMap = { intent: { label: '意图触发', type: 'primary' }, time: { label: '定时触发', type: 'warning' }, event: { label: '事件触发', type: 'success' } }
const nodeIcons = { start: '▶️', intent: '🔍', reply: '💬', transfer: '👩‍💼', end: '⏹️' }
const nodeTypeLabels = { start: '开始节点', intent: '意图判断', reply: '回复节点', transfer: '转人工', end: '结束节点' }
const nodeTagTypes = { start: 'success', intent: 'primary', reply: '', transfer: 'warning', end: 'danger' }

const flows = ref([])
const showAddDialog = ref(false)
const editingItem = ref(null)
const formData = ref({ flowName: '', triggerType: 'intent', status: 1 })

// 流程编排器
const showEditor = ref(false)
const editingFlow = ref(null)
const editingNodes = ref([])
const selectedNode = ref(null)

const defaultNodes = [
  { id: 'n_start', type: 'start', name: '开始', config: {} },
  { id: 'n_end', type: 'end', name: '结束', config: {} }
]

let nodeIdCounter = 100

function openAddDialog() {
  editingItem.value = null
  formData.value = { flowName: '', triggerType: 'intent', status: 1 }
  showAddDialog.value = true
}

function editFlow(row) {
  editingItem.value = row
  formData.value = { ...row }
  showAddDialog.value = true
}

async function saveFlow() {
  if (!formData.value.flowName) { ElMessage.warning('请填写流程名称'); return }
  try {
    if (editingItem.value) { await adminApi.updateFlow({ id: editingItem.value.id, ...formData.value }); ElMessage.success('更新成功') }
    else { await adminApi.addFlow(formData.value); ElMessage.success('新增成功') }
    showAddDialog.value = false; editingItem.value = null; formData.value = { flowName: '', triggerType: 'intent', status: 1 }; loadFlows()
  } catch (e) { ElMessage.error('操作失败') }
}

async function deleteFlow(id) {
  try {
    await ElMessageBox.confirm('确定删除该流程？', '提示', { type: 'warning' })
    flows.value = flows.value.filter(f => f.id !== id)
    ElMessage.success('删除成功')
  } catch (e) {}
}

function openFlowEditor(row) {
  editingFlow.value = row
  editingNodes.value = (row.nodes && row.nodes.length > 0) ? JSON.parse(JSON.stringify(row.nodes)) : JSON.parse(JSON.stringify(defaultNodes))
  selectedNode.value = null
  showEditor.value = true
}

function selectNode(node) { selectedNode.value = node }

function addNode(type) {
  const newNode = {
    id: 'n_' + (++nodeIdCounter),
    type,
    name: nodeTypeLabels[type],
    config: type === 'intent' ? { keywords: '', intentCode: '' } : type === 'reply' ? { template: '' } : type === 'transfer' ? { skillGroup: 'default', transferMsg: '' } : {}
  }
  // 插到结束节点前面
  const endIdx = editingNodes.value.findIndex(n => n.type === 'end')
  if (endIdx >= 0) editingNodes.value.splice(endIdx, 0, newNode)
  else editingNodes.value.push(newNode)
}

function removeNode(idx) {
  if (editingNodes.value[idx].type === 'start' || editingNodes.value[idx].type === 'end') return
  if (selectedNode.value?.id === editingNodes.value[idx].id) selectedNode.value = null
  editingNodes.value.splice(idx, 1)
}

function moveNode(idx, dir) {
  const targetIdx = idx + dir
  if (targetIdx < 0 || targetIdx >= editingNodes.value.length) return
  const temp = editingNodes.value[idx]
  editingNodes.value[idx] = editingNodes.value[targetIdx]
  editingNodes.value[targetIdx] = temp
}

function saveFlowNodes() {
  if (editingFlow.value) {
    editingFlow.value.nodes = JSON.parse(JSON.stringify(editingNodes.value))
    const idx = flows.value.findIndex(f => f.id === editingFlow.value.id)
    if (idx >= 0) flows.value[idx] = { ...editingFlow.value }
  }
  showEditor.value = false
  ElMessage.success('流程保存成功')
}

async function loadFlows() {
  try {
    const res = await adminApi.listFlows()
    flows.value = (res && res.length > 0) ? res : [
      {
        id: 1, flowName: '欢迎流程', triggerType: 'event', status: 1,
        nodes: [
          { id: 'n1', type: 'start', name: '开始', config: {} },
          { id: 'n2', type: 'reply', name: '欢迎语', config: { template: '您好！欢迎使用空白格AI客服，请问有什么可以帮您？' } },
          { id: 'n3', type: 'intent', name: '意图识别', config: { keywords: '查订单,退货,退款,产品,价格', intentCode: 'general' } },
          { id: 'n4', type: 'reply', name: '智能回复', config: { template: '根据用户意图返回知识库匹配结果' } },
          { id: 'n5', type: 'transfer', name: '转人工', config: { skillGroup: 'default', transferMsg: '正在为您转接人工客服...' } },
          { id: 'n6', type: 'end', name: '结束', config: {} }
        ]
      },
      {
        id: 2, flowName: '售后流程', triggerType: 'intent', status: 1,
        nodes: [
          { id: 'n10', type: 'start', name: '开始', config: {} },
          { id: 'n11', type: 'intent', name: '售后意图', config: { keywords: '退货,退款,换货,质量问题', intentCode: 'after_sale' } },
          { id: 'n12', type: 'reply', name: '退货指引', config: { template: '退货流程：1.登录→我的订单 2.申请退货 3.填写原因提交 4.等待审核 5.寄回商品 6.退款' } },
          { id: 'n13', type: 'intent', name: '是否解决', config: { keywords: '没解决,不行,还是不行', intentCode: 'unsolved' } },
          { id: 'n14', type: 'transfer', name: '转售后客服', config: { skillGroup: 'after_sale', transferMsg: '正在为您转接售后专员...' } },
          { id: 'n15', type: 'end', name: '结束', config: {} }
        ]
      },
      {
        id: 3, flowName: 'VIP接待流程', triggerType: 'event', status: 0,
        nodes: [
          { id: 'n20', type: 'start', name: '开始', config: {} },
          { id: 'n21', type: 'reply', name: 'VIP欢迎', config: { template: '尊敬的VIP用户，您好！专属客服为您服务 🌟' } },
          { id: 'n22', type: 'transfer', name: '转VIP客服', config: { skillGroup: 'vip', transferMsg: '正在为您转接VIP专属客服...' } },
          { id: 'n23', type: 'end', name: '结束', config: {} }
        ]
      }
    ]
  } catch (e) {
    flows.value = []
  }
}

onMounted(() => { loadFlows() })
</script>

<style scoped>
.page-wrap {}
.page-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }

.empty-state { text-align: center; padding: 60px 20px; background: #fff; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { color: var(--text-muted); margin-bottom: 20px; }

/* 流程编排器 */
.flow-editor { display: flex; gap: 20px; min-height: 500px; }

.flow-canvas {
  flex: 1; background: #FAFBFC; border-radius: 12px; padding: 24px 20px;
  border: 2px dashed #E5E6EB; overflow-y: auto; max-height: 65vh;
}
.flow-node-group { display: flex; flex-direction: column; align-items: center; }

.flow-node {
  display: flex; align-items: center; gap: 12px; width: 100%; max-width: 420px;
  padding: 12px 16px; border-radius: 12px; cursor: pointer;
  transition: all 0.25s; border: 2px solid transparent; position: relative;
}
.flow-node:hover { transform: translateX(4px); }
.flow-node.active { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(79,110,247,0.15); }

.node-start { background: linear-gradient(135deg, #E8FFEA, #BCEBC7); }
.node-start .node-icon { color: #00B42A; }
.node-intent { background: linear-gradient(135deg, #E8EDFF, #C9D6FF); }
.node-intent .node-icon { color: #4F6EF7; }
.node-reply { background: #fff; border: 1.5px solid #E5E6EB; }
.node-reply .node-icon { color: #86909C; }
.node-transfer { background: linear-gradient(135deg, #FFF3E8, #FFDCC7); }
.node-transfer .node-icon { color: #FF7D00; }
.node-end { background: linear-gradient(135deg, #FFECE8, #FFD4CC); }
.node-end .node-icon { color: #F53F3F; }

.node-icon { font-size: 22px; flex-shrink: 0; }
.node-info { flex: 1; min-width: 0; }
.node-type-label { font-size: 11px; color: var(--text-muted); margin-bottom: 2px; }
.node-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.node-config-preview { font-size: 12px; color: var(--text-muted); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.node-actions { display: flex; gap: 2px; flex-shrink: 0; }
.node-actions .el-button { padding: 2px 4px; }

/* 连接线 */
.flow-connector { display: flex; flex-direction: column; align-items: center; padding: 4px 0; }
.connector-line { width: 2px; height: 16px; background: #C9CDD4; }
.connector-arrow { color: #C9CDD4; font-size: 10px; line-height: 1; margin-top: -2px; }

.add-node-area { margin-top: 12px; text-align: center; }

/* 属性面板 */
.flow-props {
  width: 280px; background: #fff; border-radius: 12px; padding: 16px;
  border: 1px solid #E5E6EB; flex-shrink: 0; max-height: 65vh; overflow-y: auto;
}
.flow-props-empty { display: flex; flex-direction: column; }
.props-title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #F2F3F5; }
.empty-props { color: var(--text-muted); font-size: 13px; text-align: center; margin-top: 40px; }

@media (max-width: 768px) {
  .flow-editor { flex-direction: column; }
  .flow-props { width: 100%; }
}
</style>
