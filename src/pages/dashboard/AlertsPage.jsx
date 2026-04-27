import { useEffect, useState, useMemo } from 'react'
import { ChevronLeft, ChevronRight, Filter } from 'lucide-react'
import AlertCard       from '../../components/dashboard/AlertCard'
import useAlertsStore  from '../../store/alertsStore'
import { getAlerts } from '../../api/alerts'

const PER_PAGE = 20

export default function AlertsPage() {
  const { alerts, setAlerts } = useAlertsStore()

  useEffect(() => {
    if (alerts.length > 0) return

    let isActive = true

    const loadAlerts = async () => {
      try {
        const response = await getAlerts()
        if (isActive) {
          setAlerts(response)
        }
      } catch {
        // Keep the current empty state if the fetch fails.
      }
    }

    loadAlerts()

    return () => {
      isActive = false
    }
  }, [alerts.length, setAlerts])

  const [severity, setSeverity] = useState('ALL')
  const [type, setType]         = useState('ALL')
  const [server, setServer]     = useState('ALL')
  const [page, setPage]         = useState(1)

  // Get unique server names for the filter dropdown
  const serverNames = useMemo(() => {
    const names = [...new Set(alerts.map((a) => a.serverName))]
    return names.sort()
  }, [alerts])

  // Client-side filtering
  const filtered = useMemo(() => {
    return alerts.filter((a) => {
      if (severity !== 'ALL' && a.severity !== severity) return false
      if (type     !== 'ALL' && a.type     !== type)     return false
      if (server   !== 'ALL' && a.serverName !== server) return false
      return true
    })
  }, [alerts, severity, type, server])

  // Pagination
  const totalPages = Math.max(1, Math.ceil(filtered.length / PER_PAGE))
  const paginated  = filtered.slice((page - 1) * PER_PAGE, page * PER_PAGE)

  const handleFilterChange = (setter) => (e) => {
    setter(e.target.value)
    setPage(1)  // Reset to first page on filter change
  }

  const selectClass = `
    bg-[#1f2937] border border-[#374151] rounded-xl
    text-sm text-[#E6EEF2] px-3 py-2.5
    focus:outline-none focus:ring-2 focus:ring-[#3f51b5]/45
  `

  return (
    <div className="space-y-6">

      {/* Page header */}
      <div className="app-panel-soft rounded-[1.75rem] p-6 flex items-center justify-between flex-wrap gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-[0.24em] text-[#9AA6B2] mb-2">Monitoring</div>
          <h1 className="page-title">Alerts</h1>
          <p className="page-subtitle">
            {filtered.length} alert{filtered.length !== 1 ? 's' : ''} found
          </p>
        </div>
      </div>

      {/* Filter bar */}
      <div className="bg-[#1f2937] border border-[#374151] rounded-2xl p-5 app-panel-soft">
        <div className="flex items-center gap-2 mb-3">
          <Filter className="w-4 h-4 text-[#9AA6B2]" />
          <span className="text-xs font-semibold text-[#9AA6B2] uppercase tracking-wider">
            Filters
          </span>
        </div>
        <div className="flex items-center gap-3 flex-wrap">

          {/* Severity */}
          <div>
            <label className="block text-xs text-[#9AA6B2] mb-1">Severity</label>
            <select
              value={severity}
              onChange={handleFilterChange(setSeverity)}
              className={selectClass}
            >
              <option value="ALL">All Severities</option>
              <option value="WARNING">⚠️ WARNING</option>
              <option value="CRITICAL">🚨 CRITICAL</option>
            </select>
          </div>

          {/* Type */}
          <div>
            <label className="block text-xs text-[#9AA6B2] mb-1">Type</label>
            <select
              value={type}
              onChange={handleFilterChange(setType)}
              className={selectClass}
            >
              <option value="ALL">All Types</option>
              <option value="CPU">CPU</option>
              <option value="Memory">Memory</option>
              <option value="Disk">Disk</option>
            </select>
          </div>

          {/* Server */}
          <div>
            <label className="block text-xs text-[#9AA6B2] mb-1">Server</label>
            <select
              value={server}
              onChange={handleFilterChange(setServer)}
              className={selectClass}
            >
              <option value="ALL">All Servers</option>
              {serverNames.map((name) => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>
          </div>

          {/* Clear filters */}
          {(severity !== 'ALL' || type !== 'ALL' || server !== 'ALL') && (
            <div className="self-end">
              <button
                onClick={() => { setSeverity('ALL'); setType('ALL'); setServer('ALL'); setPage(1) }}
                className="
                  app-button-sm px-3 text-xs font-medium
                  text-[#9AA6B2] border border-[#374151]
                  hover:text-[#E6EEF2] hover:border-[#4b5563]
                  transition-all
                "
              >
                Clear filters
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Alerts list */}
      {paginated.length === 0 ? (
        <div className="
          bg-[#1f2937] border border-dashed border-[#374151] rounded-2xl
          flex flex-col items-center justify-center py-20 text-center app-panel-soft
        ">
          <p className="text-base font-semibold text-[#E6EEF2] mb-1">
            No alerts match your filters
          </p>
          <p className="text-sm text-[#9AA6B2]">
            Try adjusting your filter criteria
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {paginated.map((alert) => (
            <AlertCard key={alert.id} alert={alert} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <p className="text-sm text-[#9AA6B2]">
            Page {page} of {totalPages} · {filtered.length} total
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
              className="
                app-button-sm p-2 border border-[#374151]
                text-[#9AA6B2] disabled:opacity-40
                hover:border-[#4b5563] hover:text-[#E6EEF2]
                transition-all
              "
            >
              <ChevronLeft className="w-4 h-4" />
            </button>

            {/* Page numbers */}
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              const pageNum = i + 1
              return (
                <button
                  key={pageNum}
                  onClick={() => setPage(pageNum)}
                  className={`
                    w-8 h-8 rounded-lg text-sm font-medium transition-all
                    ${page === pageNum
                      ? 'bg-[#3f51b5] text-white'
                      : 'border border-[#374151] text-[#9AA6B2] hover:text-[#E6EEF2]'
                    }
                  `}
                >
                  {pageNum}
                </button>
              )
            })}

            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className="
                app-button-sm p-2 border border-[#374151]
                text-[#9AA6B2] disabled:opacity-40
                hover:border-[#4b5563] hover:text-[#E6EEF2]
                transition-all
              "
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}