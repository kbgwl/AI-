<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">🎯 意图管理</h2>
      <div class="page-actions">
        <el-button type="success" plain @click="openTestDialog"><el-icon><ChatDotRound /></el-icon> 测试对话</el-button>
        <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增意图</el-button>
      </div>
    </div>
    <div class="table-card">
      <el-table :data="intents" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="intentName" label="意图名称" min-width="120" />
        <el-table-column prop="intentCode" label="意图编码" min-width="120">
          <template #default="{ row }"><code class="code-tag">{{ row.intentCode }}</code></template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.category === 'after_sale' ? 'warning' : row.category === 'escalation' ? 'danger' : row.category === 'pre_sale' ? 'success' : 'primary'">
              {{ categoryMap[row.category] || row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="样本数" width="80">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openSamplesDialog(row)">{{ parseSamples(row.samples).length }}条</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="needTransfer" label="转人工" width="80">
          <template #default="{ row }"><el-tag :type="row.needTransfer ? 'danger' : 'info'" size="small">{{ row.needTransfer ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="editIntent(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="deleteIntent(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑意图弹窗 -->
    <el-dialog v-model="showAddDialog" :title="editingItem ? '编辑意图' : '新增意图'" width="500">
      <el-form :model="formData" label-width="90px">
        <el-form-item label="意图名称"><el-input v-model="formData.intentName" /></el-form-item>
        <el-form-item label="意图编码"><el-input v-model="formData.intentCode" placeholder="如 query_order" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formData.category"><el-option label="售后" value="after_sale" /><el-option label="升级" value="escalation" /><el-option label="售前" value="pre_sale" /><el-option label="咨询" value="inquiry" /></el-select>
        </el-form-item>
        <el-form-item label="转人工"><el-switch v-model="formData.needTransfer" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="回复模板"><el-input v-model="formData.responseTemplate" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveIntent">保存</el-button>
      </template>
    </el-dialog>

    <!-- 样本管理弹窗 -->
    <el-dialog v-model="showSamplesDialog" :title="`样本管理 - ${currentIntent?.intentName || ''}`" width="520">
      <div class="samples-wrap">
        <div class="sample-list">
          <div v-for="(s, i) in editableSamples" :key="i" class="sample-item">
            <el-input v-model="editableSamples[i]" size="small" style="flex:1" />
            <el-button type="danger" text size="small" @click="editableSamples.splice(i, 1)">删除</el-button>
          </div>
          <div v-if="editableSamples.length === 0" style="color:#909399;font-size:13px;padding:8px 0">暂无样本</div>
        </div>
        <div style="display:flex;gap:8px;margin-top:12px">
          <el-input v-model="newSampleText" placeholder="输入新样本..." size="small" style="flex:1" @keyup.enter="addSample" />
          <el-button type="primary" size="small" @click="addSample">添加</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="showSamplesDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSamples">保存</el-button>
      </template>
    </el-dialog>

    <!-- 测试对话弹窗 -->
    <el-dialog v-model="showTestDialog" title="🧪 意图测试" width="500">
      <div class="test-chat">
        <div class="test-messages" ref="testMsgBox">
          <div v-for="(msg, i) in testMessages" :key="i" :class="['test-msg', msg.role]">
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
          <div v-if="testMessages.length === 0" style="color:#909399;text-align:center;padding:30px 0;font-size:13px">
            输入一句话，AI将识别意图并回复
          </div>
        </div>
        <div style="display:flex;gap:8px;margin-top:12px">
          <el-input v-model="testInput" placeholder="输入测试语句..." size="small" @keyup.enter="sendTest" />
          <el-button type="primary" size="small" @click="sendTest">发送</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'

const categoryMap = { after_sale: '售后', escalation: '升级', inquiry: '咨询', sales: '销售', pre_sale: '售前' }
const intents = ref([])
const showAddDialog = ref(false)
const editingItem = ref(null)
const formData = ref({ intentName: '', intentCode: '', category: 'inquiry', needTransfer: 0, responseTemplate: '', status: 1 })

// 样本管理
const showSamplesDialog = ref(false)
const currentIntent = ref(null)
const editableSamples = ref([])
const newSampleText = ref('')

// 测试对话
const showTestDialog = ref(false)
const testInput = ref('')
const testMessages = ref([])
const testMsgBox = ref(null)

function parseSamples(samples) {
  try { return JSON.parse(samples || '[]') } catch { return [] }
}

function openAddDialog() {
  editingItem.value = null
  formData.value = { intentName: '', intentCode: '', category: 'inquiry', needTransfer: 0, responseTemplate: '', status: 1 }
  showAddDialog.value = true
}

function editIntent(row) { editingItem.value = row; formData.value = { ...row }; showAddDialog.value = true }

async function saveIntent() {
  if (!formData.value.intentName) { ElMessage.warning('请填写意图名称'); return }
  try {
    if (editingItem.value) { await adminApi.updateIntent({ id: editingItem.value.id, ...formData.value }); ElMessage.success('更新成功') }
    else { await adminApi.addIntent(formData.value); ElMessage.success('新增成功') }
    showAddDialog.value = false; editingItem.value = null
    loadIntents()
  } catch (e) { ElMessage.error('操作失败') }
}

async function deleteIntent(id) {
  try { await ElMessageBox.confirm('确定删除该意图？', '提示', { type: 'warning' }); await adminApi.deleteIntent(id); ElMessage.success('删除成功'); loadIntents() } catch (e) {}
}

function openSamplesDialog(row) {
  currentIntent.value = row
  editableSamples.value = [...parseSamples(row.samples)]
  newSampleText.value = ''
  showSamplesDialog.value = true
}

function addSample() {
  if (!newSampleText.value.trim()) return
  editableSamples.value.push(newSampleText.value.trim())
  newSampleText.value = ''
}

async function saveSamples() {
  try {
    await adminApi.updateIntent({ id: currentIntent.value.id, samples: JSON.stringify(editableSamples.value) })
    ElMessage.success('样本已更新')
    showSamplesDialog.value = false
    loadIntents()
  } catch (e) { ElMessage.error('保存失败') }
}

function openTestDialog() {
  testMessages.value = []
  testInput.value = ''
  showTestDialog.value = true
}

async function sendTest() {
  const text = testInput.value.trim()
  if (!text) return
  testMessages.value.push({ role: 'user', content: text })
  testInput.value = ''
  await nextTick()
  if (testMsgBox.value) testMsgBox.value.scrollTop = testMsgBox.value.scrollHeight

  // Mock意图识别：简单关键词匹配
  setTimeout(() => {
    let matched = null
    for (const intent of intents.value) {
      const samples = parseSamples(intent.samples)
      if (samples.some(s => text.includes(s) || s.includes(text))) {
        matched = intent; break
      }
    }
    if (matched) {
      testMessages.value.push({ role: 'bot', content: `🎯 识别意图：${matched.intentName}（${matched.intentCode}）\n💬 回复：${matched.responseTemplate}` })
    } else {
      testMessages.value.push({ role: 'bot', content: '❓ 未匹配到任何意图，建议添加对应样本' })
    }
    nextTick(() => { if (testMsgBox.value) testMsgBox.value.scrollTop = testMsgBox.value.scrollHeight })
  }, 500)
}

async function loadIntents() { try { const res = await adminApi.listIntents({ page: 1, size: 50 }); intents.value = res.records || res || [] } catch (e) {} }

onMounted(() => { loadIntents() })
</script>

<style scoped>
.page-wrap {}
.page-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.page-actions { display: flex; gap: 10px; }
.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }
.code-tag { background: #F2F3F5; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: var(--primary); font-family: monospace; }

.samples-wrap {}
.sample-list { max-height: 300px; overflow-y: auto; }
.sample-item { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }

.test-chat {}
.test-messages { height: 300px; overflow-y: auto; background: #F7F8FA; border-radius: 10px; padding: 16px; }
.test-msg { margin-bottom: 12px; display: flex; }
.test-msg.user { justify-content: flex-end; }
.test-msg.bot { justify-content: flex-start; }
.msg-bubble {
  max-width: 80%; padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.6;
  white-space: pre-wrap;
}
.test-msg.user .msg-bubble { background: var(--primary-gradient); color: #fff; border-bottom-right-radius: 4px; }
.test-msg.bot .msg-bubble { background: #fff; color: var(--text-primary); border-bottom-left-radius: 4px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
</style>
