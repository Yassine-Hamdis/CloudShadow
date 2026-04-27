import { create } from 'zustand'

const LIVE_WINDOW = 300

const byTimestampAsc = (a, b) =>
  new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()

const useMetricsStore = create((set) => ({
  // Map<serverId, MetricResponse[]>
  metricsByServer: {},
  // Map<serverId, MetricResponse>
  latestByServer:  {},

  // ── Replace metrics for a specific server ──────────────────────────────
  setMetrics: (serverId, metrics) => {
    set((state) => ({
      metricsByServer: {
        ...state.metricsByServer,
        // Keep the full fetched range so 1h/6h/24h/7d can render correctly.
        [serverId]: [...metrics].sort(byTimestampAsc),
      },
    }))
  },

  // ── Set the latest metric for a server ─────────────────────────────────
  setLatest: (serverId, metric) => {
    set((state) => ({
      latestByServer: {
        ...state.latestByServer,
        [serverId]: metric,
      },
    }))
  },

  // ── Append a new metric (sliding window) ───────────────────────────────
  appendMetric: (metric) => {
    const { serverId } = metric
    set((state) => {
      const existing = state.metricsByServer[serverId] || []
      const updated = [...existing, metric].sort(byTimestampAsc)

      // Keep recent points during live updates to avoid unbounded growth.
      const trimmed = updated.slice(-LIVE_WINDOW)

      return {
        metricsByServer: {
          ...state.metricsByServer,
          [serverId]: trimmed,
        },
        latestByServer: {
          ...state.latestByServer,
          [serverId]: metric,
        },
      }
    })
  },

  // ── Clear all metrics (on logout) ─────────────────────────────────────
  clearMetrics: () => {
    set({ metricsByServer: {}, latestByServer: {} })
  },
}))

export default useMetricsStore