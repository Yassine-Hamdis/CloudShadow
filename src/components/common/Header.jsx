import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate }  from 'react-router-dom'
import useAuthStore     from '../../store/authStore'
import useAlertsStore   from '../../store/alertsStore'
import useMetricsStore  from '../../store/metricsStore'
import { LogOut, Bell, AlertTriangle, ChevronRight, X } from 'lucide-react'

export default function Header() {
  const navigate  = useNavigate()
  const { email, companyName, logout } = useAuthStore()
  const { alerts, criticalCount, clearCriticalCount, clearAlerts } = useAlertsStore()
  const { clearMetrics } = useMetricsStore()
  const [showNotifications, setShowNotifications] = useState(false)
  const menuRef = useRef(null)

  const criticalAlerts = useMemo(
    () => alerts.filter((alert) => alert.severity === 'CRITICAL').slice(0, 5),
    [alerts]
  )

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowNotifications(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleLogout = () => {
    clearMetrics()
    clearAlerts()
    logout()
    navigate('/login', { replace: true })
  }

  const handleBellClick = () => {
    setShowNotifications((open) => !open)
    clearCriticalCount()
  }

  const handleViewAllAlerts = () => {
    setShowNotifications(false)
    clearCriticalCount()
    navigate('/dashboard/alerts')
  }

  // Initials avatar
  const initials = email ? email[0].toUpperCase() : '?'

  return (
    <header className="
      sticky top-0 h-20 w-full
      app-panel-soft border-x-0 border-t-0
      flex items-center justify-between px-7
      z-30 shadow-[0_10px_30px_rgba(0,0,0,0.18)]
    ">
      {/* Left: Company name */}
      <div>
        <h2 className="text-base font-semibold text-[#E6EEF2] tracking-[0.02em]">{companyName}</h2>
        <p className="text-[11px] uppercase tracking-[0.16em] text-[#9AA6B2] mt-1.5">Server Monitoring Dashboard</p>
      </div>

      {/* Right: alerts badge + user + logout */}
      <div className="flex items-center gap-3 relative" ref={menuRef}>

        {/* Critical alerts badge */}
        <button
          type="button"
          onClick={handleBellClick}
          className="
            relative p-2.5 rounded-full
            text-[#9AA6B2] hover:text-[#E6EEF2]
            hover:bg-white/5 transition-all
          "
          aria-label="Notifications"
        >
          <Bell className="w-[18px] h-[18px]" />
          {criticalCount > 0 && (
            <span className="
              absolute -top-2 -right-2
              bg-[#E53935] text-white text-xs font-bold
              rounded-full min-w-4 h-4 px-1 flex items-center justify-center
              leading-none
            ">
              {criticalCount > 99 ? '99+' : criticalCount}
            </span>
          )}
        </button>

        {showNotifications && (
          <div className="absolute right-0 top-14 w-80 max-w-[calc(100vw-2rem)] rounded-2xl border border-[#374151] app-panel-soft shadow-2xl overflow-hidden z-50">
            <div className="flex items-center justify-between px-4 py-3 border-b border-[#374151]/70">
              <div>
                <p className="text-sm font-semibold text-[#E6EEF2]">Notifications</p>
                <p className="text-xs text-[#9AA6B2]">Critical alerts require attention</p>
              </div>
              <button
                type="button"
                onClick={() => setShowNotifications(false)}
                className="p-1.5 rounded-lg text-[#9AA6B2] hover:text-[#E6EEF2] hover:bg-white/5"
                aria-label="Close notifications"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="max-h-72 overflow-auto p-2">
              {criticalAlerts.length === 0 ? (
                <div className="px-3 py-6 text-center">
                  <p className="text-sm text-[#9AA6B2]">No critical alerts right now</p>
                </div>
              ) : (
                criticalAlerts.map((alert) => (
                  <div
                    key={alert.id}
                    className="flex items-start gap-3 rounded-xl px-3 py-3 hover:bg-white/5 transition-colors"
                  >
                    <div className="p-2 rounded-lg bg-[#E53935]/10 text-[#E53935] flex-shrink-0">
                      <AlertTriangle className="w-4 h-4" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-medium text-[#E6EEF2] truncate">
                        {alert.serverName}
                      </p>
                      <p className="text-xs text-[#9AA6B2] leading-6 line-clamp-2">
                        {alert.message || `${alert.type} alert`}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="border-t border-[#374151]/70 p-3">
              <button
                type="button"
                onClick={handleViewAllAlerts}
                className="w-full app-button-sm flex items-center justify-center gap-2 bg-[#3f51b5] text-white hover:bg-[#3949a3]"
              >
                View all alerts
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}

        {/* Divider */}
        <div className="w-px h-5 bg-[#374151]/80" />

        {/* User info */}
        <div className="flex items-center gap-3">
          <div className="
            w-8 h-8 rounded-full bg-gradient-to-br from-[#3f51b5] to-[#5c6bc0]
            flex items-center justify-center
            text-sm font-semibold text-white
          ">
            {initials}
          </div>
          <span className="text-[15px] text-[#9AA6B2] max-w-[220px] truncate leading-tight">
            {email}
          </span>
        </div>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className="
            app-button-sm flex items-center gap-2
            text-[15px] text-[#9AA6B2] hover:text-[#E53935]
            hover:bg-[#E53935]/10 transition-all
          "
          title="Sign out"
        >
          <LogOut className="w-4 h-4" />
          <span className="hidden sm:block">Logout</span>
        </button>
      </div>
    </header>
  )
}