import { api, withMock } from './client'

let mockAgents = [
  { id: 1, nickname: '小林', username: 'xiaolin', skillGroup: 'after_sale', status: 1, currentSessions: 3, maxSessions: 10, role: 'agent' },
  { id: 2, nickname: '小张', username: 'xiaozhang', skillGroup: 'sales', status: 1, currentSessions: 5, maxSessions: 10, role: 'agent' },
  { id: 3, nickname: '王姐', username: 'wangjie', skillGroup: 'vip', status: 2, currentSessions: 8, maxSessions: 10, role: 'agent' },
  { id: 4, nickname: '赵经理', username: 'zhaojl', skillGroup: 'default', status: 0, currentSessions: 0, maxSessions: 15, role: 'admin' }
]

let mockIntents = [
  { id: 1, intentName: '查询订单', intentCode: 'query_order', category: 'after_sale', needTransfer: 0, status: 1, samples: '["查订单","我的订单呢","订单到哪了","物流查询","发货了吗"]', responseTemplate: '好的，我来帮您查询订单。请提供订单号或下单手机号。' },
  { id: 2, intentName: '申请退货', intentCode: 'refund', category: 'after_sale', needTransfer: 0, status: 1, samples: '["退货","我要退货","怎么退货","退货怎么操作"]', responseTemplate: '退货流程：登录→我的订单→申请退货→填写原因→寄回商品→退款' },
  { id: 3, intentName: '转人工', intentCode: 'transfer_manual', category: 'escalation', needTransfer: 1, status: 1, samples: '["转人工","人工客服","找真人","不要机器人","我要人工"]', responseTemplate: '正在为您转接人工客服...' },
  { id: 4, intentName: '退款咨询', intentCode: 'refund_inquiry', category: 'after_sale', needTransfer: 0, status: 1, samples: '["退款","什么时候退款","退款没到账","退款进度"]', responseTemplate: '原路退款3-5个工作日到账，余额退款即时到账。' },
  { id: 5, intentName: '产品咨询', intentCode: 'product_info', category: 'pre_sale', needTransfer: 0, status: 1, samples: '["产品介绍","有什么功能","AI客服能做什么","系统介绍"]', responseTemplate: '我们提供AI客服系统、AI出图、AI短视频等服务，请问您对哪款产品感兴趣？' },
  { id: 6, intentName: '价格咨询', intentCode: 'price_inquiry', category: 'pre_sale', needTransfer: 0, status: 1, samples: '["价格多少","多少钱","收费吗","怎么收费","套餐价格"]', responseTemplate: '基础版¥999/月，专业版¥2999/月，企业版¥9999/月起。' },
  { id: 7, intentName: '换货申请', intentCode: 'exchange', category: 'after_sale', needTransfer: 0, status: 1, samples: '["换货","我要换货","发错货了","商品有问题","换一个"]', responseTemplate: '换货流程：登录→我的订单→申请换货→选择原因→寄回原商品→收到新商品。' },
  { id: 8, intentName: '投诉建议', intentCode: 'complaint', category: 'escalation', needTransfer: 1, status: 1, samples: '["投诉","我要投诉","不满意","建议","反馈问题"]', responseTemplate: '非常抱歉给您带来不便，我正在为您转接专属客服处理。' }
]

let mockChannels = [
  { id: 1, channel: 'web', configName: '网页客服', botEnabled: 1, transferEnabled: 1, workTimeStart: '09:00', workTimeEnd: '21:00', status: 1, appId: '', appSecret: '', callbackUrl: '' },
  { id: 2, channel: 'wechat_mp', configName: '微信公众号', botEnabled: 1, transferEnabled: 1, workTimeStart: '09:00', workTimeEnd: '18:00', status: 1, appId: 'wx8a9b0c1d2e3f4g5h', appSecret: '****', callbackUrl: 'https://api.jnysx.cn/wechat/callback' },
  { id: 3, channel: 'wechat_mini', configName: '微信小程序', botEnabled: 1, transferEnabled: 0, workTimeStart: '00:00', workTimeEnd: '23:59', status: 1, appId: 'wx_mini_abc123', appSecret: '****', callbackUrl: 'https://api.jnysx.cn/mini/callback' },
  { id: 4, channel: 'douyin', configName: '抖音企业号', botEnabled: 1, transferEnabled: 1, workTimeStart: '10:00', workTimeEnd: '22:00', status: 0, appId: 'dy_enterprise_001', appSecret: '****', callbackUrl: 'https://api.jnysx.cn/douyin/callback' },
  { id: 5, channel: 'app', configName: 'APP内嵌客服', botEnabled: 1, transferEnabled: 1, workTimeStart: '00:00', workTimeEnd: '23:59', status: 1, appId: 'com.jnysx.app', appSecret: '', callbackUrl: '' }
]

const mockReports = [
  { reportDate: '2026-05-20', channel: 'web', totalSessions: 48, botResolved: 40, transferCount: 8, botResolveRate: '83.3%', avgResponseTime: 1.1, avgCsatScore: 4.6 },
  { reportDate: '2026-05-19', channel: 'web', totalSessions: 42, botResolved: 35, transferCount: 7, botResolveRate: '83.3%', avgResponseTime: 1.2, avgCsatScore: 4.5 },
  { reportDate: '2026-05-18', channel: 'web', totalSessions: 38, botResolved: 30, transferCount: 8, botResolveRate: '78.9%', avgResponseTime: 1.5, avgCsatScore: 4.3 },
  { reportDate: '2026-05-17', channel: 'web', totalSessions: 45, botResolved: 39, transferCount: 6, botResolveRate: '86.7%', avgResponseTime: 1.1, avgCsatScore: 4.6 },
  { reportDate: '2026-05-16', channel: 'web', totalSessions: 36, botResolved: 28, transferCount: 8, botResolveRate: '77.8%', avgResponseTime: 1.8, avgCsatScore: 4.2 },
  { reportDate: '2026-05-15', channel: 'web', totalSessions: 51, botResolved: 44, transferCount: 7, botResolveRate: '86.3%', avgResponseTime: 1.0, avgCsatScore: 4.7 },
  { reportDate: '2026-05-14', channel: 'web', totalSessions: 33, botResolved: 26, transferCount: 7, botResolveRate: '78.8%', avgResponseTime: 1.6, avgCsatScore: 4.4 }
]

export const systemConfigApi = {
  getConfig: (configKey) => api.get(`/admin/config/${configKey}`),
  setConfig: (data) => api.post('/admin/config', data),
  testConnection: () => api.post('/admin/config/test-connection')
}

export const adminApi = {
  login: (data) => withMock(
    () => api.post('/admin/login', data),
    () => Promise.reject(new Error('Mock验证由auth store本地处理'))
  ),
  dashboard: () => withMock(() => api.get('/admin/dashboard'), () => {
    return Promise.resolve({
      todaySessions: 48, botResolved: 40, transferCount: 8, totalUsers: 1586,
      botResolveRate: '83.3%', onlineAgents: 3, pendingTickets: 2, pendingQuestions: 5,
      avgCsatScore: 4.6, avgResponseTime: 1.1
    })
  }),
  listAgents: (params) => withMock(() => api.get('/admin/agents', { params }), () => Promise.resolve({ records: [...mockAgents], total: mockAgents.length })),
  addAgent: (data) => withMock(() => api.post('/admin/agent', data), () => Promise.reject(new Error('后端不可用'))),
  updateAgent: (data) => withMock(() => api.put('/admin/agent', data), () => {
    const idx = mockAgents.findIndex(a => a.id === data.id)
    if (idx !== -1) mockAgents[idx] = { ...mockAgents[idx], ...data }
    return Promise.resolve({ success: true })
  }),
  updateAgentStatus: (id, status) => withMock(() => api.put(`/admin/agent/${id}/status`, null, { params: { status } }), () => {
    const a = mockAgents.find(a => a.id === id)
    if (a) a.status = status
    return Promise.resolve({ success: true })
  }),
  listIntents: (params) => withMock(() => api.get('/admin/intents', { params }), () => Promise.resolve({ records: [...mockIntents], total: mockIntents.length })),
  addIntent: (data) => withMock(() => api.post('/admin/intent', data), () => {
    const intent = { id: Date.now(), ...data }
    mockIntents.unshift(intent)
    return Promise.resolve(intent)
  }),
  updateIntent: (data) => withMock(() => api.put('/admin/intent', data), () => {
    const idx = mockIntents.findIndex(i => i.id === data.id)
    if (idx !== -1) mockIntents[idx] = { ...mockIntents[idx], ...data }
    return Promise.resolve({ success: true })
  }),
  deleteIntent: (id) => withMock(() => api.delete(`/admin/intent/${id}`), () => {
    mockIntents = mockIntents.filter(i => i.id !== id)
    return Promise.resolve({ success: true })
  }),
  listChannels: () => withMock(() => api.get('/admin/channels'), () => Promise.resolve([...mockChannels])),
  addChannel: (data) => withMock(() => api.post('/admin/channel', data), () => {
    const ch = { id: Date.now(), ...data }
    mockChannels.push(ch)
    return Promise.resolve(ch)
  }),
  updateChannel: (data) => withMock(() => api.put('/admin/channel', data), () => {
    const idx = mockChannels.findIndex(c => c.id === data.id)
    if (idx !== -1) mockChannels[idx] = { ...mockChannels[idx], ...data }
    return Promise.resolve({ success: true })
  }),
  listFlows: () => withMock(() => api.get('/admin/flows'), () => Promise.resolve([])),
  addFlow: (data) => withMock(() => api.post('/admin/flow', data), () => Promise.resolve({ id: Date.now() })),
  updateFlow: (data) => withMock(() => api.put('/admin/flow', data), () => Promise.resolve({ success: true })),
  listMarketingRules: () => withMock(() => api.get('/admin/marketing/rules'), () => Promise.resolve([])),
  addMarketingRule: (data) => withMock(() => api.post('/admin/marketing/rule', data), () => Promise.resolve({ id: Date.now() })),
  dailyReport: (params) => withMock(() => api.get('/admin/report/daily', { params }), () => Promise.resolve(mockReports)),
  listLogs: (params) => withMock(() => api.get('/admin/logs', { params }), () => Promise.resolve({ records: [
    { id: 1, module: 'kb', action: '新增', detail: '新增知识条目：如何查询订单', operatorType: 'admin', createTime: '2026-05-20 10:30:15' },
    { id: 2, module: 'agent', action: '状态变更', detail: '客服小林从忙碌改为在线', operatorType: 'system', createTime: '2026-05-20 09:15:03' },
    { id: 3, module: 'intent', action: '编辑', detail: '修改意图"查询订单"的回复模板', operatorType: 'admin', createTime: '2026-05-20 08:42:28' },
    { id: 4, module: 'channel', action: '新增', detail: '新增渠道：抖音企业号', operatorType: 'admin', createTime: '2026-05-19 16:20:45' },
    { id: 5, module: 'kb', action: '删除', detail: '删除知识条目ID=23（已过期）', operatorType: 'admin', createTime: '2026-05-19 14:55:12' },
    { id: 6, module: 'agent', action: '新增', detail: '新增客服账号：小陈（技能组：售后）', operatorType: 'admin', createTime: '2026-05-19 11:30:00' },
    { id: 7, module: 'kb', action: '导入', detail: '批量导入知识条目12条（来自Excel）', operatorType: 'admin', createTime: '2026-05-18 15:10:33' },
    { id: 8, module: 'agent', action: '状态变更', detail: '客服王姐从在线改为忙碌', operatorType: 'system', createTime: '2026-05-18 10:05:21' }
  ], total: 8 })),
  agentSessions: (agentId) => withMock(() => api.get(`/admin/agent/${agentId}/sessions`), () => Promise.resolve([]))
}
