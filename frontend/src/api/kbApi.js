import { api, withMock } from './client'

let mockCategories = [
  { id: 1, name: '订单问题', icon: '📦' },
  { id: 2, name: '退换货', icon: '🔄' },
  { id: 3, name: '产品介绍', icon: '💡' },
  { id: 4, name: '价格套餐', icon: '💰' },
  { id: 5, name: '技术支持', icon: '🔧' }
]

let mockKbItems = [
  { id: 1, question: '如何查询订单状态？', similarQuestions: ['查订单', '订单在哪', '物流信息', '怎么查快递'], answer: '登录账户→我的订单→查看订单详情，可实时跟踪物流信息。支持按订单号、手机号、商品名查询。', categoryId: 1, itemType: 'faq', hitCount: 156, status: 1, priority: 10 },
  { id: 2, question: '退货流程是什么？', similarQuestions: ['怎么退货', '退款流程', '退换货政策', '七天无理由'], answer: '登录账户→我的订单→申请退货→填写原因→寄回商品→退款。7 天内可申请，需保持原包装。', categoryId: 2, itemType: 'faq', hitCount: 89, status: 1, priority: 10 },
  { id: 3, question: 'AI 客服系统怎么收费？', similarQuestions: ['多少钱', '价格表', '贵吗', '有优惠吗', '年付价格'], answer: '基础版¥999/月，专业版¥2999/月，企业版¥9999 月起。支持年付优惠，详询销售。', categoryId: 4, itemType: 'faq', hitCount: 67, status: 1, priority: 10 },
  { id: 4, question: '支持哪些渠道接入？', similarQuestions: ['能接抖音吗', '支持微信吗', '全渠道', '多平台接入'], answer: '支持网页、微信公众号、微信小程序、抖音、小红书、APP 等多渠道统一接入管理。', categoryId: 3, itemType: 'guide', hitCount: 45, status: 1, priority: 8 },
  { id: 5, question: '如何配置知识库？', similarQuestions: ['怎么添加问题', '知识库管理', '批量导入'], answer: '管理后台→知识库→新增分类→添加知识条目，支持批量导入 Excel/CSV，也可手动逐条添加。', categoryId: 5, itemType: 'guide', hitCount: 34, status: 1, priority: 5 },
  { id: 6, question: '退款多久到账？', similarQuestions: ['退款时间', '什么时候退钱', '退款慢'], answer: '原路退款 3-5 个工作日到账，余额退款即时到账。退款进度可在"我的订单"中查看。', categoryId: 2, itemType: 'faq', hitCount: 72, status: 1, priority: 9 },
  { id: 7, question: '如何修改收货地址？', similarQuestions: ['地址错了', '改地址', '收货人电话错了'], answer: '订单未发货前：我的订单→修改地址；已发货后需联系人工客服协助处理。', categoryId: 1, itemType: 'faq', hitCount: 58, status: 1, priority: 8 },
  { id: 8, question: 'AI 客服能做什么？', similarQuestions: ['功能', '有什么用', '能自动回复吗', '智能吗'], answer: '7×24 小时自动接待、智能问答、订单查询、退换货指引、工单创建、转人工客服等功能。', categoryId: 3, itemType: 'faq', hitCount: 93, status: 1, priority: 10 },
  { id: 9, question: '专业版和基础版有什么区别？', similarQuestions: ['版本对比', '买哪个划算', '功能差异'], answer: '专业版额外包含：多渠道接入、坐席管理、数据报表、对话流程编排、VIP 专属功能。', categoryId: 4, itemType: 'faq', hitCount: 41, status: 1, priority: 7 },
  { id: 10, question: '换货流程是什么？', similarQuestions: ['换货', '发错货', '质量问题'], answer: '登录→我的订单→申请换货→选择换货原因和商品→寄回原商品→收到新商品，约 5-7 个工作日。', categoryId: 2, itemType: 'faq', hitCount: 37, status: 1, priority: 7 },
  { id: 11, question: '如何接入微信公众号？', similarQuestions: ['公众号配置', '微信对接'], answer: '管理后台→渠道管理→新增渠道→选择微信公众号→填写 AppID 和 AppSecret→配置消息服务器 URL→完成。', categoryId: 5, itemType: 'guide', hitCount: 28, status: 1, priority: 6 },
  { id: 12, question: '数据报表包含哪些内容？', similarQuestions: ['看什么数据', '有分析报表吗'], answer: '包含会话量趋势、机器人解决率、转人工率、满意度评分、平均响应时间等核心指标，支持按日期和渠道筛选。', categoryId: 3, itemType: 'guide', hitCount: 22, status: 1, priority: 5 },
  { id: 13, question: '坐席最多支持多少人同时在线？', similarQuestions: ['多少人用', '账号限制'], answer: '基础版 5 人，专业版 20 人，企业版不限。支持按技能组分配和负载均衡。', categoryId: 4, itemType: 'faq', hitCount: 19, status: 1, priority: 5 },
  { id: 14, question: '如何设置工作时间？', similarQuestions: ['下班了怎么办', '非工作时间'], answer: '管理后台→渠道管理→编辑渠道→设置工作时间→非工作时间自动由 AI 接管，留言转工单。', categoryId: 5, itemType: 'guide', hitCount: 15, status: 1, priority: 4 },
  { id: 15, question: '支持海外用户使用吗？', similarQuestions: ['国外能用吗', '国际化'], answer: '目前主要服务国内用户，海外用户可通过网页渠道访问，暂不支持海外社交媒体接入。', categoryId: 3, itemType: 'faq', hitCount: 8, status: 1, priority: 3 },
  { id: 16, question: '支持私有化部署吗？', similarQuestions: ['本地部署', '私有云', '数据存在本地', '内网部署'], answer: '企业版支持私有化部署（本地服务器/私有云），包含完整的数据隔离、定制开发和专属技术支持服务。', categoryId: 5, itemType: 'guide', hitCount: 15, status: 1, priority: 9 },
  { id: 17, question: '有 API 接口文档吗？', similarQuestions: ['开放接口吗', 'API 文档在哪', '接口调用'], answer: '专业版和企业版提供完整的 RESTful API 文档，支持工单创建、用户管理、数据导出等功能。', categoryId: 5, itemType: 'guide', hitCount: 12, status: 1, priority: 8 },
  { id: 18, question: '接不接外包开发？', similarQuestions: ['定制开发', '量身定做', '二次开发', '私有化定制', 'OEM 贴牌'], answer: '您好！我们提供**专业定制开发与外包服务**（包括系统定制、功能开发、OEM 贴牌等）。', categoryId: 5, itemType: 'guide', hitCount: 0, status: 1, priority: 10 },
  { id: 19, question: '有短视频案例吗？', similarQuestions: ['案例展示', '客户案例', '成功案例', '短视频作品'], answer: '有的！我们帮助多家企业实现了 AI 短视频自动化生产，包括电商带货、知识付费、企业宣传等场景。', categoryId: 3, itemType: 'faq', hitCount: 0, status: 1, priority: 8 },
  { id: 20, question: '怎么联系你们？', similarQuestions: ['客服电话', '联系电话', '电话号码', '邮箱', '地址', '公司地址', '工作时间'], answer: '📞 **客服热线**：400-123-4567（工作日 9:00-18:00）\n📧 **商务邮箱**：contact@jnysx.ai\n📍 **公司地址**：山东省济南市历下区', categoryId: 5, itemType: 'guide', hitCount: 0, status: 1, priority: 10 }
]

export const kbApi = {
  listCategories: (parentId = 0) => withMock(() => api.get('/kb/categories', { params: { parentId } }), () => Promise.resolve(mockCategories)),
  addCategory: (data) => withMock(() => api.post('/kb/category', data), () => {
    const cat = { id: Date.now(), ...data }
    mockCategories.push(cat)
    return Promise.resolve(cat)
  }),
  updateCategory: (data) => withMock(() => api.put('/kb/category', data), () => {
    const idx = mockCategories.findIndex(c => c.id === data.id)
    if (idx !== -1) mockCategories[idx] = { ...mockCategories[idx], ...data }
    return Promise.resolve({ success: true })
  }),
  deleteCategory: (id) => withMock(() => api.delete(`/kb/category/${id}`), () => {
    mockCategories = mockCategories.filter(c => c.id !== id)
    return Promise.resolve({ success: true })
  }),
  listIndustries: () => withMock(() => api.get('/kb/industries'), () => Promise.resolve([
    { id: 1, industryName: '电商零售', sortId: 1, icon: '🛒', description: '电商平台、零售行业' },
    { id: 2, industryName: '教育培训', sortId: 2, icon: '📚', description: '在线教育、培训机构' },
    { id: 3, industryName: '医疗健康', sortId: 3, icon: '🏥', description: '医疗机构、健康咨询' },
    { id: 4, industryName: '金融服务', sortId: 4, icon: '💰', description: '银行、保险、证券' },
    { id: 5, industryName: '技术服务', sortId: 5, icon: '🔧', description: 'IT 服务、软件开发' }
  ])),
  addIndustry: (data) => withMock(() => api.post('/kb/industry', data), () => Promise.resolve({ id: Date.now(), ...data })),
  updateIndustry: (data) => withMock(() => api.put('/kb/industry', data), () => Promise.resolve({ success: true })),
  deleteIndustry: (id) => withMock(() => api.delete(`/kb/industry/${id}`), () => Promise.resolve({ success: true })),
  listItems: async (params) => { const res = await api.get('/kb/items', { params }); return res },
  getItem: async (id) => { const res = await api.get(`/kb/item/${id}`); return res },
  addItem: async (data) => { const res = await api.post('/kb/item', data); return res },
  updateItem: async (data) => { const res = await api.put('/kb/item', data); return res },
  deleteItem: async (id) => { const res = await api.delete(`/kb/item/${id}`); return res },
  batchImport: async (items) => { const res = await api.post('/kb/items/batch', items); return res },
  listCategories: async () => { const res = await api.get('/kb/categories'); return res },
  addCategory: async (data) => { const res = await api.post('/kb/category', data); return res },
  updateCategory: async (data) => { const res = await api.put('/kb/category', data); return res },
  deleteCategory: async (id) => { const res = await api.delete(`/kb/category/${id}`); return res },
  listIndustries: async () => { const res = await api.get('/kb/industries'); return res },
  listUnknown: async (params) => { const res = await api.get('/kb/unknown', { params }); return res },
  answerUnknown: async (id, data) => { const res = await api.post(`/kb/unknown/${id}/answer`, data); return res },
  deleteUnknown: async (id) => { const res = await api.delete(`/kb/unknown/${id}`); return res },
}
