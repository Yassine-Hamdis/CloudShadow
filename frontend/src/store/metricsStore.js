import { create } from 'zustand'

const LIVE_WINDOW = 300

const toMs = (value) => {
  if (value === null || value === undefined || value === '') return NaN

  if (typeof value === 'number') {
    if (!Number.isFinite(value)) return NaN
    return value < 1e12 ? value * 1000 : value
  }

  if (typeof value === 'string') {
    const numeric = Number(value)
    if (Number.isFinite(numeric)) {
      return numeric < 1e12 ? numeric * 1000 : numeric
    }
  }

  const parsed = new Date(value).getTime()
  return Number.isFinite(parsed) ? parsed : NaN
}

const metricTimestampMs = (metric) =>
  toMs(
    metric?.timestamp ??
    metric?.collectedAt ??
    metric?.createdAt ??
    metric?.time ??
    metric?.ts
  )

const byTimestampAsc = (a, b) =>
  metricTimestampMs(a) - metricTimestampMs(b)

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