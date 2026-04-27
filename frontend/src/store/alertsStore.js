import { create } from 'zustand'

const byTimestampDesc = (a, b) =>
  new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()

const ACK_KEY = 'alerts:lastAcknowledgedCriticalAt'

const readAckTimestamp = () => {
  const raw = localStorage.getItem(ACK_KEY)
  if (!raw) return null
  const ms = new Date(raw).getTime()
  return Number.isFinite(ms) ? raw : null
}

const countUnseenCritical = (alerts, ackTimestamp) => {
  const ackMs = ackTimestamp ? new Date(ackTimestamp).getTime() : null
  return alerts.filter((a) => {
    if (a.severity !== 'CRITICAL') return false
    if (!Number.isFinite(ackMs)) return true
    const ts = new Date(a.timestamp).getTime()
    return Number.isFinite(ts) && ts > ackMs
  }).length
}

const useAlertsStore = create((set) => ({
  alerts:        [],
  criticalCount: 0,
  lastAcknowledgedCriticalAt: readAckTimestamp(),

  // ── Replace all alerts ─────────────────────────────────────────────────
  setAlerts: (alerts) => {
    set((state) => {
      const sorted = [...alerts].sort(byTimestampDesc)
      return {
        alerts: sorted,
        criticalCount: countUnseenCritical(sorted, state.lastAcknowledgedCriticalAt),
      }
    })
  },

  // ── Insert new alert at the top (real-time) ────────────────────────────
  prependAlert: (alert) => {
    set((state) => ({
      alerts: [alert, ...state.alerts].sort(byTimestampDesc),
      criticalCount:
        alert.severity === 'CRITICAL' &&
        (!state.lastAcknowledgedCriticalAt ||
          new Date(alert.timestamp).getTime() > new Date(state.lastAcknowledgedCriticalAt).getTime())
          ? state.criticalCount + 1
          : state.criticalCount,
    }))
  },

  // ── Set critical count from API ────────────────────────────────────────
  setCriticalCount: (count) => {
    set((state) => {
      if (state.lastAcknowledgedCriticalAt) {
        return {
          criticalCount: countUnseenCritical(state.alerts, state.lastAcknowledgedCriticalAt),
        }
      }
      return { criticalCount: count }
    })
  },

  // ── Clear critical count when user acknowledges notifications ─────────
  clearCriticalCount: () => {
    const now = new Date().toISOString()
    localStorage.setItem(ACK_KEY, now)
    set({
      criticalCount: 0,
      lastAcknowledgedCriticalAt: now,
    })
  },

  // ── Increment critical count (on real-time CRITICAL alert) ─────────────
  incrementCriticalCount: () => {
    set((state) => ({
      criticalCount: state.criticalCount + 1,
    }))
  },

  // ── Clear on logout ────────────────────────────────────────────────────
  clearAlerts: () => {
    localStorage.removeItem(ACK_KEY)
    set({ alerts: [], criticalCount: 0, lastAcknowledgedCriticalAt: null })
  },
}))

export default useAlertsStore