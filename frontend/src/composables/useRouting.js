import { ref } from 'vue'
import { adminApi } from '../api'

/**
 * 智能路由 composable
 * 封装路由匹配逻辑
 */
export function useRouting() {
  const routingLoading = ref(false)

  async function matchRoute(messages) {
    const lastUserMsg = messages.filter(m => m.senderType === 'user').slice(-1)[0]
    routingLoading.value = true
    try {
      const routingRes = await adminApi.routeMatch({
        intent: lastUserMsg?.intent || 'general_query',
        content: lastUserMsg?.content || '',
        vipLevel: ''
      })
      return {
        targetGroup: routingRes?.targetGroupName || '默认组',
        waitTime: routingRes?.waitTime || 30
      }
    } finally {
      routingLoading.value = false
    }
  }

  return { matchRoute, routingLoading }
}
