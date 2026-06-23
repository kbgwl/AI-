<template>
  <div class="industry-manage">
    <el-card class="header-card">
      <div class="header-content">
        <h2>🏢 行业分类管理</h2>
        <p class="desc">为不同行业客户提供定制化的 AI 客服解决方案</p>
      </div>
    </el-card>

    <!-- 行业列表 -->
    <el-card class="list-card">
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索行业名称..."
          style="width: 200px"
          clearable
        />
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i> 新增行业
        </el-button>
      </div>

      <el-table :data="industries" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="icon" label="图标" width="80" align="center" />
        <el-table-column prop="industryName" label="行业名称" width="150" />
        <el-table-column prop="industryCode" label="行业编码" width="150" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button size="small" type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="text" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="text" danger @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增行业' : '编辑行业'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="行业名称" required>
          <el-input v-model="form.industryName" placeholder="如：电商零售" />
        </el-form-item>
        <el-form-item label="行业编码" required>
          <el-input v-model="form.industryCode" placeholder="如：ecommerce" />
        </el-form-item>
        <el-form-item label="图标" required>
          <el-input v-model="form.icon" placeholder="如：🛒" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="行业描述..."
            rows="3"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const industries = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('add')

const form = reactive({
  id: null,
  industryName: '',
  industryCode: '',
  icon: '🏢',
  description: '',
  sortOrder: 0,
  status: 1
})

// 获取行业列表
const loadIndustries = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/industry/active')
    if (res.data.code === 200) {
      industries.value = res.data.data || []
    }
  } catch (e) {
    // 如果 API 失败，使用预设数据
    industries.value = [
      { id: 1, industryName: '电商零售', industryCode: 'ecommerce', icon: '🛒', description: '电商平台、零售商城、品牌官网等', sortOrder: 1, status: 1 },
      { id: 2, industryName: '教育培训', industryCode: 'education', icon: '📚', description: 'K12 教育、职业培训、知识付费等', sortOrder: 2, status: 1 },
      { id: 3, industryName: '医疗健康', industryCode: 'healthcare', icon: '🏥', description: '医院、诊所、健康管理、医药电商等', sortOrder: 3, status: 1 },
      { id: 4, industryName: '金融服务', industryCode: 'finance', icon: '💰', description: '银行、保险、证券、理财等', sortOrder: 4, status: 1 },
      { id: 5, industryName: '生活服务', industryCode: 'lifestyle', icon: '🏠', description: '餐饮、酒店、旅游、家政等', sortOrder: 5, status: 1 },
      { id: 6, industryName: '企业服务', industryCode: 'enterprise', icon: '💼', description: 'SaaS、咨询、法律、人力资源等', sortOrder: 6, status: 1 },
      { id: 7, industryName: '科技互联网', industryCode: 'technology', icon: '💻', description: '软件开发、人工智能、云计算等', sortOrder: 7, status: 1 },
      { id: 8, industryName: '制造业', industryCode: 'manufacturing', icon: '🏭', description: '工业制造、生产加工、供应链等', sortOrder: 8, status: 1 },
      { id: 9, industryName: '文化传媒', industryCode: 'media', icon: '🎬', description: '媒体出版、广告营销、娱乐影视等', sortOrder: 9, status: 1 },
      { id: 10, industryName: '其他行业', industryCode: 'other', icon: '📁', description: '其他未分类行业', sortOrder: 10, status: 1 }
    ]
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(form, { id: null, industryName: '', industryCode: '', icon: '🏢', description: '', sortOrder: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleToggleStatus = (row) => {
  row.status = row.status === 1 ? 0 : 1
  ElMessage.success(`已${row.status === 1 ? '启用' : '禁用'} ${row.industryName}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除行业 "${row.industryName}" 吗？`, '确认删除')
    industries.value = industries.value.filter(item => item.id !== row.id)
    ElMessage.success('删除成功')
  } catch {}
}

const handleSubmit = () => {
  if (!form.industryName || !form.industryCode) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (dialogMode.value === 'add') {
    form.id = Date.now()
    industries.value.push({ ...form })
    ElMessage.success('新增成功')
  } else {
    const index = industries.value.findIndex(item => item.id === form.id)
    if (index !== -1) {
      industries.value[index] = { ...form }
      ElMessage.success('更新成功')
    }
  }
  dialogVisible.value = false
}

onMounted(() => {
  loadIndustries()
})
</script>

<style scoped>
.industry-manage {
  padding: 20px;
}
.header-card {
  margin-bottom: 20px;
}
.header-content h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}
.desc {
  margin: 0;
  color: #666;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
