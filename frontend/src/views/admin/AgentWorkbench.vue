<template>
  <div class="agent-workbench">
    <!-- 左侧：会话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>我的会话</h3>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <el-button size="small" circle>💬</el-button>
        </el-badge>
      </div>
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.sessionId"
          :class="['session-item', { active: currentSession?.sessionId === session.sessionId }]"
          @click="selectSession(session)"
        >
          <div class="session-avatar">👤</div>
          <div class="session-info">
            <div class="session-name">{{ session.userId || '用户' }}</div>
            <div class="session-preview">{{ session.lastMessage || '新会话' }}</div>
          </div>
          <div class="session-time">{{ formatTime(session.lastTime) }}</div>
        </div>
        <div v-if="sessions.length === 0" class="empty-tip">
          <div class="empty-icon">📭</div>
          <p>暂无会话</p>
          <p class="empty-sub">等待用户发起对话...</p>
        </div>
      </div>
      <div class="sidebar-footer">
        <el-button type="primary" @click="refreshSessions" :loading="refreshing" size="small">
          刷新列表
        </el-button>
      </div>
    </div>

    <!-- 右侧：聊天区域 -->
    <div class="chat-area">
      <div v-if="!currentSession" class="no-session">
        <div class="empty-icon">💬</div>
        <p>选择一个会话开始回复</p>
      </div>
      <template v-else>
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="ch-left">
            <span class="ch-session">会话：{{ currentSession.sessionId?.substring(0, 16) }}...</span>
            <el-tag size="small" type="success">进行中</el-tag>
          </div>
          <div class="ch-right">
            <el-button size="small" @click="refreshMessages" :loading="loadingMessages">刷新</el-button>
            <el-button size="small" type="danger" plain @click="closeSession">结束会话</el-button>
          </div>
        </div>
        
        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div 
            v-for="(msg, idx) in currentMessages" 
            :key="idx"
            :class="['message', msg.senderType === 'user' ? 'user' : 'agent']"
          >
            <div class="msg-avatar">{{ msg.senderType === 'user' ? '👤' : '👩‍💼' }}</div>
            <div class="msg-content">
              <div class="msg-sender" v-if="msg.senderType === 'user'">用户</div>
              <div class="msg-sender" v-else>{{ msg.senderName || '我' }}</div>
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.timestamp || msg.createTime) }}</div>
            </div>
          </div>
          <div v-if="loadingMessages" class="loading-tip">加载中...</div>
        </div>

        <!-- 输入框 -->
        <div class="input-area">
          <el-input 
            v-model="inputMessage" 
            placeholder="输入回复内容... (Enter发送)"
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim() || sending">
            {{ sending ? '发送中...' : '发送' }}
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { chatApi } from '../../api'

const agentId = ref(1) // 当前客服ID

const sessions = ref([])
const currentSession = ref(null)
const currentMessages = ref([])
const inputMessage = ref('')
const unreadCount = ref(0)
const refreshing = ref(false)
const sending = ref(false)
const loadingMessages = ref(false)
const messageListRef = ref(null)

// 定时刷新当前会话消息
let messageRefreshTimer = null

function startMessageRefresh() {
  if (messageRefreshTimer) return
  messageRefreshTimer = setInterval(() => {
    if (currentSession.value) {
      loadSessionMessages(currentSession.value.sessionId)
    }
  }, 3000)
}

function stopMessageRefresh() {
  if (messageRefreshTimer) { clearInterval(messageRefreshTimer); messageRefreshTimer = null }
}

// 定时刷新会话列表
let pollTimer = null

// 刷新会话列表
async function refreshSessions() {
  refreshing.value = true
  try {
    // 从本地存储获取所有会话
    const allSessions = JSON.parse(localStorage.getItem('ai_cs_all_sessions') || '{}')
    const sessionList = []
    
    for (const [sessionId, sessionData] of Object.entries(allSessions)) {
      if (sessionData.agentId == agentId.value || !sessionData.agentId) {
        sessionList.push({
          sessionId,
          userId: sessionData.userId || '用户',
          lastMessage: sessionData.lastMessage || '新会话',
          lastTime: sessionData.lastTime || Date.now(),
          agentId: sessionData.agentId
        })
      }
    }
    
    // 按时间排序
    sessionList.sort((a, b) => (b.lastTime || 0) - (a.lastTime || 0))
    sessions.value = sessionList
  } catch (e) {
    console.error('刷新会话列表失败:', e)
  }
  refreshing.value = false
}

// 选择会话
async function selectSession(session) {
  currentSession.value = session
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  
  // 加载该会话的消息
  await loadSessionMessages(session.sessionId)
  
  // 启动消息自动刷新
  startMessageRefresh()
}

// 刷新当前会话消息
async function refreshMessages() {
  if (!currentSession.value) return
  await loadSessionMessages(currentSession.value.sessionId)
}

// 加载会话消息
async function loadSessionMessages(sessionId) {
  loadingMessages.value = true
  try {
    const messages = await chatApi.getSessionMessages(sessionId, 100)
    if (messages && Array.isArray(messages) && messages.length > 0) {
      currentMessages.value = messages.map(msg => ({
        senderType: msg.senderType,
        senderName: msg.senderName,
        content: msg.content,
        timestamp: msg.createTime ? new Date(msg.createTime).getTime() : Date.now()
      }))
      scrollToBottom()
    } else {
      // 后端没有消息，从本地存储加载
      const localData = JSON.parse(localStorage.getItem('ai_cs_messages') || '[]')
      currentMessages.value = localData.filter(m => m.sessionId === sessionId || !m.sessionId)
    }
  } catch (e) {
    console.error('加载消息失败:', e)
    // 从本地存储加载
    const localData = JSON.parse(localStorage.getItem('ai_cs_messages') || '[]')
    currentMessages.value = localData.filter(m => m.sessionId === sessionId || !m.sessionId)
  }
  loadingMessages.value = false
}

// 发送消息
async function sendMessage() {
  if (!inputMessage.value.trim() || !currentSession.value || sending.value) return
  
  const content = inputMessage.value.trim()
  sending.value = true
  
  try {
    // 添加到本地消息列表
    const newMsg = {
      senderType: 'agent',
      senderName: '客服',
      content,
      timestamp: Date.now()
    }
    currentMessages.value.push(newMsg)
    scrollToBottom()
    
    // 调用API发送消息
    await chatApi.agentReply({
      sessionId: currentSession.value.sessionId,
      agentId: agentId.value,
      content
    })
    
    // 更新会话最后消息
    currentSession.value.lastMessage = content.substring(0, 30)
    currentSession.value.lastTime = Date.now()
    
    inputMessage.value = ''
  } catch (e) {
    console.error('发送消息失败:', e)
  }
  sending.value = false
}

// 关闭会话
function closeSession() {
  if (!currentSession.value) return
  
  // 从列表中移除
  sessions.value = sessions.value.filter(s => s.sessionId !== currentSession.value.sessionId)
  
  // 清除本地存储
  localStorage.removeItem(`ai_cs_messages_${currentSession.value.sessionId}`)
  
  currentSession.value = null
  currentMessages.value = []
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 格式化时间
function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  
  if (isToday) {
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  }
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// 定时刷新列表
function startPolling() {
  pollTimer = setInterval(refreshSessions, 5000)
}

onMounted(() => {
  refreshSessions()
  startPolling()
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  stopMessageRefresh()
})
</script>

<style scoped>
.agent-workbench { display: flex; height: 100vh; background: #f5f7fa; }

.sidebar { 
  width: 300px; 
  background: #fff; 
  border-right: 1px solid #e4e7ed; 
  display: flex; 
  flex-direction: column; 
}
.sidebar-header { 
  padding: 16px 20px; 
  border-bottom: 1px solid #e4e7ed; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
}
.sidebar-header h3 { margin: 0; font-size: 16px; font-weight: 600; }
.session-list { flex: 1; overflow-y: auto; }
.session-item { 
  display: flex; 
  align-items: center; 
  padding: 14px 16px; 
  cursor: pointer; 
  border-bottom: 1px solid #f0f0f0; 
  transition: all 0.2s; 
}
.session-item:hover { background: #f5f7fa; }
.session-item.active { background: #ecf5ff; border-left: 3px solid #409eff; }
.session-avatar { font-size: 28px; margin-right: 12px; }
.session-info { flex: 1; min-width: 0; }
.session-name { font-weight: 500; font-size: 14px; color: #303133; }
.session-preview { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px; }
.session-time { font-size: 11px; color: #c0c4cc; }
.sidebar-footer { padding: 12px 16px; border-top: 1px solid #e4e7ed; }

.empty-tip { 
  text-align: center; 
  padding: 60px 20px; 
  color: #c0c4cc; 
}
.empty-tip .empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-tip p { margin: 4px 0; }
.empty-tip .empty-sub { font-size: 12px; }

.chat-area { flex: 1; display: flex; flex-direction: column; }
.no-session { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  color: #909399; 
}
.no-session .empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }

.chat-header { 
  padding: 14px 20px; 
  background: #fff; 
  border-bottom: 1px solid #e4e7ed; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
}
.ch-left { display: flex; align-items: center; gap: 12px; }
.ch-session { font-size: 14px; color: #606266; }

.message-list { flex: 1; overflow-y: auto; padding: 20px; }
.message { display: flex; margin-bottom: 16px; animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.message.user { flex-direction: row; }
.message.agent { flex-direction: row-reverse; }
.msg-avatar { font-size: 28px; margin: 0 10px; }
.msg-content { max-width: 65%; }
.msg-sender { font-size: 12px; color: #909399; margin-bottom: 4px; }
.message.agent .msg-sender { text-align: right; }
.msg-text { 
  padding: 12px 16px; 
  border-radius: 16px; 
  font-size: 14px; 
  line-height: 1.6; 
  word-break: break-word;
}
.message.user .msg-text { background: #ecf5ff; color: #303133; border-radius: 16px 16px 16px 4px; }
.message.agent .msg-text { background: #409eff; color: #fff; border-radius: 16px 16px 4px 16px; }
.msg-time { font-size: 11px; color: #c0c4cc; margin-top: 6px; }
.message.agent .msg-time { text-align: right; }

.loading-tip { text-align: center; color: #909399; font-size: 12px; padding: 12px; }

.input-area { 
  display: flex; 
  padding: 16px 20px; 
  gap: 12px; 
  background: #fff; 
  border-top: 1px solid #e4e7ed; 
}
</style>
