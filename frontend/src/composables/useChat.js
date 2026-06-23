import { ref, computed, watch } from 'vue'
import { chatApi, kbApi } from '../api'
import { MOCK_FIRST } from '../api'

/**
 * 对话编排 composable
 * 吸收 3 种消息发送模式的编排逻辑
 */
export function useChat(messages, sessionId, useRobotMode, transferredToAgent, currentAgentName) {
  const sending = ref(false)
  const typingIndicator = ref(false)
  const emotionScore = ref(80)

  // 保存会话信息到localStorage（供客服工作台使用）
  function saveSessionInfo() {
    if (sessionId.value) {
      const allSessions = JSON.parse(localStorage.getItem('ai_cs_all_sessions') || '{}')
      const lastMsg = messages.value[messages.value.length - 1]
      allSessions[sessionId.value] = {
        userId: '用户',
        lastMessage: lastMsg?.content?.substring(0, 30) || '',
        lastTime: Date.now(),
        agentId: transferredToAgent.value ? 1 : null,
        useRobot: useRobotMode.value
      }
      localStorage.setItem('ai_cs_all_sessions', JSON.stringify(allSessions))
      
      // 保存当前会话的消息（包含backendId用于去重）
      const messagesToSave = messages.value.slice(-50).map(m => ({
        ...m,
        backendId: m.backendId || null
      }))
      localStorage.setItem(`ai_cs_messages_${sessionId.value}`, JSON.stringify(messagesToSave))
    }
  }

  // 监听消息变化，自动保存
  watch(messages, saveSessionInfo, { deep: true })

  async function sendMessage(content, messageType = 'text') {
    if (sending.value) return
    sending.value = true

    messages.value.push({ senderType: 'user', content, messageType, timestamp: Date.now() })

    try {
      typingIndicator.value = true

      if (!sessionId.value) {
        try {
          const sessionRes = await chatApi.createSession({ channel: 'web' })
          if (sessionRes?.sessionId) {
            sessionId.value = sessionRes.sessionId
          }
        } catch (e) {
          sessionId.value = 'local-' + Date.now()
        }
      }

      if (transferredToAgent.value) {
        await sendAsAgent(content, messageType)
      } else if (useRobotMode.value) {
        await sendAsRobot(content, messageType)
      } else {
        await sendAsLegacy(content, messageType)
      }
    } catch (e) {
      typingIndicator.value = false
      const errSender = transferredToAgent.value ? 'agent' : 'bot'
      const errName = transferredToAgent.value ? currentAgentName.value : undefined
      const errMsg = transferredToAgent.value ? '抱歉，网络波动，请稍等片刻我再回复您。' : '抱歉，网络异常，请稍后再试。'
      const errObj = { senderType: errSender, content: errMsg, messageType: 'text', timestamp: Date.now() }
      if (errName) errObj.senderName = errName
      messages.value.push(errObj)
    }
    sending.value = false
  }

  async function sendAsAgent(content, messageType) {
    // 人工模式下，用户消息需要保存到后端数据库（供客服工作台查看）
    try {
      await chatApi.aiChat({
        sessionId: sessionId.value,
        message: content,
        useRobot: false
      })
    } catch (e) {
      console.warn('保存用户消息失败:', e)
    }
    typingIndicator.value = false
  }

  async function sendAsRobot(content, messageType) {
    try {
      let intentResult = null
      try {
        const intentRes = await chatApi.recognizeIntent({ sessionId: sessionId.value, content })
        intentResult = intentRes
        console.log('[Intent Recognition]', intentResult)
      } catch (e) {
        console.warn('Intent recognition failed, continuing with AI chat:', e)
      }

      let replyContent = ''
      let shouldTransferToAgent = false

      if (intentResult && intentResult.confidence >= 0.7) {
        try {
          const kbMatches = await kbApi.searchItems(content)
          if (kbMatches && kbMatches.length > 0 && kbMatches[0].score >= 50) {
            replyContent = kbMatches[0].answer
            console.log('[KB Match] intent:', intentResult.intentCode, 'score:', kbMatches[0].score)
          }
        } catch (kbErr) {
          console.warn('KB retrieval failed:', kbErr)
        }
      }

      if (!replyContent) {
        const res = await chatApi.aiChat({
          sessionId: sessionId.value,
          message: content,
          useRobot: true,
          intent: intentResult?.intentCode || 'unknown'
        })
        replyContent = res?.message || '抱歉，我暂时无法处理您的请求。'
      }

      if (intentResult && intentResult.intentCode === 'transfer_manual') {
        shouldTransferToAgent = true
      }
      if (replyContent.includes('转人工') || replyContent.includes('人工客服')) {
        shouldTransferToAgent = true
      }

      typingIndicator.value = false
      messages.value.push({ senderType: 'bot', content: replyContent, messageType: 'text', timestamp: Date.now() })

      return { shouldTransferToAgent }
    } catch (e) {
      typingIndicator.value = false
      messages.value.push({ senderType: 'system', content: '机器人服务暂时不可用，已为您切换到人工模式。', timestamp: Date.now() })
      useRobotMode.value = false
    }
  }

  async function sendAsLegacy(content, messageType) {
    const res = await chatApi.sendMessage({ sessionId: sessionId.value, content, msgType: messageType })

    if (res?.sessionId && sessionId.value !== res.sessionId) {
      sessionId.value = res.sessionId
    }

    typingIndicator.value = false

    const replyContent = res?.content || res?.reply || '抱歉，我暂时无法处理您的请求。'
    messages.value.push({ senderType: 'bot', content: replyContent, messageType: 'text', timestamp: Date.now() })

    if (res?.sentiment !== undefined) emotionScore.value = res.sentiment <= -2 ? 20 : res.sentiment <= -1 ? 40 : res.sentiment >= 1 ? 90 : 70
  }

  return {
    sending,
    typingIndicator,
    emotionScore,
    sendMessage
  }
}
