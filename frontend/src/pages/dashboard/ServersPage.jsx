import { useEffect, useState } from 'react'
import { formatDistanceToNow } from 'date-fns'
import toast from 'react-hot-toast'
import { Plus, Trash2, Terminal, Copy, Check } from 'lucide-react'
import { getServers, deleteServer, getServerInstructions } from '../../api/servers'
import ServerStatusBadge from '../../components/dashboard/ServerStatusBadge'
import AddServerModal    from '../../components/modals/AddServerModal'
import InstallModal      from '../../components/modals/InstallModal'
import LoadingSpinner    from '../../components/common/LoadingSpinner'

const maskToken = (token) =>
  token ? `${token.slice(0, 6)}${'•'.repeat(6)}` : '—'

const TokenDisplay = ({ token }) => {
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    await navigator.clipboard.writeText(token)
    setCopied(true)
    toast.success('Token copied!')
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="flex items-center gap-2">
      <code className="text-xs font-mono text-[#9AA6B2] bg-[#0f1724] px-2 py-1 rounded border border-[#374151]">
        {maskToken(token)}
      </code>
      <button
        onClick={handleCopy}
        className="text-[#9AA6B2] hover:text-[#3f51b5] transition-colors"
        title="Copy token"
      >
        {copied ? <Check className="w-3.5 h-3.5 text-[#4CAF50]" /> : <Copy className="w-3.5 h-3.5" />}
      </button>
    </div>
  )
}

export default function ServersPage() {
  const [servers, setServers]         = useState([])
  const [loading, setLoading]         = useState(true)
  const [showAdd, setShowAdd]         = useState(false)
  const [installServer, setInstallServer] = useState(null)
  const [deletingId, setDeletingId]   = useState(null)

  const loadServers = async () => {
    setLoading(true)
    try {
      const data = await getServers()
      setServers(data)
    } catch {
      toast.error('Failed to load servers')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadServers() }, [])

  const handleCreated = (server) => {
    setServers((prev) => [server, ...prev])
    setInstallServer(server)
  }

  const handleDelete = async (server) => {
    if (!confirm(`Delete server "${server.name}"? This cannot be undone.`)) return

    setDeletingId(server.id)
    try {
      await deleteServer(server.id)
      setServers((prev) => prev.filter((s) => s.id !== server.id))
      toast.success(`Server "${server.name}" deleted`)
    } catch {
      toast.error('Failed to delete server')
    } finally {
      setDeletingId(null)
    }
  }

  const handleViewInstructions = async (server) => {
    try {
      const full = await getServerInstructions(server.id)
      setInstallServer(full)
    } catch {
      toast.error('Failed to load instructions')
    }
  }

  return (
    <div className="space-y-6">

      {/* Page header */}
      <div className="app-panel-soft rounded-[1.75rem] p-6 flex items-center justify-between flex-wrap gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-[0.24em] text-[#9AA6B2] mb-2">Infrastructure</div>
          <h1 className="page-title">Servers</h1>
          <p className="page-subtitle">
            Manage your monitoring agents — {servers.length} server{servers.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button
          onClick={() => setShowAdd(true)}
          className="
            app-button flex items-center gap-2
            bg-[#3f51b5] hover:bg-[#3949a3] text-white text-sm font-medium
            transition-all shadow-lg shadow-[#3f51b5]/20
          "
        >
          <Plus className="w-4 h-4" />
          Add Server
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex items-center justify-center h-48">
          <LoadingSpinner size="lg" />
        </div>
      ) : servers.length === 0 ? (
        <div className="
          bg-[#1f2937] border border-dashed border-[#374151] rounded-2xl
          flex flex-col items-center justify-center py-20 text-center app-panel-soft
        ">
          <div className="p-4 rounded-xl bg-[#374151] mb-4">
            <Terminal className="w-8 h-8 text-[#9AA6B2]" />
          </div>
          <h3 className="text-base font-semibold text-[#E6EEF2] mb-1">
            No servers yet
          </h3>
          <p className="text-sm text-[#9AA6B2] mb-4">
            Add your first server to start monitoring
          </p>
          <button
            onClick={() => setShowAdd(true)}
            className="
              app-button flex items-center gap-2
              bg-[#3f51b5] hover:bg-[#3949a3] text-white text-sm font-medium
            "
          >
            <Plus className="w-4 h-4" />
            Add Your First Server
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
          {servers.map((server) => (
            <div
              key={server.id}
              className="
                bg-[#1f2937] border border-[#374151] rounded-2xl p-5
                hover:border-[#4b5563] hover:shadow-lg transition-all duration-200 app-panel-soft
              "
            >
              {/* Server name + status */}
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h3 className="text-base font-semibold text-[#E6EEF2] font-mono mb-2">
                    {server.name}
                  </h3>
                  <ServerStatusBadge
                    serverId={server.id}
                    lastSeen={server.lastSeen}
                  />
                </div>
                <button
                  onClick={() => handleDelete(server)}
                  disabled={deletingId === server.id}
                  className="
                    p-1.5 rounded-lg text-[#9AA6B2]
                    hover:text-[#E53935] hover:bg-[#E53935]/10
                    transition-all
                  "
                  title="Delete server"
                >
                  {deletingId === server.id
                    ? <span className="w-4 h-4 border-2 border-[#374151] border-t-[#E53935] rounded-full animate-spin block" />
                    : <Trash2 className="w-4 h-4" />
                  }
                </button>
              </div>

              {/* Token */}
              <div className="mb-3">
                <p className="text-xs text-[#9AA6B2] mb-1">Agent Token</p>
                <TokenDisplay token={server.token} />
              </div>

              {/* Dates */}
              <div className="space-y-1 mb-4">
                <p className="text-xs text-[#9AA6B2]">
                  Created:{' '}
                  <span className="text-[#E6EEF2]">
                    {new Date(server.createdAt).toLocaleDateString()}
                  </span>
                </p>
                {server.lastSeen && (
                  <p className="text-xs text-[#9AA6B2]">
                    Last seen:{' '}
                    <span className="text-[#E6EEF2]">
                      {formatDistanceToNow(new Date(server.lastSeen), { addSuffix: true })}
                    </span>
                  </p>
                )}
              </div>

              {/* Actions */}
              <button
                onClick={() => handleViewInstructions(server)}
                className="
                  w-full flex items-center justify-center gap-2
                  app-button-sm text-sm font-medium
                  border border-[#374151] text-[#9AA6B2]
                  hover:border-[#3f51b5] hover:text-[#3f51b5]
                  transition-all
                "
              >
                <Terminal className="w-4 h-4" />
                View Install Instructions
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Modals */}
      {showAdd && (
        <AddServerModal
          onClose={() => setShowAdd(false)}
          onCreated={handleCreated}
        />
      )}
      {installServer && (
        <InstallModal
          server={installServer}
          onClose={() => setInstallServer(null)}
        />
      )}
    </div>
  )
}