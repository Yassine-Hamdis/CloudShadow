import { useState }    from 'react'
import toast           from 'react-hot-toast'
import { createServer } from '../../api/servers'
import { X }           from 'lucide-react'

export default function AddServerModal({ onClose, onCreated }) {
  const [name, setName]       = useState('')
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!name.trim()) { setError('Server name is required'); return }

    setLoading(true)
    try {
      const server = await createServer({ name: name.trim() })
      toast.success(`Server "${server.name}" created!`)
      onCreated(server)
      onClose()
    } catch (err) {
      const status  = err.response?.status
      const payload = err.response?.data

      if (status === 409) {
        setError('A server with this name already exists')
      } else if (status === 400) {
        const validationErrors = payload?.validationErrors
        if (validationErrors && validationErrors.length > 0) {
          setError(validationErrors[0]?.message || 'Validation error')
        } else {
          setError(payload?.message || 'Invalid input')
        }
      }
      // 403, 500 etc. are handled by axios interceptor already
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-[#1f2937] border border-[#374151] rounded-xl w-full max-w-md shadow-2xl">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#374151]">
          <h2 className="text-base font-semibold text-[#E6EEF2]">Add New Server</h2>
          <button
            onClick={onClose}
            className="text-[#9AA6B2] hover:text-[#E6EEF2] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <form onSubmit={handleSubmit} className="px-6 py-5">
          <p className="text-sm text-[#9AA6B2] mb-4">
            Enter a unique name for your server. An agent token will be generated.
          </p>

          <label className="block text-sm font-medium text-[#9AA6B2] mb-1.5">
            Server Name
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => { setName(e.target.value); setError('') }}
            placeholder="e.g. app-server-01"
            autoFocus
            className={`
              w-full px-4 py-2.5 rounded-lg
              bg-[#0f1724] border text-[#E6EEF2]
              placeholder:text-[#374151]
              focus:outline-none focus:ring-2 focus:ring-[#3f51b5]
              text-sm transition-all
              ${error ? 'border-[#E53935]' : 'border-[#374151]'}
            `}
          />
          {error && (
            <p className="text-xs text-[#E53935] mt-1">{error}</p>
          )}

          {/* Footer */}
          <div className="flex justify-end gap-3 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="
                px-4 py-2 rounded-lg text-sm font-medium
                text-[#9AA6B2] hover:text-[#E6EEF2]
                border border-[#374151] hover:border-[#4b5563]
                transition-all
              "
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className={`
                px-4 py-2 rounded-lg text-sm font-medium
                bg-[#3f51b5] hover:bg-[#3949a3] text-white
                transition-all
                ${loading ? 'opacity-70 cursor-not-allowed' : ''}
              `}
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Creating…
                </span>
              ) : (
                'Create Server'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}