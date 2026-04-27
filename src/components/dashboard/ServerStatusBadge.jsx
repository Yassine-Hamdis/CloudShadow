import { useEffect, useState } from 'react'
import { formatDistanceToNow } from 'date-fns'

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
    ? (Date.now() - new Date(lastSeen).getTime()) < 5 * 60 * 1000
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