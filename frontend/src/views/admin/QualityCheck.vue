<template>
  <div class="quality-page">
    <div class="page-header">
      <h2 class="page-title">📋 对话质量抽查</h2>
      <div class="header-actions">
        <el-button type="primary" @click="startRandomCheck" :loading="checking">
          <el-icon><Refresh /></el-icon> 随机抽检
        </el-button>
        <el-button @click="exportReport">导出报告</el-button>
      </div>
    </div>

    <!-- 抽检配置 -->
    <div class="check-config">
      <el-form :inline="true" :model="configForm">
        <el-form-item label="抽查数量">
          <el-input-number v-model="configForm.sampleSize" :min="5" :max="100" :step="5" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="configForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item label="坐席">
          <el-select v-model="configForm.agentId" placeholder="全部坐席" clearable>
            <el-option label="人工客服 1" value="1" />
            <el-option label="人工客服 2" value="2" />
            <el-option label="人工客服 3" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 抽检结果列表 -->
    <div class="check-list" v-loading="checking">
      <div class="list-header">
        <h3>抽查结果 <span class="result-count">({{ checkedSessions.length }}/{{ configForm.sampleSize }})</span></h3>
        <div class="list-stats">
          <el-tag :type="avgScore >= 4 ? 'success' : avgScore >= 3 ? 'warning' : 'danger'" effect="dark" size="large">
            <el-icon><Star /></el-icon> 平均评分：{{ avgScore.toFixed(1) }}⭐
          </el-tag>
          <el-tag type="info" effect="plain" size="large">
            <el-icon><TrendCharts /></el-icon> 问题率：{{ issueRate }}%
          </el-tag>
          <el-tag v-if="checkedSessions.length > 0" type="success" effect="light" size="large">
            <el-icon><CircleCheck /></el-icon> 已完成
          </el-tag>
        </div>
      </div>

      <el-table :data="checkedSessions" stripe style="width: 100%">
        <el-table-column prop="sessionId" label="会话 ID" width="150" />
        <el-table-column prop="agentName" label="坐席" width="120" />
        <el-table-column prop="duration" label="时长" width="80" />
        <el-table-column prop="score" label="评分" width="100">
          <template #default="{ row }">
            <div class="score-stars">
              <span v-for="i in 5" :key="i" :class="['star', i <= row.score ? 'active' : '']">⭐</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="issues" label="问题标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.issues" :key="tag" size="small" type="danger" style="margin-right: 4px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetail" title="对话详情" width="900px">
      <div class="detail-content" v-if="currentSession">
        <div class="detail-header">
          <div class="info-row"><span>会话 ID:</span> <b>{{ currentSession.sessionId }}</b></div>
          <div class="info-row"><span>坐席:</span> <b>{{ currentSession.agentName }}</b></div>
          <div class="info-row"><span>时长:</span> <b>{{ currentSession.duration }}</b></div>
          <div class="info-row"><span>时间:</span> <b>{{ currentSession.startTime }}</b></div>
        </div>

        <div class="message-log">
          <div v-for="(msg, idx) in currentSession.messages" :key="idx" :class="['msg-bubble', msg.senderType]">
            <div class="msg-sender">{{ msg.senderName || (msg.senderType === 'user' ? '用户' : '坐席') }}</div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
        </div>

        <el-divider />

        <el-form :model="currentSession" label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="currentSession.score" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text />
          </el-form-item>
          <el-form-item label="问题标签">
            <el-checkbox-group v-model="currentSession.issues">
              <el-checkbox label="响应慢" />
              <el-checkbox label="态度差" />
              <el-checkbox label="专业度低" />
              <el-checkbox label="未解决问题" />
              <el-checkbox label="流程错误" />
              <el-checkbox label="话术不当" />
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="评语">
            <el-input v-model="currentSession.comment" type="textarea" :rows="3" placeholder="请给出具体评价和改进建议" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">取消</el-button>
        <el-button type="primary" @click="saveReview">保存评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Refresh, Star, TrendCharts, CircleCheck } from '@element-plus/icons-vue'

const checking = ref(false)
const showDetail = ref(false)
const checkedSessions = ref([])
const currentSession = ref(null)

const configForm = ref({
  sampleSize: 10,
  dateRange: [],
  agentId: ''
})

const avgScore = computed(() => {
  if (!checkedSessions.value.length) return 0
  const sum = checkedSessions.value.reduce((acc, s) => acc + (s.score || 0), 0)
  return sum / checkedSessions.value.length
})

const issueRate = computed(() => {
  if (!checkedSessions.value.length) return 0
  const withIssues = checkedSessions.value.filter(s => s.issues && s.issues.length).length
  return Math.round(withIssues / checkedSessions.value.length * 100)
})

async function startRandomCheck() {
  checking.value = true
  try {
    // Mock 随机抽检
    await new Promise(r => setTimeout(r, 1500))
    const mockData = Array.from({ length: configForm.value.sampleSize }, (_, i) => ({
      sessionId: `S${Date.now()}-${i}`,
      agentName: ['人工客服 1', '人工客服 2', '人工客服 3'][Math.floor(Math.random() * 3)],
      duration: `${Math.floor(Math.random() * 30 + 5)}m`,
      score: Math.floor(Math.random() * 3 + 3),
      issues: Math.random() > 0.7 ? ['响应慢', '未解决问题'].slice(0, Math.floor(Math.random() * 2 + 1)) : [],
      comment: Math.random() > 0.5 ? '服务态度好，但响应速度有待提升' : '',
      messages: [
        { senderType: 'user', senderName: '用户', content: '你好，我想咨询订单问题' },
        { senderType: 'agent', senderName: '坐席', content: '您好，请提供订单号' },
        { senderType: 'user', senderName: '用户', content: '订单号是 123456' },
        { senderType: 'agent', senderName: '坐席', content: '好的，我帮您查询，请稍等' }
      ],
      startTime: new Date().toLocaleString()
    }))
    checkedSessions.value = mockData
    ElMessage.success(`完成 ${mockData.length} 条会话抽检`)
  } catch (e) {
    ElMessage.error('抽检失败')
  } finally {
    checking.value = false
  }
}

function viewDetail(row) {
  currentSession.value = { ...row }
  showDetail.value = true
}

function saveReview() {
  ElMessage.success('评价已保存')
  showDetail.value = false
}

function exportReport() {
  ElMessage.info('报告导出功能开发中')
}

onMounted(() => {
  // 可加载历史抽检记录
})
</script>

<style scoped>
.quality-page { padding: 24px; animation: fadeInUp 0.35s ease; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--text-primary); display: flex; align-items: center; gap: 8px; }
.result-count { font-size: 14px; color: var(--text-muted); font-weight: 400; }
.header-actions { display: flex; gap: 8px; }

.check-config { background: #fff; padding: 20px; border-radius: var(--radius-lg); margin-bottom: 20px; box-shadow: var(--shadow-card); border: 1px solid rgba(79, 110, 247, 0.1); }

.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #F0F2F5; }
.list-header h3 { font-size: 16px; font-weight: 600; color: var(--text-primary); display: flex; align-items: center; gap: 8px; }
.list-stats { display: flex; gap: 10px; align-items: center; }

.score-stars { display: flex; gap: 2px; }
.star { opacity: 0.3; transition: all 0.2s; }
.star.active { opacity: 1; transform: scale(1.1); }

.detail-content { max-height: 600px; overflow-y: auto; padding: 4px; }
.detail-header { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 20px; padding: 16px; background: linear-gradient(135deg, #F5F7FA 0%, #E8EDFF 100%); border-radius: 12px; border: 1px solid rgba(79, 110, 247, 0.15); }
.info-row { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 6px; }
.info-row b { color: var(--text-primary); font-weight: 600; }

.message-log { max-height: 300px; overflow-y: auto; padding: 16px; background: linear-gradient(135deg, #FAFBFC 0%, #F5F7FA 100%); border-radius: 12px; margin-bottom: 16px; border: 1px solid rgba(79, 110, 247, 0.08); }
.msg-bubble { padding: 10px 14px; margin-bottom: 10px; border-radius: 12px; max-width: 80%; animation: msgSlideIn 0.3s ease; position: relative; transition: all 0.2s; }
.msg-bubble:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.msg-bubble.user { background: linear-gradient(135deg, #4F6EF7 0%, #7B5EF7 100%); color: #fff; margin-left: auto; box-shadow: 0 2px 8px rgba(79, 110, 247, 0.3); }
.msg-bubble.agent { background: linear-gradient(135deg, #E8FFEA 0%, #D0F5E0 100%); color: var(--text-primary); box-shadow: 0 2px 8px rgba(0, 180, 42, 0.15); }
.msg-sender { font-size: 11px; opacity: 0.8; margin-bottom: 4px; font-weight: 500; }
.msg-content { font-size: 13px; line-height: 1.6; }

@keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes msgSlideIn { from { opacity: 0; transform: translateX(-10px); } to { opacity: 1; transform: translateX(0); } }
</style>