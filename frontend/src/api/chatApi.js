import { api, withMock, MOCK_FIRST } from './client'

let mockSessionId = null

const mockChatReplies = {
  '你好': '您好！欢迎使用空白格AI客服，请问有什么可以帮您？😊',
  '查订单': '好的，我来帮您查询订单！请提供您的订单号，或者告诉我您购买的商品名称和下单时间。\n\n📋 **查询方式：**\n1. 订单号查询（最快）\n2. 手机号查询\n3. 商品名+时间查询',
  '订单号': '已为您查询到订单信息：\n\n📦 **订单号：** ORD20260518003\n📅 **下单时间：** 2026-05-18 15:30\n🚚 **物流状态：** 运输中（预计明天送达）\n📍 **当前位置：** 济南转运中心\n\n如需了解更多详情，请点击「转人工」联系客服。',
  '退货': '退货流程如下：\n\n1️⃣ 登录账户 → 我的订单\n2️⃣ 选择需要退货的订单 → 申请退货\n3️⃣ 填写退货原因并提交\n4️⃣ 等待审核（1-2个工作日）\n5️⃣ 审核通过后寄回商品\n6️⃣ 我们收到商品后3-5个工作日退款\n\n⚠️ 注意：商品需保持原包装未使用，7天内可申请',
  '退款': '退款处理流程：\n\n💰 **退款方式：**\n- 原路退回（3-5工作日）\n- 余额退回（即时到账）\n\n如需帮助请点击「转人工」联系客服',
  '人工': '好的，正在为您转接人工客服，请稍候...\n\n👩‍💼 当前在线客服：3人\n⏱️ 预计等待时间：1-2分钟',
  '产品': '我们提供以下AI产品和服务：\n\n🤖 **AI客服系统** — 7×24小时智能接待\n🎨 **AI出图** — 电商商品图一键生成\n🎬 **AI短视频** — 智能剪辑配乐\n📞 **AI语音客服** — 电话自动应答\n\n如需了解详情，请告诉我您感兴趣的产品！',
  '价格': '我们的服务套餐如下：\n\n| 套餐 | 价格 | 功能 |\n|------|------|------|\n| 基础版 | ¥999/月 | AI客服+知识库 |\n| 专业版 | ¥2999/月 | 全渠道+坐席+报表 |\n| 企业版 | ¥9999/月 | 定制化+专属部署 |\n\n需要详细报价可以点击「转人工」联系销售顾问 💼'
}

function getMockReply(content) {
  const text = content.toLowerCase()
  for (const [key, reply] of Object.entries(mockChatReplies)) {
    if (text.includes(key)) return reply
  }
  return '抱歉，这个问题我暂时还没有学会 😅\n\n我们的知识库正在持续学习中，为了不耽误您的时间，建议您：\n\n👉 点击底部「转人工」按钮，获取专业客服支持\n👉 或拨打客服热线：400-123-4567（工作日 9:00-18:00）\n\n您也可以继续提问其他问题，我会尽力为您解答！😊'
}

export const chatApi = {
  createSession: (data) => withMock(() => api.post('/chat/session', data), () => {
    mockSessionId = 'ws-' + Date.now()
    return Promise.resolve({ sessionId: mockSessionId, wsUrl: 'ws://localhost:8765/ws' })
  }),
  sendMessage: (data) => withMock(() => api.post('/chat/message', data), async () => {
    try {
      const { kbApi } = await import('./kbApi')
      const matches = await kbApi.searchItems(data.content)
      if (matches && matches.length > 0) {
        const highScoreMatches = matches.filter(m => m.score >= 50)
        const uniqueIds = new Set(highScoreMatches.map(m => m.id))
        const isMultiIntent = uniqueIds.size >= 2 && highScoreMatches.length >= 2
        if (isMultiIntent) {
          const topMatches = highScoreMatches.slice(0, 3)
          const answers = topMatches.map((m, idx) => `${idx + 1}. ${m.answer}`).join('\n\n')
          const summary = `\n\n---\n💡 检测到您问了多个问题，已为您合并回答。如需了解某个问题的详情，请继续提问。`
          return Promise.resolve({ content: answers + summary, sessionId: mockSessionId || data.sessionId, timestamp: Date.now(), sentiment: 0 })
        }
        const bestMatch = matches[0]
        return Promise.resolve({ content: bestMatch.answer, sessionId: mockSessionId || data.sessionId, timestamp: Date.now(), sentiment: 0 })
      }
    } catch (e) {
      console.warn('知识库检索失败，使用默认回复:', e)
    }
    const replyContent = getMockReply(data.content)
    return Promise.resolve({ content: replyContent, sessionId: mockSessionId || data.sessionId, timestamp: Date.now(), sentiment: 0 })
  }),
  endSession: (sessionId) => withMock(() => api.post('/chat/session/end', null, { params: { sessionId } }), () => Promise.resolve({ success: true })),
  transferToAgent: (sessionId) => withMock(() => api.post('/chat/transfer', { sessionId }), () => {
    // Mock模式下模拟转接成功
    return Promise.resolve({ 
      success: true, 
      agentId: 1,
      agentName: '小林', 
      message: '已为您转接人工客服「小林」' 
    })
  }),
  submitCsat: (data) => withMock(() => api.post('/chat/csat', data), () => Promise.resolve({ success: true })),
  aiChat: (data) => withMock(() => api.post('/chat/ai', data), async () => {
    const replyContent = getMockReply(data.message)
    return Promise.resolve({ message: replyContent, isRobot: true, messageId: 'ai-' + Date.now() })
  }),
  toggleRobotMode: (data) => withMock(() => api.post('/chat/toggle', data), () => {
    return Promise.resolve({ useRobot: data.useRobot, message: data.useRobot ? '已切换到机器人客服模式' : '已切换到人工客服模式' })
  }),
  // 客服发送消息
  agentReply: (data) => withMock(() => api.post('/chat/agent-reply', data), () => {
    return Promise.resolve({ success: true, messageId: 'agent-' + Date.now() })
  }),
  // 获取会话消息列表
  getSessionMessages: (sessionId, limit = 50) => withMock(() => api.get('/chat/session-messages', { params: { sessionId, limit } }), () => {
    return Promise.resolve([])
  }),
  recognizeIntent: (data) => withMock(() => api.post('/chat/intent', data), () => {
    const content = (data.content || '').toLowerCase()
    const intentMap = {
      'query_order': ['订单', '查单', '物流', '快递', '发货', '配送', 'ord'],
      'refund': ['退款', '退钱', '退款慢', '到账'],
      'exchange': ['退货', '换货', '退换货', '不要了'],
      'transfer_manual': ['人工', '真人', '客服', '电话', '转接', '投诉'],
      'price_inquiry': ['价格', '多少钱', '收费', '贵', '费用', '价目', '套餐'],
      'product_info': ['产品', '功能', '服务', '支持', '能做', '可以']
    }
    for (const [intent, keywords] of Object.entries(intentMap)) {
      if (keywords.some(kw => content.includes(kw))) {
        return Promise.resolve({ intentCode: intent, confidence: 0.75, matchType: 'mock-keyword' })
      }
    }
    return Promise.resolve({ intentCode: 'unknown', confidence: 0.3, matchType: 'mock-fallback' })
  })
}