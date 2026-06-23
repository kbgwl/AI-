<template>
  <div class="chat-page">
    <!-- 聊天主区域 -->
    <div class="chat-main">
      <!-- 顶部品牌栏 -->
      <div class="chat-header">
        <div class="header-brand">
          <div class="brand-logo">🤖</div>
          <div class="brand-text">
            <span class="brand-name">空白格·AI 客服</span>
            <span class="brand-status">
              <i class="status-dot" :class="{ online: wsConnected }"></i>
              {{ wsConnected ? '在线' : '连接中...' }}
            </span>
          </div>
        </div>
        <div class="header-actions">
          <button class="tool-btn" @click="handleCloseSession" :disabled="!sessionId" style="color:#fff;">
            <el-icon><Phone /></el-icon> <span class="btn-text">结束</span>
          </button>
        </div>
      </div>
      
      <!-- 模式切换栏 -->
      <div class="mode-bar">
        <div class="mode-switch">
          <span class="switch-label">模式：</span>
          <el-switch
            v-model="useRobotMode"
            active-text="机器人"
            inactive-text="人工"
            size="small"
            @change="toggleRobotMode"
          />
          <span class="switch-label" style="margin-left: 16px;">角色：</span>
          <el-switch
            v-model="isAgentMode"
            active-text="坐席"
            inactive-text="用户"
            size="small"
          />
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎区域 -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-avatar">🤖</div>
          <h2 class="welcome-title">{{ welcomeMsg }}</h2>
          <div class="faq-tags">
            <div v-for="item in faqMenu" :key="item.value" class="faq-tag" @click="sendFaq(item)">
              <span class="faq-icon">{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>

<!-- 消息列表 -->
        <div v-for="(msg, idx) in messages" :key="idx" :class="['msg-row', msg.senderType]">
          <div class="msg-avatar" :class="msg.senderType" v-if="msg.senderType !== 'system'">
            <span v-if="msg.senderType === 'user'">👤</span>
            <span v-else-if="msg.senderType === 'agent'">👩‍💼</span>
            <span v-else>🤖</span>
          </div>
          <div class="msg-body" :class="{ 'msg-body-system': msg.senderType === 'system' }">
            <div class="msg-name" v-if="msg.senderType !== 'user' && msg.senderType !== 'system'">
              {{ msg.senderType === 'bot' ? 'AI 客服' : msg.senderName || '人工客服' }}
            </div>
            <div :class="['msg-bubble', msg.senderType, { 'msg-bubble-system': msg.senderType === 'system' }]">
              <div v-if="!msg.messageType || msg.messageType === 'text' || msg.senderType === 'system'" class="msg-text" v-html="renderMarkdown(msg.content)"></div>
              <img v-else-if="msg.messageType === 'image'" :src="msg.content" class="msg-image" />
            </div>
            <div v-if="msg.senderType === 'system' && msg.csatEntry" class="msg-csat-entry">
              <button class="tool-btn" style="color:var(--primary);font-weight:500;" @click="showCsat = true">⭐ 评价服务</button>
            </div>
            <div class="msg-meta" v-if="msg.senderType !== 'system'">
              <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
              <span v-if="msg.senderType === 'bot' && !msg.feedback" class="msg-feedback">
                <button :class="['fb-btn', { active: msg.feedback === 'like' }]" @click="feedbackMessage(idx, 'like')">👍</button>
                <button :class="['fb-btn', { active: msg.feedback === 'dislike' }]" @click="feedbackMessage(idx, 'dislike')">👎</button>
              </span>
            </div>
          </div>
        </div>

        <!-- 输入中 -->
        <div v-if="typingIndicator" class="msg-row bot">
          <div class="msg-avatar bot">🤖</div>
          <div class="msg-body">
            <div class="msg-bubble bot typing-bubble">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- 转人工提示 -->
        <div v-if="transferring" class="system-hint">
          <el-icon class="is-loading"><Loading /></el-icon> 正在为您转接人工客服...
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-toolbar">
          <button class="tool-btn" @click="triggerFileUpload" :disabled="uploading">
            <el-icon><Picture /></el-icon> {{ uploading ? '上传中...' : '图片' }}
          </button>
 <button class="tool-btn" @click="handleTransferToAgent" :disabled="transferring || isAgentMode" v-if="!transferredToAgent">
 <el-icon><User /></el-icon> 转人工
 </button>
        <button class="tool-btn" @click="openTicketForm" v-if="sessionId && !isAgentMode">
          <el-icon><Tickets /></el-icon> 申请工单
        </button>
        <button class="tool-btn" @click="showCsat = true; handleCloseSession()" v-if="sessionId">
          <el-icon><Star /></el-icon> 结束
        </button>
        <button v-if="isAgentMode" class="tool-btn" @click="showQuickReplies = !showQuickReplies">
            <el-icon><ChatLineSquare /></el-icon> 快捷回复
          </button>
        </div>

        <!-- 快捷回复面板 -->
        <div v-if="showQuickReplies && isAgentMode" class="quick-panel">
          <div v-for="qr in quickReplies" :key="qr.id" class="quick-item" @click="sendMessage(qr.content)">
            <b>{{ qr.title }}</b>
            <span>{{ qr.content }}</span>
          </div>
        </div>

        <div class="input-row">
          <el-input v-model="inputText" type="textarea" :rows="2" resize="none"
            placeholder="输入消息... (Enter发送，Shift+Enter换行)"
            @keydown.enter.exact.prevent="handleSend" />
          <button class="send-btn" :disabled="!inputText.trim() || sending" @click="handleSend">
            <el-icon><Promotion /></el-icon>
          </button>
        </div>
        <!-- 实时话术推荐 (只在坐席模式下展示) -->
        <div v-if="isAgentMode && suggestions.length" class="recommendation-panel">
          <div v-for="(s, idx) in suggestions" :key="idx" class="recommendation-item" @click="sendMessage(s)">
            {{ s }}
          </div>
        </div>
        <input type="file" ref="fileInput" style="display:none" accept="image/*" @change="handleFileUpload" />
      </div>
    </div>

    <!-- 坐席辅助面板 -->
    <div class="agent-panel" v-if="isAgentMode">
      <div class="panel-title">🛠 坐席辅助</div>
      <div class="panel-content">
        <div class="panel-block">
          <div class="block-title">📋 当前服务</div>
          <div class="info-row"><span>会话ID</span><span>{{ sessionId ? sessionId.substring(0, 12) + '...' : '-' }}</span></div>
          <div class="info-row"><span>用户标签</span><span class="tag tag-blue">新用户</span></div>
          <div class="info-row"><span>会话时长</span><span>{{ sessionDuration }}</span></div>
        </div>
        <div class="panel-block">
          <div class="block-title">📚 知识库搜索</div>
          <el-input v-model="kbSearch" placeholder="搜索知识库..." size="small" @input="searchKnowledgeBase" prefix-icon="Search" />
          <div v-for="item in kbSearchResults" :key="item.id" class="kb-item" @click="sendMessage(item.answer)">
            <div class="kb-q">{{ item.question }}</div>
            <div class="kb-a">{{ item.answer?.substring(0, 50) }}...</div>
          </div>
        </div>
        <div class="panel-block">
          <div class="block-title">😊 用户情绪</div>
          <div class="emotion-bar"><div class="emotion-fill" :style="{ width: emotionScore + '%', background: emotionColor }"></div></div>
          <span class="emotion-label">{{ emotionLabel }}</span>
        </div>
        <div class="panel-block">
          <div class="block-title">📝 对话摘要</div>
          <div class="summary-text">{{ dialogSummary || '暂无摘要' }}</div>
        </div>
      </div>
    </div>

  <!-- 满意度评价 -->
  <el-dialog v-model="showCsat" title="服务评价" width="420">
    <div class="csat-body">
      <p>请对本次服务进行评价：</p>
      <el-rate v-model="csatScore" :colors="['#F53F3F', '#FF7D00', '#00B42A']" :texts="['非常不满', '不满意', '一般', '满意', '非常满意']" show-text />
      <el-input v-model="csatComment" type="textarea" :rows="3" placeholder="请输入您的建议（选填）" />
    </div>
    <template #footer>
      <el-button @click="showCsat = false">取消</el-button>
      <el-button type="primary" @click="doSubmitCsat">提交评价</el-button>
    </template>
  </el-dialog>

  <!-- 申请工单弹窗 -->
  <el-dialog 
    v-model="showTicketForm" 
    title="📝 申请工单" 
    width="90%" 
    :fullscreen="isMobile"
    :close-on-click-modal="false"
    top="5vh"
    append-to-body
  >
    <div class="ticket-form-body" :style="{ maxHeight: isMobile ? '60vh' : 'auto', overflowY: 'auto' }">
      <p style="color:var(--text-muted);font-size:13px;margin-bottom:16px;">
        如果AI客服未能解决您的问题，可以提交工单，我们会尽快安排专人处理。
      </p>
      <el-form :model="ticketForm" :rules="ticketRules" ref="ticketFormRef" label-width="80px" size="default">
        <el-form-item label="问题分类" prop="category">
          <el-select v-model="ticketForm.category" placeholder="请选择问题分类" style="width:100%">
            <el-option label="📦 订单问题" value="订单问题" />
            <el-option label="🔄 退换货" value="退换货" />
            <el-option label="💰 退款问题" value="退款问题" />
            <el-option label="💡 产品咨询" value="产品咨询" />
            <el-option label="🔧 技术支持" value="技术支持" />
            <el-option label="📢 投诉建议" value="投诉建议" />
            <el-option label="📋 其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="ticketForm.priority">
            <el-radio :value="0">一般</el-radio>
            <el-radio :value="1">紧急</el-radio>
            <el-radio :value="2">非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="主题" prop="subject">
          <el-input v-model="ticketForm.subject" placeholder="请简要描述您的问题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="ticketForm.description" type="textarea" :rows="4" placeholder="请详细描述您遇到的问题，方便我们更快处理" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="ticketForm.contact" placeholder="手机号/邮箱（选填，方便我们联系您）" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="showTicketForm = false">取消</el-button>
      <el-button type="primary" @click="doSubmitTicket" :loading="ticketSubmitting">提交工单</el-button>
    </template>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed, watch } from 'vue'
import { chatApi, kbApi, adminApi } from '../api'
import { MOCK_FIRST } from '../api'
import MarkdownIt from 'markdown-it'
import { useChat } from '../composables/useChat'
import { useRouting } from '../composables/useRouting'
import { useTicket } from '../composables/useTicket'

const md = new MarkdownIt({ html: false, breaks: true, linkify: true })

const messages = ref(JSON.parse(localStorage.getItem('ai_cs_messages') || '[]'))
const inputText = ref('')
const sessionId = ref(localStorage.getItem('ai_cs_session_id') || null)
const wsConnected = ref(false)
const isAgentMode = ref(false)
const useRobotMode = ref(true)
const transferring = ref(false)
const transferredToAgent = ref(false)
const currentAgentName = ref('人工客服')
const uploading = ref(false)
const showQuickReplies = ref(false)

// Composables
const { sending, typingIndicator, emotionScore, sendMessage: sendChatMessage } = useChat(messages, sessionId, useRobotMode, transferredToAgent, currentAgentName)
const { matchRoute } = useRouting()
const {
  showTicketForm, ticketSubmitting, ticketForm, ticketRules,
  openTicketForm, submitTicket, showCsat, csatScore, csatComment, submitCsat
} = useTicket(sessionId)
const welcomeMsg = ref('您好！欢迎使用空白格 AI 客服')
const selectedIndustryId = ref('')
const industries = ref([])
const faqMenu = ref([
 { label: '查订单', value: '我想查订单', icon: '📦' },
 { label: '申请退货', value: '我要退货', icon: '🔄' },
 { label: '产品咨询', value: '产品介绍', icon: '💡' },
 { label: '价格咨询', value: '价格多少', icon: '💰' },
 { label: '申请工单', value: '__TICKET__', icon: '🎫' },
 { label: '转人工', value: '转人工', icon: '👩‍💼' }
])
const quickReplies = ref([])
const suggestions = ref([])
const kbSearch = ref('')
const kbSearchResults = ref([])
const dialogSummary = ref('')
const sessionStartTime = ref(null)
const messagesRef = ref(null)
const fileInput = ref(null)
let ws = null
let typingTimer = null
let sessionTimer = null

watch(messages, (val) => {
  localStorage.setItem('ai_cs_messages', JSON.stringify(val))
}, { deep: true })
watch(sessionId, (val) => {
  if (val) localStorage.setItem('ai_cs_session_id', val)
})
watch(inputText, (newVal) => {
  if (isAgentMode.value && newVal.trim().length > 0) {
    const lower = newVal.toLowerCase()
    const map = {
      '订单': ['您的订单已在处理中，请稍等。', '请提供订单号以便查询。'],
      '退货': ['可以为您办理退货，请提供订单号。', '退货流程已发送至您的邮箱。'],
      '价格': ['当前产品优惠价为 ¥199，限时促销。', '您可以在官网查看最新价格。']
    }
    let res = []
    Object.entries(map).forEach(([kw, msgs]) => {
      if (lower.includes(kw)) res = res.concat(msgs)
    })
    suggestions.value = res.slice(0, 3)
  } else {
    suggestions.value = []
  }
})

const sessionDuration = computed(() => {
  if (!sessionStartTime.value) return '0:00'
  const diff = Math.floor((Date.now() - sessionStartTime.value) / 1000)
  return `${Math.floor(diff / 60)}:${(diff % 60).toString().padStart(2, '0')}`
})

const isMobile = computed(() => {
  if (typeof window === 'undefined') return false
  return window.innerWidth < 768
})

const emotionColor = computed(() => emotionScore.value >= 70 ? '#00B42A' : emotionScore.value >= 40 ? '#FF7D00' : '#F53F3F')
const emotionLabel = computed(() => emotionScore.value >= 80 ? '😊 满意' : emotionScore.value >= 60 ? '🙂 一般' : emotionScore.value >= 40 ? '😐 中性' : emotionScore.value >= 20 ? '😟 不满' : '😡 愤怒')

function renderMarkdown(text) {
  if (!text) return ''
  const processedText = text.replace(/\\n/g, '\n')
  return md.render(processedText)
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
function scrollToBottom() { nextTick(() => { if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight }) }

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value) return
  inputText.value = ''
  await sendChatMessage(text)
  scrollToBottom()
}

async function sendMessage(content, messageType = 'text') {
  await sendChatMessage(content, messageType)
  scrollToBottom()
}

async function toggleRobotMode() {
 try {
   await chatApi.toggleRobotMode({ sessionId: sessionId.value, useRobot: useRobotMode.value })
   const modeText = useRobotMode.value ? '机器人' : '人工'
   messages.value.push({ senderType: 'system', content: `已切换到${modeText}客服模式`, timestamp: Date.now() })
   
   if (useRobotMode.value) {
     // 切换回机器人模式，重置转人工状态
     transferredToAgent.value = false
     currentAgentName.value = '人工客服'
   } else {
     // 切换到人工模式，自动转接客服
     transferredToAgent.value = false
     transferring.value = true
     try {
       const res = await chatApi.transferToAgent(sessionId.value)
       if (res?.success || res?.agentName) {
         transferredToAgent.value = true
         currentAgentName.value = res.agentName || '客服'
         messages.value.push({ 
           senderType: 'system', 
           content: `已为您分配客服「${currentAgentName.value}」，请问有什么可以帮您？`, 
           timestamp: Date.now() 
         })
       } else {
         messages.value.push({ 
           senderType: 'system', 
           content: res?.message || '当前没有在线客服，请稍后再试', 
           timestamp: Date.now() 
         })
       }
     } catch (err) {
       messages.value.push({ 
         senderType: 'system', 
         content: '转接失败，请稍后再试', 
         timestamp: Date.now() 
       })
     }
     transferring.value = false
   }
   
   scrollToBottom()
 } catch (e) {
   useRobotMode.value = !useRobotMode.value
 }
}

function sendFaq(item) {
  if (item.value === '__TICKET__') { openTicketForm(); return }
  sendMessage(item.value || item.label)
}

async function handleTransferToAgent() {
  if (transferring.value || transferredToAgent.value) return
  transferring.value = true
  try {
    const { targetGroup, waitTime } = await matchRoute(messages.value)
    messages.value.push({ senderType: 'system', content: `🧭 智能路由：为您匹配到「${targetGroup}」，预计等待 ${waitTime}秒`, timestamp: Date.now() })

    const res = await chatApi.transferToAgent(sessionId.value)
    transferredToAgent.value = true
    isAgentMode.value = true
    const groupAgents = { '售后组': ['小林','小陈'], '售前组': ['小张','小李'], 'VIP组': ['王姐'], '默认组': ['小林','小张','小陈'] }
    const agents = groupAgents[targetGroup] || groupAgents['默认组']
    currentAgentName.value = res?.agentName || agents[Math.floor(Math.random() * agents.length)]
    messages.value.push({ senderType: 'system', content: '已为您转接人工客服，请稍候...', timestamp: Date.now() })
    messages.value.push({ senderType: 'system', content: '如需评价本次服务，请点击下方按钮。', csatEntry: true, timestamp: Date.now() })
    await new Promise(r => setTimeout(r, 1500))
    messages.value.push({ senderType: 'agent', senderName: currentAgentName.value, content: `您好，我是${targetGroup}的${currentAgentName.value}，请问有什么可以帮您？`, messageType: 'text', timestamp: Date.now() })
  } catch (e) {
    messages.value.push({ senderType: 'system', content: '当前暂无在线客服，请稍后再试。', timestamp: Date.now() })
  }
  transferring.value = false
  scrollToBottom()
}

async function loadIndustries() {
  try { industries.value = await kbApi.listIndustries() } catch (e) {}
}

function initIndustryFromQuery() {
  const params = new URLSearchParams(window.location.search)
  const industryId = params.get('industryId') || params.get('industry')
  if (industryId) {
    selectedIndustryId.value = industryId
  }
}

async function switchIndustry() {
 const url = new URL(window.location.href)
 if (selectedIndustryId.value) {
   url.searchParams.set('industryId', selectedIndustryId.value)
 } else {
   url.searchParams.delete('industryId')
 }
 window.history.replaceState({}, '', url.href)
 
 if (sessionId.value) {
   try { await chatApi.endSession(sessionId.value) } catch (e) {}
   sessionId.value = null
   messages.value = []
   localStorage.removeItem('ai_cs_session_id')
 }
 createSession()
}

async function createSession() {
  try {
    const params = { channel: 'web' }
    if (selectedIndustryId.value) params.industryId = selectedIndustryId.value
    const res = await chatApi.createSession(params)
    if (res?.sessionId) {
      sessionId.value = res.sessionId
      sessionStartTime.value = Date.now()
    }
    if (res?.welcomeMessage) welcomeMsg.value = res.welcomeMessage
  } catch (e) {}
}

function handleCloseSession() {
 if (sessionId.value) chatApi.endSession(sessionId.value)
 messages.value = []
 sessionId.value = null
 transferredToAgent.value = false
 isAgentMode.value = false
 currentAgentName.value = '人工客服'
 sessionStartTime.value = null
 localStorage.removeItem('ai_cs_messages')
 localStorage.removeItem('ai_cs_session_id')
}

function feedbackMessage(idx, type) { messages.value[idx].feedback = type }

async function doSubmitCsat() {
  await submitCsat(messages, scrollToBottom)
}

async function doSubmitTicket() {
  const ticketNo = await submitTicket(ticketFormRef.value)
  if (ticketNo) {
    messages.value.push({
      senderType: 'system',
      content: `✅ 工单提交成功！工单号：${ticketNo}\n我们会尽快安排专人处理，您可在后台查看工单进度。`,
      timestamp: Date.now()
    })
    scrollToBottom()
  }
}

function triggerFileUpload() { fileInput.value?.click() }
async function handleFileUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  messages.value.push({ senderType: 'user', content: URL.createObjectURL(file), messageType: 'image', timestamp: Date.now() })
  scrollToBottom()
  uploading.value = false
  e.target.value = ''
}

function toggleMode(val) {
  if (val) {
    quickReplies.value = [
      { id: 1, title: '问候', content: '您好！很高兴为您服务，请问有什么可以帮您？' },
      { id: 2, title: '查订单', content: '好的，我来帮您查询订单，请提供您的订单号。' },
      { id: 3, title: '退货指引', content: '退货流程：1.登录→我的订单 2.申请退货 3.填写原因提交' },
      { id: 4, title: '结束服务', content: '感谢您的咨询，祝您生活愉快！' }
    ]
  }
}
watch(isAgentMode, toggleMode)

let searchDebounce = null
function searchKnowledgeBase() {
 clearTimeout(searchDebounce)
 searchDebounce = setTimeout(async () => {
   if (!kbSearch.value.trim()) { kbSearchResults.value = []; return }
   try {
     const allItems = await kbApi.listItems()
     const keyword = kbSearch.value.trim().toLowerCase()
     kbSearchResults.value = (allItems || []).filter(item =>
       (item.question && item.question.toLowerCase().includes(keyword)) ||
       (item.answer && item.answer.toLowerCase().includes(keyword))
     )
   } catch (e) { kbSearchResults.value = [] }
 }, 300)
}

function connectWebSocket() {
  if (MOCK_FIRST) {
    wsConnected.value = true
    return
  }
  const wsUrl = `ws://${window.location.host}/ws/chat`
  try {
    ws = new WebSocket(wsUrl)
    ws.onopen = () => { wsConnected.value = true }
    ws.onclose = () => { wsConnected.value = false; setTimeout(connectWebSocket, 5000) }
    ws.onerror = () => { wsConnected.value = false }
    ws.onmessage = (e) => {
      try {
        const msg = JSON.parse(e.data)
        if (msg.type === 'chat') {
          typingIndicator.value = false
          let senderType = msg.senderType
          const content = msg.content || ''
          if (content.includes('智能路由') || content.includes('转接人工') || content.includes('为您匹配') || content.includes('预计等待') || content.includes('请稍候')) {
            senderType = 'system'
          }
          messages.value.push({ senderType: senderType, content: content, messageType: msg.messageType || 'text', timestamp: Date.now() })
          scrollToBottom()
        }
        if (msg.type === 'typing' && msg.senderType !== 'user') { typingIndicator.value = true; clearTimeout(typingTimer); typingTimer = setTimeout(() => { typingIndicator.value = false }, 3000) }
      } catch (e) { console.warn('WS message parse error:', e) }
    }
  } catch (e) { wsConnected.value = false }
}

// 定时拉取新消息
let messagePollTimer = null

async function pollNewMessages() {
  if (!sessionId.value) return
  
  try {
    const res = await chatApi.getSessionMessages(sessionId.value, 100)
    if (!res || !Array.isArray(res)) return
    
    for (const msg of res) {
      // 只处理客服/AI消息
      if (msg.senderType !== 'agent' && msg.senderType !== 'ai' && msg.senderType !== 'bot') continue
      
      // 简单去重：检查内容+发送者是否已存在
      const exists = messages.value.some(m => 
        m.content === msg.content && 
        m.senderType === 'agent' &&
        Math.abs((m.timestamp || 0) - new Date(msg.createTime).getTime()) < 3000
      )
      if (exists) continue
      
      messages.value.push({
        senderType: 'agent',
        senderName: msg.senderName || '客服',
        content: msg.content,
        messageType: 'text',
        timestamp: msg.createTime ? new Date(msg.createTime).getTime() : Date.now()
      })
      scrollToBottom()
    }
  } catch (e) {
    console.warn('[Poll]', e.message)
  }
}

function startMessagePolling() {
  if (messagePollTimer) return
  messagePollTimer = setInterval(pollNewMessages, 2000)
  pollNewMessages()
}

function stopMessagePolling() {
  if (messagePollTimer) { clearInterval(messagePollTimer); messagePollTimer = null }
}

watch(transferredToAgent, (val) => {
  if (val) {
    nextTick(() => startMessagePolling())
  } else {
    stopMessagePolling()
  }
})

// 也监听 useRobotMode 变化
watch(useRobotMode, (val) => {
  if (val) {
    // 机器人模式，停止轮询
    stopMessagePolling()
  } else if (transferredToAgent.value) {
    // 人工模式且已转接，启动轮询
    nextTick(() => startMessagePolling())
  }
})

onMounted(async () => {
  loadIndustries()
  initIndustryFromQuery()
  createSession()
  connectWebSocket()
  sessionTimer = setInterval(() => {}, 1000)
  // 只在人工模式且已转接时启动轮询
  if (!useRobotMode.value && transferredToAgent.value) {
    startMessagePolling()
  }
})

onBeforeUnmount(() => {
  ws?.close()
  clearTimeout(typingTimer)
  clearInterval(sessionTimer)
  stopMessagePolling()
})
</script>

<style scoped>
.chat-page { display: flex; height: 100vh; background: var(--bg); }

.chat-main { flex: 1; display: flex; flex-direction: column; max-width: 820px; margin: 0 auto; width: 100%; }

/* 顶部品牌栏 */
.chat-header {
  background: var(--primary-gradient);
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(79,110,247,0.3);
  position: relative;
  z-index: 10;
}
.header-brand { display: flex; align-items: center; gap: 12px; }
.brand-logo { 
  font-size: 28px; 
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
  animation: float 3s ease-in-out infinite;
}
.brand-name { font-size: 18px; font-weight: 600; color: #fff; display: block; }
.brand-status { font-size: 12px; color: rgba(255,255,255,0.8); display: flex; align-items: center; gap: 4px; }
.status-dot { 
  width: 8px; height: 8px; border-radius: 50%; 
  background: rgba(255,255,255,0.5);
  transition: all 0.3s;
}
.status-dot.online { 
  background: #00FF88; 
  box-shadow: 0 0 8px rgba(0,255,136,0.6);
  animation: pulse 2s infinite;
}
@keyframes pulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:0.6;transform:scale(1.1)} }
.header-actions { display: flex; align-items: center; gap: 8px; }

/* 模式切换栏 */
.mode-bar { 
  background: #fafbfc; 
  border-bottom: 1px solid var(--border); 
  padding: 12px 16px 8px;
  position: relative;
}
.mode-switch { display: flex; justify-content: center; align-items: center; gap: 12px; white-space: nowrap; }
.switch-label { color: var(--text-secondary); font-size: 12px; font-weight: 500; }
.mode-switch :deep(.el-switch) { --el-switch-font-size: 11px; }

/* 消息区 */
.chat-messages { 
  flex: 1; overflow-y: auto; padding: 24px 20px; 
  background: var(--bg);
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(79,110,247,0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(123,94,247,0.03) 0%, transparent 50%);
}

/* 欢迎区 */
.welcome-section { text-align: center; padding: 12px 16px 20px; }
.welcome-avatar { 
  font-size: 56px; margin-bottom: 16px; 
  animation: float 3s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1));
}
@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-8px)} }
.welcome-title { 
  font-size: 18px; color: var(--text-primary); font-weight: 600; 
  margin: 0 0 20px 0; line-height: 1.4;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.faq-tags { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; padding: 0 8px; }
.faq-tag {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 24px; border-radius: 28px;
  border: 2px solid var(--primary); color: var(--primary);
  cursor: pointer; font-size: 14px; font-weight: 500;
  background: #fff; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(79, 110, 247, 0.1);
  min-width: 120px;
}
.faq-tag:hover { 
  background: var(--primary-gradient); 
  color: #fff; 
  transform: translateY(-3px); 
  box-shadow: 0 8px 24px rgba(79,110,247,0.35);
  border-color: transparent;
}
.faq-tag:active { transform: scale(0.96) translateY(-1px); }
.faq-icon { font-size: 18px; display: inline-flex; align-items: center; line-height: 1; }

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-header { height: 56px; padding: 0 12px; }
  .brand-logo { font-size: 24px; }
  .brand-name { font-size: 16px; }
  .mode-bar { padding: 8px 12px; }
  .mode-switch { gap: 6px; }
  .welcome-section { padding: 24px 12px 16px; }
  .welcome-avatar { font-size: 48px; margin-bottom: 12px; }
  .welcome-title { font-size: 16px; margin: 0 0 16px 0; }
  .faq-tags { gap: 8px; padding: 0; }
  .faq-tag { 
    padding: 10px 18px; 
    font-size: 13px; 
    border-radius: 24px;
    min-width: 100px;
    box-shadow: 0 2px 8px rgba(79, 110, 247, 0.1);
  }
  .faq-tag:hover { transform: translateY(-2px); }
  .chat-header { padding: 0 12px; height: 56px; }
  .brand-name { font-size: 16px; }
  .header-actions :deep(.el-switch) { margin-left: 8px; }
  .tool-btn { font-size: 12px; padding: 6px 10px; }
  .chat-input { padding: 10px 12px 14px; }
  .msg-bubble { max-width: 100%; padding: 8px 12px; font-size: 13px; }
  .msg-body { max-width: 85%; }
}

/* 消息行 */
.msg-row { display: flex; gap: 10px; margin-bottom: 12px; animation: msgIn 0.3s ease; }
@keyframes msgIn { from{opacity:0;transform:translateY(10px)} to{opacity:1;transform:translateY(0)} }
.msg-row.user { flex-direction: row-reverse; }
.msg-row.system { justify-content: center; }

.msg-avatar {
  width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 18px; flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.msg-avatar.user { background: var(--primary-gradient); }
.msg-avatar.bot { background: #E8EDFF; }
.msg-avatar.agent { background: #E8FFEA; }

.msg-body { max-width: 85%; }
.msg-name { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }

.msg-bubble {
  padding: 12px 16px; line-height: 1.6; font-size: 14px; word-break: break-word;
  min-width: 60px;
}
.msg-bubble.bot {
  background: #fff; color: var(--text-primary); border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.msg-bubble.user {
  background: var(--primary-gradient); color: #fff; border-radius: 20px 20px 4px 20px;
  box-shadow: 0 2px 12px rgba(79,110,247,0.3);
}
.msg-bubble.agent {
  background: #fff; color: var(--text-primary); border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06); border-left: 3px solid var(--success);
}

.msg-text :deep(p) { margin: 0; display: inline; }
.msg-text :deep(ul), .msg-text :deep(ol) { padding-left: 18px; }
.msg-text :deep(strong) { color: var(--text-primary); }
.msg-text :deep(code) { background: #F2F3F5; padding: 2px 6px; border-radius: 4px; font-size: 13px; color: var(--primary); }
.msg-image { max-width: 200px; border-radius: 12px; cursor: pointer; transition: transform 0.2s; }
.msg-image:hover { transform: scale(1.05); }

.msg-meta { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.msg-time { font-size: 11px; color: var(--text-muted); }
.msg-row.user .msg-meta { justify-content: flex-end; }
.fb-btn {
  border: none; background: none; cursor: pointer; font-size: 14px; opacity: 0.4;
  padding: 2px 4px; border-radius: 4px; transition: all 0.2s;
}
.fb-btn:hover { opacity: 0.8; transform: scale(1.1); }
.fb-btn.active { opacity: 1; background: var(--primary-light); }

/* 打字中 */
.typing-bubble { display: flex; gap: 6px; padding: 14px 18px !important; background: #fff !important; border-radius: 20px 20px 20px 4px !important; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.dot {
  width: 8px; height: 8px; border-radius: 50%; background: var(--primary);
  animation: bounce 1.4s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-8px)} }

.system-hint { text-align: center; padding: 6px; color: var(--text-muted); font-size: 12px; display: flex; align-items: center; justify-content: center; gap: 6px; }

/* 系统消息样式 */
.msg-row.system { justify-content: center; margin: 8px 0; }
.msg-row.system .msg-avatar { display: none; }
.msg-row.system .msg-body { max-width: 90%; text-align: center; }
.msg-row.system .msg-bubble-system {
  background: linear-gradient(135deg, #F5F7FA 0%, #E8EDFF 100%);
  border: 1px solid rgba(79, 110, 247, 0.15);
  border-radius: 24px;
  padding: 10px 20px;
  box-shadow: 0 2px 12px rgba(79, 110, 247, 0.1);
  display: inline-block;
  font-size: 13px;
  color: var(--text-secondary);
  animation: fadeInUp 0.3s ease;
}
.msg-row.system .msg-bubble-system :deep(.msg-text) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}
.msg-row.system .msg-name { display: none; }
.msg-row.system .msg-meta { justify-content: center; margin-top: 0; }

/* 输入区 */
.chat-input { 
  border-top: 1px solid var(--border); 
  background: #fff; 
  padding: 12px 20px 16px; 
  flex-shrink: 0;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.03);
}
.input-toolbar { display: flex; gap: 6px; margin-bottom: 10px; }
.tool-btn {
  border: none; background: none; cursor: pointer; font-size: 13px; color: var(--text-muted);
  display: flex; align-items: center; gap: 4px; padding: 6px 10px; border-radius: 8px; transition: all 0.2s;
}
.tool-btn:hover { background: var(--primary-light); color: var(--primary); }
.tool-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-text { display: none; }
@media (min-width: 769px) { .btn-text { display: inline; } }

.quick-panel { background: #FAFBFC; border: 1px solid var(--border); border-radius: 12px; padding: 8px; margin-bottom: 10px; max-height: 180px; overflow-y: auto; }
.quick-item { padding: 8px 10px; cursor: pointer; border-radius: 8px; transition: background 0.2s; }
.quick-item:hover { background: var(--primary-light); }
.quick-item b { color: var(--primary); margin-right: 8px; }
.quick-item span { font-size: 12px; color: var(--text-muted); }

/* 实时话术推荐面板 */
.recommendation-panel {
  background: linear-gradient(135deg, #F5F7FA 0%, #FFF9E6 100%);
  border: 1px solid rgba(79, 110, 247, 0.15);
  border-radius: 12px;
  padding: 10px 12px;
  margin-top: 8px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(79, 110, 247, 0.1);
  animation: fadeInUp 0.3s ease;
}
.recommendation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin: 4px 0;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(79, 110, 247, 0.08);
  font-size: 13px;
  color: var(--text-secondary);
  position: relative;
  overflow: hidden;
}
.recommendation-item::before {
  content: '💡';
  font-size: 14px;
  opacity: 0.8;
}
.recommendation-item:hover {
  transform: translateX(4px);
  background: linear-gradient(90deg, rgba(79, 110, 247, 0.05) 0%, rgba(79, 110, 247, 0) 100%);
  border-color: rgba(79, 110, 247, 0.3);
  box-shadow: 0 4px 12px rgba(79, 110, 247, 0.15);
}
.recommendation-item:active {
  transform: scale(0.98);
}

.input-row { display: flex; gap: 10px; align-items: flex-end; }
.input-row :deep(.el-textarea__inner) { 
  border-radius: 16px !important; 
  border-color: var(--border) !important; 
  resize: none !important; 
  font-size: 14px;
  transition: all 0.2s !important;
}
.input-row :deep(.el-textarea__inner):focus { 
  border-color: var(--primary) !important; 
  box-shadow: 0 0 0 3px rgba(79,110,247,0.1) !important; 
}

.send-btn {
  width: 48px; height: 48px; border-radius: 16px; border: none;
  background: var(--primary-gradient); color: #fff; font-size: 20px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(79,110,247,0.3);
}
.send-btn:hover:not(:disabled) { 
  transform: scale(1.08); 
  box-shadow: 0 4px 16px rgba(79,110,247,0.4); 
}
.send-btn:active:not(:disabled) { transform: scale(0.95); }
.send-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 坐席面板 */
.agent-panel {
  width: 340px; background: #fff; border-left: 1px solid var(--border);
  overflow-y: auto; flex-shrink: 0; box-shadow: -2px 0 12px rgba(0,0,0,0.04);
}
.panel-title { padding: 16px 20px; font-size: 15px; font-weight: 600; border-bottom: 1px solid var(--border); color: var(--text-primary); }
.panel-content { padding: 16px 20px; }
.panel-block { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #F2F3F5; }
.panel-block:last-child { border-bottom: none; }
.block-title { font-size: 13px; font-weight: 600; color: var(--text-primary); margin-bottom: 10px; }
.info-row { display: flex; justify-content: space-between; padding: 4px 0; font-size: 13px; color: var(--text-secondary); }
.tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.tag-blue { background: var(--primary-light); color: var(--primary); }

.kb-item { padding: 8px; margin-top: 8px; background: #FAFBFC; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.kb-item:hover { background: var(--primary-light); transform: translateX(2px); }
.kb-q { font-size: 13px; font-weight: 600; color: var(--primary); }
.kb-a { font-size: 12px; color: var(--text-muted); margin-top: 4px; }

.emotion-bar { height: 8px; background: #F2F3F5; border-radius: 4px; overflow: hidden; margin: 8px 0 6px; }
.emotion-fill { height: 100%; border-radius: 4px; transition: all 0.5s; }
.emotion-label { font-size: 12px; color: var(--text-muted); }
.summary-text { font-size: 13px; color: var(--text-muted); line-height: 1.6; }

/* 评价弹窗 */
.csat-body { text-align: center; }
.csat-body p { margin-bottom: 16px; color: var(--text-primary); }
.csat-body :deep(.el-rate) { justify-content: center; margin-bottom: 16px; }

/* 移动端响应式 */
@media (max-width: 768px) {
  .chat-page {
    height: 100vh;
    overflow: hidden;
  }
  .chat-main { 
    max-width: 100%; 
    flex-direction: column;
    height: 100vh;
    display: flex;
  }
  
  .chat-header {
    flex-direction: column;
    padding: 12px;
    gap: 12px;
  }
  .header-brand { width: 100%; justify-content: center; }
  .header-actions {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
    gap: 8px;
  }
  .header-actions .el-switch { 
    margin: 0; 
    flex-shrink: 0;
  }
  .header-actions .tool-btn {
    flex-shrink: 0;
    max-width: 100px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .header-actions .el-select { width: 100px !important; }
  .tool-btn { padding: 4px 6px; font-size: 11px; }
  
  .welcome-section { padding: 30px 16px; }
  .welcome-avatar { font-size: 60px; margin-bottom: 16px; }
  .welcome-title { font-size: 20px; padding: 0 20px; }
  .welcome-sub { font-size: 14px; padding: 0 20px; }
  
  .faq-tags {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
    padding: 0 16px;
  }
  .faq-tag {
    padding: 10px 16px;
    font-size: 13px;
    flex: 0 1 calc(50% - 4px);
    justify-content: center;
  }
  .faq-icon { font-size: 16px; }
  
  .chat-messages { padding: 12px; }
  .msg-avatar {
    width: 32px; height: 32px;
    font-size: 16px;
  }
  .msg-body { max-width: 75%; }
  .msg-bubble {
    padding: 10px 14px;
    font-size: 13px;
    line-height: 1.6;
  }
  .msg-image { max-width: 150px; }
  
  .chat-input {
    padding: 10px 12px 14px;
  }
  .input-toolbar {
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;
    max-width: 100%;
  }
  .input-toolbar .tool-btn {
    flex: 0 0 auto;
    min-width: 60px;
    max-width: 80px;
    flex-basis: calc(33.333% - 4px);
  }
  .tool-btn {
    font-size: 12px;
    padding: 6px 8px;
    white-space: nowrap;
  }
  .input-row { 
    gap: 8px; 
    width: 100%;
  }
  .input-row :deep(.el-textarea__inner) {
    font-size: 13px !important;
    padding: 8px 10px !important;
    flex: 1;
    min-width: 0;
  }
  .send-btn {
    flex-shrink: 0;
    width: 42px; height: 42px;
    font-size: 18px;
  }
  
  .quick-panel { display: none; }
  .agent-panel { display: none; }
  .chat-main { flex-direction: column; }
}

@media (max-width: 375px) {
  .faq-tag {
    flex: 0 1 100%;
  }
  .header-brand {
    flex-direction: column;
    gap: 4px;
  }
  .brand-text { text-align: center; }
  .brand-name { font-size: 14px; }
  .brand-status { font-size: 11px; }
}
</style>
