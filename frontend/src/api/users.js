import api from './axios'

/**
 * GET /api/users
 * @returns {Promise<UserResponse[]>}
 */
export const getUsers = () =>
  api.get('/api/users')

/**
 * GET /api/users/{id}
 * @param {number} id
 * @returns {Promise<UserResponse>}
 */
export const getUser = (id) =>
  api.get(`/api/users/${id}`)

/**
 * POST /api/users
 * @param {{ email: string, password: string, role: string }} data
 * @returns {Promise<UserResponse>}
 */
export const createUser = (data) =>
  api.post('/api/users', data)

/**
 * DELETE /api/users/{id}
 * @param {number} id
 * @returns {Promise<null>}
 */
export const deleteUser = (id) =>
  api.delete(`/api/users/${id}`)