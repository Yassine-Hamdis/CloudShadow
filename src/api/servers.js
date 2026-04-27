import api from './axios'

/**
 * GET /api/servers
 * @returns {Promise<ServerResponse[]>}
 */
export const getServers = () =>
  api.get('/api/servers')

/**
 * GET /api/servers/{id}
 * @param {number} id
 * @returns {Promise<ServerResponse>}
 */
export const getServer = (id) =>
  api.get(`/api/servers/${id}`)

/**
 * GET /api/servers/{id}/instructions
 * @param {number} id
 * @returns {Promise<ServerResponse>} ← includes installInstructions
 */
export const getServerInstructions = (id) =>
  api.get(`/api/servers/${id}/instructions`)

/**
 * POST /api/servers
 * @param {{ name: string }} data
 * @returns {Promise<ServerResponse>} ← includes installInstructions + token
 */
export const createServer = (data) =>
  api.post('/api/servers', data)

/**
 * DELETE /api/servers/{id}
 * @param {number} id
 * @returns {Promise<null>}
 */
export const deleteServer = (id) =>
  api.delete(`/api/servers/${id}`)