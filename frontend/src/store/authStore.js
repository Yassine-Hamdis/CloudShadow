import { create } from 'zustand'

const useAuthStore = create((set) => ({
  token:       null,
  email:       null,
  role:        null,
  companyId:   null,
  companyName: null,
  isAuthenticated: false,

  // ── Save auth data to store + localStorage ──────────────────────────────
  login: (authResponse) => {
    const { token, email, role, companyId, companyName } = authResponse

    localStorage.setItem('token',       token)
    localStorage.setItem('email',       email)
    localStorage.setItem('role',        role)
    localStorage.setItem('companyId',   String(companyId))
    localStorage.setItem('companyName', companyName)

    set({
      token,
      email,
      role,
      companyId,
      companyName,
      isAuthenticated: true,
    })
  },

  // ── Clear store + localStorage ──────────────────────────────────────────
  logout: () => {
    localStorage.clear()
    set({
      token:           null,
      email:           null,
      role:            null,
      companyId:       null,
      companyName:     null,
      isAuthenticated: false,
    })
  },

  // ── Rehydrate from localStorage on app start ────────────────────────────
  loadFromStorage: () => {
    const token       = localStorage.getItem('token')
    const email       = localStorage.getItem('email')
    const role        = localStorage.getItem('role')
    const companyId   = localStorage.getItem('companyId')
    const companyName = localStorage.getItem('companyName')

    if (token && email && role && companyId && companyName) {
      set({
        token,
        email,
        role,
        companyId:   Number(companyId),
        companyName,
        isAuthenticated: true,
      })
    }
  },
}))

export default useAuthStore