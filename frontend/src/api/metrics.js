import api from './axios'

/**
 * GET /api/metrics
 * @returns {Promise<MetricResponse[]>}
 */
export const getAllMetrics = () =>
  api.get('/api/metrics')

/**
 * GET /api/metrics/server/{serverId}
 * @param {number} serverId
 * @returns {Promise<MetricResponse[]>}
 */
export const getMetricsByServer = (serverId) =>
  api.get(`/api/metrics/server/${serverId}`)

/**
 * GET /api/metrics/server/{serverId}/latest
 * @param {number} serverId
 * @returns {Promise<MetricResponse>}
 */
export const getLatestMetric = (serverId) =>
  api.get(`/api/metrics/server/${serverId}/latest`)

/**
 * GET /api/metrics/range?from=<ISO>&to=<ISO>
 * @param {string} from  ISO string
 * @param {string} to    ISO string
 * @returns {Promise<MetricResponse[]>}
 */
export const getMetricsByRange = (from, to) =>
  api.get('/api/metrics/range', { params: { from, to } })