// Re-export all API modules for backward compatibility
export { api, MOCK_FIRST, withMock } from './client'
export { chatApi } from './chatApi'
export { kbApi } from './kbApi'
export { ticketApi } from './ticketApi'
export { adminApi } from './adminApi'
export { routingApi } from './routingApi'

// Default export for backward compatibility
import api from './client'
export default api
