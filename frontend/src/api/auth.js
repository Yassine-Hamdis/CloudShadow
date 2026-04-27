import api from './axios'

/**
 * POST /api/auth/login
 * @param {{ email: string, password: string }} credentials
 * @returns {Promise<AuthResponse>}
 */
export const login = (credentials) =>
  api.post('/api/auth/login', credentials)

/**
 * POST /api/auth/register
 * @param {{ companyName: string, email: string, password: string }} data
 * @returns {Promise<AuthResponse>}
 */
export const register = (data) =>
  api.post('/api/auth/register', data)