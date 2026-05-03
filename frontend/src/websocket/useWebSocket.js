import { useEffect, useRef } from 'react'
import { Client }            from '@stomp/stompjs'
import SockJS                from 'sockjs-client'
import toast                 from 'react-hot-toast'
import useAuthStore          from '../store/authStore'
import useMetricsStore       from '../store/metricsStore'
import useAlertsStore        from '../store/alertsStore'

const WS_URL = import.meta.env.VITE_WS_URL || 
               (window.location.origin.replace('http', 'ws') + '/ws') ||
               'ws://localhost:8080/ws'

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

const metricTimestamp = (metric) =>
  metric?.timestamp ?? metric?.collectedAt ?? metric?.createdAt ?? metric?.time ?? metric?.ts ?? null

export const useWebSocket = () => {
  const clientRef = useRef(null)

  const { companyId, isAuthenticated } = useAuthStore()
  const { appendMetric }               = useMetricsStore()
  const { prependAlert, incrementCriticalCount } = useAlertsStore()

  useEffect(() => {
    if (!isAuthenticated || !companyId) return

    // ── Create STOMP client with SockJS ──────────────────────────────────
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay:   5000,
      onConnect: () => {
        console.log('[WS] Connected to CloudShadow WebSocket')

        // ── Topic 1: Metrics ─────────────────────────────────────────────
        client.subscribe(
          `/topic/company/${companyId}/metrics`,
          (frame) => {
            try {
              const message = JSON.parse(frame.body)

              // Security: verify companyId matches
              if (Number(message.companyId) !== Number(companyId)) return

              if (message.type === 'NEW_METRIC') {
                const metric = message.data
                appendMetric(metric)

                const serverId = Number(metric?.serverId ?? metric?.serverID ?? metric?.server?.id)
                const tsValue = metricTimestamp(metric)
                const tsMs = toMs(tsValue)

                if (Number.isFinite(serverId) && Number.isFinite(tsMs)) {
                  window.dispatchEvent(
                    new CustomEvent('server-status-change', {
                      detail: {
                        serverId,
                        serverName: metric?.serverName ?? metric?.name,
                        lastSeen: new Date(tsMs).toISOString(),
                        status: 'ONLINE',
                      },
                    })
                  )
                }
              }
            } catch (err) {
              console.error('[WS] Error parsing metric message:', err)
            }
          }
        )

        // ── Topic 2: Alerts ──────────────────────────────────────────────
        client.subscribe(
          `/topic/company/${companyId}/alerts`,
          (frame) => {
            try {
              const message = JSON.parse(frame.body)

              if (Number(message.companyId) !== Number(companyId)) return

              if (message.type === 'NEW_ALERT') {
                const alert = message.data

                prependAlert(alert)

                if (alert.severity === 'CRITICAL') {
                  incrementCriticalCount()
                  toast.error(
                    `🚨 CRITICAL alert on ${alert.serverName}: ${alert.type}`,
                    { duration: 6000 }
                  )
                } else {
                  toast(
                    `⚠️ WARNING alert on ${alert.serverName}: ${alert.type}`,
                    {
                      duration: 5000,
                      style: {
                        background: '#1f2937',
                        color: '#FFC107',
                        border: '1px solid #FFC107',
                      },
                    }
                  )
                }
              }
            } catch (err) {
              console.error('[WS] Error parsing alert message:', err)
            }
          }
        )

        // ── Topic 3: Server Status ───────────────────────────────────────
        client.subscribe(
          `/topic/company/${companyId}/status`,
          (frame) => {
            try {
              const message = JSON.parse(frame.body)

              if (Number(message.companyId) !== Number(companyId)) return

              const { serverName, status } = message.data

              if (status === 'ONLINE') {
                toast.success(`🟢 ${serverName} is back online`, {
                  duration: 4000,
                })
              } else if (status === 'OFFLINE') {
                toast.error(`🔴 ${serverName} is offline`, {
                  duration: 6000,
                })
              }

              // Status badge update is handled by serversStore or local state
              // We dispatch a custom event so ServerStatusBadge can react
              window.dispatchEvent(
                new CustomEvent('server-status-change', { detail: message.data })
              )
            } catch (err) {
              console.error('[WS] Error parsing status message:', err)
            }
          }
        )
      },
      onDisconnect: () => {
        console.log('[WS] Disconnected from CloudShadow WebSocket')
      },
      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame)
      },
    })

    client.activate()
    clientRef.current = client

    // ── Cleanup on unmount or logout ─────────────────────────────────────
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate()
        clientRef.current = null
      }
    }
  }, [isAuthenticated, companyId])
}