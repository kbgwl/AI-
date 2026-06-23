import { api } from './client'

const STATUS_MAP = { 0: 'pending', 1: 'processing', 2: 'resolved', 3: 'closed' }
const STATUS_REVERSE = { pending: 0, processing: 1, resolved: 2, closed: 3 }

function toFrontend(ticket) {
  return {
    ...ticket,
    status: STATUS_REVERSE[ticket.status] ?? 0,
    subject: ticket.title || ticket.subject || '',
    category: ticket.ticketType || ticket.category || '',
    assignee: ticket.assigneeName || ticket.assignee || '',
    contact: ticket.userId || ticket.contact || '',
  }
}

function toBackend(data) {
  const out = { ...data }
  if (typeof out.status === 'number') out.status = STATUS_MAP[out.status] || 'pending'
  if (out.subject && !out.title) out.title = out.subject
  if (out.category && !out.ticketType) out.ticketType = out.category
  return out
}

export const ticketApi = {
  create: async (data) => {
    const res = await api.post('/admin/tickets', toBackend(data))
    return res
  },
  list: async (params) => {
    const query = {}
    if (params) {
      if (params.status !== undefined && params.status !== null && params.status !== '') query.status = STATUS_MAP[params.status] || params.status
      if (params.type) query.type = params.type
      if (params.priority) query.priority = params.priority
    }
    query.page = params?.page || 1
    query.size = params?.size || 10
    const res = await api.get('/admin/tickets', { params: query })
    if (res && res.records) {
      res.records = res.records.map(toFrontend)
    }
    return res
  },
  get: async (id) => {
    const res = await api.get(`/admin/tickets/${id}`)
    return res ? toFrontend(res) : res
  },
  assign: async (id, agentId) => {
    const res = await api.put(`/admin/tickets/${id}/assign`, { agentId, agentName: '' })
    return res
  },
  resolve: async (id, resolution) => {
    const res = await api.put(`/admin/tickets/${id}/resolve`, { solution: resolution })
    return res
  },
  close: async (id) => {
    const res = await api.put(`/admin/tickets/${id}/close`)
    return res
  },
  delete: async (id) => {
    const res = await api.delete(`/admin/tickets/${id}`)
    return res
  },
  getLogs: async (id) => {
    return []
  }
}
