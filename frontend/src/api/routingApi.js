import { api, withMock } from './client'

let mockRoutingRules = [
  { id: 1, ruleName: '订单问题路由售后', conditionType: 'skill', conditionValue: { skillGroup: 'after_sale' }, targetGroup: 'after_sale', priority: 90, enabled: true },
  { id: 2, ruleName: 'VIP客户优先服务', conditionType: 'vip', conditionValue: { vipLevel: ['gold', 'platinum'] }, targetGroup: 'vip', priority: 95, enabled: true },
  { id: 3, ruleName: '负载均衡分配', conditionType: 'load', conditionValue: { maxLoad: 10 }, targetGroup: 'default', priority: 50, enabled: true },
  { id: 4, ruleName: '退款关键词路由', conditionType: 'keyword', conditionValue: { keywords: ['退款', '退钱', '不想要'] }, targetGroup: 'after_sale', priority: 80, enabled: true },
  { id: 5, ruleName: '价格咨询路由售前', conditionType: 'skill', conditionValue: { skillGroup: 'sales' }, targetGroup: 'sales', priority: 70, enabled: false },
  { id: 6, ruleName: '投诉关键词升级', conditionType: 'keyword', conditionValue: { keywords: ['投诉', '不满意', '差评'] }, targetGroup: 'vip', priority: 85, enabled: true }
]

export const routingApi = {
  listRoutingRules: () => withMock(() => api.get('/admin/routing/rules'), () => Promise.resolve([...mockRoutingRules])),
  saveRoutingRule: (data) => withMock(() => api.post('/admin/routing/rule', data), () => {
    if (data.id) {
      const idx = mockRoutingRules.findIndex(r => r.id === data.id)
      if (idx !== -1) mockRoutingRules[idx] = { ...mockRoutingRules[idx], ...data }
    } else {
      mockRoutingRules.unshift({ id: Date.now(), ...data })
    }
    return Promise.resolve({ success: true })
  }),
  deleteRoutingRule: (id) => withMock(() => api.delete(`/admin/routing/rule/${id}`), () => {
    mockRoutingRules = mockRoutingRules.filter(r => r.id !== id)
    return Promise.resolve({ success: true })
  }),
  routeMatch: (data) => withMock(
    () => api.post('/routing/match', data),
    () => {
      const { intent, content, vipLevel } = data
      const rules = [
        { conditionType: 'vip', priority: 95, targetGroup: 'vip', match: () => vipLevel && ['gold','platinum','diamond'].includes(vipLevel) },
        { conditionType: 'keyword', priority: 90, targetGroup: 'after_sale', keywords: ['订单','退货','退款','退钱','换货','物流','发货'], match: () => !intent || ['query_order','refund','exchange','refund_inquiry'].includes(intent) },
        { conditionType: 'keyword', priority: 85, targetGroup: 'vip', keywords: ['投诉','不满意','差评'], match: () => intent === 'complaint' || (content && /投诉|不满意|差评/.test(content)) },
        { conditionType: 'skill', priority: 80, targetGroup: 'sales', skillGroup: 'pre_sale', match: () => ['product_info','price_inquiry'].includes(intent) },
        { conditionType: 'load', priority: 50, targetGroup: 'default', match: () => true }
      ]
      for (const rule of rules.sort((a,b) => b.priority - a.priority)) {
        if (rule.match()) {
          const groupMap = { after_sale: '售后组', sales: '售前组', vip: 'VIP组', default: '默认组' }
          return Promise.resolve({ targetGroup: rule.targetGroup, targetGroupName: groupMap[rule.targetGroup], matchedRule: rule.conditionType, waitTime: rule.targetGroup === 'vip' ? 5 : 30 + Math.floor(Math.random() * 60) })
        }
      }
      return Promise.resolve({ targetGroup: 'default', targetGroupName: '默认组', matchedRule: 'fallback', waitTime: 60 })
    }
  )
}
