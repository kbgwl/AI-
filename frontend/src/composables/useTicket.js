import { ref } from 'vue'
import { ticketApi, chatApi } from '../api'

/**
 * 工单 composable
 * 封理工单创建和满意度评价逻辑
 */
export function useTicket(sessionId) {
  const showTicketForm = ref(false)
  const ticketSubmitting = ref(false)
  const showCsat = ref(false)
  const csatScore = ref(5)
  const csatComment = ref('')

  const ticketForm = ref({
    category: '',
    priority: 0,
    subject: '',
    description: '',
    contact: ''
  })

  const ticketRules = {
    category: [{ required: true, message: '请选择问题分类', trigger: 'change' }],
    subject: [{ required: true, message: '请填写问题主题', trigger: 'blur' }],
    description: [{ required: true, message: '请详细描述您的问题', trigger: 'blur' }]
  }

  function openTicketForm() {
    ticketForm.value = { category: '', priority: 0, subject: '', description: '', contact: '' }
    showTicketForm.value = true
  }

  async function submitTicket(ticketFormRef) {
    if (!ticketFormRef) return
    try {
      await ticketFormRef.validate()
    } catch { return }

    ticketSubmitting.value = true
    try {
      const res = await ticketApi.create({
        ...ticketForm.value,
        sessionId: sessionId.value,
        channel: 'web'
      })
      showTicketForm.value = false
      return res?.ticketNo || 'TK' + Date.now()
    } finally {
      ticketSubmitting.value = false
    }
  }

  async function submitCsat(messages, scrollToBottom) {
    if (sessionId.value) {
      try { await chatApi.submitCsat({ sessionId: sessionId.value, score: csatScore.value, comment: csatComment.value }) } catch (e) {}
    }
    showCsat.value = false
    messages.value.push({ senderType: 'system', content: '感谢您的评价！', timestamp: Date.now() })
    scrollToBottom()
  }

  return {
    showTicketForm,
    ticketSubmitting,
    ticketForm,
    ticketRules,
    openTicketForm,
    submitTicket,
    showCsat,
    csatScore,
    csatComment,
    submitCsat
  }
}
