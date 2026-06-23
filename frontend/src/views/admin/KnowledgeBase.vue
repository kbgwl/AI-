<template>
 <div class="page-wrap">
 <div class="page-top">
 <h2 class="page-title">📚 知识库管理</h2>
 <div class="page-actions">
 <el-select v-model="selectedIndustry" placeholder="选择行业" style="width:150px;margin-right:10px" clearable>
 <el-option label="全部行业" value="" />
 <el-option v-for="ind in industries" :key="ind.id" :label="ind.industryName" :value="ind.id" />
 </el-select>
 <el-input v-model="searchKey" placeholder="搜索知识条目..." prefix-icon="Search" style="width:220px" clearable />
 <el-button type="success" plain @click="showImportDialog = true"><el-icon><Upload /></el-icon> 批量导入</el-button>
 <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增条目</el-button>
 </div>
 </div>

    <!-- 主Tab切换：知识条目 / 未知问题 -->
    <el-tabs v-model="activeMainTab" class="main-tabs">
      <el-tab-pane label="知识条目" name="items">
        <!-- 分类Tab -->
        <div class="category-tabs">
          <span :class="['tab-item', { active: activeCategory === 0 }]" @click="activeCategory = 0">全部</span>
          <span v-for="cat in categories" :key="cat.id" :class="['tab-item', { active: activeCategory === cat.id }]" @click="activeCategory = cat.id">
            {{ cat.icon }} {{ cat.name }}
          </span>
          <span class="tab-item add-cat" @click="showCatDialog = true">+ 新增分类</span>
        </div>

        <!-- 表格 -->
        <div class="table-card">
          <el-table :data="pagedItems" stripe style="width:100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="question" label="问题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="answer" label="答案" min-width="240" show-overflow-tooltip />
            <el-table-column prop="itemType" label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="row.itemType === 'faq' ? 'primary' : 'warning'" size="small">{{ row.itemType === 'faq' ? 'FAQ' : '指引' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="hitCount" label="命中次数" width="90" sortable />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="审核" width="90">
        <template #default="{ row }">
          <el-tag :type="reviewTagType(row.reviewStatus)" size="small">{{ reviewStatusMap[row.reviewStatus] || '待审核' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="70">
        <template #default="{ row }">
          <span style="color:#909399;font-size:12px">v{{ row.version || 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="editItem(row)">编辑</el-button>
          <el-button v-if="row.reviewStatus !== 'approved'" type="success" text size="small" @click="reviewItem(row, 'approved')">通过</el-button>
          <el-button v-if="row.reviewStatus !== 'approved'" type="warning" text size="small" @click="reviewItem(row, 'rejected')">驳回</el-button>
          <el-button type="info" text size="small" @click="showVersionHistory(row)">版本</el-button>
          <el-button type="danger" text size="small" @click="deleteItem(row.id)">删除</el-button>
        </template>
      </el-table-column>
          </el-table>
          <!-- 分页 -->
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="filteredItems.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
              small
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 未知问题Tab -->
      <el-tab-pane label="未知问题" name="unknown">
        <div class="unknown-header">
          <span class="unknown-tip">💡 用户提问但AI无法回答的问题，可补充到知识库</span>
        </div>
        <div class="table-card">
          <el-table :data="unknownList" stripe style="width:100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="question" label="未知问题" min-width="300" />
            <el-table-column prop="hitCount" label="提问次数" width="100" sortable />
            <el-table-column prop="createTime" label="首次提问" width="170" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" text size="small" @click="answerUnknown(row)">补充回答</el-button>
                <el-button type="info" text size="small" @click="ignoreUnknown(row.id)">忽略</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑知识条目弹窗 -->
    <el-dialog v-model="showAddDialog" :title="editingItem ? '编辑知识条目' : '新增知识条目'" width="560">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="问题"><el-input v-model="formData.question" placeholder="输入常见问题" /></el-form-item>
        <el-form-item label="答案"><el-input v-model="formData.answer" type="textarea" :rows="4" placeholder="输入回答内容" /></el-form-item>
 <el-form-item label="分类">
 <el-select v-model="formData.categoryId" placeholder="选择分类">
 <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
 </el-select>
 </el-form-item>
 <el-form-item label="行业">
 <el-select v-model="formData.industryId" placeholder="选择行业" clearable>
 <el-option v-for="ind in industries" :key="ind.id" :label="ind.industryName" :value="ind.id" />
 </el-select>
 </el-form-item>
 <el-form-item label="类型">
          <el-radio-group v-model="formData.itemType"><el-radio value="faq">FAQ</el-radio><el-radio value="guide">指引</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增分类弹窗 -->
    <el-dialog v-model="showCatDialog" title="新增知识分类" width="420">
      <el-form :model="catForm" label-width="80px">
        <el-form-item label="分类名"><el-input v-model="catForm.name" placeholder="如：售后问题" /></el-form-item>
        <el-form-item label="图标">
          <el-select v-model="catForm.icon" placeholder="选择图标">
            <el-option v-for="ic in catIcons" :key="ic" :label="ic" :value="ic" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCatDialog = false">取消</el-button>
        <el-button type="primary" @click="addCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="showImportDialog" title="批量导入知识条目" width="500">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #title>
          支持 .xlsx / .csv 格式，表头：问题 | 答案 | 分类ID | 类型(faq/guide)
        </template>
      </el-alert>
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        accept=".xlsx,.csv,.xls"
      >
        <el-icon style="font-size:40px;color:#C0C4CC"><UploadFilled /></el-icon>
        <div style="color:#606266">将文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="doImport" :disabled="!importFile">确认导入</el-button>
      </template>
    </el-dialog>

  <!-- 补充回答弹窗 -->
  <el-dialog v-model="showAnswerDialog" title="补充回答" width="560">
    <el-form :model="answerForm" label-width="80px">
    <el-form-item label="问题"><el-input :model-value="answerForm.question" disabled /></el-form-item>
    <el-form-item label="答案"><el-input v-model="answerForm.answer" type="textarea" :rows="4" placeholder="输入回答内容" /></el-form-item>
    <el-form-item label="分类">
    <el-select v-model="answerForm.categoryId" placeholder="选择分类">
    <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
    </el-select>
    </el-form-item>
    </el-form>
    <template #footer>
    <el-button @click="showAnswerDialog = false">取消</el-button>
    <el-button type="primary" @click="saveAnswer">保存到知识库</el-button>
    </template>
  </el-dialog>

  <!-- 版本历史抽屉 -->
  <el-drawer v-model="showVersionDrawer" title="📋 版本历史" size="420">
    <div v-if="versionHistory.length === 0" style="color:#909399;text-align:center;padding:40px 0">暂无版本记录</div>
    <el-timeline v-else>
      <el-timeline-item v-for="ver in versionHistory" :key="ver.version"
        :timestamp="ver.createTime" placement="top"
        :type="ver.version === currentVersionItem?.version ? 'primary' : 'info'">
        <div class="ver-card">
          <div class="ver-header">
            <span class="ver-tag">v{{ ver.version }}</span>
            <el-tag v-if="ver.version === currentVersionItem?.version" type="success" size="small">当前</el-tag>
          </div>
          <div class="ver-question"><strong>问题：</strong>{{ ver.question }}</div>
          <div class="ver-answer"><strong>答案：</strong>{{ ver.answer?.substring(0, 100) }}{{ ver.answer?.length > 100 ? '...' : '' }}</div>
          <div class="ver-meta">
            <span>{{ ver.operator || '系统' }}</span>
            <span v-if="ver.reviewNote"> · {{ ver.reviewNote }}</span>
          </div>
          <el-button v-if="ver.version !== currentVersionItem?.version" type="primary" text size="small" @click="rollbackVersion(ver)" style="margin-top:8px">回滚到此版本</el-button>
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { kbApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'

const categories = ref([])
const items = ref([])
const industries = ref([])
const selectedIndustry = ref()
const searchKey = ref('')
const activeCategory = ref(0)
const activeMainTab = ref('items')
const showAddDialog = ref(false)
const showCatDialog = ref(false)
const showImportDialog = ref(false)
const showAnswerDialog = ref(false)
const editingItem = ref(null)
const importFile = ref(null)
const unknownList = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

const formData = ref({ question: '', answer: '', categoryId: 1, industryId: null, itemType: 'faq', status: 1, priority: 10 })
const catForm = ref({ name: '', icon: '📁' })
const answerForm = ref({ id: 0, question: '', answer: '', categoryId: 1, industryId: null })

const catIcons = ['📁', '🛒', '💰', '🔧', '📦', '🎯', '📋', '💡', '🔑', '🌐']

// ========== 审核机制 ==========
const reviewStatusMap = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
function reviewTagType(status) {
  const map = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return map[status] || 'warning'
}

function reviewItem(row, status) {
  const label = status === 'approved' ? '通过' : '驳回'
  ElMessageBox.confirm(`确定${label}该知识条目？`, '审核确认', {
    type: status === 'approved' ? 'success' : 'warning',
    confirmButtonText: label,
    cancelButtonText: '取消',
    inputPattern: status === 'rejected' ? /.+/ : null,
    inputErrorMessage: '请填写驳回原因',
    inputPlaceholder: status === 'rejected' ? '请输入驳回原因' : ''
  }).then(({ value }) => {
    row.reviewStatus = status
    row.reviewNote = value || (status === 'approved' ? '审核通过' : '审核驳回')
    row.reviewTime = new Date().toLocaleString('zh-CN', { hour12: false })
    ElMessage.success(`已${label}`)
  }).catch(() => {})
}

// ========== 版本管理 ==========
const showVersionDrawer = ref(false)
const currentVersionItem = ref(null)
const versionHistory = ref([])

function showVersionHistory(row) {
  currentVersionItem.value = row
  // 模拟版本历史：当前版本 + 之前版本
  const current = row.version || 1
  const history = []
  for (let v = 1; v <= current; v++) {
    history.push({
      version: v,
      question: v === current ? row.question : row.question + '（旧版）',
      answer: v === current ? row.answer : '（历史版本内容，当前版本已更新）',
      operator: v === 1 ? '管理员' : '管理员',
      reviewNote: v === current ? row.reviewNote || '编辑保存' : '初始创建',
      createTime: row.createTime || new Date().toLocaleString('zh-CN', { hour12: false })
    })
  }
  versionHistory.value = history.reverse()
  showVersionDrawer.value = true
}

function rollbackVersion(ver) {
  ElMessageBox.confirm(`确定回滚到 v${ver.version}？当前版本将被覆盖。`, '回滚确认', { type: 'warning' }).then(() => {
    if (currentVersionItem.value) {
      currentVersionItem.value.version = (currentVersionItem.value.version || 1) + 1
      currentVersionItem.value.answer = ver.answer
      currentVersionItem.value.reviewStatus = 'pending'
    }
    ElMessage.success('已回滚，新版本待审核')
    showVersionDrawer.value = false
  }).catch(() => {})
}

const filteredItems = computed(() => {
 let list = items.value
 if (activeCategory.value > 0) list = list.filter(i => i.categoryId === activeCategory.value)
 if (selectedIndustry.value) list = list.filter(i => i.industryId === selectedIndustry.value)
 if (searchKey.value) list = list.filter(i => i.question.includes(searchKey.value) || i.answer.includes(searchKey.value))
 return list
})

const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredItems.value.slice(start, start + pageSize.value)
})

function openAddDialog() {
 editingItem.value = null
 formData.value = { question: '', answer: '', categoryId: activeCategory.value || 1, industryId: null, itemType: 'faq', status: 1, priority: 10 }
 showAddDialog.value = true
}

function editItem(row) {
 editingItem.value = row
 formData.value = { ...row, industryId: row.industryId || null }
 showAddDialog.value = true
}

async function saveItem() {
  if (!formData.value.question || !formData.value.answer) { ElMessage.warning('请填写问题和答案'); return }
  try {
    if (editingItem.value) {
      await kbApi.updateItem({ id: editingItem.value.id, ...formData.value })
      ElMessage.success('更新成功')
    } else {
      await kbApi.addItem(formData.value)
      ElMessage.success('新增成功')
    }
    showAddDialog.value = false
    editingItem.value = null
    loadItems()
  } catch (e) { ElMessage.error('操作失败') }
}

async function deleteItem(id) {
  try {
    await ElMessageBox.confirm('确定删除该知识条目？', '提示', { type: 'warning' })
    await kbApi.deleteItem(id)
    ElMessage.success('删除成功')
    loadItems()
  } catch (e) {}
}

async function addCategory() {
  if (!catForm.value.name) { ElMessage.warning('请输入分类名'); return }
  try {
    await kbApi.addCategory(catForm.value)
    ElMessage.success('分类添加成功')
    showCatDialog.value = false
    catForm.value = { name: '', icon: '📁' }
    loadCategories()
  } catch (e) { ElMessage.error('添加失败') }
}

function handleFileChange(file) {
  importFile.value = file.raw
}

async function doImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  const file = importFile.value
  const reader = new FileReader()
  
  reader.onload = async (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const sheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[sheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet)
      
      if (jsonData.length === 0) {
        ElMessage.warning('文件为空，请检查内容')
        return
      }
      
      // 验证和转换数据
      const importData = jsonData.map((row, idx) => ({
        id: Date.now() + idx,
        question: row['问题'] || row['Question'] || row['question'] || '',
        answer: row['答案'] || row['Answer'] || row['answer'] || '',
        categoryId: row['分类 ID'] || row['CategoryId'] || row['category_id'] || activeCategory.value || 1,
        itemType: (row['类型'] || row['Type'] || row['type'] || 'faq') === 'guide' ? 'guide' : 'faq',
        status: 1,
        priority: 10
      }))
      
      // 验证必填字段
      const invalidItems = importData.filter(item => !item.question || !item.answer)
      if (invalidItems.length > 0) {
        ElMessage.warning(`有 ${invalidItems.length} 条数据缺少问题或答案，已跳过`)
      }
      
      const validData = importData.filter(item => item.question && item.answer)
      
      if (validData.length === 0) {
        ElMessage.error('没有可导入的有效数据')
        return
      }
      
      // 批量导入
      const loading = ElMessage.loading(`正在导入 ${validData.length} 条数据...`)
      let successCount = 0
      let failCount = 0
      
      for (const item of validData) {
        try {
          await kbApi.addItem(item)
          successCount++
        } catch (e) {
          failCount++
        }
      }
      
      loading.close()
      ElMessage.success(`导入完成！成功 ${successCount} 条，失败 ${failCount} 条`)
      showImportDialog.value = false
      importFile.value = null
      loadItems()
      
    } catch (e) {
      console.error(e)
      ElMessage.error('文件解析失败，请检查文件格式')
    }
  }
  
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  
  reader.readAsArrayBuffer(file)
}

function answerUnknown(row) {
  answerForm.value = { id: row.id, question: row.question, answer: '', categoryId: 1 }
  showAnswerDialog.value = true
}

async function saveAnswer() {
  if (!answerForm.value.answer) { ElMessage.warning('请填写答案'); return }
  try {
    await kbApi.answerUnknown(answerForm.value.id, { question: answerForm.value.question, answer: answerForm.value.answer, categoryId: answerForm.value.categoryId })
    ElMessage.success('已补充到知识库')
    showAnswerDialog.value = false
    loadItems()
    loadUnknown()
  } catch (e) { ElMessage.error('操作失败') }
}

async function ignoreUnknown(id) {
  try {
    await ElMessageBox.confirm('确定忽略该未知问题？', '提示', { type: 'info' })
    await kbApi.ignoreUnknown(id)
    ElMessage.success('已忽略')
    loadUnknown()
  } catch (e) {}
}

async function loadItems() {
  try { const res = await kbApi.listItems({ page: 1, size: 50 }); items.value = res.records || res || [] } catch (e) {}
}

async function loadCategories() {
  try { categories.value = await kbApi.listCategories() } catch (e) {}
}

async function loadIndustries() {
  try { industries.value = await kbApi.listIndustries() } catch (e) {}
}

async function loadUnknown() {
  try {
    const res = await kbApi.listUnknown({ page: 1, size: 50 })
    unknownList.value = res.records || []
  } catch (e) {}
}

onMounted(() => { loadIndustries(); loadCategories(); loadItems(); loadUnknown() })
</script>

<style scoped>
.page-wrap {}
.page-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.page-actions { display: flex; gap: 10px; }

.main-tabs :deep(.el-tabs__item) { font-size: 14px; font-weight: 500; }
.main-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }

.category-tabs { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.tab-item {
  padding: 6px 16px; border-radius: 20px; font-size: 13px; cursor: pointer;
  background: #fff; color: var(--text-secondary); border: 1px solid var(--border); transition: all 0.2s;
}
.tab-item:hover { border-color: var(--primary); color: var(--primary); }
.tab-item.active { background: var(--primary-gradient); color: #fff; border-color: transparent; }
.tab-item.add-cat { border-style: dashed; color: var(--primary); border-color: var(--primary); opacity: 0.7; }
.tab-item.add-cat:hover { opacity: 1; }

.table-card { background: #fff; border-radius: var(--radius-lg); padding: 4px; box-shadow: var(--shadow-sm); }

.pagination-wrap { padding: 12px 16px; display: flex; justify-content: flex-end; }

.unknown-header { margin-bottom: 12px; }
.unknown-tip { font-size: 13px; color: var(--text-muted); background: #FFF7E8; padding: 8px 16px; border-radius: 8px; display: inline-block; }

/* ========== 版本历史 ========== */
.ver-card { background: #f5f7fa; border-radius: 8px; padding: 12px 16px; }
.ver-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.ver-tag { background: var(--primary-gradient, linear-gradient(135deg, #667eea, #764ba2)); color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.ver-question { font-size: 13px; margin-bottom: 4px; }
.ver-answer { font-size: 12px; color: #606266; margin-bottom: 6px; }
.ver-meta { font-size: 11px; color: #909399; }
</style>
