import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminApi } from '../api/index'

const MOCK_ACCOUNTS = {
  admin: { password: 'admin123', id: 1, username: 'admin', nickname: '超级管理员', role: 'admin', avatar: null },
  manager: { password: 'manager123', id: 2, username: 'manager', nickname: '运营主管', role: 'manager', avatar: null },
  agent01: { password: 'agent123', id: 3, username: 'agent01', nickname: '客服小林', role: 'agent', avatar: null }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('ai_cs_user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const userName = computed(() => user.value?.nickname || user.value?.username || '')
  const userRole = computed(() => user.value?.role || '')
  const userInitial = computed(() => {
    const name = userName.value
    return name ? name.charAt(0) : '?'
  })

  const roleNameMap = { admin: '超级管理员', manager: '运营主管', agent: '客服坐席' }
  const userRoleName = computed(() => roleNameMap[userRole.value] || '未知角色')

  async function login(username, password) {
    try {
      const res = await adminApi.login({ username, password })
      user.value = res
      persist()
      return res
    } catch (e) {
      const account = MOCK_ACCOUNTS[username]
      if (account && account.password === password) {
        const mockUser = { id: account.id, username: account.username, nickname: account.nickname, role: account.role, avatar: account.avatar }
        user.value = mockUser
        persist()
        return mockUser
      }
      throw new Error('账号或密码错误')
    }
  }

  function logout() {
    user.value = null
    localStorage.removeItem('ai_cs_user')
  }

  function persist() {
    localStorage.setItem('ai_cs_user', JSON.stringify(user.value))
  }

  return { user, isLoggedIn, userName, userRole, userRoleName, userInitial, login, logout, persist }
})