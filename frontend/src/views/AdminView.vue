<template>
  <div class="admin-layout">
    <!-- 左侧导航 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-logo" @click="sidebarCollapsed = !sidebarCollapsed">
        <div class="logo-icon-wrap">
          <span class="logo-icon">🤖</span>
        </div>
        <span class="logo-text" v-show="!sidebarCollapsed">AI客服管理</span>
        <span class="logo-collapse-hint" v-show="!sidebarCollapsed">◀</span>
      </div>

      <!-- 授权状态提示 -->
      <div v-if="!licenseStore.valid" class="license-banner" @click="$router.push('/admin/license')">
        <span class="license-banner-icon">⚠️</span>
        <span v-show="!sidebarCollapsed" class="license-banner-text">系统未授权，点击激活</span>
      </div>

      <el-menu :default-active="activeMenu" @select="handleMenuSelect" class="sidebar-menu" :collapse="sidebarCollapsed">
        <!-- 工作台 -->
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据概览</span></el-menu-item>
        <el-menu-item index="/agent-workbench"><el-icon><Service /></el-icon><span>客服工作台</span></el-menu-item>

        <!-- 客服业务 -->
        <el-sub-menu index="biz">
          <template #title><el-icon><ChatDotRound /></el-icon><span>客服业务</span></template>
          <el-menu-item index="/admin/agents" v-if="licenseStore.hasFeature('agent_manage')"><el-icon><User /></el-icon><span>客服管理</span></el-menu-item>
          <el-menu-item index="/admin/tickets" v-if="licenseStore.hasFeature('ticket_manage')"><el-icon><Tickets /></el-icon><span>工单管理</span></el-menu-item>
          <el-menu-item index="/admin/channels" v-if="licenseStore.hasFeature('channel_manage')"><el-icon><Connection /></el-icon><span>渠道管理</span></el-menu-item>
          <el-menu-item index="/admin/routing" v-if="licenseStore.hasFeature('smart_routing')"><el-icon><Guide /></el-icon><span>智能路由</span></el-menu-item>
          <el-menu-item index="/admin/quality" v-if="licenseStore.hasFeature('quality_check')"><el-icon><DocumentChecked /></el-icon><span>质量抽查</span></el-menu-item>
        </el-sub-menu>

        <!-- AI能力 -->
        <el-sub-menu index="ai">
          <template #title><el-icon><MagicStick /></el-icon><span>AI能力</span></template>
          <el-menu-item index="/admin/kb"><el-icon><Collection /></el-icon><span>知识库</span></el-menu-item>
          <el-menu-item index="/admin/intents"><el-icon><Aim /></el-icon><span>意图管理</span></el-menu-item>
          <el-menu-item index="/admin/flows"><el-icon><Share /></el-icon><span>对话流程</span></el-menu-item>
        </el-sub-menu>

        <!-- 系统管理（仅admin） -->
        <el-sub-menu v-if="authStore.userRole === 'admin'" index="sys">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/admin/orders"><el-icon><ShoppingCart /></el-icon><span>订单管理</span></el-menu-item>
          <el-menu-item index="/admin/payment-config"><el-icon><CreditCard /></el-icon><span>支付配置</span></el-menu-item>
          <el-menu-item index="/admin/system-config"><el-icon><Setting /></el-icon><span>系统配置</span></el-menu-item>
          <el-menu-item index="/admin/tenant" v-if="licenseStore.hasFeature('multi_tenant')"><el-icon><OfficeBuilding /></el-icon><span>租户管理</span></el-menu-item>
          <el-menu-item index="/admin/logs" v-if="licenseStore.hasFeature('operation_log')"><el-icon><Document /></el-icon><span>操作日志</span></el-menu-item>
        </el-sub-menu>

        <!-- 其他 -->
        <el-menu-item index="/admin/industry"><el-icon><OfficeBuilding /></el-icon><span>行业分类</span></el-menu-item>
        <el-menu-item index="/admin/integration"><el-icon><Link /></el-icon><span>系统接入</span></el-menu-item>
        <el-menu-item index="/admin/license"><el-icon><Key /></el-icon>
          <span>系统授权</span>
          <el-tag v-if="!licenseStore.valid" type="danger" size="small" style="margin-left:6px">未激活</el-tag>
          <el-tag v-else-if="licenseStore.plan === 'trial'" type="warning" size="small" style="margin-left:6px">试用</el-tag>
          <el-tag v-else type="success" size="small" style="margin-left:6px">{{ licenseStore.planName }}</el-tag>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <button class="back-chat-btn" @click="$router.push('/chat')">
          <el-icon><ChatDotRound /></el-icon>
          <span v-show="!sidebarCollapsed">返回对话</span>
        </button>
        <div class="sidebar-brand" v-show="!sidebarCollapsed">
          <span>空白格·AI</span>
        </div>
      </div>
    </div>
    <!-- 右侧内容 -->
    <div class="admin-main">
      <div class="admin-topbar">
        <div class="topbar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <!-- 消息通知 -->
          <el-popover placement="bottom-end" :width="380" trigger="click" @show="onNotifOpen">
            <template #reference>
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="topbar-badge">
                <el-button :icon="Bell" circle size="small" />
              </el-badge>
            </template>
            <div class="notif-panel">
              <div class="notif-header">
                <span class="notif-title">消息通知</span>
                <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="markAllRead">全部已读</el-button>
              </div>
              <div class="notif-list" v-if="notifications.length > 0">
                <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: !n.read }" @click="handleNotifClick(n)">
                  <div class="notif-icon" :class="n.type">{{ notifIconMap[n.type] || '📢' }}</div>
                  <div class="notif-body">
                    <div class="notif-text">{{ n.title }}</div>
                    <div class="notif-desc">{{ n.desc }}</div>
                    <div class="notif-time">{{ n.time }}</div>
                  </div>
                  <div v-if="!n.read" class="notif-dot"></div>
                </div>
              </div>
              <div v-else class="notif-empty">
                <el-empty description="暂无消息" :image-size="60" />
              </div>
              <div class="notif-footer" v-if="notifications.length > 0">
                <el-button link type="primary" size="small" @click="clearAllNotifs">清空全部</el-button>
              </div>
            </div>
          </el-popover>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="topbar-user">
              <el-avatar :size="32" :style="{background: 'var(--primary-gradient)'}">{{ authStore.userInitial }}</el-avatar>
              <span class="admin-name" v-show="!isMobile">{{ authStore.userName }}</span>
              <el-tag size="small" type="info" style="margin-left:4px" v-show="!isMobile">{{ authStore.userRoleName }}</el-tag>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">个人设置</el-dropdown-item>
                <el-dropdown-item command="password" :icon="Lock">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Bell, ArrowDown, User, Lock, SwitchButton, Guide, OfficeBuilding, DocumentChecked, Service, Link, Key, ShoppingCart, ChatDotRound, MagicStick, Setting, CreditCard } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useLicenseStore } from '../stores/license'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const licenseStore = useLicenseStore()

onMounted(async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await licenseStore.fetchFeatures()
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '数据概览')
const sidebarCollapsed = ref(false)
const isMobile = ref(false)

const notifIconMap = { ticket: '🎫', transfer: '👤', system: '⚙️', alert: '⚠️' }

const notifications = ref([
  { id: 1, type: 'ticket', title: '新工单待处理', desc: '客户"张先生"提交了退款申请工单 #WO20260521001', time: '3分钟前', read: false, route: '/admin/tickets' },
  { id: 2, type: 'transfer', title: '转人工请求', desc: '用户"李女士"请求转接人工客服，等待中...', time: '8分钟前', read: false, route: '/admin/agents' },
  { id: 3, type: 'system', title: '知识库更新完成', desc: '批量导入12条知识条目已生效', time: '25分钟前', read: false, route: '/admin/kb' },
  { id: 4, type: 'alert', title: '客服离线提醒', desc: '客服"小陈"已离线超过30分钟，当前仅2人在线', time: '1小时前', read: true, route: '/admin/agents' },
  { id: 5, type: 'system', title: '日报已生成', desc: '5月20日数据报表已自动生成，点击查看', time: '2小时前', read: true, route: '/admin/reports' },
])

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

function onNotifOpen() {}

function markAllRead() {
  notifications.value.forEach(n => { n.read = true })
  ElMessage.success('已全部标记为已读')
}

function handleNotifClick(n) {
  n.read = true
  if (n.route) router.push(n.route)
}

function clearAllNotifs() {
  notifications.value = []
  ElMessage.success('已清空全部通知')
}

let notifTimer = null
const newNotifTemplates = [
  { type: 'ticket', title: '新工单待处理', desc: '客户提交了新的售后咨询工单', route: '/admin/tickets' },
  { type: 'transfer', title: '转人工请求', desc: '有用户请求转接人工客服', route: '/admin/agents' },
  { type: 'alert', title: '满意度预警', desc: '今日满意度评分低于90%，请关注', route: '/admin/reports' },
  { type: 'system', title: '意图命中提升', desc: '"查询订单"意图识别准确率提升至96%', route: '/admin/intents' },
]
function simulateNewNotif() {
  const tmpl = newNotifTemplates[Math.floor(Math.random() * newNotifTemplates.length)]
  const id = Date.now()
  notifications.value.unshift({ ...tmpl, id, time: '刚刚', read: false })
  if (notifications.value.length > 20) notifications.value.pop()
}
onMounted(() => {
  notifTimer = setInterval(simulateNewNotif, 60000)
})
onUnmounted(() => {
  if (notifTimer) clearInterval(notifTimer)
})

function handleMenuSelect(index) {
  if (index === route.path) return
  router.push(index)
}

function handleUserCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning' })
      .then(() => {
        authStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
  else if (cmd === 'profile') { router.push('/admin/profile') }
  else if (cmd === 'password') { router.push('/admin/change-password') }
}

function checkMobile() { isMobile.value = window.innerWidth < 768 }
onMounted(() => { checkMobile(); window.addEventListener('resize', checkMobile) })
onUnmounted(() => { window.removeEventListener('resize', checkMobile) })
</script>

<style>
:root {
  --primary: #4F6EF7;
  --primary-dark: #3D5BD9;
  --primary-light: #E8EDFF;
  --primary-gradient: linear-gradient(135deg, #4F6EF7 0%, #7B5EF7 50%, #A855F7 100%);
  --bg: #F0F2F5;
  --border: #E5E6EB;
  --text-primary: #1D2129;
  --text-secondary: #4E5969;
  --text-muted: #86909C;
  --transition-fast: 0.15s ease;
  --transition-slow: 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif; background: var(--bg); }
.admin-layout { display: flex; height: 100vh; background: var(--bg); }
.sidebar { width: 240px; background: #fff; display: flex; flex-direction: column; border-right: 1px solid var(--border); flex-shrink: 0; box-shadow: 2px 0 16px rgba(0,0,0,0.04); transition: width var(--transition-slow); position: relative; z-index: 10; }
.sidebar.collapsed { width: 68px; }
.sidebar-logo { height: 64px; display: flex; align-items: center; justify-content: center; gap: 10px; background: var(--primary-gradient); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; transition: all var(--transition-slow); position: relative; flex-shrink: 0; }
.sidebar-logo:hover { filter: brightness(1.05); }
.logo-icon-wrap { width: 36px; height: 36px; border-radius: 10px; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.logo-icon { font-size: 20px; }
.logo-text { letter-spacing: 1px; white-space: nowrap; }
.logo-collapse-hint { position: absolute; right: 12px; font-size: 10px; opacity: 0.6; }
.sidebar.collapsed .logo-collapse-hint { display: none; }
.sidebar-menu { flex: 1; border-right: none !important; padding: 8px 0; overflow-y: auto; overflow-x: hidden; }
.sidebar-menu :deep(.el-menu-item) { height: 46px; line-height: 46px; margin: 3px 10px; border-radius: 10px; color: var(--text-secondary); font-size: 14px; transition: all var(--transition-fast); }
.sidebar-menu :deep(.el-menu-item:hover) { background: var(--primary-light); color: var(--primary); transform: translateX(3px); }
.sidebar-menu :deep(.el-menu-item.is-active) { background: var(--primary-gradient) !important; color: #fff !important; font-weight: 600; box-shadow: 0 2px 12px rgba(79,110,247,0.35); transform: translateX(3px); }
.sidebar-menu :deep(.el-menu-item .el-icon) { font-size: 18px; margin-right: 10px; }
.sidebar-menu :deep(.el-sub-menu__title) { height: 46px; line-height: 46px; margin: 3px 10px; border-radius: 10px; color: var(--text-secondary); font-size: 14px; transition: all var(--transition-fast); }
.sidebar-menu :deep(.el-sub-menu__title:hover) { background: var(--primary-light); color: var(--primary); }
.sidebar-menu :deep(.el-sub-menu .el-menu-item) { padding-left: 48px !important; min-width: 0; }
.sidebar.collapsed .sidebar-menu :deep(.el-menu-item), .sidebar.collapsed .sidebar-menu :deep(.el-sub-menu__title) { margin: 3px 6px; justify-content: center; padding: 0 !important; }
.sidebar.collapsed .sidebar-menu :deep(.el-menu-item .el-icon), .sidebar.collapsed .sidebar-menu :deep(.el-sub-menu__title .el-icon) { margin-right: 0; }
.sidebar-footer { padding: 12px; border-top: 1px solid var(--border); flex-shrink: 0; }
.back-chat-btn { width: 100%; height: 42px; border: 1.5px solid var(--primary); border-radius: 10px; background: #fff; color: var(--primary); font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; transition: all var(--transition-fast); }
.back-chat-btn:hover { background: var(--primary-gradient); color: #fff; box-shadow: 0 4px 12px rgba(79,110,247,0.3); transform: translateY(-1px); }
.sidebar-brand { text-align: center; margin-top: 8px; font-size: 12px; color: var(--text-muted); letter-spacing: 2px; }
.admin-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.admin-topbar { height: 60px; background: #fff; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0; box-shadow: 0 1px 8px rgba(0,0,0,0.04); }
.topbar-left { display: flex; align-items: center; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.topbar-badge { margin-right: 8px; }
.topbar-user { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 6px 14px; border-radius: 10px; transition: all var(--transition-fast); }
.topbar-user:hover { background: #F7F8FA; }
.admin-name { font-size: 14px; color: var(--text-secondary); font-weight: 500; }
.license-banner { display: flex; align-items: center; gap: 8px; padding: 8px 14px; margin: 8px 10px; border-radius: 8px; background: #fff2f0; border: 1px solid #ffccc7; cursor: pointer; font-size: 13px; color: #cf1322; }
.license-banner:hover { background: #fff1f0; }
.license-banner-icon { font-size: 16px; flex-shrink: 0; }
.license-banner-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sidebar.collapsed .license-banner { padding: 8px; justify-content: center; margin: 8px 6px; }
.sidebar.collapsed .license-banner-text { display: none; }
.notif-panel { margin: -12px; }
.notif-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid var(--border); }
.notif-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.notif-list { max-height: 360px; overflow-y: auto; }
.notif-item { display: flex; align-items: flex-start; gap: 10px; padding: 12px 16px; cursor: pointer; transition: all var(--transition-fast); border-bottom: 1px solid #f5f5f5; }
.notif-item:hover { background: #f7f8fa; }
.notif-item.unread { background: #f0f5ff; }
.notif-icon { font-size: 20px; flex-shrink: 0; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.notif-icon.ticket { background: #ecf5ff; }
.notif-icon.transfer { background: #f0f9eb; }
.notif-icon.system { background: #f4f4f5; }
.notif-icon.alert { background: #fdf6ec; }
.notif-body { flex: 1; min-width: 0; }
.notif-text { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.notif-desc { font-size: 12px; color: var(--text-muted); margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notif-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
.notif-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary); flex-shrink: 0; margin-top: 6px; }
.notif-empty { padding: 24px 0; }
.notif-footer { padding: 8px 16px; text-align: center; border-top: 1px solid var(--border); }
.admin-content { flex: 1; overflow-y: auto; padding: 20px 24px; }
@media (max-width: 768px) {
  .sidebar { width: 68px; }
  .sidebar .logo-text, .sidebar .logo-collapse-hint { display: none; }
  .sidebar-menu :deep(.el-menu-item span) { display: none; }
  .sidebar-menu :deep(.el-menu-item) { justify-content: center; padding: 0 !important; }
  .sidebar-menu :deep(.el-menu-item .el-icon) { margin-right: 0; }
  .sidebar-menu :deep(.el-sub-menu__title span) { display: none; }
  .sidebar-menu :deep(.el-sub-menu__title) { justify-content: center; padding: 0 !important; }
  .sidebar-menu :deep(.el-sub-menu__title .el-icon) { margin-right: 0; }
  .back-chat-btn span { display: none; }
  .admin-content { padding: 12px; }
  .admin-topbar { padding: 0 12px; }
  .license-banner-text { display: none; }
  .license-banner { padding: 8px; justify-content: center; }
}
</style>
