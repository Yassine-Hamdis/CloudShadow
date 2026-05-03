import { create } from 'zustand'

const LIVE_WINDOW = 300

const toMs = (value) => {
  if (value === null || value === undefined || value === '') return NaN

  if (typeof value === 'number') {
    if (!Number.isFinite(value)) return NaN
    return value < 1e12 ? value * 1000 : value
  }

  if (typeof value === 'string') {
    const s = value.trim()

    const numeric = Number(s)
    if (Number.isFinite(numeric)) {
      return numeric < 1e12 ? numeric * 1000 : numeric
    }

    const m = s.match(/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2}:\d{2})(\.\d+)?(Z|[+-]\d{2}:?\d{2})?$/)
    if (m) {
      const date = m[1]
      const time = m[2]
      let frac = m[3] || ''
      if (frac) {
        frac = frac.slice(0, 4)
        if (frac.length === 2) frac = frac + '0'
      }
      const tz = m[4] || 'Z'
      const iso = `${date}T${time}${frac}${tz}`
      const parsedIso = Date.parse(iso)
      if (Number.isFinite(parsedIso)) return parsedIso
    }

    const parsed = Date.parse(s)
    return Number.isFinite(parsed) ? parsed : NaN
  }

  const parsed = Date.parse(value)
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