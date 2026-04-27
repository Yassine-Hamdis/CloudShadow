import api from './axios'

/**
 * GET /api/alerts
 * @returns {Promise<AlertResponse[]>}
 */
export const getAlerts = () =>
  api.get('/api/alerts')

/**
 * GET /api/alerts/server/{serverId}
 * @param {number} serverId
 * @returns {Promise<AlertResponse[]>}
 */
export const getAlertsByServer = (serverId) =>
  api.get(`/api/alerts/server/${serverId}`)

/**
 * GET /api/alerts/severity/{severity}
 * @param {'WARNING'|'CRITICAL'} severity
 * @returns {Promise<AlertResponse[]>}
 */
export const getAlertsBySeverity = (severity) =>
  api.get(`/api/alerts/severity/${severity}`)

/**
 * GET /api/alerts/type/{type}
 * @param {'CPU'|'Memory'|'Disk'} type
 * @returns {Promise<AlertResponse[]>}
 */
export const getAlertsByType = (type) =>
  api.get(`/api/alerts/type/${type}`)

/**
 * GET /api/alerts/critical/count
 * @returns {Promise<number>}
 */
export const getCriticalCount = () =>
  api.get('/api/alerts/critical/count')