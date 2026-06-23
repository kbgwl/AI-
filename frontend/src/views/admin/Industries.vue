<template>
  <div class="page-wrap">
    <div class="page-top">
      <h2 class="page-title">🏢 行业管理</h2>
      <div class="page-actions">
        <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增行业</el-button>
      </div>
    </div>

    <!-- 行业列表 -->
    <div class="table-card">
      <el-table :data="industries" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="industryName" label="行业名称" min-width="150" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <span style="font-size: 24px">{{ row.icon }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="editItem(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="deleteItem(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editingItem ? '编辑行业' : '新增行业'" width="560">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="行业名称">
          <el-input v-model="formData.industryName" placeholder="如：电商零售" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="如：🛒" style="width: 100px" />
          <span style="margin-left: 10px; color: #999">建议使用 emoji</span>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="行业描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { kbApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const industries = ref([])
const showDialog = ref(false)
const editingItem = ref(null)
const formData = ref({
  industryName: '',
  icon: '🏢',
  description: '',
  sortOrder: 0,
  status: 1
})

async function loadIndustries() {
  try {
    const res = await kbApi.listIndustries()
    industries.value = res || []
  } catch (e) {
    ElMessage.error('加载行业列表失败')
  }
}

function openAddDialog() {
  editingItem.value = null
  formData.value = {
    industryName: '',
    icon: '🏢',
    description: '',
    sortOrder: 0,
    status: 1
  }
  showDialog.value = true
}

function editItem(row) {
  editingItem.value = row
  formData.value = { ...row }
  showDialog.value = true
}

async function saveItem() {
  if (!formData.value.industryName) {
    ElMessage.warning('请填写行业名称')
    return
  }
  try {
    if (editingItem.value) {
      await kbApi.updateIndustry(formData.value)
      ElMessage.success('更新成功')
    } else {
      await kbApi.addIndustry(formData.value)
      ElMessage.success('新增成功')
    }
    showDialog.value = false
    loadIndustries()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function deleteItem(id) {
  try {
    await ElMessageBox.confirm('确定删除该行业？删除后该行业下的知识库仍然保留', '提示', { type: 'warning' })
    await kbApi.deleteIndustry(id)
    ElMessage.success('删除成功')
    loadIndustries()
  } catch (e) {}
}

onMounted(() => {
  loadIndustries()
})
</script>

<style scoped>
.page-wrap {
  padding: 20px;
}
.page-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.page-actions {
  display: flex;
  gap: 10px;
}
.table-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 4px;
  box-shadow: var(--shadow-sm);
}
</style>
