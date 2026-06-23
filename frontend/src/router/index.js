import { createRouter, createWebHistory } from 'vue-router'
import { useLicenseStore } from '../stores/license'

const routes = [
  {
    path: '/',
    redirect: '/chat'
  },
  {
    path: '/license',
    name: 'License',
    component: () => import('../views/LicenseView.vue'),
    meta: { title: '系统授权', guest: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    meta: { title: 'AI客服对话', icon: 'ChatDotRound' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/agent-workbench',
    name: 'AgentWorkbench',
    component: () => import('../views/admin/AgentWorkbench.vue'),
    meta: { title: '客服工作台', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/AdminView.vue'),
    meta: { title: '管理后台', icon: 'Setting', requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '数据概览', requiresAuth: true, feature: 'dashboard' } },
      { path: 'kb', name: 'KnowledgeBase', component: () => import('../views/admin/KnowledgeBase.vue'), meta: { title: '知识库管理', requiresAuth: true, feature: 'knowledge_base' } },
      { path: 'intents', name: 'Intents', component: () => import('../views/admin/Intents.vue'), meta: { title: '意图管理', requiresAuth: true, feature: 'intent_manage' } },
      { path: 'agents', name: 'Agents', component: () => import('../views/admin/Agents.vue'), meta: { title: '客服管理', requiresAuth: true, feature: 'agent_manage' } },
      { path: 'tickets', name: 'Tickets', component: () => import('../views/admin/Tickets.vue'), meta: { title: '工单管理', requiresAuth: true, feature: 'ticket_manage' } },
      { path: 'channels', name: 'Channels', component: () => import('../views/admin/Channels.vue'), meta: { title: '渠道管理', requiresAuth: true, feature: 'channel_manage' } },
      { path: 'routing', name: 'Routing', component: () => import('../views/admin/Routing.vue'), meta: { title: '智能路由', requiresAuth: true, feature: 'smart_routing' } },
      { path: 'quality', name: 'QualityCheck', component: () => import('../views/admin/QualityCheck.vue'), meta: { title: '质量抽查', requiresAuth: true, feature: 'quality_check' } },
      { path: 'flows', name: 'Flows', component: () => import('../views/admin/Flows.vue'), meta: { title: '对话流程', requiresAuth: true, feature: 'dialog_flow' } },
      { path: 'logs', name: 'Logs', component: () => import('../views/admin/Logs.vue'), meta: { title: '操作日志', requiresAuth: true, feature: 'operation_log' } },
      { path: 'orders', name: 'Orders', component: () => import('../views/admin/OrderManage.vue'), meta: { title: '订单管理', requiresAuth: true } },
      { path: 'payment-config', name: 'PaymentConfig', component: () => import('../views/admin/PaymentConfig.vue'), meta: { title: '支付配置', requiresAuth: true } },
      { path: 'system-config', name: 'SystemConfig', component: () => import('../views/admin/SystemConfig.vue'), meta: { title: '系统配置', requiresAuth: true } },
      { path: 'industry', name: 'Industry', component: () => import('../views/industry/IndustryManage.vue'), meta: { title: '行业分类', requiresAuth: true } },
      { path: 'integration', name: 'Integration', component: () => import('../views/admin/Integration.vue'), meta: { title: '系统接入', requiresAuth: true } },
      { path: 'tenant', name: 'TenantManage', component: () => import('../views/admin/TenantManage.vue'), meta: { title: '租户管理', requiresAuth: true, feature: 'multi_tenant' } },
      { path: 'license', name: 'LicenseManage', component: () => import('../views/LicenseView.vue'), meta: { title: '系统授权', requiresAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('../views/admin/ProfileView.vue'), meta: { title: '个人设置', requiresAuth: true } },
      { path: 'change-password', name: 'ChangePassword', component: () => import('../views/admin/ChangePasswordView.vue'), meta: { title: '修改密码', requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || 'AI客服'} — 空白格·AI`

  const user = JSON.parse(localStorage.getItem('ai_cs_user') || 'null')

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!user) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    const requiredFeature = to.meta.feature
    if (requiredFeature) {
      const licenseStore = useLicenseStore()
      if (!licenseStore.hasFeature(requiredFeature)) {
        next({ path: '/admin/license' })
        return
      }
    }
  }

  if (to.meta.guest && user && to.name !== 'License') {
    next({ path: '/admin/dashboard' })
    return
  }

  next()
})

export default router
