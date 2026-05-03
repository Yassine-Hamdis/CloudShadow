import { useEffect, useState } from 'react'
import { formatDistanceToNow } from 'date-fns'

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

/**
 * Determines server status:
 * ONLINE  = lastSeen within 5 minutes
 * OFFLINE = lastSeen > 5 minutes ago OR null
 *
 * Also listens for server-status-change WebSocket events.
 */
export default function ServerStatusBadge({ serverId, lastSeen: initialLastSeen }) {
  const [lastSeen, setLastSeen] = useState(initialLastSeen)

  // Keep local state in sync when parent selects a different server.
  useEffect(() => {
    setLastSeen(initialLastSeen)
  }, [serverId, initialLastSeen])

  // Listen for real-time status changes via custom event
  useEffect(() => {
    const handler = (e) => {
      if (Number(e.detail.serverId) === Number(serverId)) {
        setLastSeen(e.detail.lastSeen)
      }
    }
    window.addEventListener('server-status-change', handler)
    return () => window.removeEventListener('server-status-change', handler)
  }, [serverId])

  const isOnline = lastSeen
    ? (Date.now() - toMs(lastSeen)) < 5 * 60 * 1000
    : false

  const label     = isOnline ? 'ONLINE' : 'OFFLINE'
  const color     = isOnline ? '#4CAF50' : '#E53935'
  const bgColor   = isOnline ? 'rgba(76,175,80,0.12)' : 'rgba(229,57,53,0.12)'
  const borderColor = isOnline ? 'rgba(76,175,80,0.3)' : 'rgba(229,57,53,0.3)'

  return (
    <div className="flex flex-col items-start gap-1">
      <span
        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold"
        style={{ color, backgroundColor: bgColor, border: `1px solid ${borderColor}` }}
      >
        {/* Pulse dot */}
        <span
          className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${isOnline ? 'animate-pulse' : ''}`}
          style={{ backgroundColor: color }}
        />
        {label}
      </span>
      {lastSeen && (
        <span className="text-xs text-[#9AA6B2]">
          {formatDistanceToNow(new Date(lastSeen), { addSuffix: true })}
        </span>
      )}
      {!lastSeen && (
        <span className="text-xs text-[#9AA6B2]">Never connected</span>
      )}
    </div>
  )
}